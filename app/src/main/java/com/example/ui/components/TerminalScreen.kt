package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TerminalLine
import com.example.model.ThemePreset
import com.example.viewmodel.TerminalViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val listState = rememberLazyListState()

    val lines = activeSession?.lines?.collectAsState()?.value ?: emptyList()
    val isRunning = activeSession?.isRunning?.collectAsState()?.value ?: false
    val exitCode = activeSession?.exitCode?.collectAsState()?.value

    // Auto scroll to bottom
    LaunchedEffect(lines.size, autoScroll) {
        if (autoScroll && lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    // Cursor Blink animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    // Pinch to zoom font scale
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        val newSize = (fontSizeSp * zoomChange).coerceIn(10f, 24f)
        viewModel.setFontSize(newSize)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .transformable(transformableState)
            .testTag("terminal_screen_area")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                val annotatedString = remember(line, searchQuery, currentTheme) {
                    buildLineAnnotatedString(line, searchQuery, currentTheme)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(line.rawText))
                                Toast.makeText(context, "Copied line to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                ) {
                    Text(
                        text = annotatedString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * 1.25f).sp,
                        color = currentTheme.foreground
                    )
                }
            }

            // Blinking cursor / status indicator at the end of output
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                ) {
                    if (isRunning) {
                        Box(
                            modifier = Modifier
                                .size(width = (fontSizeSp * 0.6f).dp, height = (fontSizeSp * 1.1f).dp)
                                .alpha(cursorAlpha)
                                .background(currentTheme.cursor)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF3B1E22))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Session Terminated (Code: ${exitCode ?: 0})",
                                color = Color(0xFFFF8080),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildLineAnnotatedString(
    line: TerminalLine,
    searchQuery: String,
    theme: ThemePreset
): AnnotatedString {
    return buildAnnotatedString {
        line.spans.forEach { span ->
            val color = if (span.inverse) {
                span.backgroundColor ?: theme.background
            } else {
                span.textColor ?: theme.foreground
            }

            val bgColor = if (span.inverse) {
                span.textColor ?: theme.foreground
            } else {
                span.backgroundColor
            }

            val textDec = when {
                span.underline && span.strikethrough -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                span.underline -> TextDecoration.Underline
                span.strikethrough -> TextDecoration.LineThrough
                else -> TextDecoration.None
            }

            withStyle(
                style = SpanStyle(
                    color = color,
                    background = bgColor ?: Color.Transparent,
                    fontWeight = span.fontWeight,
                    fontStyle = span.fontStyle,
                    textDecoration = textDec
                )
            ) {
                append(span.text)
            }
        }

        // Apply Search Highlight overlay if query is present
        if (searchQuery.isNotBlank()) {
            val fullText = line.rawText
            var matchIndex = fullText.indexOf(searchQuery, ignoreCase = true)
            while (matchIndex >= 0) {
                addStyle(
                    style = SpanStyle(
                        background = Color(0xFFFFEE55),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    start = matchIndex,
                    end = matchIndex + searchQuery.length
                )
                matchIndex = fullText.indexOf(searchQuery, matchIndex + searchQuery.length, ignoreCase = true)
            }
        }
    }
}
