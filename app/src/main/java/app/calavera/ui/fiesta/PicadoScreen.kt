package app.calavera.ui.fiesta

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.container
import app.calavera.data.Flag
import app.calavera.data.earn
import app.calavera.domain.Picado
import app.calavera.domain.Reward
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.art.Ink
import app.calavera.ui.art.drawPicado
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.Segmented
import app.calavera.ui.components.SoftButton
import app.calavera.ui.components.TopBar
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType

/** Flag proportions: a little taller than wide. */
private const val FLAG_ASPECT = 0.8f

@Composable
fun PicadoScreen(home: Home, go: Go) {
    val c = ctx()
    var fold by rememberSaveable { mutableStateOf(Picado.Fold.QUARTER) }
    var color by rememberSaveable { mutableStateOf((home.flags.size + 1) % Picado.colors) }
    val cuts = remember { mutableStateListOf<List<Picado.Pt>>() }
    var stroke by remember { mutableStateOf<List<Picado.Pt>>(emptyList()) }
    var open by remember { mutableStateOf(false) }
    val opening = remember { Animatable(0f) }
    LaunchedEffect(open) { opening.animateTo(if (open) 1f else 0f, tween(700)) }
    val design = Picado(fold, color, cuts.toList())

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(stringResource(R.string.pic_title), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(if (open) R.string.pic_why else R.string.pic_folded), style = UType.Sub)
            if (!open) {
                Segmented(listOf(Picado.Fold.HALF, Picado.Fold.QUARTER), fold, { stringResource(if (it == Picado.Fold.HALF) R.string.pic_half else R.string.pic_quarter) }, {
                    if (it != fold) { fold = it; cuts.clear() }
                })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.pic_color), style = UType.Small)
                Ink.tissue.forEachIndexed { i, col ->
                    Box(Modifier.size(30.dp).clip(CircleShape).background(col).border(if (i == color) 3.dp else 0.dp, U.c.ink, CircleShape).clickable { color = i })
                }
            }
            Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                val k = opening.value
                if (k < 0.5f) {
                    // The folded piece, cut with the finger.
                    val aspect = if (fold == Picado.Fold.HALF) FLAG_ASPECT / 2 else FLAG_ASPECT
                    Canvas(
                        Modifier
                            .fillMaxHeight(if (fold == Picado.Fold.HALF) 1f else 0.75f)
                            .aspectRatio(aspect)
                            .graphicsLayer(scaleX = 1f - k, compositingStrategy = CompositingStrategy.Offscreen)
                            .pointerInput(fold) {
                                detectDragGestures(
                                    onDragStart = { o -> stroke = listOf(Picado.Pt((o.x / size.width).coerceIn(0f, 1f), (o.y / size.height).coerceIn(0f, 1f))) },
                                    onDragEnd = { if (stroke.isNotEmpty()) cuts += stroke; stroke = emptyList() },
                                    onDragCancel = { if (stroke.isNotEmpty()) cuts += stroke; stroke = emptyList() },
                                ) { change, _ ->
                                    val p = Picado.Pt((change.position.x / size.width).coerceIn(0f, 1f), (change.position.y / size.height).coerceIn(0f, 1f))
                                    val last = stroke.lastOrNull()
                                    if (last == null || kotlin.math.hypot(p.x - last.x, p.y - last.y) > 0.012f) stroke = stroke + p
                                }
                            },
                    ) {
                        val col = Ink.tissue[color]
                        drawRect(col)
                        val w = Picado.CUT * size.width
                        (cuts + listOf(stroke)).forEach { s ->
                            if (s.isEmpty()) return@forEach
                            if (s.size == 1) drawCircle(Color.Transparent, w / 2, Offset(s[0].x * size.width, s[0].y * size.height), blendMode = BlendMode.Clear)
                            else {
                                val path = Path()
                                s.forEachIndexed { i, pt -> if (i == 0) path.moveTo(pt.x * size.width, pt.y * size.height) else path.lineTo(pt.x * size.width, pt.y * size.height) }
                                drawPath(path, Color.Transparent, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round), blendMode = BlendMode.Clear)
                            }
                        }
                        // The folds: dashed along the right edge, and the bottom when folded in four.
                        val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                        drawLine(Color.Black.copy(alpha = 0.35f), Offset(size.width - 2f, 0f), Offset(size.width - 2f, size.height), 3f, pathEffect = dash)
                        if (fold == Picado.Fold.QUARTER) drawLine(Color.Black.copy(alpha = 0.35f), Offset(0f, size.height - 2f), Offset(size.width, size.height - 2f), 3f, pathEffect = dash)
                    }
                } else {
                    Canvas(
                        Modifier.fillMaxHeight().aspectRatio(FLAG_ASPECT)
                            .graphicsLayer(scaleX = (k - 0.5f) * 2f, compositingStrategy = CompositingStrategy.Offscreen),
                    ) { drawPicado(design, Rect(Offset.Zero, size)) }
                }
            }
            val hanging = home.garland.size
            Text(stringResource(R.string.pic_count, home.flags.size, hanging), style = UType.Small)
            Spacer(Modifier.height(8.dp))
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!open) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SoftButton(stringResource(R.string.pic_undo), { if (cuts.isNotEmpty()) cuts.removeAt(cuts.lastIndex) }, Modifier.weight(1f), icon = Icons.AutoMirrored.Rounded.Undo, color = U.c.muted, soft = U.c.surface)
                    SoftButton(stringResource(R.string.pic_clear), { cuts.clear() }, Modifier.weight(1f), icon = Icons.Rounded.Refresh, color = U.c.muted, soft = U.c.surface)
                }
                PrimaryButton(stringResource(R.string.pic_unfold), { open = true }, Modifier.fillMaxWidth(), enabled = cuts.isNotEmpty())
            } else {
                PrimaryButton(stringResource(R.string.pic_hang), {
                    val enc = design.encode()
                    c.container.write {
                        db.dao().insert(Flag(design = enc, hungAt = System.currentTimeMillis()))
                        earn(Reward.FLAG)
                    }
                    Toast.makeText(c, R.string.pic_hung, Toast.LENGTH_LONG).show()
                    cuts.clear()
                    open = false
                }, Modifier.fillMaxWidth())
                SoftButton(stringResource(R.string.pic_refold), { open = false }, Modifier.fillMaxWidth(), color = U.c.muted, soft = U.c.surface)
            }
        }
    }
}
