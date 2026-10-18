package app.calavera.ui.casa

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.calavera.R
import app.calavera.container
import app.calavera.data.earn
import app.calavera.data.today
import app.calavera.domain.Reward
import app.calavera.domain.Rules
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.Calaca
import app.calavera.ui.art.drawGarland
import app.calavera.ui.art.fearPicture
import app.calavera.ui.components.Block
import app.calavera.ui.components.PetalPill
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.SoftButton
import app.calavera.ui.components.Tip
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import kotlinx.coroutines.delay
import kotlin.math.sin

/** Seconds since the screen appeared, ticking every frame: for flutter, flicker and twinkle. */
@Composable
fun rememberSeconds(): Float {
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withInfiniteAnimationFrameMillis { it }
        while (true) withInfiniteAnimationFrameMillis { t = (it - start) / 1000f }
    }
    return t
}

@Composable
fun CasaTab(home: Home, go: Go, toFiesta: () -> Unit) {
    val c = ctx()
    val s = home.settings
    var dancing by remember { mutableStateOf(false) }
    LaunchedEffect(dancing) {
        if (dancing) {
            delay(4200)
            dancing = false
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 20.dp, end = 6.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (s.name.isBlank()) stringResource(R.string.casa_hello_anon) else stringResource(R.string.casa_hello, s.name),
                style = UType.Title, modifier = Modifier.weight(1f), maxLines = 1,
            )
            PetalPill(s.petals, Modifier.clickable { go.wardrobe() })
            IconButton(onClick = { go.settings() }) { Icon(Icons.Rounded.Settings, stringResource(R.string.settings_title), tint = U.c.muted) }
        }

        Scene(home, dancing) { dancing = true }

        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FiestaChip(home)
            Tip("casa", stringResource(R.string.tip_casa), s)
            CatrinaCard(home)
            TodayCard(home, go)
            SoftButton(stringResource(R.string.wardrobe), { go.wardrobe() }, Modifier.fillMaxWidth(), color = U.c.turquoise, soft = U.c.turquoiseSoft, icon = Icons.Rounded.Checkroom)
            if (home.garland.isEmpty()) {
                Text(stringResource(R.string.garland_empty), style = UType.Muted, modifier = Modifier.fillMaxWidth().clickable(onClick = toFiesta).padding(vertical = 4.dp), textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** The calaca under a night sky, a garland of the flags still hanging above it. */
@Composable
private fun Scene(home: Home, dancing: Boolean, onTap: () -> Unit) {
    val t = rememberSeconds()
    val c = ctx()
    Box(
        Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onTap),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val r = java.util.Random(11)
            repeat(40) { i ->
                val x = r.nextFloat() * size.width
                val y = r.nextFloat() * size.height * 0.75f
                val a = 0.25f + 0.35f * (0.5f + 0.5f * sin(t * (0.7f + i % 5 * 0.3f) + i))
                drawCircle(Color(0xFFFBF1DE).copy(alpha = a), 1.2f + (i % 3), Offset(x, y))
            }
            // A warm glow on the floor where the calaca dances.
            drawOval(
                Brush.radialGradient(listOf(Color(0x55F7A21B), Color.Transparent), Offset(size.width / 2, size.height * 0.9f), size.width * 0.4f),
                Offset(size.width * 0.1f, size.height * 0.8f), androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height * 0.2f),
            )
        }
        if (home.garland.isNotEmpty()) {
            Canvas(Modifier.fillMaxWidth().height(110.dp).graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
                drawGarland(home.garland.take(7).reversed(), size.width, 12f, size.width / 8.4f, t)
            }
        }
        Calaca(home.settings.look, Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp).size(230.dp, 300.dp), dancing = dancing)
        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val line = home.age?.let { T.plural(c, R.plurals.casa_age, it) } ?: stringResource(R.string.casa_tap)
            Text(line, style = UType.Name.copy(color = U.c.marigold))
        }
    }
}

@Composable
private fun FiestaChip(home: Home) {
    val c = ctx()
    val text = if (home.fiesta) stringResource(R.string.fiesta_now) else T.plural(c, R.plurals.fiesta_in, home.daysToFiesta)
    Row(
        Modifier.fillMaxWidth().clip(U.RMd).background(if (home.fiesta) U.c.marigold else U.c.surface).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("☆", style = UType.Strong.copy(color = if (home.fiesta) U.c.onMarigold else U.c.marigold))
        Spacer(Modifier.width(10.dp))
        Text(text, style = UType.Body.copy(color = if (home.fiesta) U.c.onMarigold else U.c.ink), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CatrinaCard(home: Home) {
    val c = ctx()
    val words = c.resources.getStringArray(R.array.catrina_words)
    val errands = c.resources.getStringArray(R.array.catrina_errands)
    val i = Rules.wordOfDay(home.today, words.size)
    val done = home.day?.errandDone == true
    Block(stringResource(R.string.catrina_came)) {
        Text("«" + words[i] + "»", style = UType.Hand.copy(fontSize = 25.sp, lineHeight = 29.sp))
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = U.c.line)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.catrina_errand), style = UType.Small)
        Spacer(Modifier.height(4.dp))
        Text(errands[i % errands.size], style = UType.Body)
        Spacer(Modifier.height(12.dp))
        if (done) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, null, tint = U.c.lime, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.errand_done), style = UType.Muted.copy(color = U.c.lime))
            }
        } else {
            PrimaryButton(stringResource(R.string.errand_do), {
                c.container.write {
                    val d = today()
                    db.dao().put(d.copy(errandDone = true))
                    earn(Reward.ERRAND)
                }
            }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TodayCard(home: Home, go: Go) {
    val c = ctx()
    val card = home.todayCard
    Row(
        Modifier.fillMaxWidth().clip(U.RLg).background(U.c.surface).border(1.dp, U.c.line, U.RLg).clickable { go.loteria() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(58.dp, 78.dp).clip(U.RSm).background(if (card != null) Color(0xFFF4E9D4) else U.c.fuchsia), contentAlignment = Alignment.Center) {
            if (card != null) Canvas(Modifier.size(50.dp)) { fearPicture(card.ordinal, androidx.compose.ui.geometry.Rect(Offset.Zero, size)) }
            else Text("?", style = UType.Title.copy(color = U.c.ink))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.today_card).uppercase(), style = UType.Eyebrow)
            Spacer(Modifier.height(4.dp))
            if (card != null) {
                Text(T.card(c, card), style = UType.Name)
                Text(T.fear(c, card), style = UType.Muted)
            } else Text(stringResource(R.string.today_card_draw), style = UType.Strong)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = U.c.faint)
    }
}
