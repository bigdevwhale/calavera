package app.calavera.ui.art

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.calavera.domain.Look
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** The colours the art is painted with, so it draws the same in the app and in the renderers. */
object Ink {
    val bone = Color(0xFFF6EEDD)
    val boneEdge = Color(0xFF8E7F69)
    val socket = Color(0xFF1A1330)
    val fuchsia = Color(0xFFE8418E)
    val turquoise = Color(0xFF26C6B3)
    val marigold = Color(0xFFF7A21B)
    val marigoldDeep = Color(0xFFD9670B)
    val lime = Color(0xFFB9D83C)
    val violet = Color(0xFF9B6BEA)
    val rosa = Color(0xFFFF8FB8)
    val gold = Color(0xFFF5C84C)
    val night = Color(0xFF1E1733)
    val black = Color(0xFF221B2E)
    val leaf = Color(0xFF3FA34D)
    val flame = Color(0xFFFFD46B)
    val wood = Color(0xFFB86B32)

    val paints = listOf(fuchsia, turquoise, marigold, lime, violet)
    val tissue = listOf(fuchsia, turquoise, marigold, lime, violet, rosa)
    val blooms = listOf(marigold, fuchsia, turquoise, lime, violet, rosa)
}

/**
 * The calaca, dressed in [look]. [t] is seconds for the idle sway; [dance] 0..1 blends in a dance.
 * Drawn in an 80 × 110 unit box scaled to fit, feet at the bottom.
 */
fun DrawScope.drawCalaca(look: Look, t: Float, dance: Float = 0f) {
    val s = minOf(size.width / 80f, size.height / 110f)
    val ox = (size.width - 80f * s) / 2f
    val oy = size.height - 110f * s
    val paint = Ink.paints[look.color.coerceIn(0, Ink.paints.size - 1)]
    val bounce = abs(sin(t * 5.5f)) * 3.2f * dance
    val lean = sin(t * 2.75f) * 6f * dance + sin(t * 1.3f) * 1.5f
    translate(ox, oy - bounce * s) {
        rotate(lean, Offset(40f * s, 106f * s)) {
            Figure(this, s, look, paint, t, dance).draw()
        }
    }
}

private class Figure(val d: DrawScope, val s: Float, val look: Look, val paint: Color, val t: Float, val dance: Float) {
    fun p(x: Float, y: Float) = Offset(x * s, y * s)
    fun r(l: Float, tp: Float, rr: Float, b: Float) = Rect(l * s, tp * s, rr * s, b * s)

    private val shoulderL = Offset(30f, 54f)
    private val shoulderR = Offset(50f, 54f)
    private val upper = 12f
    private val fore = 11.5f

    fun draw() = with(d) {
        val (handL, handR) = targets()
        val elbowL = ik(shoulderL, handL, -1f)
        val elbowR = ik(shoulderR, handR, 1f)
        val skirt = look.clothes == "vestido"

        if (look.hand == "cane") cane(handR)
        legs(skirt)
        torso()
        clothes()
        if (look.extra == "rebozo") rebozo()
        if (look.extra == "collar") collar()
        if (look.hand == "guitar") guitar()
        arm(shoulderL, elbowL, handL)
        arm(shoulderR, elbowR, handR)
        when (look.hand) {
            "bouquet" -> bouquet(handR)
            "maracas" -> { maraca(handL, -1f); maraca(handR, 1f) }
        }
        head()
    }

    /** Where the hands go: by what they hold, swaying, and up in the air when dancing. */
    private fun targets(): Pair<Offset, Offset> {
        val sw = sin(t * 1.6f)
        var l = Offset(24f + sw * 0.8f, 75f)
        var r = when (look.hand) {
            "bouquet" -> Offset(57f, 62f + sw)
            "guitar" -> Offset(46f + sin(t * 9f) * 1.5f * (0.3f + dance), 71f)
            "cane" -> Offset(58f, 79f)
            "maracas" -> Offset(55f, 63f + sin(t * 7f) * 2f)
            else -> Offset(56f - sw * 0.8f, 75f)
        }
        if (look.hand == "guitar") l = Offset(36.5f, 55.5f)
        if (look.hand == "maracas") l = Offset(25f, 63f + sin(t * 7f + 1f) * 2f)
        if (dance > 0f) {
            val wave = sin(t * 5.5f)
            val upL = Offset(20f + wave * 3f, 38f + cos(t * 5.5f) * 2f)
            val upR = Offset(60f - wave * 3f, 38f - cos(t * 5.5f) * 2f)
            if (look.hand != "guitar") l = lerp(l, upL, dance)
            if (look.hand != "guitar" && look.hand != "cane") r = lerp(r, upR, dance)
        }
        return l to r
    }

