package app.calavera.ui.fiesta

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.calavera.R
import app.calavera.container
import app.calavera.data.earn
import app.calavera.data.today
import app.calavera.domain.Fear
import app.calavera.domain.Reward
import app.calavera.domain.Rules
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.Ink
import app.calavera.ui.art.fearPicture
import app.calavera.ui.art.sugarSkull
import app.calavera.ui.components.Block
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.TopBar
import app.calavera.ui.ctx
import app.calavera.ui.theme.Lobster
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType

private val frames = listOf(Ink.fuchsia, Ink.turquoise, Ink.marigold, Ink.violet, Ink.lime, Ink.rosa)

@Composable
fun LoteriaScreen(home: Home, go: Go) {
    val c = ctx()
    val card = home.todayCard
    // A card drawn on an earlier visit today opens face up; a fresh draw waits to be turned.
    var flipped by rememberSaveable { mutableStateOf(card != null) }
    var reading by remember { mutableStateOf<Fear?>(null) }
    val angle by animateFloatAsState(if (flipped) 180f else 0f, tween(650), label = "flip")

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(stringResource(R.string.lot_title), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (card == null) Text(stringResource(R.string.lot_intro), style = UType.Sub)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .width(210.dp)
                        .aspectRatio(0.66f)
                        .graphicsLayer { rotationY = angle; cameraDistance = 14f * density }
                        .clickable(enabled = card != null) { flipped = !flipped },
                ) {
                    if (angle < 90f || card == null) CardBack(Modifier.fillMaxSize())
                    else Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) { CardFace(card, 210.dp, Modifier.fillMaxSize()) }
                }
            }
            if (card == null) {
                PrimaryButton(stringResource(R.string.lot_draw), {
                    val pick = Rules.draw(home.drawn.toSet(), home.today.toEpochDay())
                    c.container.write { db.dao().put(today().copy(card = pick.name)) }
                }, Modifier.fillMaxWidth())
            } else if (!flipped) {
                Text(stringResource(R.string.lot_tap), style = UType.Muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                Reading(card)
                if (home.day?.cardDone == true) Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = U.c.lime, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.lot_did_done), style = UType.Muted.copy(color = U.c.lime))
                } else PrimaryButton(stringResource(R.string.lot_did), {
                    c.container.write {
                        db.dao().put(today().copy(cardDone = true))
                        earn(Reward.CARD)
                    }
                }, Modifier.fillMaxWidth())
                Text(stringResource(R.string.lot_tomorrow), style = UType.Small)
            }

            Text(stringResource(R.string.lot_deck).uppercase(), style = UType.Eyebrow, modifier = Modifier.padding(top = 10.dp))
            Text(stringResource(R.string.lot_count, home.drawn.size, Fear.entries.size), style = UType.Muted)
            Fear.entries.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { f ->
                        Box(Modifier.weight(1f).aspectRatio(0.66f)) {
                            if (f in home.drawn) CardFace(f, 80.dp, Modifier.fillMaxSize().clickable { reading = f })
                            else CardBack(Modifier.fillMaxSize(), small = true)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    reading?.let { f ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { reading = null },
            containerColor = U.c.surface,
            title = { Text(T.card(c, f), style = UType.Title) },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { Reading(f, plain = true) } },
            confirmButton = { androidx.compose.material3.TextButton(onClick = { reading = null }) { Text(stringResource(R.string.close), color = U.c.marigold) } },
        )
    }
}

@Composable
private fun Reading(f: Fear, plain: Boolean = false) {
    val c = ctx()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!plain) Text(T.fear(c, f), style = UType.Title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        else Text(T.fear(c, f), style = UType.Strong)
        Block(stringResource(R.string.lot_says)) { Text(T.reflection(c, f), style = UType.Body) }
        Block(stringResource(R.string.lot_try)) { Text(T.exercise(c, f), style = UType.Hand) }
    }
}

/** A lotería card: a painted picture in a coloured frame, its number and its name. */
@Composable
fun CardFace(f: Fear, width: Dp, modifier: Modifier = Modifier) {
    val c = ctx()
    val frame = frames[f.ordinal % frames.size]
    val big = width > 120.dp
    Column(
        modifier.clip(U.RSm).background(Color(0xFFF7EEDC)).border(if (big) 5.dp else 2.5.dp, frame, U.RSm).padding(if (big) 10.dp else 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text((f.ordinal + 1).toString(), fontFamily = Lobster, fontSize = if (big) 20.sp else 10.sp, color = Color(0xFF2A1F3A), modifier = Modifier.align(Alignment.Start))
        Box(Modifier.weight(1f).fillMaxWidth().clip(U.RSm).background(frame.copy(alpha = 0.16f))) {
            Canvas(Modifier.fillMaxSize()) {
                val side = size.minDimension * 0.92f
                fearPicture(f.ordinal, Rect(Offset(center.x - side / 2, center.y - side / 2), androidx.compose.ui.geometry.Size(side, side)))
            }
        }
        Spacer(Modifier.height(if (big) 6.dp else 2.dp))
        Text(T.card(c, f), fontFamily = Lobster, fontSize = if (big) 22.sp else 9.sp, color = Color(0xFF2A1F3A), maxLines = 1)
    }
}

/** The back of a card: fuchsia with a skull and the deck's name. */
@Composable
fun CardBack(modifier: Modifier = Modifier, small: Boolean = false) {
    Box(modifier.clip(U.RSm).background(Ink.fuchsia).border(if (small) 2.dp else 5.dp, Ink.gold, U.RSm), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / 6
            var y = step / 2
            var row = 0
            while (y < size.height) {
                var x = if (row % 2 == 0) step / 2 else step
                while (x < size.width) {
                    drawCircle(Color.White.copy(alpha = 0.12f), step * 0.18f, Offset(x, y))
                    x += step
                }
                y += step
                row++
            }
            sugarSkull(Offset(size.width / 2, size.height * (if (small) 0.5f else 0.42f)), size.width * 0.2f, Ink.turquoise)
        }
        if (!small) Text(
            stringResource(R.string.lot_back), fontFamily = Lobster, fontSize = 22.sp, color = Ink.bone, textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
        )
    }
}
