package app.calavera.ui.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.calavera.domain.Offering
import app.calavera.domain.Picado
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The outline of a papel picado flag: a rectangle with a scalloped bottom edge. */
fun flagPath(r: Rect): Path = Path().apply {
    val teeth = 8
    val tooth = r.width / teeth
    moveTo(r.left, r.top)
    lineTo(r.right, r.top)
    lineTo(r.right, r.bottom - tooth * 0.5f)
    for (i in 0 until teeth) {
        val x0 = r.right - i * tooth
        quadraticTo(x0 - tooth / 2, r.bottom + tooth * 0.15f, x0 - tooth, r.bottom - tooth * 0.5f)
    }
    close()
}

/**
 * A papel picado in [r]: the tissue in its colour with the cuts showing through. [life] 1..0 fades
 * and droops it as the week goes by. Draw inside an offscreen layer, since cuts clear pixels.
 */
fun DrawScope.drawPicado(p: Picado, r: Rect, life: Float = 1f, cuts: List<List<Picado.Pt>> = p.unfolded()) {
    val col = Ink.tissue[p.color.coerceIn(0, Ink.tissue.size - 1)]
    val faded = lerpColor(col, Color(0xFFD8CFC4), (1f - life) * 0.55f).copy(alpha = 0.35f + 0.65f * life)
    drawPath(flagPath(r), faded)
    // Border cut: a row of small holes along the top, like the punched trim.
    val holes = 10
    for (i in 0 until holes) {
        drawCircle(Color.Transparent, r.width * 0.012f, Offset(r.left + r.width * (i + 0.5f) / holes, r.top + r.height * 0.08f), blendMode = BlendMode.Clear)
    }
    val w = Picado.CUT * r.width / 2f
    for (s in cuts) {
        if (s.isEmpty()) continue
        val path = Path()
        s.forEachIndexed { i, pt ->
            val x = r.left + pt.x * r.width
            val y = r.top + pt.y * r.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        if (s.size == 1) drawCircle(Color.Transparent, w / 2, Offset(r.left + s[0].x * r.width, r.top + s[0].y * r.height), blendMode = BlendMode.Clear)
        else drawPath(path, Color.Transparent, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round), blendMode = BlendMode.Clear)
    }
}

fun lerpColor(a: Color, b: Color, k: Float): Color {
    val t = k.coerceIn(0f, 1f)
    return Color(a.red + (b.red - a.red) * t, a.green + (b.green - a.green) * t, a.blue + (b.blue - a.blue) * t, a.alpha + (b.alpha - a.alpha) * t)
}

/**
 * A string of flags across the top of [width]: [flags] are designs with their life left. Each sways
 * a little with [t]. Draw inside an offscreen layer.
 */
fun DrawScope.drawGarland(flags: List<Pair<Picado, Float>>, width: Float, top: Float, flagW: Float, t: Float) {
    val n = flags.size.coerceAtLeast(1)
    val gap = width / n
    val sag = flagW * 0.25f
    val rope = Path().apply {
        moveTo(0f, top)
        quadraticTo(width / 2, top + sag * 2, width, top)
    }
    drawPath(rope, Color(0xFFEDE3CF).copy(alpha = 0.8f), style = Stroke(1.5f))
    flags.forEachIndexed { i, (p, life) ->
        val cx = gap * (i + 0.5f)
        val k = cx / width
        val y = top + 4 * sag * k * (1 - k) * 0.5f * 2 - 1f
        val droop = (1f - life) * flagW * 0.12f
        val sway = sin(t * 1.3f + i * 0.9f) * 3f * (0.4f + life)
        rotate(sway + (1f - life) * (if (i % 2 == 0) 8f else -8f), Offset(cx, y)) {
            drawPicado(p, Rect(cx - flagW / 2, y + droop, cx + flagW / 2, y + droop + flagW * 1.25f), life)
        }
    }
}