    private fun lerp(a: Offset, b: Offset, k: Float) = Offset(a.x + (b.x - a.x) * k, a.y + (b.y - a.y) * k)

    /** Two-bone reach: the elbow for a hand at [target]; [side] -1 bends it out to the left. */
    private fun ik(shoulder: Offset, target: Offset, side: Float): Offset {
        val dx = target.x - shoulder.x
        val dy = target.y - shoulder.y
        val dist = hypot(dx, dy).coerceIn(abs(upper - fore) + 0.1f, upper + fore - 0.05f)
        val base = atan2(dy, dx)
        val a = acos(((upper * upper + dist * dist - fore * fore) / (2 * upper * dist)).coerceIn(-1f, 1f))
        val ang = base - side * a * (if (dy < 0) -1f else 1f)
        return Offset(shoulder.x + upper * cos(ang), shoulder.y + upper * sin(ang))
    }

    private fun bone(a: Offset, b: Offset, w: Float = 2.5f) = with(d) {
        drawLine(Ink.boneEdge, p(a.x, a.y), p(b.x, b.y), (w + 1.1f) * s, StrokeCap.Round)
        drawLine(Ink.bone, p(a.x, a.y), p(b.x, b.y), w * s, StrokeCap.Round)
        drawCircle(Ink.bone, w * 0.62f * s, p(a.x, a.y))
        drawCircle(Ink.bone, w * 0.62f * s, p(b.x, b.y))
    }

    private fun arm(sh: Offset, el: Offset, hand: Offset) = with(d) {
        bone(sh, el)
        bone(el, hand, 2.2f)
        drawCircle(Ink.boneEdge, 2.35f * s, p(hand.x, hand.y))
        drawCircle(Ink.bone, 1.9f * s, p(hand.x, hand.y))
    }

    private fun legs(skirt: Boolean) = with(d) {
        val pants = look.clothes in setOf("guayabera", "charro")
        val kneeL = Offset(35f, 92f)
        val kneeR = Offset(45f, 92f)
        val ankleL = Offset(34f, 104f)
        val ankleR = Offset(46f, 104f)
        if (!skirt) {
            if (pants) {
                val c = if (look.clothes == "charro") Ink.black else Color(0xFF3B2F5E)
                for ((a, b, e) in listOf(Triple(Offset(36f, 76f), kneeL, ankleL), Triple(Offset(44f, 76f), kneeR, ankleR))) {
                    drawLine(c, p(a.x, a.y), p(b.x, b.y), 6.4f * s, StrokeCap.Round)
                    drawLine(c, p(b.x, b.y), p(e.x, e.y - 1f), 6f * s, StrokeCap.Round)
                }
                if (look.clothes == "charro") {
                    for (y in listOf(80f, 85f, 90f, 95f, 100f)) {
                        drawCircle(Ink.gold, 0.8f * s, p(32.6f + (y - 80f) * -0.05f, y))
                        drawCircle(Ink.gold, 0.8f * s, p(47.4f + (y - 80f) * 0.05f, y))
                    }
                }
            } else {
                bone(Offset(36f, 79f), kneeL, 2.7f)
                bone(kneeL, ankleL, 2.4f)
                bone(Offset(44f, 79f), kneeR, 2.7f)
                bone(kneeR, ankleR, 2.4f)
            }
        }
        for ((x, dir) in listOf(31.5f to -1f, 48.5f to 1f)) {
            val shoe = if (pants || skirt) Ink.black else Ink.bone
            drawOval(Ink.boneEdge, p(x - 4.3f - dir * 0.4f, 103.6f), Size(8.6f * s, 4.4f * s))
            drawOval(shoe, p(x - 3.9f - dir * 0.4f, 104f), Size(7.8f * s, 3.6f * s))
        }
    }

