package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CommandCategory
import com.example.model.QuickCommand
import com.example.model.ScriptModel
import com.example.model.SystemSpecs
import com.example.model.ThemePreset
import com.example.model.VirtualKey
import com.example.model.VirtualKeyType
import com.example.terminal.TerminalSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val _sessions = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessions = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String>("")
    val activeSessionId = _activeSessionId.asStateFlow()

    val activeSession = combine(_sessions, _activeSessionId) { sessionList, activeId ->
        sessionList.find { it.id == activeId } ?: sessionList.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _currentTheme = MutableStateFlow(ThemePreset.DARK_CLASSIC)
    val currentTheme = _currentTheme.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(13.5f)
    val fontSizeSp = _fontSizeSp.asStateFlow()

    private val _cursorBlinking = MutableStateFlow(true)
    val cursorBlinking = _cursorBlinking.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll = _autoScroll.asStateFlow()

    private val _ctrlActive = MutableStateFlow(false)
    val ctrlActive = _ctrlActive.asStateFlow()

    private val _altActive = MutableStateFlow(false)
    val altActive = _altActive.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory = _commandHistory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible = _isSearchVisible.asStateFlow()

    private val _quickCommands = MutableStateFlow(QuickCommand.PRESETS)
    val quickCommands = _quickCommands.asStateFlow()

    private val _scripts = MutableStateFlow(ScriptModel.BUILTIN_SCRIPTS)
    val scripts = _scripts.asStateFlow()

    private val _systemSpecs = MutableStateFlow(SystemSpecs())
    val systemSpecs = _systemSpecs.asStateFlow()

    private var sessionCounter = 1

    init {
        createSession()
        refreshSystemSpecs()
    }

    fun createSession(title: String? = null) {
        val app = getApplication<Application>()
        val sessionName = title ?: "Term $sessionCounter"
        sessionCounter++

        val newSession = TerminalSession(
            initialTitle = sessionName,
            context = app,
            theme = _currentTheme.value,
            scope = viewModelScope
        )

        val updated = _sessions.value + newSession
        _sessions.value = updated
        _activeSessionId.value = newSession.id
    }

    fun switchSession(sessionId: String) {
        if (_sessions.value.any { it.id == sessionId }) {
            _activeSessionId.value = sessionId
        }
    }

    fun closeSession(sessionId: String) {
        val currentList = _sessions.value
        val toClose = currentList.find { it.id == sessionId } ?: return
        toClose.close()

        val remaining = currentList.filter { it.id != sessionId }
        _sessions.value = remaining

        if (remaining.isEmpty()) {
            createSession()
        } else if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = remaining.last().id
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        _sessions.value.find { it.id == sessionId }?.setTitle(newTitle)
    }

    fun restartCurrentSession() {
        activeSession.value?.restart()
    }

    fun clearCurrentBuffer() {
        activeSession.value?.clearBuffer()
    }

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    fun submitInput() {
        val text = _inputText.value
        val session = activeSession.value ?: return

        session.sendInput(text + "\n")
        if (text.isNotBlank()) {
            val updatedHistory = (_commandHistory.value + text).distinct().takeLast(50)
            _commandHistory.value = updatedHistory
        }
        _inputText.value = ""
    }

    fun onVirtualKeyPressed(key: VirtualKey) {
        val session = activeSession.value ?: return

        when (key.type) {
            VirtualKeyType.MODIFIER -> {
                if (key.id == "ctrl") {
                    _ctrlActive.value = !_ctrlActive.value
                } else if (key.id == "alt") {
                    _altActive.value = !_altActive.value
                }
            }
            VirtualKeyType.ARROW -> {
                when (key.id) {
                    "up" -> session.sendArrowUp()
                    "down" -> session.sendArrowDown()
                    "left" -> session.sendArrowLeft()
                    "right" -> session.sendArrowRight()
                }
            }
            VirtualKeyType.SPECIAL -> {
                when (key.id) {
                    "esc" -> session.sendEsc()
                    "tab" -> session.sendTab()
                    "home" -> session.sendInput("\u001b[H")
                    "end" -> session.sendInput("\u001b[F")
                    "pgup" -> session.sendInput("\u001b[5~")
                    "pgdn" -> session.sendInput("\u001b[6~")
                    "c_c" -> session.sendCtrlC()
                    "c_d" -> session.sendCtrlD()
                    "c_z" -> session.sendCtrlZ()
                    "c_l" -> session.sendCtrlL()
                    else -> session.sendInput(key.sendValue)
                }
            }
            VirtualKeyType.TEXT -> {
                if (_ctrlActive.value) {
                    // Send Control character
                    val char = key.sendValue.firstOrNull()?.uppercaseChar()
                    if (char != null && char in 'A'..'Z') {
                        val ctrlCode = (char.code - 64).toChar().toString()
                        session.sendInput(ctrlCode)
                    } else {
                        session.sendInput(key.sendValue)
                    }
                    _ctrlActive.value = false
                } else {
                    session.sendInput(key.sendValue)
                }
                if (_altActive.value) {
                    _altActive.value = false
                }
            }
        }
    }

    fun toggleCtrl() {
        _ctrlActive.value = !_ctrlActive.value
    }

    fun toggleAlt() {
        _altActive.value = !_altActive.value
    }

    fun setTheme(theme: ThemePreset) {
        _currentTheme.value = theme
        _sessions.value.forEach { it.updateTheme(theme) }
    }

    fun setFontSize(size: Float) {
        _fontSizeSp.value = size.coerceIn(10f, 24f)
    }

    fun toggleAutoScroll() {
        _autoScroll.value = !_autoScroll.value
    }

    fun toggleSearch() {
        _isSearchVisible.value = !_isSearchVisible.value
        if (!_isSearchVisible.value) {
            _searchQuery.value = ""
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun executeQuickCommand(command: QuickCommand) {
        val session = activeSession.value ?: return
        if (command.executeImmediately) {
            session.sendInput(command.command + "\n")
            val updatedHistory = (_commandHistory.value + command.command).distinct().takeLast(50)
            _commandHistory.value = updatedHistory
        } else {
            _inputText.value = command.command
        }
    }

    fun executeScript(script: ScriptModel) {
        val session = activeSession.value ?: return
        val lines = script.scriptContent.trim().lines()
        for (line in lines) {
            session.sendInput(line + "\n")
        }
    }

    fun addCustomScript(name: String, description: String, content: String) {
        val newScript = ScriptModel(
            id = "custom_" + System.currentTimeMillis(),
            name = name,
            description = description,
            scriptContent = content,
            isBuiltIn = false
        )
        _scripts.value = _scripts.value + newScript
    }

    fun deleteScript(scriptId: String) {
        _scripts.value = _scripts.value.filter { it.id != scriptId }
    }

    fun exportActiveLogs(context: Context) {
        val session = activeSession.value ?: return
        val text = session.getExportText()
        if (text.isBlank()) {
            Toast.makeText(context, "Terminal buffer is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, "ReTerminal Transcript (${session.title.value})")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export ReTerminal Logs")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun refreshSystemSpecs() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            
            // Architecture & CPU
            val arch = System.getProperty("os.arch") ?: Build.SUPPORTED_ABIS.firstOrNull() ?: "aarch64"
            val cores = Runtime.getRuntime().availableProcessors()
            val kernel = System.getProperty("os.version") ?: "Linux"

            // RAM
            val totalRam = Runtime.getRuntime().totalMemory() / (1024 * 1024)
            val freeRam = Runtime.getRuntime().freeMemory() / (1024 * 1024)
            val maxRam = Runtime.getRuntime().maxMemory() / (1024 * 1024)

            // Storage
            val stat = StatFs(Environment.getDataDirectory().path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val bytesTotal = stat.blockCountLong * stat.blockSizeLong
            val freeStorageGb = "%.1f GB".format(bytesAvailable / (1024.0 * 1024.0 * 1024.0))
            val totalStorageGb = "%.1f GB".format(bytesTotal / (1024.0 * 1024.0 * 1024.0))

            // Battery
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, batteryFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            // Network
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(activeNetwork)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            _systemSpecs.value = SystemSpecs(
                architecture = "$arch (ARM64 Native)",
                osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                sdkInt = Build.VERSION.SDK_INT,
                deviceModel = Build.MODEL,
                manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
                kernelVersion = kernel,
                cpuCores = cores,
                totalMemoryFormatted = "$maxRam MB heap",
                availableMemoryFormatted = "$freeRam MB free",
                internalStorageFreeFormatted = freeStorageGb,
                internalStorageTotalFormatted = totalStorageGb,
                batteryLevel = batteryPct,
                isCharging = isCharging,
                isNetworkConnected = isConnected
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        _sessions.value.forEach { it.close() }
    }
}