/** A cempasúchil bloom: a ruffled ball of orange. */
fun DrawScope.marigold(c: Offset, r: Float, glow: Boolean = false) {
    if (glow) drawCircle(Brush.radialGradient(listOf(Ink.marigold.copy(alpha = 0.35f), Color.Transparent), c, r * 2.4f), r * 2.4f, c)
    for (ring in 0 until 3) {
        val rr = r * (1f - ring * 0.28f)
        val n = 12 - ring * 2
        for (k in 0 until n) {
            val a = k * 2 * PI.toFloat() / n + ring * 0.3f
            drawCircle(if (ring == 1) Ink.marigold else Ink.marigoldDeep, rr * 0.36f, Offset(c.x + cos(a) * rr * 0.68f, c.y + sin(a) * rr * 0.68f))
        }
    }
    drawCircle(Ink.gold, r * 0.22f, c)
}

/** A church candle with a living flame; [flicker] is time in seconds. */
fun DrawScope.candle(base: Offset, w: Float, h: Float, flicker: Float, lit: Boolean = true, wax: Color = Color(0xFFF4E9D4)) {
    drawRoundRect(wax, Offset(base.x - w / 2, base.y - h), Size(w, h), CornerRadius(w * 0.15f))
    drawRect(Color.Black.copy(alpha = 0.1f), Offset(base.x + w * 0.15f, base.y - h), Size(w * 0.35f, h))
    drawLine(Color(0xFF3A2A1A), Offset(base.x, base.y - h), Offset(base.x, base.y - h - w * 0.25f), w * 0.08f)
    if (!lit) return
    val f = 1f + sin(flicker * 11f) * 0.08f + sin(flicker * 17f) * 0.05f
    val tip = Offset(base.x + sin(flicker * 7f) * w * 0.06f, base.y - h - w * 1.25f * f)
    drawCircle(Brush.radialGradient(listOf(Ink.flame.copy(alpha = 0.45f), Color.Transparent), Offset(base.x, base.y - h - w * 0.5f), w * 2.6f), w * 2.6f, Offset(base.x, base.y - h - w * 0.5f))
    val flame = Path().apply {
        moveTo(tip.x, tip.y)
        cubicTo(base.x + w * 0.42f, base.y - h - w * 0.55f, base.x + w * 0.25f, base.y - h - w * 0.05f, base.x, base.y - h - w * 0.05f)
        cubicTo(base.x - w * 0.25f, base.y - h - w * 0.05f, base.x - w * 0.42f, base.y - h - w * 0.55f, tip.x, tip.y)
        close()
    }
    drawPath(flame, Ink.flame)
    drawOval(Color.White.copy(alpha = 0.85f), Offset(base.x - w * 0.09f, base.y - h - w * 0.5f), Size(w * 0.18f, w * 0.38f))
}

/** A little sugar skull for the altar. */
fun DrawScope.sugarSkull(c: Offset, r: Float, accent: Color) {
    drawRoundRect(Ink.bone, Offset(c.x - r * 0.55f, c.y + r * 0.2f), Size(r * 1.1f, r * 0.8f), CornerRadius(r * 0.3f))
    drawOval(Ink.bone, Offset(c.x - r, c.y - r), Size(2 * r, r * 1.7f))
    for (x in listOf(-0.42f, 0.42f)) {
        drawCircle(accent, r * 0.36f, Offset(c.x + x * r, c.y - r * 0.05f))
        drawCircle(Ink.socket, r * 0.22f, Offset(c.x + x * r, c.y - r * 0.05f))
    }
    drawCircle(accent, r * 0.16f, Offset(c.x, c.y - r * 0.62f))
    drawCircle(Ink.socket, r * 0.08f, Offset(c.x, c.y + r * 0.35f))
    for (k in -2..2) drawLine(Ink.socket, Offset(c.x + k * r * 0.14f, c.y + r * 0.62f), Offset(c.x + k * r * 0.14f, c.y + r * 0.88f), r * 0.04f)
}

