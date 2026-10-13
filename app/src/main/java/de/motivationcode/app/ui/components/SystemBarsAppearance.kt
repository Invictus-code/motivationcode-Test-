package de.motivationcode.app.ui.components

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Setzt helle oder dunkle Symbole in Status- und Navigationsleiste passend zum Hintergrund. */
@Composable
fun SystemBarsAppearance(lightBackground: Boolean) {
    val view = LocalView.current
    val window = LocalActivity.current?.window ?: return
    if (view.isInEditMode) return
    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = lightBackground
            isAppearanceLightNavigationBars = lightBackground
        }
    }
}
