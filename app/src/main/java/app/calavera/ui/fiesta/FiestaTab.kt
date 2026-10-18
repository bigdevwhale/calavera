package app.calavera.ui.fiesta

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.domain.Look
import app.calavera.domain.Picado
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.art.Ink
import app.calavera.ui.art.candle
import app.calavera.ui.art.drawCalaca
import app.calavera.ui.art.drawPicado
import app.calavera.ui.art.fearPicture
import app.calavera.ui.art.marigold
import app.calavera.ui.art.panDeMuerto
import app.calavera.ui.art.sugarSkull
import app.calavera.ui.components.Tip
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType

@Composable
fun FiestaTab(home: Home, go: Go) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.statusBarsPadding().padding(start = 4.dp, top = 16.dp, bottom = 6.dp)) {
            Text(stringResource(R.string.fiesta_title), style = UType.Display)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.fiesta_sub), style = UType.Sub)
        }
        Tip("fiesta", stringResource(R.string.tip_fiesta), home.settings)
        Practice(R.string.p_loteria, R.string.p_loteria_desc, Ink.fuchsia, due = home.todayCard == null, onClick = go::loteria) { fearPicture(3, Rect(Offset.Zero, size)) }
        Practice(R.string.p_calaverita, R.string.p_calaverita_desc, Ink.violet, onClick = go::calaveritas) { sugarSkull(center, size.minDimension * 0.36f, Ink.fuchsia) }
        Practice(R.string.p_picado, R.string.p_picado_desc, Ink.turquoise, layered = true, onClick = go::picado) {
            drawPicado(Picado.preset(1), Rect(size.width * 0.12f, size.height * 0.1f, size.width * 0.88f, size.height * 0.9f))
        }
        Practice(R.string.p_camino, R.string.p_camino_desc, Ink.marigold, onClick = go::camino) {
            marigold(Offset(size.width * 0.35f, size.height * 0.62f), size.minDimension * 0.2f)
            marigold(Offset(size.width * 0.66f, size.height * 0.36f), size.minDimension * 0.17f)
        }
        Practice(R.string.p_check, R.string.p_check_desc, Ink.lime, due = home.checkDue, onClick = go::check) { thermometer() }
        Practice(R.string.p_plan, R.string.p_plan_desc, Ink.rosa, onClick = go::plan) {
            candle(Offset(size.width * 0.38f, size.height * 0.95f), size.width * 0.16f, size.height * 0.38f, 0.4f)
            candle(Offset(size.width * 0.64f, size.height * 0.95f), size.width * 0.16f, size.height * 0.26f, 1.7f)
        }
        Practice(R.string.p_pan, R.string.p_pan_desc, Color(0xFFE0A85A), onClick = go::pan) { panDeMuerto(Offset(size.width / 2, size.height * 0.58f), size.minDimension * 0.4f) }
        Practice(R.string.p_viejos, R.string.p_viejos_desc, Ink.gold, onClick = go::viejos) {
            drawCalaca(Look(hat = "sombrero", paint = "stars", clothes = "poncho", hand = "cane", extra = "glasses", color = 2), 0f)
        }
        Spacer(Modifier.height(20.dp))
    }
}

private fun DrawScope.thermometer() {
    val w = size.width
    val h = size.height
    drawRoundRect(Ink.bone, Offset(w * 0.42f, h * 0.1f), androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.62f), androidx.compose.ui.geometry.CornerRadius(w * 0.08f))
    drawCircle(Ink.bone, w * 0.15f, Offset(w * 0.5f, h * 0.76f))
    drawCircle(Color(0xFFE5484D), w * 0.11f, Offset(w * 0.5f, h * 0.76f))
    drawRect(Color(0xFFE5484D), Offset(w * 0.465f, h * 0.38f), androidx.compose.ui.geometry.Size(w * 0.07f, h * 0.36f))
    for (k in 0..4) drawLine(Ink.socket, Offset(w * 0.58f, h * (0.18f + k * 0.1f)), Offset(w * 0.66f, h * (0.18f + k * 0.1f)), 2f)
}

@Composable
private fun Practice(title: Int, desc: Int, color: Color, due: Boolean = false, layered: Boolean = false, onClick: () -> Unit, art: DrawScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(U.RLg).background(U.c.surface).border(1.dp, U.c.line, U.RLg).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(62.dp).clip(U.RMd).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(50.dp).then(if (layered) Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen) else Modifier)) { art() }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(title), style = UType.Name, modifier = Modifier.weight(1f, fill = false))
                if (due) {
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.p_due), style = UType.Small.copy(color = U.c.onMarigold), modifier = Modifier.clip(RoundedCornerShape(50)).background(U.c.marigold).padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(stringResource(desc), style = UType.Muted)
        }
    }
}
