package app.calavera.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.calavera.BuildConfig
import app.calavera.R
import app.calavera.container
import app.calavera.data.Check
import app.calavera.data.Day
import app.calavera.data.Flag
import app.calavera.data.Footprint
import app.calavera.data.Memory
import app.calavera.data.Petals
import app.calavera.data.Poem
import app.calavera.data.Settings
import app.calavera.data.Soul
import app.calavera.data.birthdayGift
import app.calavera.domain.Fear
import app.calavera.domain.Picado
import app.calavera.domain.Rules
import app.calavera.notify.Alarms
import app.calavera.ui.casa.CasaTab
import app.calavera.ui.casa.WardrobeScreen
import app.calavera.ui.components.rememberNow
import app.calavera.ui.fiesta.CalaveritaScreen
import app.calavera.ui.fiesta.CaminoScreen
import app.calavera.ui.fiesta.CheckScreen
import app.calavera.ui.fiesta.FiestaTab
import app.calavera.ui.fiesta.LoteriaScreen
import app.calavera.ui.fiesta.PanScreen
import app.calavera.ui.fiesta.PicadoScreen
import app.calavera.ui.fiesta.PlanScreen
import app.calavera.ui.fiesta.PoemScreen
import app.calavera.ui.fiesta.ViejosScreen
import app.calavera.ui.ofrenda.OfrendaTab
import app.calavera.ui.ofrenda.SoulEditScreen
import app.calavera.ui.ofrenda.SoulScreen
import app.calavera.ui.onboarding.OnboardingScreen
import app.calavera.ui.settings.SettingsScreen
import app.calavera.ui.theme.Nunito
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class Tab { CASA, OFRENDA, FIESTA }

/** Everything stored, plus what is derived from it, recomputed every minute or on change. */
@Immutable
class Home(
    val souls: List<Soul>,
    val memories: List<Memory>,
    val flags: List<Flag>,
    val poems: List<Poem>,
    val footprints: List<Footprint>,
    val checks: List<Check>,
    val days: List<Day>,
    val settings: Settings,
    val now: Long,
) {
    val zone: ZoneId = ZoneId.systemDefault()
    val today: LocalDate = LocalDate.now(zone)
    val birth: LocalDate? = Rules.birth(settings.birth)
    val age: Int? = Rules.age(birth, today)
    val fiesta: Boolean = Rules.fiesta(today)
    val daysToFiesta: Int = Rules.daysToFiesta(today)
    val day: Day? = days.firstOrNull { it.day == today.toString() }
    val drawn: List<Fear> = days.mapNotNull { d -> d.card.takeIf { it.isNotEmpty() }?.let { runCatching { Fear.valueOf(it) }.getOrNull() } }.distinct()
    val todayCard: Fear? = day?.card?.takeIf { it.isNotEmpty() }?.let { runCatching { Fear.valueOf(it) }.getOrNull() }

    /** Flags still on the garland, newest first, with the life left in them. */
    val garland: List<Pair<Picado, Float>> = flags.mapNotNull { f ->
        val life = Rules.flagLife(f.hungAt, now)
        if (life <= 0f) null else Picado.decode(f.design)?.let { it to life }
    }

    fun soul(id: Long) = souls.firstOrNull { it.id == id }
    fun memoriesOf(id: Long) = memories.filter { it.soulId == id }
    val candles: Int = souls.count { Rules.candleLit(it.candleAt, today, zone) }
    val checkDue: Boolean = Rules.checkDue(checks.maxOfOrNull { it.at }, now)
}

/** Everything a screen can open. */
class Go(private val nav: NavHostController) {
    fun wardrobe() = nav.navigate("wardrobe") { launchSingleTop = true }
    fun soul(id: Long) = nav.navigate("soul/$id")
    fun editSoul(id: Long) = nav.navigate("soul-edit/$id")
    fun loteria() = nav.navigate("loteria") { launchSingleTop = true }
    fun calaveritas() = nav.navigate("calaveritas") { launchSingleTop = true }
    fun poem(id: Long) = nav.navigate("poem/$id")
    fun picado() = nav.navigate("picado") { launchSingleTop = true }
    fun camino() = nav.navigate("camino") { launchSingleTop = true }
    fun check() = nav.navigate("check") { launchSingleTop = true }
    fun plan() = nav.navigate("plan") { launchSingleTop = true }
    fun pan() = nav.navigate("pan") { launchSingleTop = true }
    fun viejos() = nav.navigate("viejos") { launchSingleTop = true }
    fun settings() = nav.navigate("settings") { launchSingleTop = true }
    fun back() = nav.popBackStack()
    fun home() = nav.popBackStack("main", false)
}

