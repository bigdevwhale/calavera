package app.calavera.ui.fiesta

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.calavera.R
import app.calavera.container
import app.calavera.data.Check
import app.calavera.data.Footprint
import app.calavera.data.earn
import app.calavera.domain.Reward
import app.calavera.domain.Rules
import app.calavera.ui.Care
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.Ink
import app.calavera.ui.art.drawRoad
import app.calavera.ui.art.marigold
import app.calavera.ui.art.panDeMuerto
import app.calavera.ui.casa.rememberSeconds
import app.calavera.ui.components.Block
import app.calavera.ui.components.ConfirmDialog
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.TopBar
import app.calavera.ui.components.UTextField
import app.calavera.ui.ctx
import app.calavera.ui.theme.Lobster
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import kotlinx.coroutines.delay
import org.json.JSONObject

/** A screen with a top bar and a scrolling column in the app's gutter. */
@Composable
private fun Page(title: String, go: Go, actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}, bottom: @Composable () -> Unit = {}, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        TopBar(title, go::back, actions)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
            Spacer(Modifier.height(20.dp))
        }
        bottom()
    }
}

@Composable
fun CaminoScreen(home: Home, go: Go) {
    val c = ctx()
    val t = rememberSeconds()
    var who by rememberSaveable { mutableStateOf("") }
    var what by rememberSaveable { mutableStateOf("") }
    var pulling by remember { mutableStateOf<Footprint?>(null) }
    Page(stringResource(R.string.cam_title), go) {
        Text(stringResource(R.string.cam_intro), style = UType.Sub)
        Box(Modifier.fillMaxWidth().height(300.dp).clip(U.RLg).background(U.c.bg2)) {
            Canvas(Modifier.fillMaxSize()) { drawRoad(home.footprints.size, t) }
            Text(
                T.plural(c, R.plurals.cam_count, home.footprints.size), style = UType.Name.copy(color = U.c.marigold),
                modifier = Modifier.align(Alignment.TopStart).padding(14.dp),
            )
        }
        Block(null) {
            Text(stringResource(R.string.cam_who), style = UType.Small)
            UTextField(who, { who = it }, placeholder = stringResource(R.string.cam_who_hint))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.cam_what), style = UType.Small)
            UTextField(what, { what = it }, placeholder = stringResource(R.string.cam_what_hint))
            Spacer(Modifier.height(12.dp))
            PrimaryButton(stringResource(R.string.cam_add), {
                val f = Footprint(who = who.trim(), what = what.trim(), at = System.currentTimeMillis())
                Care.check(f.who + " " + f.what)
                who = ""; what = ""
                c.container.write { db.dao().insert(f); earn(Reward.FOOTPRINT) }
            }, Modifier.fillMaxWidth(), enabled = who.isNotBlank())
        }
        if (home.footprints.isEmpty()) Text(stringResource(R.string.cam_empty), style = UType.Muted)
        home.footprints.reversed().forEach { f ->
            Row(
                Modifier.fillMaxWidth().clip(U.RMd).background(U.c.surface).clickable { pulling = f }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(Modifier.size(26.dp)) { marigold(center, size.minDimension / 2.2f) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(f.who, style = UType.Strong)
                    if (f.what.isNotBlank()) Text(f.what, style = UType.Muted)
                }
            }
        }
    }
    pulling?.let { f ->
        ConfirmDialog(stringResource(R.string.cam_delete), f.who, stringResource(R.string.delete), { c.container.write { db.dao().deleteFootprint(f.id) } }, { pulling = null }, danger = true)
    }
}

