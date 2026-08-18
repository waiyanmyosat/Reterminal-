package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemePreset
import com.example.model.VirtualKey
import com.example.model.VirtualKeyType
import com.example.viewmodel.TerminalViewModel

@Composable
fun VirtualKeysBar(
    viewModel: TerminalViewModel,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val ctrlActive by viewModel.ctrlActive.collectAsState()
    val altActive by viewModel.altActive.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val commandHistory by viewModel.commandHistory.collectAsState()

    var showExtendedKeys by remember { mutableStateOf(false) }
    var showHistoryMenu by remember { mutableStateOf(false) }

    Surface(
        color = currentTheme.background.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            // Extended Keys Row (Collapsible)
            AnimatedVisibility(visible = showExtendedKeys) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VirtualKey.EXTENDED_KEYS.forEach { key ->
                        VirtualKeyButton(
                            key = key,
                            isHighlighted = false,
                            theme = currentTheme,
                            onClick = { viewModel.onVirtualKeyPressed(key) }
                        )
                    }
                }
            }

            // Primary Virtual Keys Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand / Collapse Extra Keys Toggle
                Box(
                    modifier = Modifier
                        .size(height = 36.dp, width = 34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(currentTheme.foreground.copy(alpha = 0.1f))
                        .clickable { showExtendedKeys = !showExtendedKeys },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showExtendedKeys) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle extra keys",
                        tint = currentTheme.foreground.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                VirtualKey.DEFAULT_TOP_KEYS.forEach { key ->
                    val isHighlighted = when (key.id) {
                        "ctrl" -> ctrlActive
                        "alt" -> altActive
                        else -> false
                    }

                    VirtualKeyButton(
                        key = key,
                        isHighlighted = isHighlighted,
                        theme = currentTheme,
                        onClick = { viewModel.onVirtualKeyPressed(key) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Command Input Box & Fast Action Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // History Dropdown
                Box {
                    IconButton(
                        onClick = { showHistoryMenu = true },
                        modifier = Modifier
                            .testTag("history_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Command History",
                            tint = currentTheme.promptColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showHistoryMenu,
                        onDismissRequest = { showHistoryMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (commandHistory.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No command history yet") },
                                onClick = { showHistoryMenu = false }
                            )
                        } else {
                            commandHistory.reversed().take(15).forEach { cmd ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = cmd,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateInputText(cmd)
                                        showHistoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Command Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    placeholder = {
                        Text(
                            text = "Enter shell command...",
                            color = currentTheme.foreground.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        autoCorrectEnabled = false
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { viewModel.submitInput() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = currentTheme.foreground,
                        unfocusedTextColor = currentTheme.foreground,
                        focusedBorderColor = currentTheme.promptColor,
                        unfocusedBorderColor = currentTheme.foreground.copy(alpha = 0.2f),
                        cursorColor = currentTheme.cursor,
                        focusedContainerColor = currentTheme.background,
                        unfocusedContainerColor = currentTheme.background
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier
                        .testTag("command_input_field")
                        .weight(1f)
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Enter / Send Button
                IconButton(
                    onClick = { viewModel.submitInput() },
                    modifier = Modifier
                        .testTag("send_command_button")
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentTheme.promptColor.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Execute Command",
                        tint = currentTheme.promptColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VirtualKeyButton(
    key: VirtualKey,
    isHighlighted: Boolean,
    theme: ThemePreset,
    onClick: () -> Unit
) {
    val bg = when {
        isHighlighted -> theme.promptColor
        key.type == VirtualKeyType.MODIFIER -> theme.selection
        key.type == VirtualKeyType.SPECIAL -> theme.foreground.copy(alpha = 0.15f)
        key.type == VirtualKeyType.ARROW -> theme.foreground.copy(alpha = 0.12f)
        else -> theme.foreground.copy(alpha = 0.08f)
    }

    val textColor = when {
        isHighlighted -> Color.Black
        key.type == VirtualKeyType.MODIFIER -> theme.promptColor
        key.type == VirtualKeyType.SPECIAL -> theme.ansiCyan
        else -> theme.foreground
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(36.dp)
            .width(if (key.label.length > 2) 46.dp else 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = key.label,
            color = textColor,
            fontSize = if (key.label.length > 3) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