    private fun torso() = with(d) {
        bone(Offset(40f, 50f), Offset(40f, 75f), 2.2f)
        // Ribs: four arches narrowing downwards.
        for (i in 0 until 4) {
            val y = 56f + i * 4f
            val hw = 9.5f - i * 1.1f
            val rect = r(40f - hw, y - 1f, 40f + hw, y + 7f)
            drawArc(Ink.boneEdge, 195f, 150f, false, rect.topLeft, rect.size, style = Stroke(3.1f * s, cap = StrokeCap.Round))
            drawArc(Ink.bone, 195f, 150f, false, rect.topLeft, rect.size, style = Stroke(2f * s, cap = StrokeCap.Round))
        }
        bone(shoulderL, shoulderR, 2.1f)
        // Pelvis: two wings with holes.
        for (x in listOf(33.5f, 40.5f)) {
            drawOval(Ink.boneEdge, p(x - 0.5f, 73.5f), Size(7f * s, 7f * s))
            drawOval(Ink.bone, p(x, 74f), Size(6f * s, 6f * s))
            drawCircle(Ink.socket, 1f * s, p(x + 3f, 77.4f))
        }
        // Neck.
        for (y in listOf(47.5f, 50f)) drawRoundRect(Ink.bone, p(38.6f, y), Size(2.8f * s, 1.8f * s), androidx.compose.ui.geometry.CornerRadius(0.6f * s))
    }

    private fun clothes() = with(d) {
        when (look.clothes) {
            "poncho" -> {
                val body = Path().apply {
                    moveTo(27f * s, 53f * s); lineTo(53f * s, 53f * s); lineTo(58f * s, 75f * s); lineTo(22f * s, 75f * s); close()
                }
                drawPath(body, Color(0xFF7A3E9D))
                val stripes = listOf(Ink.fuchsia, Ink.turquoise, Ink.marigold, Ink.lime, Ink.fuchsia)
                stripes.forEachIndexed { i, c ->
                    val y = 57f + i * 3.8f
                    val inset = (y - 53f) / 22f * 5f
                    drawRect(c, p(27f - inset, y), Size((26f + 2 * inset) * s, 1.6f * s))
                }
                // A neck slit and the fringe.
                drawPath(Path().apply { moveTo(37f * s, 53f * s); lineTo(40f * s, 58f * s); lineTo(43f * s, 53f * s); close() }, Ink.bone)
                for (i in 0..18) {
                    val x = 22.5f + i * 1.95f
                    drawLine(Ink.marigold, p(x, 75f), p(x, 77.4f), 0.7f * s)
                }
            }
            "guayabera" -> {
                val c = Color(0xFF2FA9A0)
                drawRoundRect(c, p(29f, 52f), Size(22f * s, 25f * s), androidx.compose.ui.geometry.CornerRadius(3f * s))
                drawPath(Path().apply { moveTo(36f * s, 52f * s); lineTo(40f * s, 57f * s); lineTo(44f * s, 52f * s); close() }, Ink.bone)
                for (x in listOf(33f, 47f)) drawLine(Color(0xFF237F78), p(x, 56f), p(x, 75f), 0.8f * s)
                for (y in listOf(60f, 64f, 68f, 72f)) drawCircle(Ink.bone, 0.6f * s, p(40f, y))
            }
            "charro" -> {
                drawRoundRect(Ink.black, p(28.5f, 52f), Size(23f * s, 17f * s), androidx.compose.ui.geometry.CornerRadius(3f * s))
                drawRect(Ink.bone, p(37.5f, 52f), Size(5f * s, 17f * s))
                for (y in listOf(55f, 59f, 63f, 67f)) {
                    drawCircle(Ink.gold, 0.75f * s, p(31f, y)); drawCircle(Ink.gold, 0.75f * s, p(49f, y))
                }
                drawLine(Ink.gold, p(28.5f, 68.6f), p(51.5f, 68.6f), 0.8f * s)
                // The moño: a red bow at the neck.
                drawPath(Path().apply { moveTo(40f * s, 53f * s); lineTo(35.5f * s, 51f * s); lineTo(35.5f * s, 55f * s); close() }, Color(0xFFD7263D))
                drawPath(Path().apply { moveTo(40f * s, 53f * s); lineTo(44.5f * s, 51f * s); lineTo(44.5f * s, 55f * s); close() }, Color(0xFFD7263D))
                drawCircle(Color(0xFFA51C30), 1f * s, p(40f, 53f))
            }
            "vestido" -> {
                val c = Ink.fuchsia
                val deep = Color(0xFFB72A6C)
                drawPath(Path().apply {
                    moveTo(29f * s, 53f * s); lineTo(51f * s, 53f * s); lineTo(46f * s, 71f * s); lineTo(34f * s, 71f * s); close()
                }, c)
                drawPath(Path().apply { moveTo(35f * s, 53f * s); quadraticTo(40f * s, 60f * s, 45f * s, 53f * s); close() }, Ink.bone)
                // Three ruffled tiers flaring to the floor.
                val tiers = listOf(Triple(71f, 82f, c), Triple(81f, 92f, deep), Triple(91f, 103f, c))
                tiers.forEachIndexed { i, (top, bot, col) ->
                    val wt = 7f + i * 5f
                    val wb = 11f + i * 5.5f
                    val path = Path().apply {
                        moveTo((40f - wt) * s, top * s); lineTo((40f + wt) * s, top * s); lineTo((40f + wb) * s, bot * s)
                        val n = 6 + i * 2
                        val step = 2 * wb / n
                        for (k in 0 until n) {
                            val x0 = 40f + wb - k * step
                            quadraticTo((x0 - step / 2) * s, (bot + 2.2f) * s, (x0 - step) * s, bot * s)
                        }
                        close()
                    }
                    drawPath(path, col)
                    for (k in 0..(6 + i * 2)) drawCircle(Ink.bone.copy(alpha = 0.85f), 0.55f * s, p(40f - wb + k * (2 * wb / (6 + i * 2)), bot - 1.2f))
                }
                drawLine(Ink.marigold, p(34f, 71f), p(46f, 71f), 1.4f * s)
            }
        }
    }

