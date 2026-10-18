package app.calavera

import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import app.calavera.data.DemoSeed
import app.calavera.data.Settings
import app.calavera.ui.CalaveraRoot
import app.calavera.ui.theme.CalaveraTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Renders the main screens with the demo person to PNGs (run with `-Prender`): a look without a phone. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class RenderScreens {
    @get:Rule
    val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private val out = File(System.getProperty("render.out") ?: "build/art", "screens").apply { mkdirs() }

    private fun settle(ms: Long = 3000) {
        rule.mainClock.autoAdvance = false
        var t = 0L
        while (t < ms) {
            Thread.sleep(40)
            rule.mainClock.advanceTimeBy(100)
            t += 100
        }
    }

    private fun shot(name: String) {
        // Draw the window by hand: capturing waits for a frame, which a paused clock never gives.
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        rule.mainClock.advanceTimeByFrame()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val root = rule.activity.window.decorView
        val b = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        root.draw(android.graphics.Canvas(b))
        File(out, "$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun tap(text: String, ms: Long = 2500, exact: Boolean = false) {
        settle(600)
        rule.onAllNodesWithText(text, substring = !exact, useUnmergedTree = exact)[0].performClick()
        settle(ms)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    private fun back() {
        rule.onAllNodesWithContentDescription("Back")[0].performClick()
        settle(1500)
    }

    @Test
    fun screens() {
        val app = ApplicationProvider.getApplicationContext<CalaveraApp>()
        runBlocking { DemoSeed.seedIfEmpty(app.container, ru = false) }
        rule.setContent {
            val s: Settings? by app.container.settings.settings.collectAsState(initial = null)
            CalaveraTheme { s?.let { CalaveraRoot(it) } }
        }
        settle(5000)
        shot("01-casa")
        tap("Ofrenda", exact = true)
        shot("02-ofrenda")
        tap("Nana Rosa", exact = true)
        shot("03-soul")
        back()
        tap("Fiesta", exact = true)
        shot("04-fiesta")
        tap("Lotería of fears")
        shot("05-loteria")
        tap("Draw today")
        tap("Tap the card", 2000)
        shot("06-card")
        back()
        tap("Calaveritas", exact = true)
        shot("07-calaveritas")
        back()
        tap("Papel picado", exact = true)
        shot("08-picado")
        back()
        tap("Marigold road", exact = true)
        shot("09-camino")
        back()
        tap("Fear thermometer", exact = true)
        shot("10-check")
    }
}
