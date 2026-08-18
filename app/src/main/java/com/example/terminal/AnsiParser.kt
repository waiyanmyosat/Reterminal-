package com.example.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.model.AnsiSpan
import com.example.model.TerminalLine
import com.example.model.ThemePreset

class AnsiParser(private var theme: ThemePreset = ThemePreset.DARK_CLASSIC) {

    fun updateTheme(newTheme: ThemePreset) {
        this.theme = newTheme
    }

    private fun getAnsiColor(code: Int): Color {
        return when (code) {
            30 -> theme.ansiBlack
            31 -> theme.ansiRed
            32 -> theme.ansiGreen
            33 -> theme.ansiYellow
            34 -> theme.ansiBlue
            35 -> theme.ansiMagenta
            36 -> theme.ansiCyan
            37 -> theme.ansiWhite

            90 -> theme.ansiBrightBlack
            91 -> theme.ansiBrightRed
            92 -> theme.ansiBrightGreen
            93 -> theme.ansiBrightYellow
            94 -> theme.ansiBrightBlue
            95 -> theme.ansiBrightMagenta
            96 -> theme.ansiBrightCyan
            97 -> theme.ansiBrightWhite

            40 -> theme.ansiBlack
            41 -> theme.ansiRed
            42 -> theme.ansiGreen
            43 -> theme.ansiYellow
            44 -> theme.ansiBlue
            45 -> theme.ansiMagenta
            46 -> theme.ansiCyan
            47 -> theme.ansiWhite

            100 -> theme.ansiBrightBlack
            101 -> theme.ansiBrightRed
            102 -> theme.ansiBrightGreen
            103 -> theme.ansiBrightYellow
            104 -> theme.ansiBrightBlue
            105 -> theme.ansiBrightMagenta
            106 -> theme.ansiBrightCyan
            107 -> theme.ansiBrightWhite

            else -> theme.foreground
        }
    }

    private fun get256Color(code: Int): Color {
        return when (code) {
            in 0..7 -> getAnsiColor(30 + code)
            in 8..15 -> getAnsiColor(90 + (code - 8))
            in 16..231 -> {
                val index = code - 16
                val b = (index % 6) * 51
                val g = ((index / 6) % 6) * 51
                val r = ((index / 36) % 6) * 51
                Color(r, g, b)
            }
            in 232..255 -> {
                val gray = (code - 232) * 10 + 8
                Color(gray, gray, gray)
            }
            else -> theme.foreground
        }
    }

    fun parseChunk(input: String): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val rawLines = input.replace("\r\n", "\n").replace("\r", "\n").split("\n")

        for (raw in rawLines) {
            lines.add(parseLine(raw))
        }
        return lines
    }

    fun parseLine(input: String): TerminalLine {
        if (!input.contains('\u001b')) {
            return TerminalLine(spans = listOf(AnsiSpan(text = input, textColor = theme.foreground)))
        }

        val spans = mutableListOf<AnsiSpan>()
        var currentIndex = 0
        val length = input.length

        var currentFg: Color? = theme.foreground
        var currentBg: Color? = null
        var isBold = false
        var isItalic = false
        var isUnderline = false
        var isInverse = false

        while (currentIndex < length) {
            val escapeIndex = input.indexOf('\u001b', currentIndex)
            if (escapeIndex == -1) {
                // Remainder is plain text
                val text = input.substring(currentIndex)
                if (text.isNotEmpty()) {
                    spans.add(
                        AnsiSpan(
                            text = text,
                            textColor = currentFg,
                            backgroundColor = currentBg,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            underline = isUnderline,
                            inverse = isInverse
                        )
                    )
                }
                break
            }

            // Add text before escape code
            if (escapeIndex > currentIndex) {
                val text = input.substring(currentIndex, escapeIndex)
                spans.add(
                    AnsiSpan(
                        text = text,
                        textColor = currentFg,
                        backgroundColor = currentBg,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                        underline = isUnderline,
                        inverse = isInverse
                    )
                )
            }

            // Process escape sequence
            if (escapeIndex + 1 < length && input[escapeIndex + 1] == '[') {
                var endIndex = escapeIndex + 2
                while (endIndex < length && !input[endIndex].isLetter()) {
                    endIndex++
                }

                if (endIndex < length) {
                    val commandChar = input[endIndex]
                    val paramsStr = input.substring(escapeIndex + 2, endIndex)

                    if (commandChar == 'm') {
                        // SGR color/style sequence
                        val parts = if (paramsStr.isEmpty()) listOf(0) else paramsStr.split(";").mapNotNull { it.toIntOrNull() }
                        var pIdx = 0
                        while (pIdx < parts.size) {
                            val code = parts[pIdx]
                            when (code) {
                                0 -> {
                                    currentFg = theme.foreground
                                    currentBg = null
                                    isBold = false
                                    isItalic = false
                                    isUnderline = false
                                    isInverse = false
                                }
                                1 -> isBold = true
                                2 -> isBold = false // Dim
                                3 -> isItalic = true
                                4 -> isUnderline = true
                                7 -> isInverse = true
                                22 -> isBold = false
                                23 -> isItalic = false
                                24 -> isUnderline = false
                                27 -> isInverse = false
                                in 30..37 -> currentFg = getAnsiColor(code)
                                38 -> {
                                    // 256 or RGB color
                                    if (pIdx + 2 < parts.size && parts[pIdx + 1] == 5) {
                                        currentFg = get256Color(parts[pIdx + 2])
                                        pIdx += 2
                                    } else if (pIdx + 4 < parts.size && parts[pIdx + 1] == 2) {
                                        currentFg = Color(parts[pIdx + 2], parts[pIdx + 3], parts[pIdx + 4])
                                        pIdx += 4
                                    }
                                }
                                39 -> currentFg = theme.foreground
                                in 40..47 -> currentBg = getAnsiColor(code)
                                48 -> {
                                    if (pIdx + 2 < parts.size && parts[pIdx + 1] == 5) {
                                        currentBg = get256Color(parts[pIdx + 2])
                                        pIdx += 2
                                    } else if (pIdx + 4 < parts.size && parts[pIdx + 1] == 2) {
                                        currentBg = Color(parts[pIdx + 2], parts[pIdx + 3], parts[pIdx + 4])
                                        pIdx += 4
                                    }
                                }
                                49 -> currentBg = null
                                in 90..97 -> currentFg = getAnsiColor(code)
                                in 100..107 -> currentBg = getAnsiColor(code)
                            }
                            pIdx++
                        }
                    }
                    currentIndex = endIndex + 1
                } else {
                    currentIndex = escapeIndex + 1
                }
            } else {
                currentIndex = escapeIndex + 1
            }
        }

        if (spans.isEmpty()) {
            return TerminalLine(spans = listOf(AnsiSpan(text = "", textColor = theme.foreground)))
        }

        return TerminalLine(spans = spans)
    }
}