    private fun rebozo() = with(d) {
        val c = Color(0xFF3C8DBC)
        val path = Path().apply {
            moveTo(25f * s, 52f * s); quadraticTo(40f * s, 46f * s, 55f * s, 52f * s)
            lineTo(57f * s, 64f * s); quadraticTo(40f * s, 72f * s, 23f * s, 64f * s); close()
        }
        drawPath(path, c)
        for (k in 0..2) {
            val y = 56f + k * 3f
            drawLine(Ink.bone.copy(alpha = 0.6f), p(25f + k, y), p(55f - k, y), 0.6f * s)
        }
        for (i in 0..12) {
            val x = 24f + i * 2.7f
            val y = 64f + sin((x - 40f) / 17f * PI.toFloat() / 2 + PI.toFloat() / 2) * 4f
            drawLine(c, p(x, y), p(x, y + 3f), 0.6f * s)
        }
    }

    private fun collar() = with(d) {
        for (i in 0..9) {
            val a = PI * (0.12 + 0.76 * i / 9.0)
            val x = 40f + 11.5f * cos(a).toFloat()
            val y = 51f + 6f * sin(a).toFloat()
            marigoldBloom(Offset(x, y), 2.5f)
        }
    }

    private fun guitar() = with(d) {
        val wood = Ink.wood
        rotate(-28f, p(42f, 66f)) {
            drawLine(Color(0xFF5B3A1E), p(42f, 64f), p(42f, 42f), 2.2f * s, StrokeCap.Round)
            drawRoundRect(Color(0xFF3A2614), p(40.4f, 39f), Size(3.2f * s, 4.5f * s), androidx.compose.ui.geometry.CornerRadius(0.8f * s))
            drawCircle(wood, 7f * s, p(42f, 71f))
            drawCircle(wood, 5.3f * s, p(42f, 62.5f))
            drawCircle(Color(0xFF3A2614), 2f * s, p(42f, 66.5f))
            drawCircle(Ink.marigold, 2.5f * s, p(42f, 66.5f), style = Stroke(0.5f * s))
            for (k in -1..1) drawLine(Ink.bone.copy(alpha = 0.7f), p(42f + k * 0.5f, 42f), p(42f + k * 0.5f, 74f), 0.2f * s)
            drawRect(Color(0xFF3A2614), p(39.5f, 74f), Size(5f * s, 1.2f * s))
        }
    }

