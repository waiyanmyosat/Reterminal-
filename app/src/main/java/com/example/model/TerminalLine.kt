package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

data class AnsiSpan(
    val text: String,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontStyle: FontStyle = FontStyle.Normal,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val inverse: Boolean = false
)

data class TerminalLine(
    val spans: List<AnsiSpan> = emptyList(),
    val rawText: String = spans.joinToString("") { it.text }
) {
    companion object {
        fun plain(text: String, color: Color? = null): TerminalLine {
            return TerminalLine(
                spans = listOf(AnsiSpan(text = text, textColor = color))
            )
        }
    }
}