/** Pan de muerto: a round loaf with crossed bones on top. */
fun DrawScope.panDeMuerto(c: Offset, r: Float) {
    drawOval(Color(0xFFC98A3E), Offset(c.x - r, c.y - r * 0.55f), Size(2 * r, r * 1.1f))
    drawOval(Color(0xFFE0A85A), Offset(c.x - r * 0.9f, c.y - r * 0.55f), Size(1.8f * r, r * 0.8f))
    for (a in listOf(-25f, 25f, 90f)) rotate(a, c) { drawRoundRect(Color(0xFFB57430), Offset(c.x - r * 0.8f, c.y - r * 0.38f), Size(r * 1.6f, r * 0.16f), CornerRadius(r * 0.08f)) }
    drawCircle(Color(0xFFB57430), r * 0.18f, Offset(c.x, c.y - r * 0.32f))
    for (k in 0 until 8) drawCircle(Color.White.copy(alpha = 0.7f), r * 0.03f, Offset(c.x + (k - 4) * r * 0.2f, c.y - r * 0.1f + (k % 3) * r * 0.08f))
}

/** A small picture of an offering in a box of [s] with centre [c]. */
fun DrawScope.offering(o: Offering, c: Offset, s: Float) {
    val u = s / 10f
    when (o) {
        Offering.BREAD -> panDeMuerto(c, 4f * u)
        Offering.COFFEE -> {
            drawRoundRect(Color(0xFFB4532A), Offset(c.x - 3f * u, c.y - 2f * u), Size(6f * u, 5.5f * u), CornerRadius(1.4f * u))
            drawArc(Color(0xFFB4532A), -90f, 180f, false, Offset(c.x + 1.8f * u, c.y - 1f * u), Size(3f * u, 3f * u), style = Stroke(0.9f * u))
            drawOval(Color(0xFF3B2314), Offset(c.x - 2.6f * u, c.y - 2.4f * u), Size(5.2f * u, 1.3f * u))
            for (k in -1..1) drawLine(Color.White.copy(alpha = 0.5f), Offset(c.x + k * 1.3f * u, c.y - 3f * u), Offset(c.x + k * 1.3f * u + 0.5f * u, c.y - 5f * u), 0.4f * u, StrokeCap.Round)
        }
        Offering.FRUIT -> {
            drawCircle(Color(0xFFF08A24), 2.6f * u, Offset(c.x - 1.4f * u, c.y + 1f * u))
            drawCircle(Color(0xFFDA3B3B), 2.2f * u, Offset(c.x + 2f * u, c.y + 1.4f * u))
            drawCircle(Ink.lime, 1.9f * u, Offset(c.x + 0.4f * u, c.y - 2f * u))
            drawOval(Ink.leaf, Offset(c.x + 0.6f * u, c.y - 4.6f * u), Size(2f * u, 1f * u))
        }
        Offering.SUGAR_SKULL -> sugarSkull(c, 3.6f * u, Ink.fuchsia)
        Offering.CHOCOLATE -> {
            drawRoundRect(Color(0xFF5A3420), Offset(c.x - 3.6f * u, c.y - 2.6f * u), Size(7.2f * u, 5.2f * u), CornerRadius(0.6f * u))
            for (i in 0..2) for (j in 0..1) drawRoundRect(Color(0xFF6E4029), Offset(c.x - 3.2f * u + i * 2.3f * u, c.y - 2.2f * u + j * 2.3f * u), Size(2f * u, 2f * u), CornerRadius(0.3f * u))
            drawRect(Ink.fuchsia, Offset(c.x - 3.6f * u, c.y + 0.3f * u), Size(7.2f * u, 2.3f * u))
        }
        Offering.TEQUILA -> {
            drawRoundRect(Color(0xFF9CCB7A).copy(alpha = 0.85f), Offset(c.x - 1.8f * u, c.y - 1.5f * u), Size(3.6f * u, 6f * u), CornerRadius(0.8f * u))
            drawRect(Color(0xFF9CCB7A).copy(alpha = 0.85f), Offset(c.x - 0.7f * u, c.y - 4.4f * u), Size(1.4f * u, 3f * u))
            drawRect(Ink.marigold, Offset(c.x - 1.8f * u, c.y + 0.6f * u), Size(3.6f * u, 1.8f * u))
            drawRect(Color(0xFF7A4A24), Offset(c.x - 0.8f * u, c.y - 5f * u), Size(1.6f * u, 0.9f * u))
        }
        Offering.BOOK -> {
            drawRoundRect(Color(0xFF3C6DB0), Offset(c.x - 3.6f * u, c.y - 2.8f * u), Size(7.2f * u, 5.6f * u), CornerRadius(0.5f * u))
            drawRect(Color(0xFFF4E9D4), Offset(c.x - 3.2f * u, c.y + 2f * u), Size(6.4f * u, 0.7f * u))
            drawLine(Ink.gold, Offset(c.x - 2f * u, c.y - 1f * u), Offset(c.x + 2f * u, c.y - 1f * u), 0.5f * u)
        }
        Offering.MUSIC -> {
            drawCircle(Ink.gold, 1.5f * u, Offset(c.x - 1.6f * u, c.y + 2.4f * u))
            drawCircle(Ink.gold, 1.5f * u, Offset(c.x + 2.6f * u, c.y + 1.6f * u))
            drawLine(Ink.gold, Offset(c.x - 0.2f * u, c.y + 2.4f * u), Offset(c.x - 0.2f * u, c.y - 3.6f * u), 0.6f * u)
            drawLine(Ink.gold, Offset(c.x + 4f * u, c.y + 1.6f * u), Offset(c.x + 4f * u, c.y - 4.4f * u), 0.6f * u)
            drawLine(Ink.gold, Offset(c.x - 0.2f * u, c.y - 3.6f * u), Offset(c.x + 4f * u, c.y - 4.4f * u), 1.2f * u)
        }
        Offering.CIGAR -> {
            rotate(-20f, c) {
                drawRoundRect(Color(0xFF8A5A2E), Offset(c.x - 4f * u, c.y - 0.8f * u), Size(7.4f * u, 1.6f * u), CornerRadius(0.8f * u))
                drawRect(Ink.gold, Offset(c.x - 2.2f * u, c.y - 0.8f * u), Size(1.1f * u, 1.6f * u))
                drawCircle(Color(0xFFFF6A2A), 0.7f * u, Offset(c.x + 3.5f * u, c.y))
            }
            drawLine(Color.White.copy(alpha = 0.4f), Offset(c.x + 3.5f * u, c.y - 2f * u), Offset(c.x + 3f * u, c.y - 4.5f * u), 0.4f * u, StrokeCap.Round)
        }
        Offering.TOY -> {
            drawCircle(Ink.fuchsia, 2.6f * u, Offset(c.x, c.y + 1f * u))
            drawArc(Ink.gold, 200f, 140f, false, Offset(c.x - 2.6f * u, c.y - 1.6f * u), Size(5.2f * u, 5.2f * u), style = Stroke(0.5f * u))
            drawLine(Ink.lime, Offset(c.x + 1.8f * u, c.y - 1f * u), Offset(c.x + 4.4f * u, c.y - 3.6f * u), 0.5f * u)
        }
        Offering.SWEETS -> {
            for ((dx, col) in listOf(-2.4f to Ink.fuchsia, 0f to Ink.turquoise, 2.4f to Ink.lime)) {
                drawCircle(col, 1.4f * u, Offset(c.x + dx * u, c.y + (if (dx == 0f) -1.2f else 1f) * u))
            }
        }
        Offering.WATER -> {
            drawPath(Path().apply {
                moveTo(c.x - 2.4f * u, c.y - 3.2f * u); lineTo(c.x + 2.4f * u, c.y - 3.2f * u); lineTo(c.x + 1.8f * u, c.y + 3.4f * u); lineTo(c.x - 1.8f * u, c.y + 3.4f * u); close()
            }, Color(0xFFBFE6F2).copy(alpha = 0.6f))
            drawRect(Color(0xFF5CC0E0).copy(alpha = 0.7f), Offset(c.x - 2.05f * u, c.y - 0.6f * u), Size(4.1f * u, 3.8f * u))
        }
    }
}

