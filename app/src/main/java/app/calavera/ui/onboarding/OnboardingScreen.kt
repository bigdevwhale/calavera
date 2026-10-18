package app.calavera.ui.onboarding

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.container
import app.calavera.domain.Look
import app.calavera.domain.Picado
import app.calavera.domain.Rules
import app.calavera.domain.Slot
import app.calavera.domain.Wardrobe
import app.calavera.ui.T
import app.calavera.ui.art.Calaca
import app.calavera.ui.art.Ink
import app.calavera.ui.art.drawGarland
import app.calavera.ui.casa.rememberSeconds
import app.calavera.ui.components.Chip
import app.calavera.ui.components.DateDialog
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.SoftButton
import app.calavera.ui.components.UTextField
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import java.time.LocalDate

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = ctx()
    var step by rememberSaveable { mutableStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var birth by rememberSaveable { mutableStateOf("") }
    var hat by rememberSaveable { mutableStateOf("flowers") }
    var color by rememberSaveable { mutableStateOf(0) }
    var picking by rememberSaveable { mutableStateOf(false) }
    val t = rememberSeconds()
    val look = Look(hat = hat, color = color)
    val age = Rules.age(Rules.birth(birth))
    val gifts = Wardrobe.giftsForAge(age)

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().height(if (step == 1) 220.dp else 340.dp)) {
                Canvas(Modifier.fillMaxWidth().height(100.dp).graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
                    drawGarland((0 until 7).map { Picado.preset(it) to 1f }, size.width, 10f, size.width / 8.4f, t)
                }
                val shown = if (step == 2) look.copy(extra = gifts.firstOrNull { Wardrobe.item(it)?.slot == Slot.EXTRA } ?: "none") else look
                Calaca(shown, Modifier.align(Alignment.BottomCenter).size(if (step == 1) 150.dp else 210.dp, if (step == 1) 190.dp else 270.dp), dancing = step == 0)
            }
            Column(Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (step) {
                    0 -> {
                        Text(stringResource(R.string.onb1_title), style = UType.Display.copy(textAlign = TextAlign.Center))
                        Text(stringResource(R.string.onb1_text), style = UType.Sub.copy(textAlign = TextAlign.Center))
                    }
                    1 -> {
                        Text(stringResource(R.string.onb2_title), style = UType.Title)
                        Text(stringResource(R.string.onb2_text), style = UType.Sub.copy(textAlign = TextAlign.Center))
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.onb_name), style = UType.Small, modifier = Modifier.fillMaxWidth())
                        UTextField(name, { name = it.take(30) }, placeholder = stringResource(R.string.onb_name_hint))
                        Text(stringResource(R.string.onb_birth), style = UType.Small, modifier = Modifier.fillMaxWidth())
                        SoftButton(
                            Rules.birth(birth)?.let { T.date(c, it) } ?: stringResource(R.string.onb_birth_pick),
                            { picking = true }, Modifier.fillMaxWidth(), color = U.c.marigold, soft = U.c.marigoldSoft, icon = Icons.Rounded.Cake,
                        )
                        Text(stringResource(R.string.onb_birth_hint), style = UType.Muted, modifier = Modifier.fillMaxWidth())
                    }
                    else -> {
                        Text(stringResource(R.string.onb3_title), style = UType.Title)
                        Text(stringResource(R.string.onb3_text), style = UType.Sub.copy(textAlign = TextAlign.Center))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Wardrobe.of(Slot.HAT).filter { it.price == 0 }.forEach { Chip(T.item(c, it.id), it.id == hat, { hat = it.id }, color = U.c.marigold) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Ink.paints.forEachIndexed { i, col ->
                                Box(Modifier.size(36.dp).clip(CircleShape).background(col).border(if (i == color) 3.dp else 0.dp, U.c.ink, CircleShape).clickable { color = i })
                            }
                        }
                        if (gifts.isNotEmpty()) Text(
                            stringResource(R.string.onb_elder_gift, gifts.joinToString(", ") { T.item(c, it) }),
                            style = UType.Body.copy(color = U.c.marigold, textAlign = TextAlign.Center),
                        )
                        Text(stringResource(R.string.onb_care), style = UType.Small.copy(textAlign = TextAlign.Center))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        PrimaryButton(stringResource(if (step < 2) R.string.next else R.string.onb_start), {
            if (step < 2) step++ else {
                val b = Rules.birth(birth)
                val today = LocalDate.now()
                c.container.write {
                    settings.update { s ->
                        val extra = gifts.firstOrNull { Wardrobe.item(it)?.slot == Slot.EXTRA } ?: "none"
                        s.copy(
                            onboarded = true,
                            name = name.trim(),
                            birth = birth,
                            look = s.look.copy(hat = hat, color = color, extra = extra),
                            owned = s.owned + gifts,
                            // This year's present is still to come if the birthday hasn't been yet.
                            giftYear = if (b != null && !Rules.birthdayPassed(b, today)) today.year - 1 else today.year,
                        )
                    }
                }
                onDone()
            }
        }, Modifier.fillMaxWidth().padding(16.dp))
    }
    if (picking) {
        DateDialog(Rules.birth(birth) ?: LocalDate.now().minusYears(35), { picking = false }) { birth = it.toString() }
    }
}
