package app.calavera

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import app.calavera.domain.Look
import app.calavera.domain.Offering
import app.calavera.domain.Picado
import app.calavera.domain.Slot
import app.calavera.domain.Wardrobe
import app.calavera.ui.art.drawAltar
import app.calavera.ui.art.drawCalaca
import app.calavera.ui.art.drawGarland
import app.calavera.ui.art.drawRoad
import app.calavera.ui.art.fearPicture
import app.calavera.ui.art.offering
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Draws the calaca in every outfit, the altar, the cards and the offerings to app/build/art (run with `-Prender`). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class RenderArt {
    private val out = File(System.getProperty("render.out") ?: "build/art", "sheets").apply { mkdirs() }
    private val night = Color(0xFF1E1733)

    private fun sheet(name: String, w: Int, h: Int, draw: DrawScope.() -> Unit) {
        val img = ImageBitmap(w, h)
        val canvas = androidx.compose.ui.graphics.Canvas(img)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(w.toFloat(), h.toFloat())) {
            drawRect(night)
            drawContext.canvas.saveLayer(Rect(0f, 0f, w.toFloat(), h.toFloat()), Paint())
            draw()
            drawContext.canvas.restore()
        }
        File(out, "$name.png").outputStream().use { img.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /** Draws [block] in cell [i] of a grid, as if the cell were the whole canvas. */
    private fun DrawScope.cell(i: Int, cols: Int, cw: Float, ch: Float, block: DrawScope.() -> Unit) {
        translate((i % cols) * cw, (i / cols) * ch) {
            val prev = drawContext.size
            drawContext.size = Size(cw, ch)
            block()
            drawContext.size = prev
        }
    }

    @Test
    fun wardrobe() {
        val rows = Slot.entries.map { s -> Wardrobe.of(s) }
        val cols = rows.maxOf { it.size }
        val cw = 260f
        val ch = 360f
        sheet("wardrobe", (cols * cw).toInt(), (rows.size * ch).toInt()) {
            rows.forEachIndexed { r, items ->
                items.forEachIndexed { k, item -> cell(r * cols + k, cols, cw, ch) { drawCalaca(Look().with(item), 1.2f) } }
            }
        }
    }

    @Test
    fun outfits() {
        val looks = listOf(
            Look(),
            Look(hat = "catrina", paint = "hearts", clothes = "vestido", hand = "bouquet", extra = "glasses", color = 0),
            Look(hat = "sombrero", paint = "stars", clothes = "charro", hand = "guitar", extra = "mustache", color = 2),
            Look(hat = "tophat", paint = "lace", clothes = "guayabera", hand = "cane", extra = "goldtooth", color = 1),
            Look(hat = "none_hat", paint = "spirals", clothes = "bare", hand = "maracas", extra = "rebozo", color = 4),
        )
        sheet("outfits", 5 * 400, 560) {
            looks.forEachIndexed { i, l -> cell(i, 5, 400f, 560f) { drawCalaca(l, 1.2f) } }
        }
        sheet("dance", 5 * 400, 560) {
            (0 until 5).forEach { i -> cell(i, 5, 400f, 560f) { drawCalaca(looks[1], 0.15f * i, 1f) } }
        }
    }

    @Test
    fun altar() {
        sheet("altar", 900, 1060) {
            drawAltar(listOf(0.4f, 0.62f, 0.8f).map { it * size.height }, 1f, true, (0 until 5).map { Picado.preset(it) })
        }
        sheet("garland", 1080, 300) {
            drawGarland((0 until 7).map { Picado.preset(it) to (1f - it * 0.13f) }, size.width, 10f, size.width / 8.4f, 0f)
        }
        sheet("road", 600, 700) { drawRoad(9, 0f) }
    }

    @Test
    fun cards() {
        sheet("cards", 6 * 200, 2 * 200) {
            (0 until 12).forEach { i ->
                val x = (i % 6) * 200f
                val y = (i / 6) * 200f
                drawRect(Color(0xFFF7EEDC), Offset(x + 10, y + 10), Size(180f, 180f))
                fearPicture(i, Rect(x + 20, y + 20, x + 180, y + 180))
            }
        }
        sheet("offerings", 6 * 140, 2 * 140) {
            Offering.entries.forEachIndexed { i, o -> offering(o, Offset((i % 6) * 140f + 70, (i / 6) * 140f + 70), 110f) }
        }
    }
}
