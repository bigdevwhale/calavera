package app.calavera.domain

/**
 * A papel picado design. The paper is folded, cut while folded, and unfolded: [cuts] are strokes on
 * the folded piece (0..1 on both axes, the folds along the right and, for [Fold.QUARTER], the bottom
 * edge), and [unfolded] mirrors them across the whole flag.
 */
data class Picado(val fold: Fold, val color: Int, val cuts: List<List<Pt>>) {
    enum class Fold { HALF, QUARTER }

    data class Pt(val x: Float, val y: Float)

    fun encode(): String = buildString {
        append(fold.name).append(';').append(color).append(';')
        append(cuts.joinToString("|") { s -> s.joinToString(" ") { "%.3f,%.3f".format(java.util.Locale.ROOT, it.x, it.y) } })
    }

    /** Every cut on the open flag, 0..1 on both axes. */
    fun unfolded(): List<List<Pt>> {
        val out = mutableListOf<List<Pt>>()
        for (s in cuts) {
            val left = s.map { if (fold == Fold.HALF) Pt(it.x / 2, it.y) else Pt(it.x / 2, it.y / 2) }
            out += left
            out += left.map { Pt(1 - it.x, it.y) }
            if (fold == Fold.QUARTER) {
                out += left.map { Pt(it.x, 1 - it.y) }
                out += left.map { Pt(1 - it.x, 1 - it.y) }
            }
        }
        return out
    }

    companion object {
        /** Cut width as a share of the folded piece's width. */
        const val CUT = 0.075f

        /** Tissue colours: fuchsia, turquoise, marigold, lime, violet, rosa. */
        const val colors = 6

        fun decode(s: String): Picado? = runCatching {
            val p = s.split(';', limit = 3)
            val cuts = p[2].split('|').filter { it.isNotBlank() }.map { stroke ->
                stroke.trim().split(' ').map { xy -> xy.split(',').let { Pt(it[0].toFloat(), it[1].toFloat()) } }
            }
            Picado(Fold.valueOf(p[0]), p[1].toInt().coerceIn(0, colors - 1), cuts)
        }.getOrNull()

        /** Ready-made designs for the decorations that aren't anyone's own work. */
        fun preset(i: Int): Picado {
            val r = java.util.Random(i * 7919L + 13)
            val cuts = mutableListOf<List<Pt>>()
            // A flower: petals as short strokes radiating from near the fold corner.
            val cx = 0.78f
            val cy = 0.62f
            for (k in 0 until 5) {
                val a = Math.PI * (0.5 + k * 0.25)
                cuts += listOf(Pt(cx + (0.08 * Math.cos(a)).toFloat(), cy + (0.08 * Math.sin(a)).toFloat()), Pt(cx + (0.2 * Math.cos(a)).toFloat(), cy + (0.2 * Math.sin(a)).toFloat()))
            }
            // Small diamonds and dashes scattered around.
            repeat(4 + r.nextInt(3)) {
                val x = 0.12f + r.nextFloat() * 0.5f
                val y = 0.12f + r.nextFloat() * 0.7f
                cuts += listOf(Pt(x, y), Pt(x + 0.06f, y + 0.02f))
            }
            cuts += listOf(Pt(0.1f, 0.92f), Pt(0.5f, 0.92f))
            return Picado(Fold.HALF, i % colors, cuts)
        }
    }
}
