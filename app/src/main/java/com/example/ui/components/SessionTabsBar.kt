package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemePreset
import com.example.terminal.TerminalSession
import com.example.viewmodel.TerminalViewModel

@Composable
fun SessionTabsBar(
    viewModel: TerminalViewModel,
    onOpenQuickCommands: () -> Unit,
    onOpenScripts: () -> Unit,
    onOpenSystemInfo: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var renameSessionTarget by remember { mutableStateOf<TerminalSession?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    Surface(
        color = currentTheme.background,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp)
        ) {
            // App Branding Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(start = 4.dp, end = 6.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(currentTheme.promptColor.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "ReTerminal Logo",
                    tint = currentTheme.promptColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Scrollable Sessions Tab List
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                sessions.forEach { session ->
                    val isActive = session.id == activeSessionId
                    val isRunning by session.isRunning.collectAsState()
                    val title by session.title.collectAsState()

                    SessionTabItem(
                        title = title,
                        isActive = isActive,
                        isRunning = isRunning,
                        theme = currentTheme,
                        onClick = { viewModel.switchSession(session.id) },
                        onClose = { viewModel.closeSession(session.id) },
                        onLongClick = {
                            renameSessionTarget = session
                            renameInputText = title
                        }
                    )
                }

                // Add Session Tab Button
                IconButton(
                    onClick = { viewModel.createSession() },
                    modifier = Modifier
                        .testTag("add_session_button")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Session",
                        tint = currentTheme.promptColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Action Buttons (Search, Quick Commands, Overflow Menu)
            IconButton(
                onClick = { viewModel.toggleSearch() },
                modifier = Modifier
                    .testTag("search_toggle_button")
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Terminal",
                    tint = currentTheme.foreground.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .testTag("overflow_menu_button")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = currentTheme.foreground.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Quick Commands") },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenQuickCommands()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Script Runner") },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenScripts()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("System & ARM64 Info") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenSystemInfo()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export Terminal Logs") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            viewModel.exportActiveLogs(context)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear Active Screen") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            viewModel.clearCurrentBuffer()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Restart Session") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            viewModel.restartCurrentSession()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Terminal Settings") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    }

    // Rename Session Dialog
    renameSessionTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { renameSessionTarget = null },
            title = { Text("Rename Session") },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("Session Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            viewModel.renameSession(target.id, renameInputText.trim())
                        }
                        renameSessionTarget = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameSessionTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SessionTabItem(
    title: String,
    isActive: Boolean,
    isRunning: Boolean,
    theme: ThemePreset,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onLongClick: () -> Unit
) {
    val tabBg = if (isActive) theme.selection else Color.Transparent
    val textColor = if (isActive) theme.foreground else theme.foreground.copy(alpha = 0.6f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tabBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Status Dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isRunning) theme.promptColor else Color(0xFFFF5252))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close tab",
                tint = textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