    private fun cane(hand: Offset) = with(d) {
        val c = Color(0xFF7A4A24)
        drawLine(c, p(hand.x + 0.5f, hand.y), p(hand.x + 2f, 106f), 1.6f * s, StrokeCap.Round)
        drawArc(c, 180f, 180f, false, p(hand.x - 3.6f, hand.y - 2.2f), Size(4.6f * s, 4.4f * s), style = Stroke(1.6f * s, cap = StrokeCap.Round))
    }

    fun marigoldBloom(c: Offset, rad: Float) = with(d) {
        for (k in 0 until 10) {
            val a = k * PI.toFloat() / 5
            drawCircle(Ink.marigoldDeep, rad * 0.55f * s, p(c.x + cos(a) * rad * 0.6f, c.y + sin(a) * rad * 0.6f))
        }
        drawCircle(Ink.marigold, rad * 0.75f * s, p(c.x, c.y))
        drawCircle(Ink.gold, rad * 0.3f * s, p(c.x, c.y))
    }

    private fun bouquet(hand: Offset) = with(d) {
        for ((dx, dy) in listOf(-3f to -9f, 0.5f to -11f, 3.5f to -8f)) drawLine(Ink.leaf, p(hand.x, hand.y), p(hand.x + dx, hand.y + dy), 0.8f * s)
        drawOval(Ink.leaf, p(hand.x - 4.5f, hand.y - 6f), Size(3.5f * s, 1.6f * s))
        drawOval(Ink.leaf, p(hand.x + 1.5f, hand.y - 5f), Size(3.5f * s, 1.6f * s))
        marigoldBloom(Offset(hand.x - 3f, hand.y - 9.5f), 2.8f)
        marigoldBloom(Offset(hand.x + 3.5f, hand.y - 8.5f), 2.6f)
        marigoldBloom(Offset(hand.x + 0.5f, hand.y - 12f), 3f)
        drawCircle(Ink.fuchsia, 1.1f * s, p(hand.x, hand.y - 1.2f))
    }

    private fun maraca(hand: Offset, side: Float) = with(d) {
        val shake = sin(t * 14f) * 12f
        rotate(side * (20f + shake), p(hand.x, hand.y)) {
            drawLine(Color(0xFF7A4A24), p(hand.x, hand.y + 1f), p(hand.x, hand.y - 4f), 1.3f * s, StrokeCap.Round)
            drawOval(if (side < 0) Ink.turquoise else Ink.fuchsia, p(hand.x - 3f, hand.y - 10.5f), Size(6f * s, 7f * s))
            drawLine(Ink.marigold, p(hand.x - 2.9f, hand.y - 7f), p(hand.x + 2.9f, hand.y - 7f), 1f * s)
            drawLine(Ink.lime, p(hand.x - 2.6f, hand.y - 8.8f), p(hand.x + 2.6f, hand.y - 8.8f), 0.7f * s)
        }
    }

    private fun head() = with(d) {
        val jaw = (if (dance > 0f) abs(sin(t * 8f)) * 1.6f * dance else 0f) + (if ((t % 7f) > 6.6f) 1.1f else 0f)
        // Jaw first, so the cheekbones sit over it.
        drawRoundRect(Ink.boneEdge, p(31.4f, 35.4f + jaw), Size(17.2f * s, 12.2f * s), androidx.compose.ui.geometry.CornerRadius(5f * s))
        drawRoundRect(Ink.bone, p(32f, 36f + jaw), Size(16f * s, 11f * s), androidx.compose.ui.geometry.CornerRadius(4.6f * s))
        drawOval(Ink.boneEdge, p(26.4f, 16.4f), Size(27.2f * s, 26.2f * s))
        drawOval(Ink.bone, p(27f, 17f), Size(26f * s, 25f * s))
        // Teeth.
        drawLine(Ink.socket, p(34f, 43.6f + jaw * 0.5f), p(46f, 43.6f + jaw * 0.5f), 0.5f * s)
        for (i in 0..6) {
            val x = 34f + i * 2f
            drawLine(Ink.socket, p(x, 41.6f + jaw * 0.3f), p(x, 45.6f + jaw), 0.45f * s)
        }
        if (look.extra == "goldtooth") {
            drawRect(Ink.gold, p(42.1f, 41.7f + jaw * 0.3f), Size(1.8f * s, (3.8f + jaw * 0.7f) * s))
            drawCircle(Color.White.copy(alpha = 0.9f), 0.4f * s, p(42.6f, 42.5f))
        }
        paintFace()
        // Eye sockets with a glint.
        for (x in listOf(34.2f, 45.8f)) {
            drawOval(Ink.socket, p(x - 4.4f, 26.1f), Size(8.8f * s, 9.8f * s))
            drawCircle(Color.White.copy(alpha = 0.85f), 0.9f * s, p(x + 1.4f, 29f))
        }
        // An upside-down heart for a nose.
        val nose = Path().apply {
            moveTo(40f * s, 40.4f * s)
            cubicTo(37f * s, 38.2f * s, 37.8f * s, 35.6f * s, 39.2f * s, 36.6f * s)
            lineTo(40f * s, 37.3f * s)
            lineTo(40.8f * s, 36.6f * s)
            cubicTo(42.2f * s, 35.6f * s, 43f * s, 38.2f * s, 40f * s, 40.4f * s)
            close()
        }
        drawPath(nose, Ink.socket)
        when (look.extra) {
            "glasses" -> glasses()
            "mustache" -> mustache()
        }
        hat()
    }