/**
 * The ofrenda behind the portraits: an arch of marigolds, papel picado, three tiers in cloth, candles,
 * flowers and bread. [tiers] are the tiers' top edges (y) for laying out portraits; [fiesta] lights it up.
 */
fun DrawScope.drawAltar(tiers: List<Float>, t: Float, fiesta: Boolean, flags: List<Picado>) {
    val w = size.width
    val h = size.height
    // The arch of flowers.
    val archTop = h * 0.04f
    val archPath = Path().apply {
        moveTo(w * 0.06f, h * 0.95f)
        lineTo(w * 0.06f, h * 0.3f)
        cubicTo(w * 0.06f, archTop, w * 0.94f, archTop, w * 0.94f, h * 0.3f)
        lineTo(w * 0.94f, h * 0.95f)
    }
    drawPath(archPath, Ink.leaf.copy(alpha = 0.55f), style = Stroke(w * 0.035f, cap = StrokeCap.Round))
    val m = androidx.compose.ui.graphics.PathMeasure().apply { setPath(archPath, false) }
    val n = 34
    for (i in 0..n) {
        val pos = m.getPosition(m.length * i / n)
        marigold(pos, w * 0.026f, glow = fiesta && i % 3 == 0)
    }
    // Papel picado inside the arch.
    val fw = w * 0.105f
    val left = w * 0.17f
    val span = w * 0.66f
    flags.take(5).forEachIndexed { i, p ->
        val cx = left + span * (i + 0.5f) / 5
        rotate(sin(t * 1.2f + i) * 2.5f, Offset(cx, h * 0.095f)) {
            drawPicado(p, Rect(cx - fw / 2, h * 0.095f, cx + fw / 2, h * 0.095f + fw * 1.2f))
        }
    }
    drawLine(Color(0xFFEDE3CF).copy(alpha = 0.8f), Offset(left - fw * 0.2f, h * 0.095f), Offset(left + span + fw * 0.2f, h * 0.095f), 1.5f)
    // Tiers: cloth with a lace edge, widening downwards.
    val cloths = listOf(Color(0xFF6B3FA0), Color(0xFFC2306F), Color(0xFF1F8F86))
    tiers.forEachIndexed { i, y ->
        val inset = w * (0.24f - i * 0.07f)
        val bottom = if (i + 1 < tiers.size) tiers[i + 1] else h * 0.97f
        drawRect(cloths[i % cloths.size], Offset(inset, y), Size(w - 2 * inset, bottom - y))
        drawRect(Color.Black.copy(alpha = 0.18f), Offset(inset, y), Size(w - 2 * inset, (bottom - y) * 0.12f))
        val scallops = ((w - 2 * inset) / (w * 0.035f)).toInt()
        val sw = (w - 2 * inset) / scallops
        for (k in 0 until scallops) {
            drawArc(Color(0xFFF4E9D4), 0f, 180f, true, Offset(inset + k * sw, y - sw * 0.5f), Size(sw, sw))
            drawCircle(cloths[i % cloths.size], sw * 0.14f, Offset(inset + k * sw + sw / 2, y + sw * 0.15f))
        }
    }
    // Candles on the bottom tier and the floor, flowers and bread on the sides.
    val floor = h * 0.97f
    val cw = w * 0.032f
    for ((x, hh) in listOf(0.13f to 0.13f, 0.2f to 0.09f, 0.8f to 0.09f, 0.87f to 0.13f)) candle(Offset(w * x, floor), cw, h * hh, t + x * 10f)
    // Pots of marigolds and sugar skulls along the middle tier.
    val mid = tiers[1]
    for (x in listOf(0.27f, 0.73f)) {
        drawRoundRect(Color(0xFFB4532A), Offset(w * x - w * 0.03f, mid - w * 0.05f), Size(w * 0.06f, w * 0.05f), CornerRadius(w * 0.008f))
        for ((dx, dy) in listOf(-0.022f to -0.075f, 0.022f to -0.075f, 0f to -0.1f)) marigold(Offset(w * (x + dx), mid + w * dy), w * 0.024f)
    }
    for (x in listOf(0.38f, 0.62f)) sugarSkull(Offset(w * x, mid - w * 0.03f), w * 0.024f, if (x < 0.5f) Ink.lime else Ink.violet)
    val low = tiers.last()
    for ((x, hh) in listOf(0.3f to 0.05f, 0.42f to 0.07f, 0.58f to 0.07f, 0.7f to 0.05f)) candle(Offset(w * x, low + h * 0.085f), cw * 0.8f, h * hh, t + x * 13f)
    panDeMuerto(Offset(w * 0.5f, low + h * 0.07f), w * 0.045f)
    sugarSkull(Offset(w * 0.36f, floor - w * 0.04f), w * 0.03f, Ink.turquoise)
    sugarSkull(Offset(w * 0.64f, floor - w * 0.04f), w * 0.03f, Ink.fuchsia)
    // Marigold petals strewn on the floor: the path the souls follow home.
    val r = java.util.Random(7)
    repeat(if (fiesta) 70 else 26) {
        val x = w * (0.08f + r.nextFloat() * 0.84f)
        val y = floor - r.nextFloat() * h * 0.025f
        drawCircle(if (r.nextBoolean()) Ink.marigold else Ink.marigoldDeep, w * 0.006f + r.nextFloat() * w * 0.005f, Offset(x, y))
    }
    if (fiesta) drawRect(Brush.verticalGradient(listOf(Color.Transparent, Ink.marigold.copy(alpha = 0.16f)), startY = h * 0.5f, endY = h), Offset(0f, h * 0.5f), Size(w, h * 0.5f))
}