@Composable
fun CheckScreen(home: Home, go: Go) {
    val c = ctx()
    val statements = c.resources.getStringArray(R.array.chk_statements)
    val scale = c.resources.getStringArray(R.array.chk_scale)
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var taking by rememberSaveable { mutableStateOf(home.checkDue) }
    val last = home.checks.lastOrNull()
    Page(stringResource(R.string.chk_title), go) {
        if (taking) {
            Text(stringResource(R.string.chk_intro), style = UType.Sub)
            statements.forEachIndexed { i, st ->
                Block(null) {
                    Text(st, style = UType.Body)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (v in 1..5) {
                            val on = answers[i] == v
                            Box(
                                Modifier.weight(1f).height(40.dp).clip(U.RSm)
                                    .background(if (on) heat(v) else U.c.surface2)
                                    .clickable { answers[i] = v },
                                contentAlignment = Alignment.Center,
                            ) { Text(v.toString(), style = UType.Button.copy(color = if (on) U.c.onMarigold else U.c.muted)) }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(scale.first(), style = UType.Small, modifier = Modifier.weight(1f))
                        Text(scale.last(), style = UType.Small)
                    }
                }
            }
            PrimaryButton(stringResource(R.string.chk_save), {
                val list = statements.indices.map { answers[it] ?: 3 }
                val score = Rules.score(list)
                c.container.write {
                    db.dao().insert(Check(at = System.currentTimeMillis(), answers = list.joinToString(","), score = score))
                    earn(Reward.CHECK)
                }
                answers.clear()
                taking = false
            }, Modifier.fillMaxWidth(), enabled = answers.size == statements.size)
        } else if (last != null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.chk_result, last.score), fontFamily = Lobster, fontSize = 72.sp, color = heat(1 + last.score / 25))
                    Text(stringResource(level(last.score)), style = UType.Body.copy(textAlign = TextAlign.Center))
                }
            }
            val first = home.checks.first()
            if (home.checks.size > 1) Text(stringResource(R.string.chk_change, last.score - first.score), style = UType.Strong.copy(color = if (last.score <= first.score) U.c.lime else U.c.fuchsia))
            Block(stringResource(R.string.chk_history)) {
                Chart(home.checks)
                Spacer(Modifier.height(8.dp))
                home.checks.reversed().forEach { ch ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(T.date(c, ch.at), style = UType.Muted, modifier = Modifier.weight(1f))
                        Text("${ch.score}°", style = UType.Strong.copy(color = heat(1 + ch.score / 25)))
                    }
                }
            }
            val days = (28 - ((home.now - last.at) / (24L * 3600 * 1000)).toInt()).coerceAtLeast(0)
            if (days > 0) Text(T.plural(c, R.plurals.chk_next, days), style = UType.Small)
            app.calavera.ui.components.SoftButton(stringResource(R.string.chk_again), { taking = true }, Modifier.fillMaxWidth(), color = U.c.marigold, soft = U.c.marigoldSoft)
        }
    }
}

private fun level(score: Int) = when {
    score < 35 -> R.string.chk_cool
    score < 65 -> R.string.chk_warm
    else -> R.string.chk_hot
}

/** 1 cool turquoise … 5 hot fuchsia. */
private fun heat(v: Int): Color = when (v.coerceIn(1, 5)) {
    1 -> Ink.turquoise
    2 -> Ink.lime
    3 -> Ink.marigold
    4 -> Color(0xFFF26B3A)
    else -> Ink.fuchsia
}

@Composable
private fun Chart(checks: List<Check>) {
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val n = checks.size
        if (n == 0) return@Canvas
        for (k in 0..4) drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, size.height * k / 4), Offset(size.width, size.height * k / 4), 1f)
        val pts = checks.mapIndexed { i, ch ->
            Offset(if (n == 1) size.width / 2 else size.width * i / (n - 1), size.height * (1f - ch.score / 100f))
        }
        val path = Path().apply { pts.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } }
        drawPath(path, Ink.marigold, style = Stroke(5f, cap = StrokeCap.Round))
        pts.forEachIndexed { i, p -> drawCircle(heat(1 + checks[i].score / 25), 9f, p) }
    }
}

private val planKeys = listOf("music", "food", "place", "guests", "words", "remember", "body", "care", "papers", "pets", "know")