@Composable
fun CalaveraRoot(settings: Settings) {
    val context = LocalContext.current
    val container = context.container
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableStateOf(Tab.CASA) }
    val start = remember { if (settings.onboarded) "main" else "welcome" }
    val dao = container.db.dao()

    val souls by dao.observeSouls().collectAsStateWithLifecycle(initialValue = null)
    val memories by dao.observeMemories().collectAsStateWithLifecycle(initialValue = emptyList())
    val flags by dao.observeFlags().collectAsStateWithLifecycle(initialValue = emptyList())
    val poems by dao.observePoems().collectAsStateWithLifecycle(initialValue = emptyList())
    val footprints by dao.observeFootprints().collectAsStateWithLifecycle(initialValue = emptyList())
    val checks by dao.observeChecks().collectAsStateWithLifecycle(initialValue = emptyList())
    val days by dao.observeDays().collectAsStateWithLifecycle(initialValue = emptyList())
    val tick = rememberNow(60_000)
    val home = remember(souls, memories, flags, poems, footprints, checks, days, settings, tick) {
        Home(souls.orEmpty(), memories, flags, poems, footprints, checks, days, settings, System.currentTimeMillis())
    }
    val go = remember(nav) { Go(nav) }
    var gift by remember { mutableStateOf<String?>(null) }

    // Catrina's birthday present, once a year, the first time the app is opened on or after the day.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            container.birthdayGift()?.let { gift = it }
            Alarms.clear(context)
        }
    }

    // Onboarded elsewhere (a restored backup, the demo seed): leave the welcome screens.
    LaunchedEffect(settings.onboarded) {
        if (settings.onboarded && nav.currentDestination?.route == "welcome") nav.navigate("main") { popUpTo("welcome") { inclusive = true } }
    }

    val askNotify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        container.scope.launch { Alarms.reschedule(context) }
    }

    Box(Modifier.fillMaxSize().background(U.c.bg)) {
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.background(U.c.bg),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(160)) },
        ) {
            composable("welcome") {
                OnboardingScreen {
                    nav.navigate("main") { popUpTo("welcome") { inclusive = true } }
                    if (Build.VERSION.SDK_INT >= 33 && !Alarms.canNotify(context) && !BuildConfig.DEMO) askNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            composable("main") { MainTabs(tab, { tab = it }, go, home) }
            composable("wardrobe") { WardrobeScreen(home, go) }
            composable("soul/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                SoulScreen(e.arguments?.getLong("id") ?: 0L, home, go)
            }
            composable("soul-edit/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                SoulEditScreen(e.arguments?.getLong("id") ?: 0L, home, go)
            }
            composable("loteria") { LoteriaScreen(home, go) }
            composable("calaveritas") { CalaveritaScreen(home, go) }
            composable("poem/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                PoemScreen(e.arguments?.getLong("id") ?: 0L, home, go)
            }
            composable("picado") { PicadoScreen(home, go) }
            composable("camino") { CaminoScreen(home, go) }
            composable("check") { CheckScreen(home, go) }
            composable("plan") { PlanScreen(home, go) }
            composable("pan") { PanScreen(go) }
            composable("viejos") { ViejosScreen(go) }
            composable("settings") { SettingsScreen(home, go) }
        }
        PetalToast(Modifier.align(Alignment.TopCenter))
    }

    if (Care.open.value) CareDialog { Care.open.value = false }
    gift?.let { g ->
        GiftDialog(g, onWear = {
            container.write {
                this.settings.update { s -> app.calavera.domain.Wardrobe.item(g)?.let { s.copy(look = s.look.with(it)) } ?: s }
            }
            gift = null
        }) { gift = null }
    }
}

