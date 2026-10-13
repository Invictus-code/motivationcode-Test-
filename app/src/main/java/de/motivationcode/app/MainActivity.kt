package de.motivationcode.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.motivationcode.app.ui.navigation.AppNavigation
import de.motivationcode.app.ui.theme.TagesmotivationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TagesmotivationTheme {
                AppNavigation()
            }
        }
    }
}
