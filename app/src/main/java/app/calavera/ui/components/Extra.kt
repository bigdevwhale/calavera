package app.calavera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.calavera.R
import app.calavera.container
import app.calavera.data.Settings
import app.calavera.ui.art.marigold
import app.calavera.ui.ctx
import app.calavera.ui.theme.U
import app.calavera.ui.theme.UType

/** A little marigold, the app's coin. */
@Composable
fun PetalIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) { marigold(Offset(size.width / 2, size.height / 2), size.minDimension / 2.1f) }
}

/** Petal balance as a pill. */
@Composable
fun PetalPill(petals: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(50)).background(U.c.marigoldSoft).padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PetalIcon(Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(petals.toString(), style = UType.Button, color = U.c.marigold)
    }
}

/** Catrina's hint for a screen, shown once until dismissed. */
@Composable
fun Tip(key: String, text: String, settings: Settings, modifier: Modifier = Modifier) {
    val k = "tip:$key"
    if (k in settings.seen) return
    val c = ctx()
    Row(
        modifier.fillMaxWidth().clip(U.RMd).background(U.c.fuchsiaSoft).border(1.dp, U.c.fuchsia.copy(alpha = 0.4f), U.RMd).padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text, style = UType.Muted.copy(color = U.c.ink), modifier = Modifier.weight(1f))
        TextButton(onClick = { c.container.write { this.settings.update { it.copy(seen = it.seen + k) } } }) {
            Text(stringResource(R.string.tip_ok), color = U.c.fuchsia, style = UType.Button)
        }
    }
}

/** A card with an eyebrow, the app's standard block. */
@Composable
fun Block(eyebrow: String?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth().clip(U.RLg).background(U.c.surface).border(1.dp, U.c.line, U.RLg).padding(16.dp)) {
        if (eyebrow != null) {
            Text(eyebrow.uppercase(), style = UType.Eyebrow)
            Spacer(Modifier.size(8.dp))
        }
        content()
    }
}

/** Centre a column of content with the app's side gutter. */
@Composable
fun Gutter(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { content() }
}
