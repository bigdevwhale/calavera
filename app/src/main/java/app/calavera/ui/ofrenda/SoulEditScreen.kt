package app.calavera.ui.ofrenda

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.container
import app.calavera.data.Photos
import app.calavera.data.Soul
import app.calavera.domain.Offering
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.components.ConfirmDialog
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.SoftButton
import app.calavera.ui.components.SwitchRow
import app.calavera.ui.components.TopBar
import app.calavera.ui.components.UTextField
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoulEditScreen(id: Long, home: Home, go: Go) {
    val c = ctx()
    val old = home.soul(id)
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf(old?.name ?: "") }
    var relation by rememberSaveable { mutableStateOf(old?.relation ?: "") }
    var born by rememberSaveable { mutableStateOf(old?.born ?: "") }
    var died by rememberSaveable { mutableStateOf(old?.died ?: "") }
    var pet by rememberSaveable { mutableStateOf(old?.pet ?: false) }
    var photo by rememberSaveable { mutableStateOf(old?.photo ?: "") }
    var offerings by rememberSaveable { mutableStateOf(old?.offeringList?.toSet() ?: emptySet()) }
    var deleting by remember { mutableStateOf(false) }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val f = withContext(Dispatchers.IO) { Photos.import(c, uri) }
            if (f != null) {
                if (photo != old?.photo) Photos.delete(c, photo)
                photo = f
            }
        }
    }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        TopBar(stringResource(if (old == null) R.string.soul_new else R.string.soul_edit), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val preview = Soul(id = old?.id ?: 1, name = name.ifBlank { "?" }, photo = photo, pet = pet, createdAt = 0)
                Box(Modifier.size(96.dp, 122.dp).clip(U.RMd)) { SoulPicture(preview, Modifier.fillMaxSize()) }
                Spacer(Modifier.width(14.dp))
                Column {
                    SoftButton(
                        stringResource(if (photo.isEmpty()) R.string.soul_photo else R.string.soul_photo_change),
                        { pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        icon = Icons.Rounded.Image, color = U.c.marigold, soft = U.c.marigoldSoft,
                    )
                    if (photo.isNotEmpty()) TextButton(onClick = { if (photo != old?.photo) Photos.delete(c, photo); photo = "" }) {
                        Text(stringResource(R.string.soul_photo_remove), color = U.c.muted)
                    }
                }
            }
            Label(R.string.soul_name)
            UTextField(name, { name = it }, placeholder = stringResource(R.string.soul_name_hint))
            Label(R.string.soul_relation)
            UTextField(relation, { relation = it }, placeholder = stringResource(R.string.soul_relation_hint))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Label(R.string.soul_born)
                    UTextField(born, { born = it.take(20) }, placeholder = stringResource(R.string.soul_year_hint))
                }
                Column(Modifier.weight(1f)) {
                    Label(R.string.soul_died)
                    UTextField(died, { died = it.take(20) }, placeholder = stringResource(R.string.soul_year_hint))
                }
            }
            SwitchRow(stringResource(R.string.soul_pet), null, pet, { pet = it }, color = U.c.marigold)
            Label(R.string.soul_loved)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Offering.entries.forEach { o ->
                    val on = o.name in offerings
                    OfferingTile(o, on) { offerings = if (on) offerings - o.name else offerings + o.name }
                }
            }
            if (old != null) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { deleting = true }) { Text(stringResource(R.string.soul_delete), color = U.c.danger, style = UType.Button) }
            }
            Spacer(Modifier.height(12.dp))
        }
        PrimaryButton(stringResource(R.string.save), {
            val soul = (old ?: Soul(name = "", createdAt = System.currentTimeMillis())).copy(
                name = name.trim(), relation = relation.trim(), born = born.trim(), died = died.trim(), pet = pet, photo = photo,
                offerings = Offering.entries.filter { it.name in offerings }.joinToString(",") { it.name },
            )
            if (old != null && old.photo.isNotEmpty() && old.photo != photo) Photos.delete(c, old.photo)
            c.container.write { if (old == null) db.dao().insert(soul) else db.dao().update(soul) }
            go.back()
        }, Modifier.fillMaxWidth().padding(16.dp), enabled = name.isNotBlank())
    }
    if (deleting && old != null) {
        ConfirmDialog(stringResource(R.string.soul_delete), stringResource(R.string.soul_delete_text), stringResource(R.string.soul_delete_do), {
            Photos.delete(c, old.photo)
            c.container.write { db.dao().deleteMemoriesOf(old.id); db.dao().deleteSoul(old.id) }
            go.home()
        }, { deleting = false }, danger = true)
    }
}

@Composable
private fun Label(res: Int) {
    Text(stringResource(res), style = UType.Small, modifier = Modifier.padding(top = 4.dp, start = 2.dp))
}
