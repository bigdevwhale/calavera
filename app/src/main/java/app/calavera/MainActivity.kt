package app.calavera

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.calavera.data.Settings
import app.calavera.ui.CalaveraRoot
import app.calavera.ui.theme.CalaveraTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A night of candles: light icons in the bars.
        val style = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            CalaveraTheme {
                // Wait for settings so the welcome screen doesn't flash for someone already dressed up.
                settings?.let { CalaveraRoot(it) }
            }
        }
    }
}
