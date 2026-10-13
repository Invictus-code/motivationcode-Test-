package de.motivationcode.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2C5364),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC4E7F8),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF8C4A2F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF380D00),
    background = Color(0xFFF8F9FB),
    surface = Color(0xFFF8F9FB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FCFEA),
    onPrimary = Color(0xFF003546),
    primaryContainer = Color(0xFF1F4D5F),
    onPrimaryContainer = Color(0xFFC4E7F8),
    secondary = Color(0xFFFFB59B),
    onSecondary = Color(0xFF552008),
    secondaryContainer = Color(0xFF71351D),
    onSecondaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF111416),
    surface = Color(0xFF111416),
)

@Composable
fun TagesmotivationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