    private fun paintFace() = with(d) {
        val eyes = listOf(Offset(34.2f, 31f), Offset(45.8f, 31f))
        when (look.paint) {
            "petals" -> {
                for (e in eyes) for (k in 0 until 10) {
                    val a = k * PI.toFloat() / 5
                    val c = Offset(e.x + cos(a) * 6.4f, e.y + sin(a) * 6.9f)
                    rotate(a * 180f / PI.toFloat(), p(c.x, c.y)) { drawOval(if (k % 2 == 0) paint else Ink.marigold, p(c.x - 1.2f, c.y - 0.7f), Size(2.4f * s, 1.4f * s)) }
                }
                flower(Offset(40f, 22f), 2.4f, paint)
            }
            "hearts" -> {
                for (e in eyes) for (k in 0 until 12) {
                    val a = k * PI.toFloat() / 6
                    drawCircle(paint, 0.7f * s, p(e.x + cos(a) * 6.3f, e.y + sin(a) * 6.8f))
                }
                heart(Offset(40f, 22.5f), 3.2f, paint)
                heart(Offset(30.2f, 39.5f), 1.4f, Ink.fuchsia)
                heart(Offset(49.8f, 39.5f), 1.4f, Ink.fuchsia)
            }
            "spirals" -> {
                spiral(Offset(40f, 22.4f), 3.6f, paint)
                spiral(Offset(30.4f, 39f), 1.8f, paint)
                spiral(Offset(49.6f, 39f), 1.8f, paint)
                for (e in eyes) drawArc(paint, 200f, 140f, false, p(e.x - 6.4f, e.y - 6.8f), Size(12.8f * s, 13.6f * s), style = Stroke(0.8f * s, cap = StrokeCap.Round))
            }
            "stars" -> {
                star(Offset(40f, 22f), 3.4f, paint)
                for (e in eyes) for (k in 0 until 5) {
                    val a = -PI.toFloat() / 2 + k * PI.toFloat() * 2 / 5
                    star(Offset(e.x + cos(a) * 6.8f, e.y + sin(a) * 7.2f), 1f, if (k % 2 == 0) paint else Ink.gold)
                }
            }
            "lace" -> {
                for (e in eyes) for (k in 0 until 12) {
                    val a = k * PI.toFloat() / 6
                    val c = Offset(e.x + cos(a) * 6.2f, e.y + sin(a) * 6.7f)
                    drawCircle(paint, 1.05f * s, p(c.x, c.y), style = Stroke(0.5f * s))
                }
                for (k in -3..3) drawCircle(paint, 0.55f * s, p(40f + k * 2.2f, 21f + abs(k) * 0.6f))
                drawCircle(paint, 1.6f * s, p(40f, 23.6f))
            }
        }
    }

    fun flower(c: Offset, rad: Float, col: Color) = with(d) {
        for (k in 0 until 6) {
            val a = k * PI.toFloat() / 3
            drawCircle(col, rad * 0.5f * s, p(c.x + cos(a) * rad * 0.62f, c.y + sin(a) * rad * 0.62f))
        }
        drawCircle(Ink.gold, rad * 0.42f * s, p(c.x, c.y))
    }