@Composable
fun PlanScreen(home: Home, go: Go) {
    val c = ctx()
    val titles = c.resources.getStringArray(R.array.plan_titles)
    val hints = c.resources.getStringArray(R.array.plan_hints)
    val values = remember {
        val json = runCatching { JSONObject(home.settings.plan) }.getOrNull()
        mutableStateListOf(*planKeys.map { json?.optString(it).orEmpty() }.toTypedArray())
    }
    var saved by remember { mutableStateOf(false) }
    // Saves a moment after typing stops.
    LaunchedEffect(values.toList()) {
        delay(700)
        val json = JSONObject()
        planKeys.forEachIndexed { i, k -> if (values[i].isNotBlank()) json.put(k, values[i]) }
        val text = json.toString()
        if (text != home.settings.plan && !(home.settings.plan.isEmpty() && json.length() == 0)) {
            c.container.write { settings.update { it.copy(plan = text) } }
            values.forEach { Care.check(it) }
            saved = true
        }
    }
    Page(stringResource(R.string.plan_title), go, actions = {
        IconButton(onClick = {
            val text = buildString {
                append(c.getString(R.string.plan_share_title)).append("\n\n")
                planKeys.indices.filter { values[it].isNotBlank() }.forEach { i -> append(titles[i]).append('\n').append(values[i].trim()).append("\n\n") }
            }.trim()
            c.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
        }) { Icon(Icons.Rounded.Share, stringResource(R.string.plan_share), tint = U.c.muted) }
    }) {
        Text(stringResource(R.string.plan_intro), style = UType.Sub)
        planKeys.indices.forEach { i ->
            Block(titles[i]) {
                if (planKeys[i] == "papers") {
                    Text(stringResource(R.string.plan_papers_warn), style = UType.Small.copy(color = U.c.fuchsia))
                    Spacer(Modifier.height(6.dp))
                }
                UTextField(values[i], { values[i] = it }, placeholder = hints[i], minLines = 2)
            }
        }
        if (saved) Text(stringResource(R.string.plan_saved), style = UType.Small)
    }
}

@Composable
fun PanScreen(go: Go) {
    val c = ctx()
    val questions = remember { c.resources.getStringArray(R.array.pan_questions).toList().shuffled() }
    var i by rememberSaveable { mutableStateOf(0) }
    Page(stringResource(R.string.pan_title), go, bottom = {
        PrimaryButton(stringResource(R.string.pan_next), {
            i = (i + 1) % questions.size
            c.container.write { earn(Reward.QUESTION) }
        }, Modifier.fillMaxWidth().padding(16.dp))
    }) {
        Text(stringResource(R.string.pan_intro), style = UType.Sub)
        Box(Modifier.fillMaxWidth().height(130.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(150.dp, 110.dp)) { panDeMuerto(Offset(size.width / 2, size.height * 0.6f), size.width * 0.42f) }
        }
        AnimatedContent(i, transitionSpec = { (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut()) }, label = "q") { k ->
            Column(
                Modifier.fillMaxWidth().clip(U.RXl).background(Color(0xFFF7EEDC)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.pan_count, k + 1, questions.size), style = UType.Small.copy(color = Color(0xFF8A6A4A)))
                Spacer(Modifier.height(12.dp))
                Text(questions[k], fontFamily = Lobster, fontSize = 26.sp, lineHeight = 33.sp, color = Color(0xFF2A1F3A), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ViejosScreen(go: Go) {
    val c = ctx()
    val names = c.resources.getStringArray(R.array.vie_names)
    val ages = c.resources.getStringArray(R.array.vie_ages)
    val texts = c.resources.getStringArray(R.array.vie_texts)
    Page(stringResource(R.string.vie_title), go) {
        Text(stringResource(R.string.vie_intro), style = UType.Sub)
        Column(Modifier.fillMaxWidth().clip(U.RLg).background(U.c.marigold).padding(18.dp)) {
            Text(stringResource(R.string.vie_levy_title), style = UType.Title.copy(color = U.c.onMarigold))
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.vie_levy), style = UType.Body.copy(color = U.c.onMarigold))
        }
        names.indices.forEach { i ->
            Row(Modifier.fillMaxWidth().clip(U.RLg).background(U.c.surface).border(1.dp, U.c.line, U.RLg).padding(14.dp)) {
                Box(Modifier.size(58.dp).clip(CircleShape).background(Ink.tissue[i % Ink.tissue.size].copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                    Text(ages[i], fontFamily = Lobster, fontSize = 22.sp, color = Ink.tissue[i % Ink.tissue.size])
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(names[i], style = UType.Name)
                    Spacer(Modifier.height(4.dp))
                    Text(texts[i], style = UType.Muted)
                }
            }
        }
    }
}
