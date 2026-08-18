package com.example.terminal

import android.content.Context
import com.example.model.TerminalLine
import com.example.model.ThemePreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    initialTitle: String = "Session",
    private val context: Context,
    private var theme: ThemePreset = ThemePreset.DARK_CLASSIC,
    private val scope: CoroutineScope
) {
    private val _title = MutableStateFlow(initialTitle)
    val title = _title.asStateFlow()

    private val _lines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val lines = _lines.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _exitCode = MutableStateFlow<Int?>(null)
    val exitCode = _exitCode.asStateFlow()

    private val _currentDir = MutableStateFlow("~")
    val currentDir = _currentDir.asStateFlow()

    private var process: Process? = null
    private var outputStream: OutputStream? = null
    private var readJob: Job? = null
    private val ansiParser = AnsiParser(theme)

    private val maxScrollback = 1200

    init {
        start()
    }

    fun updateTheme(newTheme: ThemePreset) {
        this.theme = newTheme
        ansiParser.updateTheme(newTheme)
    }

    fun setTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun start() {
        if (_isRunning.value) return

        try {
            val proc = LocalProcessRunner.startShell(context)
            process = proc
            outputStream = proc.outputStream
            _isRunning.value = true
            _exitCode.value = null

            // Initial banner
            appendLine(TerminalLine.plain("\u001b[1;36m┌──────────────────────────────────────────────┐\u001b[0m"))
            appendLine(TerminalLine.plain("\u001b[1;36m│\u001b[0m  \u001b[1;32mReTerminal\u001b[0m • Native Local Shell (ARM64)    \u001b[1;36m│\u001b[0m"))
            appendLine(TerminalLine.plain("\u001b[1;36m│\u001b[0m  Type \u001b[1;33mhelp\u001b[0m or tap \u001b[1;35mQuick Commands\u001b[0m to begin       \u001b[1;36m│\u001b[0m"))
            appendLine(TerminalLine.plain("\u001b[1;36m└──────────────────────────────────────────────┘\u001b[0m"))

            // Send initial setup to shell
            sendInput("alias ll='ls -la'\n")
            sendInput("cd \"$context.filesDir/home\" 2>/dev/null || true\n")

            readJob = scope.launch(Dispatchers.IO) {
                val reader = BufferedReader(InputStreamReader(proc.inputStream, Charsets.UTF_8))
                val buffer = CharArray(1024)
                var charBuffer = StringBuilder()

                try {
                    while (isActive) {
                        val count = reader.read(buffer, 0, buffer.size)
                        if (count == -1) break

                        val text = String(buffer, 0, count)
                        charBuffer.append(text)

                        // If contains newline or prompt end, flush lines
                        if (charBuffer.contains('\n')) {
                            val chunk = charBuffer.toString()
                            val parsedLines = ansiParser.parseChunk(chunk)
                            appendLines(parsedLines)
                            charBuffer.clear()
                        } else if (charBuffer.length > 256) {
                            val parsedLine = ansiParser.parseLine(charBuffer.toString())
                            appendLine(parsedLine)
                            charBuffer.clear()
                        }
                    }

                    if (charBuffer.isNotEmpty()) {
                        val parsedLine = ansiParser.parseLine(charBuffer.toString())
                        appendLine(parsedLine)
                        charBuffer.clear()
                    }

                    val code = proc.waitFor()
                    _exitCode.value = code
                    _isRunning.value = false
                    appendLine(TerminalLine.plain("\n\u001b[1;31m[Process completed with exit code $code]\u001b[0m"))
                } catch (e: Exception) {
                    _isRunning.value = false
                    appendLine(TerminalLine.plain("\u001b[1;31m[Session disconnected: ${e.localizedMessage}]\u001b[0m"))
                }
            }
        } catch (e: Exception) {
            _isRunning.value = false
            appendLine(TerminalLine.plain("\u001b[1;31m[Failed to spawn local shell: ${e.localizedMessage}]\u001b[0m"))
        }
    }

    fun sendInput(input: String) {
        scope.launch(Dispatchers.IO) {
            try {
                outputStream?.let { os ->
                    os.write(input.toByteArray(Charsets.UTF_8))
                    os.flush()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendLine(TerminalLine.plain("\u001b[1;31m[Write error: ${e.localizedMessage}]\u001b[0m"))
                }
            }
        }
    }

    fun sendCtrlC() {
        sendInput("\u0003")
    }

    fun sendCtrlD() {
        sendInput("\u0004")
    }

    fun sendCtrlZ() {
        sendInput("\u001a")
    }

    fun sendCtrlL() {
        sendInput("\u000c")
        clearBuffer()
    }

    fun sendTab() {
        sendInput("\t")
    }

    fun sendArrowUp() {
        sendInput("\u001b[A")
    }

    fun sendArrowDown() {
        sendInput("\u001b[B")
    }

    fun sendArrowLeft() {
        sendInput("\u001b[D")
    }

    fun sendArrowRight() {
        sendInput("\u001b[C")
    }

    fun sendEsc() {
        sendInput("\u001b")
    }

    fun restart() {
        close()
        start()
    }

    fun clearBuffer() {
        _lines.value = emptyList()
    }

    fun getExportText(): String {
        return _lines.value.joinToString("\n") { it.rawText }
    }

    fun close() {
        readJob?.cancel()
        try {
            outputStream?.close()
        } catch (_: Exception) {}
        try {
            process?.destroy()
        } catch (_: Exception) {}
        _isRunning.value = false
    }

    private fun appendLine(line: TerminalLine) {
        val current = _lines.value.toMutableList()
        current.add(line)
        if (current.size > maxScrollback) {
            val trimmed = current.subList(current.size - maxScrollback, current.size)
            _lines.value = trimmed
        } else {
            _lines.value = current
        }
    }

    private fun appendLines(newLines: List<TerminalLine>) {
        val current = _lines.value.toMutableList()
        current.addAll(newLines)
        if (current.size > maxScrollback) {
            val trimmed = current.subList(current.size - maxScrollback, current.size)
            _lines.value = trimmed
        } else {
            _lines.value = current
        }
    }
}