    private fun heart(c: Offset, rad: Float, col: Color) = with(d) {
        val path = Path().apply {
            moveTo(c.x * s, (c.y + rad) * s)
            cubicTo((c.x - rad * 1.4f) * s, (c.y - rad * 0.1f) * s, (c.x - rad * 0.9f) * s, (c.y - rad * 1.2f) * s, c.x * s, (c.y - rad * 0.45f) * s)
            cubicTo((c.x + rad * 0.9f) * s, (c.y - rad * 1.2f) * s, (c.x + rad * 1.4f) * s, (c.y - rad * 0.1f) * s, c.x * s, (c.y + rad) * s)
            close()
        }
        drawPath(path, col)
    }

    private fun spiral(c: Offset, rad: Float, col: Color) = with(d) {
        val path = Path()
        val turns = 2.2f
        val n = 40
        for (i in 0..n) {
            val k = i / n.toFloat()
            val a = k * turns * 2 * PI.toFloat()
            val rr = rad * k
            val x = (c.x + cos(a) * rr) * s
            val y = (c.y + sin(a) * rr) * s
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, col, style = Stroke(0.75f * s, cap = StrokeCap.Round))
    }

    private fun star(c: Offset, rad: Float, col: Color) = with(d) {
        val path = Path()
        for (i in 0 until 10) {
            val a = -PI.toFloat() / 2 + i * PI.toFloat() / 5
            val rr = if (i % 2 == 0) rad else rad * 0.45f
            val x = (c.x + cos(a) * rr) * s
            val y = (c.y + sin(a) * rr) * s
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, col)
    }

    private fun glasses() = with(d) {
        for (x in listOf(34.2f, 45.8f)) drawCircle(Ink.gold, 5.4f * s, p(x, 31f), style = Stroke(0.9f * s))
        drawLine(Ink.gold, p(38.8f, 30.4f), p(41.2f, 30.4f), 0.9f * s)
        drawLine(Ink.gold, p(28.8f, 30.4f), p(27.2f, 29.6f), 0.9f * s)
        drawLine(Ink.gold, p(51.2f, 30.4f), p(52.8f, 29.6f), 0.9f * s)
    }

    private fun mustache() = with(d) {
        val m = Path().apply {
            moveTo(40f * s, 40.6f * s)
            cubicTo(37f * s, 39.4f * s, 34f * s, 40.4f * s, 32.4f * s, 42.4f * s)
            cubicTo(31.6f * s, 43.4f * s, 30.4f * s, 42.4f * s, 31f * s, 41.4f * s)
            cubicTo(30f * s, 43.6f * s, 33f * s, 44.6f * s, 35f * s, 43.2f * s)
            cubicTo(37f * s, 42f * s, 39f * s, 42.2f * s, 40f * s, 41.8f * s)
            cubicTo(41f * s, 42.2f * s, 43f * s, 42f * s, 45f * s, 43.2f * s)
            cubicTo(47f * s, 44.6f * s, 50f * s, 43.6f * s, 49f * s, 41.4f * s)
            cubicTo(49.6f * s, 42.4f * s, 48.4f * s, 43.4f * s, 47.6f * s, 42.4f * s)
            cubicTo(46f * s, 40.4f * s, 43f * s, 39.4f * s, 40f * s, 40.6f * s)
            close()
        }
        drawPath(m, Ink.black)
    }

