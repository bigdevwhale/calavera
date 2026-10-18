package app.calavera.ui.fiesta

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import app.calavera.R
import app.calavera.container
import app.calavera.data.Poem
import app.calavera.data.earn
import app.calavera.domain.Look
import app.calavera.domain.Picado
import app.calavera.domain.Reward
import app.calavera.ui.Care
import app.calavera.ui.Go
import app.calavera.ui.Home
import app.calavera.ui.T
import app.calavera.ui.art.Ink
import app.calavera.ui.art.drawCalaca
import app.calavera.ui.art.drawPicado
import app.calavera.ui.art.marigold
import app.calavera.ui.components.ConfirmDialog
import app.calavera.ui.components.PrimaryButton
import app.calavera.ui.components.TopBar
import app.calavera.ui.components.UTextField
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType
import java.io.File

@Composable
fun CalaveritaScreen(home: Home, go: Go) {
    val c = ctx()
    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding()) {
        TopBar(stringResource(R.string.cal_title), go::back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.cal_intro), style = UType.Sub)
            PrimaryButton(stringResource(R.string.cal_new), { go.poem(0) }, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add)
            if (home.poems.isEmpty()) Text(stringResource(R.string.cal_empty), style = UType.Muted)
            home.poems.forEach { p ->
                Column(
                    Modifier.fillMaxWidth().clip(U.RLg).background(U.c.surface).border(1.dp, U.c.line, U.RLg).clickable { go.poem(p.id) }.padding(16.dp),
                ) {
                    Text(p.text, style = UType.Hand, maxLines = 6, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text(T.date(c, p.at), style = UType.Small)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun PoemScreen(id: Long, home: Home, go: Go) {
    val c = ctx()
    val old = home.poems.firstOrNull { it.id == id }
    var text by rememberSaveable { mutableStateOf(old?.text ?: "") }
    var menu by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val name = home.settings.name.ifBlank { if (c.resources.configuration.locales[0].language == "ru") "я" else "me" }

    Column(Modifier.fillMaxSize().background(U.c.bg).statusBarsPadding().navigationBarsPadding().imePadding()) {
        TopBar(stringResource(R.string.cal_edit_title), go::back) {
            if (old != null) {
                IconButton(onClick = { share(c, old.text, home.settings.name, home.settings.look) }) { Icon(Icons.Rounded.Share, stringResource(R.string.share), tint = U.c.muted) }
                IconButton(onClick = { deleting = true }) { Icon(Icons.Rounded.Delete, stringResource(R.string.delete), tint = U.c.muted) }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.cal_tips), style = UType.Muted)
            Box {
                androidx.compose.material3.TextButton(onClick = { menu = true }) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = U.c.marigold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.cal_examples), color = U.c.marigold, style = UType.Button)
                }
                DropdownMenu(menu, { menu = false }, containerColor = U.c.surface2) {
                    c.resources.getStringArray(R.array.cal_examples).forEach { ex ->
                        val filled = ex.replace("{name}", name)
                        DropdownMenuItem(
                            text = { Text(filled.lineSequence().first() + "…", style = UType.Body) },
                            onClick = { text = filled; menu = false },
                        )
                    }
                }
            }
            UTextField(text, { text = it }, placeholder = stringResource(R.string.cal_hint), minLines = 8)
        }
        PrimaryButton(stringResource(R.string.save), {
            val t = text.trim()
            Care.check(t)
            c.container.write {
                if (old == null) {
                    db.dao().insert(Poem(text = t, at = System.currentTimeMillis()))
                    earn(Reward.POEM)
                } else db.dao().update(old.copy(text = t))
            }
            go.back()
        }, Modifier.fillMaxWidth().padding(16.dp), enabled = text.isNotBlank())
    }
    if (deleting && old != null) {
        ConfirmDialog(stringResource(R.string.cal_delete), old.text.lineSequence().first(), stringResource(R.string.delete), {
            c.container.write { db.dao().deletePoem(old.id) }
            go.back()
        }, { deleting = false }, danger = true)
    }
}

/** Paints the verse on a fiesta card (papel picado, the calaca, marigolds) and hands it to the share sheet. */
private fun share(c: Context, text: String, name: String, look: Look) {
    val file = runCatching { card(c, text, name, look) }.getOrNull() ?: return
    val uri = FileProvider.getUriForFile(c, c.packageName + ".files", file)
    val send = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri).putExtra(Intent.EXTRA_TEXT, text)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    c.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun card(c: Context, text: String, name: String, look: Look): File {
    val w = 1080
    val h = 1350
    val img = ImageBitmap(w, h)
    val canvas = androidx.compose.ui.graphics.Canvas(img)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, canvas, Size(w.toFloat(), h.toFloat())) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1E1733), Color(0xFF2C2350))))
        val fw = w / 6.2f
        // Cuts clear pixels, so the flags get a layer of their own over the sky.
        drawContext.canvas.saveLayer(Rect(0f, 0f, w.toFloat(), fw * 1.2f), androidx.compose.ui.graphics.Paint())
        for (i in 0 until 6) {
            val cx = fw * 0.6f + i * fw
            drawPicado(Picado.preset(i), Rect(cx - fw * 0.42f, 0f, cx + fw * 0.42f, fw * 1.05f))
        }
        drawContext.canvas.restore()
        val r = java.util.Random(3)
        repeat(36) { drawCircle(Color.White.copy(alpha = 0.25f), 1.5f + r.nextFloat() * 2f, Offset(r.nextFloat() * w, fw * 1.2f + r.nextFloat() * h * 0.5f)) }
        for (i in 0 until 9) marigold(Offset(w * (0.06f + i * 0.11f), h - 70f), 34f)
    }
    // The calaca, small, in the corner, drawn on its own layer.
    val figW = 300
    val figH = 410
    val fig = ImageBitmap(figW, figH)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(fig), Size(figW.toFloat(), figH.toFloat())) { drawCalaca(look, 1.2f) }
    val bmp = img.asAndroidBitmap()
    val ac = android.graphics.Canvas(bmp)
    ac.drawBitmap(fig.asAndroidBitmap(), (w - figW - 40).toFloat(), (h - figH - 120).toFloat(), null)

    val hand = ResourcesCompat.getFont(c, R.font.caveat)
    val sign = ResourcesCompat.getFont(c, R.font.lobster)
    val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFBF1DE.toInt(); textSize = 60f; typeface = hand }
    val lines = text.trim()
    var size = 64f
    var layout: StaticLayout
    do {
        body.textSize = size
        layout = StaticLayout.Builder.obtain(lines, 0, lines.length, body, w - 200).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.05f).build()
        size -= 4f
    } while (layout.height > h - 760 && size > 30f)
    ac.save()
    ac.translate(100f, 300f)
    layout.draw(ac)
    ac.restore()
    val by = if (name.isBlank()) c.getString(R.string.cal_card_anon) else c.getString(R.string.cal_card_by, name)
    val signPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF7A21B.toInt(); textSize = 54f; typeface = sign }
    ac.drawText(by, 100f, (300 + layout.height + 110).toFloat(), signPaint)
    val brand = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x99FBF1DE.toInt(); textSize = 34f; typeface = sign }
    ac.drawText("Calavera", 100f, h - 140f, brand)

    val dir = File(c.cacheDir, "share").apply { mkdirs() }
    val file = File(dir, "calaverita.png")
    file.outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    return file
}
