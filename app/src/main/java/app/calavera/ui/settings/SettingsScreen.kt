package app.calavera.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.calavera.BuildConfig
import app.calavera.R
import app.calavera.container
import app.calavera.data.Backup
import app.calavera.data.Settings
import app.calavera.domain.Rules
import app.calavera.ui.Care
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.Language
import app.calavera.ui.T
import app.calavera.ui.components.Chip
import app.calavera.ui.components.ConfirmDialog
import app.calavera.ui.components.DateDialog
import app.calavera.ui.components.InfoRow
import app.calavera.ui.components.SectionTitle
import app.calavera.ui.components.SoftButton
import app.calavera.ui.components.SwitchRow
import app.calavera.ui.components.TimeDialog
import app.calavera.ui.components.TopBar
import app.calavera.ui.components.UCard
import app.calavera.ui.components.UTextField
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun SettingsScreen(home: Home, go: Go) {
    val context = LocalContext.current
    val c = context.container
    val s = home.settings
    var name by remember { mutableStateOf(s.name) }
    var status by remember { mutableStateOf<String?>(null) }
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pickTime by remember { mutableStateOf(false) }
    var pickBirth by remember { mutableStateOf(false) }
    fun set(change: (Settings) -> Settings) = c.write { settings.update(change) }

    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        c.scope.launch {
            val ok = runCatching { withContext(Dispatchers.IO) { Backup.write(context, uri, Backup.collect(c.db, c.settings.now())) } }.isSuccess
            status = context.getString(if (ok) R.string.backup_done else R.string.backup_failed)
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> restoreUri = uri }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(stringResource(R.string.settings_title), onBack = { go.back() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            SectionTitle(stringResource(R.string.set_you))
            UCard {
                Text(stringResource(R.string.set_name), style = UType.Small)
                Spacer(Modifier.height(6.dp))
                UTextField(name, { name = it.take(30); set { st -> st.copy(name = name.trim()) } }, placeholder = stringResource(R.string.onb_name_hint))
                InfoRow(
                    Icons.Rounded.Cake, stringResource(R.string.set_birth),
                    Rules.birth(s.birth)?.let { T.date(context, it) } ?: stringResource(R.string.set_birth_none),
                    tint = U.c.marigold, soft = U.c.marigoldSoft, onClick = { pickBirth = true },
                )
            }

            SectionTitle(stringResource(R.string.set_notify_section))
            UCard {
                SwitchRow(stringResource(R.string.set_notify), stringResource(R.string.set_notify_hint), s.notify, { on -> set { it.copy(notify = on) } }, color = U.c.marigold)
                if (s.notify) InfoRow(Icons.Rounded.Schedule, stringResource(R.string.set_notify), stringResource(R.string.set_notify_at, T.clock(context, s.notifyAt)), tint = U.c.marigold, soft = U.c.marigoldSoft, onClick = { pickTime = true })
            }

            SectionTitle(stringResource(R.string.set_language))
            UCard {
                val cur = Language.current()
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(stringResource(R.string.lang_system), cur == Language.SYSTEM, { Language.set(Language.SYSTEM) }, color = U.c.marigold)
                    Chip("English", cur == Language.EN, { Language.set(Language.EN) }, color = U.c.marigold)
                    Chip("Русский", cur == Language.RU, { Language.set(Language.RU) }, color = U.c.marigold)
                }
            }

            SectionTitle(stringResource(R.string.set_backup))
            UCard {
                Text(stringResource(R.string.set_backup_hint), style = UType.Muted)
                InfoRow(Icons.Rounded.Upload, stringResource(R.string.backup_export), stringResource(R.string.backup_export_hint), tint = U.c.turquoise, soft = U.c.turquoiseSoft, onClick = { export.launch("Calavera.zip") })
                InfoRow(Icons.Rounded.Download, stringResource(R.string.backup_import), stringResource(R.string.backup_import_hint), tint = U.c.turquoise, soft = U.c.turquoiseSoft, onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) })
                status?.let { Text(it, style = UType.Muted.copy(color = U.c.turquoise)) }
            }

            SectionTitle(stringResource(R.string.set_help))
            UCard {
                Text(stringResource(R.string.set_help_hint), style = UType.Muted)
                Spacer(Modifier.height(10.dp))
                SoftButton(stringResource(R.string.help_find), { Care.openHelplines(context) }, Modifier.fillMaxWidth(), color = U.c.fuchsia, soft = U.c.fuchsiaSoft, icon = Icons.Rounded.Favorite)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.about, BuildConfig.VERSION_NAME), style = UType.Small.copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(40.dp))
        }
    }

    if (pickTime) TimeDialog(s.notifyAt, onDismiss = { pickTime = false }) { m -> set { it.copy(notifyAt = m) } }
    if (pickBirth) DateDialog(Rules.birth(s.birth) ?: LocalDate.now().minusYears(35), { pickBirth = false }) { d ->
        set {
            val today = LocalDate.now()
            // Moving the birthday shouldn't hand out a second present this year, nor skip this year's.
            val gy = if (it.giftYear >= today.year) it.giftYear else if (!Rules.birthdayPassed(d, today)) today.year - 1 else it.giftYear
            it.copy(birth = d.toString(), giftYear = gy)
        }
    }

    restoreUri?.let { uri ->
        ConfirmDialog(
            stringResource(R.string.backup_import), stringResource(R.string.backup_import_confirm), stringResource(R.string.backup_import_do),
            onConfirm = {
                c.scope.launch {
                    val ok = runCatching {
                        withContext(Dispatchers.IO) {
                            val (content, dir) = Backup.read(context, uri)
                            Backup.restore(context, c.db, c.settings, content, dir)
                        }
                    }.isSuccess
                    status = context.getString(if (ok) R.string.backup_restored else R.string.backup_bad)
                }
            },
            onDismiss = { restoreUri = null }, danger = true,
        )
    }
}