/** The marigold road: a winding path with a bloom for each footprint, starting from the bottom. */
fun DrawScope.drawRoad(count: Int, t: Float) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.97f)
        cubicTo(w * 0.05f, h * 0.8f, w * 0.95f, h * 0.62f, w * 0.5f, h * 0.47f)
        cubicTo(w * 0.1f, h * 0.32f, w * 0.85f, h * 0.17f, w * 0.55f, h * 0.05f)
    }
    drawPath(path, Color(0xFF3A2D5E), style = Stroke(w * 0.11f, cap = StrokeCap.Round))
    drawPath(path, Color(0xFF45366F), style = Stroke(w * 0.07f, cap = StrokeCap.Round))
    val m = androidx.compose.ui.graphics.PathMeasure().apply { setPath(path, false) }
    val slots = 40
    for (i in 0 until slots) {
        val pos = m.getPosition(m.length * (i + 0.5f) / slots)
        val side = if (i % 2 == 0) -1f else 1f
        val tan = m.getTangent(m.length * (i + 0.5f) / slots)
        val nrm = Offset(-tan.y, tan.x) * (w * 0.05f * side)
        val at = pos + nrm
        if (i < count) marigold(at, w * 0.03f * (1f + 0.06f * sin(t * 2f + i)), glow = i == count - 1)
        else drawCircle(Color.White.copy(alpha = 0.07f), w * 0.012f, at)
    }
}

