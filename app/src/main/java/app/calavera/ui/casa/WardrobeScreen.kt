package app.calavera.ui.casa

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.container
import app.calavera.data.buy
import app.calavera.domain.Item
import app.calavera.domain.Slot
import app.calavera.domain.Wardrobe
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.Calaca
import app.calavera.ui.art.Ink
import app.calavera.ui.components.Chip
import app.calavera.ui.components.PetalIcon
import app.calavera.ui.components.PetalPill
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.TopBar
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType

@Composable
fun WardrobeScreen(home: Home, go: Go) {
    val c = ctx()
    val s = home.settings
    var slot by rememberSaveable { mutableStateOf(Slot.HAT) }
    var trying by remember { mutableStateOf<Item?>(null) }
    val preview = trying?.let { s.look.with(it) } ?: s.look

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(stringResource(R.string.wardrobe_title), go::back) { PetalPill(s.petals, Modifier.padding(end = 12.dp)) }
        Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
            Calaca(preview, Modifier.size(190.dp, 250.dp), dancing = trying != null)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Slot.entries.forEach { sl -> Chip(T.slot(c, sl), sl == slot, { slot = sl; trying = null }, color = U.c.marigold) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (slot == Slot.PAINT) {
                Text(stringResource(R.string.paint_color), style = UType.Small)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Ink.paints.forEachIndexed { i, col ->
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(col)
                                .border(if (i == s.look.color) 3.dp else 0.dp, U.c.ink, CircleShape)
                                .clickable { c.container.write { settings.update { it.copy(look = it.look.copy(color = i)) } } },
                        )
                    }
                }
            }
            Wardrobe.of(slot).chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { item ->
                        ItemTile(item, owned = item.id in s.owned, wearing = s.look.of(slot) == item.id, selected = trying?.id == item.id, look = s.look, modifier = Modifier.weight(1f)) {
                            trying = if (s.look.of(slot) == item.id) null else item
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text(stringResource(R.string.wardrobe_hint), style = UType.Muted)
        }
        trying?.let { item ->
            val owned = item.id in s.owned
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                when {
                    owned -> PrimaryButton(stringResource(R.string.item_wear), { c.container.write { buy(item) }; trying = null }, Modifier.fillMaxWidth())
                    item.elder -> Text(stringResource(R.string.item_elder), style = UType.Body.copy(textAlign = TextAlign.Center), modifier = Modifier.fillMaxWidth())
                    s.petals < item.price -> PrimaryButton(stringResource(R.string.item_poor), {}, Modifier.fillMaxWidth(), enabled = false)
                    else -> PrimaryButton(stringResource(R.string.item_buy, item.price), { c.container.write { buy(item) }; trying = null }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ItemTile(item: Item, owned: Boolean, wearing: Boolean, selected: Boolean, look: app.calavera.domain.Look, modifier: Modifier, onClick: () -> Unit) {
    val c = ctx()
    val border = when {
        selected -> U.c.marigold
        wearing -> U.c.turquoise
        else -> U.c.line
    }
    Column(
        modifier.clip(U.RMd).background(U.c.surface).border(if (selected || wearing) 2.dp else 1.dp, border, U.RMd).clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.85f), contentAlignment = Alignment.Center) {
            Calaca(look.with(item), Modifier.fillMaxSize(), still = true)
            if (!owned) {
                Box(Modifier.align(Alignment.BottomEnd).clip(CircleShape).background(U.c.bg.copy(alpha = 0.85f)).padding(5.dp)) {
                    if (item.elder) Icon(Icons.Rounded.Lock, null, Modifier.size(14.dp), tint = U.c.muted)
                    else Row(verticalAlignment = Alignment.CenterVertically) {
                        PetalIcon(Modifier.size(13.dp))
                        Text(" ${item.price}", style = UType.Small.copy(color = U.c.marigold))
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(T.item(c, item.id), style = UType.Small.copy(color = if (wearing) U.c.turquoise else U.c.ink, textAlign = TextAlign.Center), maxLines = 2)
        if (wearing) Text(stringResource(R.string.item_wearing), style = UType.Small.copy(color = U.c.turquoise))
    }
}