/** "+3 petals", floating down from the top for a moment whenever petals are earned. */
@Composable
private fun PetalToast(modifier: Modifier) {
    var shown by remember { mutableStateOf<Int?>(null) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        Petals.events.collect { n ->
            shown = n
            visible = true
            delay(1800)
            visible = false
        }
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.statusBarsPadding().padding(top = 10.dp),
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
    ) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(U.c.marigold).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            app.calavera.ui.components.PetalIcon(Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(T.plural(ctx(), R.plurals.petals_plus, shown ?: 0), style = UType.Button, color = U.c.onMarigold)
        }
    }
}

@Composable
fun CareDialog(onDismiss: () -> Unit) {
    val c = ctx()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = U.c.surface,
        title = { Text(stringResource(R.string.help_title), style = UType.Title) },
        text = { Text(stringResource(R.string.help_text), style = UType.Body) },
        confirmButton = {
            TextButton(onClick = { Care.openHelplines(c); onDismiss() }) { Text(stringResource(R.string.help_find), color = U.c.marigold, style = UType.Button) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.help_ok), color = U.c.muted, style = UType.Button) } },
    )
}

@Composable
private fun GiftDialog(gift: String, onWear: () -> Unit, onDismiss: () -> Unit) {
    val c = ctx()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = U.c.surface,
        title = { Text(stringResource(R.string.gift_title), style = UType.Title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (gift.isNotEmpty()) {
                    val look = c.container.settings.settings.collectAsStateWithLifecycle(initialValue = null).value?.look
                    if (look != null) app.calavera.ui.art.Calaca(look.with(app.calavera.domain.Wardrobe.item(gift)!!), Modifier.size(150.dp, 190.dp), dancing = true)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (gift.isNotEmpty()) stringResource(R.string.gift_text, T.item(c, gift)) else stringResource(R.string.gift_text_petals),
                    style = UType.Body,
                )
            }
        },
        confirmButton = {
            if (gift.isNotEmpty()) TextButton(onClick = onWear) { Text(stringResource(R.string.gift_wear), color = U.c.marigold, style = UType.Button) }
            else TextButton(onClick = onDismiss) { Text(stringResource(R.string.done), color = U.c.marigold, style = UType.Button) }
        },
        dismissButton = if (gift.isNotEmpty()) ({ TextButton(onClick = onDismiss) { Text(stringResource(R.string.close), color = U.c.muted, style = UType.Button) } }) else null,
    )
}

@Composable
private fun MainTabs(tab: Tab, onTab: (Tab) -> Unit, go: Go, home: Home) {
    Column(Modifier.fillMaxSize().background(U.c.bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                Tab.CASA -> CasaTab(home, go) { onTab(Tab.FIESTA) }
                Tab.OFRENDA -> OfrendaTab(home, go)
                Tab.FIESTA -> FiestaTab(home, go)
            }
        }
        HorizontalDivider(color = U.c.line)
        Row(
            Modifier.fillMaxWidth().background(U.c.bg2).navigationBarsPadding().height(64.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(Icons.Rounded.Home, stringResource(R.string.tab_casa), tab == Tab.CASA) { onTab(Tab.CASA) }
            NavItem(Icons.Rounded.LocalFireDepartment, stringResource(R.string.tab_ofrenda), tab == Tab.OFRENDA, badge = home.fiesta && home.candles < home.souls.size) { onTab(Tab.OFRENDA) }
            NavItem(Icons.Rounded.Celebration, stringResource(R.string.tab_fiesta), tab == Tab.FIESTA) { onTab(Tab.FIESTA) }
        }
    }
}

@Composable
private fun RowScope.NavItem(icon: ImageVector, label: String, selected: Boolean, badge: Boolean = false, onClick: () -> Unit) {
    val color = if (selected) U.c.marigold else U.c.faint
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(if (selected) U.c.marigoldSoft else Color.Transparent).padding(horizontal = 18.dp, vertical = 4.dp),
            ) { Icon(icon, null, Modifier.size(22.dp), tint = color) }
            if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 10.dp).size(9.dp).clip(RoundedCornerShape(50)).background(U.c.fuchsia))
        }
        Spacer(Modifier.height(3.dp))
        Text(label, fontFamily = Nunito, fontSize = 11.5.sp, color = color, maxLines = 1)
    }
}
