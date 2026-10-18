package app.calavera.ui.ofrenda

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.container
import app.calavera.data.Memory
import app.calavera.data.earn
import app.calavera.domain.Offering
import app.calavera.domain.Reward
import app.calavera.domain.Rules
import app.calavera.ui.Care
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.candle
import app.calavera.ui.art.offering
import app.calavera.ui.casa.rememberSeconds
import app.calavera.ui.components.Block
import app.calavera.ui.components.ConfirmDialog
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.SoftButton
import app.calavera.ui.components.TopBar
import app.calavera.ui.components.UTextField
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoulScreen(id: Long, home: Home, go: Go) {
    val c = ctx()
    val s = home.soul(id) ?: return
    val t = rememberSeconds()
    val lit = Rules.candleLit(s.candleAt, home.today, home.zone)
    var memory by rememberSaveable { mutableStateOf("") }
    var deleting by remember { mutableStateOf<Memory?>(null) }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        TopBar(s.name, go::back) {
            IconButton(onClick = { go.editSoul(s.id) }) { Icon(Icons.Rounded.Edit, stringResource(R.string.edit), tint = U.c.muted) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Portrait(s, 130.dp, home) {}
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.name, style = UType.Title)
                    if (s.relation.isNotBlank()) Text(s.relation, style = UType.Sub)
                    val years = Rules.years(s.born, s.died)
                    if (years.isNotEmpty()) Text(years, style = UType.Name.copy(color = U.c.marigold))
                    Spacer(Modifier.height(10.dp))
                    Canvas(Modifier.size(40.dp, 70.dp)) { candle(Offset(size.width / 2, size.height), size.width * 0.4f, size.height * 0.5f, t, lit) }
                }
            }
            if (lit) Text(stringResource(R.string.soul_candle_lit), style = UType.Body.copy(color = U.c.marigold))
            else PrimaryButton(stringResource(R.string.soul_candle), {
                c.container.write {
                    db.dao().update(s.copy(candleAt = System.currentTimeMillis()))
                    earn(Reward.CANDLE)
                }
            }, Modifier.fillMaxWidth())

            Block(stringResource(R.string.soul_offerings)) {
                val list = s.offeringList.mapNotNull { runCatching { Offering.valueOf(it) }.getOrNull() }
                if (list.isEmpty()) Text(stringResource(R.string.soul_offerings_none), style = UType.Muted)
                else FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    list.forEach { o -> OfferingTile(o, selected = true, onClick = null) }
                }
            }

            Block(stringResource(R.string.soul_memories)) {
                UTextField(memory, { memory = it }, placeholder = stringResource(R.string.soul_memory_hint), minLines = 3)
                Spacer(Modifier.height(8.dp))
                SoftButton(stringResource(R.string.soul_memory_add), {
                    val text = memory.trim()
                    if (text.isEmpty()) return@SoftButton
                    Care.check(text)
                    memory = ""
                    c.container.write {
                        db.dao().insert(Memory(soulId = s.id, text = text, at = System.currentTimeMillis()))
                        earn(Reward.MEMORY)
                    }
                }, Modifier.fillMaxWidth(), color = U.c.marigold, soft = U.c.marigoldSoft)
                Spacer(Modifier.height(12.dp))
                val list = home.memoriesOf(s.id)
                if (list.isEmpty()) Text(stringResource(R.string.soul_memories_none), style = UType.Muted)
                list.forEach { m ->
                    Column(Modifier.fillMaxWidth().clickable { deleting = m }.padding(vertical = 8.dp)) {
                        Text(m.text, style = UType.Hand)
                        Text(T.date(c, m.at), style = UType.Small)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    deleting?.let { m ->
        ConfirmDialog(stringResource(R.string.memory_delete), m.text, stringResource(R.string.delete), { c.container.write { db.dao().deleteMemory(m.id) } }, { deleting = null }, danger = true)
    }
}

/** An offering as a little tile with its picture and name; tappable when [onClick] is given. */
@Composable
fun OfferingTile(o: Offering, selected: Boolean, onClick: (() -> Unit)?) {
    val c = ctx()
    Column(
        Modifier
            .width(80.dp)
            .clip(U.RMd)
            .background(if (selected) U.c.marigoldSoft else U.c.surface2)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) U.c.marigold else U.c.line, U.RMd)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(40.dp)) { Canvas(Modifier.fillMaxSize()) { offering(o, center, size.minDimension) } }
        Spacer(Modifier.height(4.dp))
        Text(T.offering(c, o), style = UType.Small.copy(color = if (selected) U.c.ink else U.c.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center), maxLines = 2, minLines = 2)
    }
}
