package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.QuickCommandsSheet
import com.example.ui.components.ScriptRunnerSheet
import com.example.ui.components.SessionTabsBar
import com.example.ui.components.SystemInfoDialog
import com.example.ui.components.TerminalSearchBar
import com.example.ui.components.TerminalScreen
import com.example.ui.components.TerminalSettingsDialog
import com.example.ui.components.VirtualKeysBar
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TerminalViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                TerminalAppMain(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            val session = viewModel.activeSession.value
            if (session != null) {
                if (it.isCtrlPressed) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_C -> {
                            session.sendCtrlC()
                            return true
                        }
                        KeyEvent.KEYCODE_D -> {
                            session.sendCtrlD()
                            return true
                        }
                        KeyEvent.KEYCODE_L -> {
                            session.sendCtrlL()
                            return true
                        }
                        KeyEvent.KEYCODE_Z -> {
                            session.sendCtrlZ()
                            return true
                        }
                    }
                }
                when (keyCode) {
                    KeyEvent.KEYCODE_TAB -> {
                        session.sendTab()
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        session.sendArrowUp()
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        session.sendArrowDown()
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        session.sendArrowLeft()
                        return true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        session.sendArrowRight()
                        return true
                    }
                    KeyEvent.KEYCODE_ESCAPE -> {
                        session.sendEsc()
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalAppMain(viewModel: TerminalViewModel) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()

    var showQuickCommands by remember { mutableStateOf(false) }
    var showScripts by remember { mutableStateOf(false) }
    var showSystemInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.ime),
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(currentTheme.background)
        ) {
            // Top Sessions & Actions Bar
            SessionTabsBar(
                viewModel = viewModel,
                onOpenQuickCommands = { showQuickCommands = true },
                onOpenScripts = { showScripts = true },
                onOpenSystemInfo = { showSystemInfo = true },
                onOpenSettings = { showSettings = true }
            )

            // Search Bar (if activated)
            if (isSearchVisible) {
                TerminalSearchBar(viewModel = viewModel)
            }

            // Central Terminal Output Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                TerminalScreen(viewModel = viewModel)
            }

            // Bottom Virtual Keys & Input Bar
            VirtualKeysBar(viewModel = viewModel)
        }

        // Modals & Bottom Sheets
        if (showQuickCommands) {
            QuickCommandsSheet(
                viewModel = viewModel,
                onDismiss = { showQuickCommands = false }
            )
        }

        if (showScripts) {
            ScriptRunnerSheet(
                viewModel = viewModel,
                onDismiss = { showScripts = false }
            )
        }

        if (showSystemInfo) {
            SystemInfoDialog(
                viewModel = viewModel,
                onDismiss = { showSystemInfo = false }
            )
        }

        if (showSettings) {
            TerminalSettingsDialog(
                viewModel = viewModel,
                onDismiss = { showSettings = false }
            )
        }
    }
}