/** A lotería picture for each fear, drawn in a square [r]. */
fun DrawScope.fearPicture(i: Int, r: Rect) {
    val c = r.center
    val u = r.width / 10f
    translate(0f, 0f) {
        when (i) {
            0 -> { // El Olvido: footprints fading into the distance.
                for (k in 0 until 5) {
                    val a = 1f - k * 0.18f
                    val x = c.x + (if (k % 2 == 0) -1.2f else 1.2f) * u
                    val y = r.bottom - u * (1.5f + k * 1.7f)
                    drawOval(Ink.socket.copy(alpha = a), Offset(x - 0.8f * u, y - 1.1f * u), Size(1.6f * u * (1 - k * 0.1f), 2.2f * u * (1 - k * 0.1f)))
                }
            }
            1 -> { // La Nada: a crescent moon in an empty sky.
                drawCircle(Ink.gold, 3.2f * u, c)
                drawCircle(Color(0xFFF4E9D4), 2.8f * u, Offset(c.x + 1.5f * u, c.y - 0.8f * u))
                for ((dx, dy) in listOf(-3.5f to -3f, 3.4f to 3f, -2.5f to 3.6f)) drawCircle(Ink.gold, 0.3f * u, Offset(c.x + dx * u, c.y + dy * u))
            }
            2 -> { // El Dolor: a rose with thorns.
                drawLine(Ink.leaf, Offset(c.x, c.y), Offset(c.x, r.bottom - u), 0.5f * u)
                for (k in 0..2) drawLine(Ink.leaf, Offset(c.x, c.y + (1.5f + k * 1.2f) * u), Offset(c.x + (if (k % 2 == 0) 0.8f else -0.8f) * u, c.y + (1.1f + k * 1.2f) * u), 0.35f * u)
                drawCircle(Color(0xFFC8243B), 2.3f * u, Offset(c.x, c.y - 1f * u))
                drawArc(Color(0xFF8E1428), 200f, 220f, false, Offset(c.x - 1.3f * u, c.y - 2.3f * u), Size(2.6f * u, 2.6f * u), style = Stroke(0.35f * u))
            }
            3 -> { // El Reloj: an hourglass.
                val g = Path().apply {
                    moveTo(c.x - 2.6f * u, c.y - 3.6f * u); lineTo(c.x + 2.6f * u, c.y - 3.6f * u); lineTo(c.x + 0.3f * u, c.y)
                    lineTo(c.x + 2.6f * u, c.y + 3.6f * u); lineTo(c.x - 2.6f * u, c.y + 3.6f * u); lineTo(c.x - 0.3f * u, c.y); close()
                }
                drawPath(g, Color(0xFFBFE6F2).copy(alpha = 0.6f))
                drawPath(Path().apply { moveTo(c.x - 1.4f * u, c.y + 3.5f * u); lineTo(c.x + 1.4f * u, c.y + 3.5f * u); lineTo(c.x, c.y + 1.4f * u); close() }, Ink.marigold)
                drawPath(Path().apply { moveTo(c.x - 1.6f * u, c.y - 2.2f * u); lineTo(c.x + 1.6f * u, c.y - 2.2f * u); lineTo(c.x, c.y - 0.2f * u); close() }, Ink.marigold)
                drawRect(Color(0xFF7A4A24), Offset(c.x - 3.2f * u, c.y - 4.2f * u), Size(6.4f * u, 0.7f * u))
                drawRect(Color(0xFF7A4A24), Offset(c.x - 3.2f * u, c.y + 3.5f * u), Size(6.4f * u, 0.7f * u))
            }
            4 -> { // La Mano: an open hand of bone.
                drawRoundRect(Ink.bone, Offset(c.x - 2f * u, c.y - 0.5f * u), Size(4f * u, 4f * u), CornerRadius(1f * u))
                for ((k, len) in listOf(-1.5f to 3.4f, -0.5f to 4.2f, 0.5f to 4.4f, 1.5f to 3.6f)) {
                    drawLine(Ink.boneEdge, Offset(c.x + k * u, c.y), Offset(c.x + k * u * 1.3f, c.y - len * u), 0.9f * u, StrokeCap.Round)
                    drawLine(Ink.bone, Offset(c.x + k * u, c.y), Offset(c.x + k * u * 1.3f, c.y - len * u), 0.65f * u, StrokeCap.Round)
                }
                drawLine(Ink.bone, Offset(c.x - 2f * u, c.y + 1.5f * u), Offset(c.x - 3.6f * u, c.y - 0.6f * u), 0.7f * u, StrokeCap.Round)
            }
            5 -> { // El Espejo: a hand mirror.
                drawLine(Color(0xFF7A4A24), Offset(c.x, c.y + 2f * u), Offset(c.x, r.bottom - 0.8f * u), 1f * u, StrokeCap.Round)
                drawOval(Ink.gold, Offset(c.x - 2.8f * u, c.y - 4f * u), Size(5.6f * u, 6.6f * u))
                drawOval(Color(0xFFBFE6F2), Offset(c.x - 2.2f * u, c.y - 3.4f * u), Size(4.4f * u, 5.4f * u))
                drawLine(Color.White.copy(alpha = 0.8f), Offset(c.x - 1.2f * u, c.y - 2f * u), Offset(c.x - 0.2f * u, c.y - 3f * u), 0.3f * u)
            }
            6 -> { // La Despedida: two hearts, one fading.
                heartAt(Offset(c.x - 1.4f * u, c.y), 2.2f * u, Color(0xFFC8243B))
                heartAt(Offset(c.x + 1.6f * u, c.y + 0.6f * u), 2f * u, Color(0xFFC8243B).copy(alpha = 0.4f))
            }
            7 -> { // El Pendiente: a long list unrolling.
                drawRect(Color(0xFFF4E9D4), Offset(c.x - 2.6f * u, c.y - 3.6f * u), Size(5.2f * u, 7.4f * u))
                for (k in 0..4) drawLine(Ink.socket.copy(alpha = 0.6f), Offset(c.x - 1.8f * u, c.y - 2.6f * u + k * 1.4f * u), Offset(c.x + 1.8f * u, c.y - 2.6f * u + k * 1.4f * u), 0.25f * u)
                drawRoundRect(Color(0xFFC9A577), Offset(c.x - 3.2f * u, c.y - 4.2f * u), Size(6.4f * u, 1f * u), CornerRadius(0.5f * u))
                drawRoundRect(Color(0xFFC9A577), Offset(c.x - 3.2f * u, c.y + 3.5f * u), Size(6.4f * u, 1f * u), CornerRadius(0.5f * u))
            }
            8 -> candle(Offset(c.x, r.bottom - 0.8f * u), 2f * u, 4.4f * u, 0.3f) // La Soledad: a single candle.
            9 -> { // El Juicio: scales.
                drawLine(Color(0xFF7A4A24), Offset(c.x, c.y - 3.6f * u), Offset(c.x, c.y + 3.6f * u), 0.5f * u)
                drawLine(Color(0xFF7A4A24), Offset(c.x - 3.4f * u, c.y - 2.4f * u), Offset(c.x + 3.4f * u, c.y - 3f * u), 0.5f * u)
                drawArc(Ink.gold, 0f, 180f, true, Offset(c.x - 4.6f * u, c.y - 0.6f * u), Size(2.6f * u, 1.6f * u))
                drawArc(Ink.gold, 0f, 180f, true, Offset(c.x + 2f * u, c.y - 1.4f * u), Size(2.6f * u, 1.6f * u))
                drawRect(Color(0xFF7A4A24), Offset(c.x - 1.6f * u, c.y + 3.4f * u), Size(3.2f * u, 0.6f * u))
            }
            10 -> { // Lo Desconocido: a door ajar with light.
                drawRoundRect(Color(0xFF7A4A24), Offset(c.x - 2.6f * u, c.y - 3.8f * u), Size(5.2f * u, 7.8f * u), CornerRadius(2.6f * u, 2.6f * u))
                drawRoundRect(Ink.flame, Offset(c.x - 2f * u, c.y - 3.2f * u), Size(4f * u, 7f * u), CornerRadius(2f * u))
                drawPath(Path().apply { moveTo(c.x - 2f * u, c.y - 1.8f * u); lineTo(c.x + 0.6f * u, c.y - 1.2f * u); lineTo(c.x + 0.6f * u, c.y + 4f * u); lineTo(c.x - 2f * u, c.y + 3.8f * u); close() }, Color(0xFF5A3418))
            }
            else -> { // El Cuerpo: crossed bones under a heart.
                for (a in listOf(-35f, 35f)) rotate(a, c) {
                    drawLine(Ink.boneEdge, Offset(c.x - 3.4f * u, c.y + 1f * u), Offset(c.x + 3.4f * u, c.y + 1f * u), 1.2f * u, StrokeCap.Round)
                    drawLine(Ink.bone, Offset(c.x - 3.4f * u, c.y + 1f * u), Offset(c.x + 3.4f * u, c.y + 1f * u), 0.9f * u, StrokeCap.Round)
                    for (x in listOf(-3.6f, 3.6f)) for (y in listOf(0.5f, 1.5f)) drawCircle(Ink.bone, 0.55f * u, Offset(c.x + x * u, c.y + y * u))
                }
                heartAt(Offset(c.x, c.y - 1.6f * u), 1.8f * u, Color(0xFFC8243B))
            }
        }
    }
}

fun DrawScope.heartAt(c: Offset, rad: Float, col: Color) {
    val path = Path().apply {
        moveTo(c.x, c.y + rad)
        cubicTo(c.x - rad * 1.4f, c.y - rad * 0.1f, c.x - rad * 0.9f, c.y - rad * 1.2f, c.x, c.y - rad * 0.45f)
        cubicTo(c.x + rad * 0.9f, c.y - rad * 1.2f, c.x + rad * 1.4f, c.y - rad * 0.1f, c.x, c.y + rad)
        close()
    }
    drawPath(path, col)
}
