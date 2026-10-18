package app.calavera.ui.ofrenda

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.calavera.R
import app.calavera.data.Photos
import app.calavera.data.Soul
import app.calavera.domain.Picado
import app.calavera.domain.Rules
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.Ink
import app.calavera.ui.art.candle
import app.calavera.ui.art.drawAltar
import app.calavera.ui.casa.rememberSeconds
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.Tip
import app.calavera.ui.ctx
import app.calavera.ui.theme.Lobster
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val presets = (0 until 5).map { Picado.preset(it) }

@Composable
fun OfrendaTab(home: Home, go: Go) {
    val c = ctx()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            Text(stringResource(R.string.ofrenda_title), style = UType.Display)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.ofrenda_sub), style = UType.Sub)
        }
        Spacer(Modifier.height(8.dp))
        if (home.souls.isEmpty()) {
            Altar(home, emptyList(), go)
            Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.ofrenda_empty), style = UType.Title)
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.ofrenda_empty_text), style = UType.Sub.copy(textAlign = TextAlign.Center))
                Spacer(Modifier.height(16.dp))
                PrimaryButton(stringResource(R.string.ofrenda_add), { go.editSoul(0) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
            }
        } else {
            Altar(home, home.souls, go)
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (home.fiesta) {
                    Text(stringResource(R.string.ofrenda_fiesta), style = UType.Body.copy(color = U.c.onMarigold), modifier = Modifier.fillMaxWidth().clip(U.RMd).background(U.c.marigold).padding(14.dp))
                }
                Tip("ofrenda", stringResource(R.string.tip_ofrenda), home.settings)
                Text(T.plural(c, R.plurals.candles_lit, home.candles), style = UType.Small, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                home.souls.forEach { s -> SoulRow(s, home) { go.soul(s.id) } }
                PrimaryButton(stringResource(R.string.ofrenda_add), { go.editSoul(0) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/** The altar, drawn, with the portraits standing on its two upper tiers. */
@Composable
private fun Altar(home: Home, souls: List<Soul>, go: Go) {
    val t = rememberSeconds()
    val mine = home.garland.map { it.first }
    val flags = (mine + presets).take(5)
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        val w = maxWidth
        val h = w * 1.18f
        val tiers = listOf(0.44f, 0.64f, 0.81f)
        Canvas(Modifier.fillMaxWidth().height(h).graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
            drawAltar(tiers.map { it * size.height }, t, home.fiesta, flags)
        }
        val top = souls.take(3)
        val second = souls.drop(3).take(4)
        val fw = (w * 0.19f).coerceAtMost(96.dp)
        Row(Modifier.fillMaxWidth().offset(y = h * tiers[0] - fw * 1.3f), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
            top.forEach { Portrait(it, fw, home) { go.soul(it.id) } }
        }
        if (second.isNotEmpty()) {
            val fw2 = fw * 0.82f
            Row(Modifier.fillMaxWidth().offset(y = h * tiers[1] - fw2 * 1.3f), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                second.forEach { Portrait(it, fw2, home) { go.soul(it.id) } }
            }
        }
        Spacer(Modifier.height(h))
    }
}

/** A framed portrait: the photo, or a painted card with the initial; a glow while their candle burns. */
@Composable
fun Portrait(s: Soul, width: Dp, home: Home, onClick: () -> Unit) {
    val lit = Rules.candleLit(s.candleAt, home.today, home.zone)
    Box(Modifier.width(width).height(width * 1.28f).clickable(onClick = onClick)) {
        if (lit) Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(Ink.flame.copy(alpha = 0.45f), Color.Transparent), center, size.maxDimension * 0.75f), size.maxDimension * 0.75f, center)
        }
        Box(
            Modifier.fillMaxSize().padding(3.dp).clip(U.RSm).background(Color(0xFFE9B949)).padding(4.dp).clip(U.RSm).background(U.c.bg2),
            contentAlignment = Alignment.Center,
        ) {
            SoulPicture(s, Modifier.fillMaxSize())
        }
    }
}

@Composable
fun SoulPicture(s: Soul, modifier: Modifier) {
    val c = ctx()
    var bmp by remember(s.photo) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(s.photo) {
        if (s.photo.isEmpty()) return@LaunchedEffect
        bmp = withContext(Dispatchers.IO) { runCatching { BitmapFactory.decodeFile(Photos.file(c, s.photo).path)?.asImageBitmap() }.getOrNull() }
    }
    val b = bmp
    if (b != null) Image(b, s.name, modifier, contentScale = ContentScale.Crop)
    else Box(modifier.background(Brush.verticalGradient(listOf(Ink.tissue[(s.id % 6).toInt()].copy(alpha = 0.55f), U.c.bg2))), contentAlignment = Alignment.Center) {
        if (s.pet) Icon(Icons.Rounded.Pets, null, tint = U.c.ink, modifier = Modifier.fillMaxSize(0.45f))
        else Text(s.name.trim().take(1).uppercase(), fontFamily = Lobster, fontSize = 30.sp, color = U.c.ink)
    }
}

@Composable
private fun SoulRow(s: Soul, home: Home, onClick: () -> Unit) {
    val t = rememberSeconds()
    val lit = Rules.candleLit(s.candleAt, home.today, home.zone)
    Row(
        Modifier.fillMaxWidth().clip(U.RMd).background(U.c.surface).border(1.dp, U.c.line, U.RMd).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp, 60.dp).clip(U.RSm)) { SoulPicture(s, Modifier.fillMaxSize()) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(s.name, style = UType.Name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val sub = listOf(s.relation, Rules.years(s.born, s.died)).filter { it.isNotBlank() }.joinToString(" · ")
            if (sub.isNotEmpty()) Text(sub, style = UType.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Canvas(Modifier.size(22.dp, 40.dp)) { candle(Offset(size.width / 2, size.height), size.width * 0.42f, size.height * 0.48f, t, lit) }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = U.c.faint)
    }
}