    private fun hat() = with(d) {
        when (look.hat) {
            "flowers" -> {
                for (i in 0 until 7) {
                    val a = PI.toFloat() * (1.08f + 0.84f * i / 6f)
                    val c = Offset(40f + cos(a) * 12.6f, 29.5f + sin(a) * 12.6f)
                    val la = a + 0.35f
                    drawOval(Ink.leaf, p(c.x + cos(la) * 3f - 1.6f, c.y + sin(la) * 3f - 0.8f), Size(3.2f * s, 1.6f * s))
                    flower(c, if (i == 3) 4f else 3.1f, Ink.blooms[i % Ink.blooms.size])
                }
            }
            "sombrero" -> {
                val straw = Color(0xFFE9B949)
                val strawDark = Color(0xFFB98A2C)
                drawOval(strawDark, p(11f, 17.4f), Size(58f * s, 10f * s))
                drawOval(straw, p(11f, 16.4f), Size(58f * s, 9.4f * s))
                val crown = Path().apply {
                    moveTo(30f * s, 21f * s); cubicTo(30f * s, 4f * s, 50f * s, 4f * s, 50f * s, 21f * s); close()
                }
                drawPath(crown, straw)
                // A zigzag band in the paint colour.
                val band = Path()
                for (i in 0..10) {
                    val x = 30.8f + i * 1.84f
                    val y = if (i % 2 == 0) 15.4f else 18f
                    if (i == 0) band.moveTo(x * s, y * s) else band.lineTo(x * s, y * s)
                }
                drawPath(band, paint, style = Stroke(1.3f * s))
                for (i in 0 until 9) {
                    val x = 13f + i * 6.8f
                    drawCircle(if (i % 2 == 0) Ink.fuchsia else Ink.turquoise, 1.1f * s, p(x, 21.4f + sin(i / 8f * PI.toFloat()) * 4f))
                }
            }
            "tophat" -> {
                drawRoundRect(Ink.black, p(31f, 1f), Size(18f * s, 19f * s), androidx.compose.ui.geometry.CornerRadius(1.2f * s))
                drawRect(Ink.fuchsia, p(31f, 14.4f), Size(18f * s, 3.2f * s))
                drawRoundRect(Ink.black, p(25f, 18.2f), Size(30f * s, 3f * s), androidx.compose.ui.geometry.CornerRadius(1.5f * s))
                marigoldBloom(Offset(45.4f, 15.6f), 3f)
                drawOval(Color.White.copy(alpha = 0.08f), p(33f, 2.5f), Size(3f * s, 11f * s))
            }
            "catrina" -> {
                rotate(-9f, p(40f, 20f)) {
                    // Feathers behind, then the brim, the crown and a heap of flowers.
                    for ((dx, h, c) in listOf(Triple(-10f, 22f, Ink.violet), Triple(-6f, 26f, Ink.turquoise), Triple(-2f, 21f, Ink.fuchsia))) {
                        rotate(dx * 2.2f, p(30f + dx, 14f)) { drawOval(c, p(28f + dx, 14f - h), Size(4.5f * s, h * s)) }
                    }
                    drawOval(Color(0xFF4A2D7A), p(6f, 16.5f), Size(68f * s, 10.5f * s))
                    drawOval(Color(0xFF5D3A96), p(6f, 15.5f), Size(68f * s, 9.6f * s))
                    drawPath(Path().apply { moveTo(29f * s, 21f * s); cubicTo(29f * s, 6f * s, 51f * s, 6f * s, 51f * s, 21f * s); close() }, Color(0xFF5D3A96))
                    drawRect(Ink.fuchsia, p(29.6f, 16.4f), Size(20.8f * s, 2.4f * s))
                    flower(Offset(22f, 17f), 4.4f, Ink.fuchsia)
                    flower(Offset(28f, 13.5f), 3.8f, Ink.rosa)
                    marigoldBloom(Offset(16.5f, 19.5f), 3.2f)
                    flower(Offset(33.5f, 11f), 2.8f, Ink.turquoise)
                    drawOval(Ink.leaf, p(12f, 15f), Size(5f * s, 2f * s))
                    for (i in 0 until 12) drawCircle(Ink.bone.copy(alpha = 0.7f), 0.5f * s, p(9f + i * 5.6f, 23.8f + sin(i / 11f * PI.toFloat()) * 1.8f))
                }
            }
        }
    }
}

/** The calaca on screen, swaying; [dancing] makes it dance. [still] freezes it (renders, thumbnails). */
@Composable
fun Calaca(look: Look, modifier: Modifier = Modifier, dancing: Boolean = false, still: Boolean = false) {
    var t by remember { mutableFloatStateOf(1.2f) }
    var dance by remember { mutableFloatStateOf(0f) }
    val wantDance by rememberUpdatedState(dancing)
    LaunchedEffect(still) {
        if (still) return@LaunchedEffect
        var last = -1L
        while (true) {
            withInfiniteAnimationFrameMillis { ms ->
                val dt = if (last < 0) 0f else ((ms - last) / 1000f).coerceAtMost(0.1f)
                last = ms
                t += dt
                val goal = if (wantDance) 1f else 0f
                dance += (goal - dance) * (dt * 4f).coerceAtMost(1f)
            }
        }
    }
    Canvas(modifier) { drawCalaca(look, t, if (still) 0f else dance) }
}
