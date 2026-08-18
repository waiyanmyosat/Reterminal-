package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3FB950),
    onPrimary = Color(0xFF0D1117),
    primaryContainer = Color(0xFF1E3A24),
    onPrimaryContainer = Color(0xFF7EE787),
    secondary = Color(0xFF58A6FF),
    onSecondary = Color(0xFF0D1117),
    secondaryContainer = Color(0xFF1B2C40),
    onSecondaryContainer = Color(0xFFA5D6FF),
    tertiary = Color(0xFFBC8CFF),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF30363D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A7F37),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFF7E2),
    onPrimaryContainer = Color(0xFF093915),
    secondary = Color(0xFF0969DA),
    onSecondary = Color.White,
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF1F2328),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2328),
    surfaceVariant = Color(0xFFEAEEF2),
    onSurfaceVariant = Color(0xFF57606A),
    outline = Color(0xFFD0D7DE)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek terminal dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
