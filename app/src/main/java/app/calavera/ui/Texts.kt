package app.calavera.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import app.calavera.R
import app.calavera.domain.Fear
import app.calavera.domain.Offering
import app.calavera.domain.Safety
import app.calavera.domain.Slot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Words and dates, in the app's language. Plain functions over a [Context] so receivers can use them too. */
object T {
    private fun locale(c: Context): Locale = c.resources.configuration.locales[0]

    private val items = mapOf(
        "none_hat" to R.string.item_none_hat, "flowers" to R.string.item_flowers, "sombrero" to R.string.item_sombrero,
        "tophat" to R.string.item_tophat, "catrina" to R.string.item_catrina,
        "petals" to R.string.item_petals, "hearts" to R.string.item_hearts, "spirals" to R.string.item_spirals,
        "stars" to R.string.item_stars, "lace" to R.string.item_lace,
        "bare" to R.string.item_bare, "poncho" to R.string.item_poncho, "guayabera" to R.string.item_guayabera,
        "charro" to R.string.item_charro, "vestido" to R.string.item_vestido,
        "none_hand" to R.string.item_none_hand, "bouquet" to R.string.item_bouquet, "maracas" to R.string.item_maracas,
        "guitar" to R.string.item_guitar, "cane" to R.string.item_cane,
        "none" to R.string.item_none, "collar" to R.string.item_collar, "mustache" to R.string.item_mustache,
        "glasses" to R.string.item_glasses, "rebozo" to R.string.item_rebozo, "goldtooth" to R.string.item_goldtooth,
    )

    fun item(c: Context, id: String): String = items[id]?.let { c.getString(it) } ?: id

    fun slot(c: Context, s: Slot): String = c.getString(
        when (s) {
            Slot.HAT -> R.string.slot_hat
            Slot.PAINT -> R.string.slot_paint
            Slot.CLOTHES -> R.string.slot_clothes
            Slot.HAND -> R.string.slot_hand
            Slot.EXTRA -> R.string.slot_extra
        }
    )

    fun offering(c: Context, o: Offering): String = c.getString(
        when (o) {
            Offering.BREAD -> R.string.off_bread
            Offering.COFFEE -> R.string.off_coffee
            Offering.FRUIT -> R.string.off_fruit
            Offering.SUGAR_SKULL -> R.string.off_sugar_skull
            Offering.CHOCOLATE -> R.string.off_chocolate
            Offering.TEQUILA -> R.string.off_tequila
            Offering.BOOK -> R.string.off_book
            Offering.MUSIC -> R.string.off_music
            Offering.CIGAR -> R.string.off_cigar
            Offering.TOY -> R.string.off_toy
            Offering.SWEETS -> R.string.off_sweets
            Offering.WATER -> R.string.off_water
        }
    )

    fun card(c: Context, f: Fear): String = c.resources.getStringArray(R.array.fear_cards)[f.ordinal]
    fun fear(c: Context, f: Fear): String = c.resources.getStringArray(R.array.fear_names)[f.ordinal]
    fun reflection(c: Context, f: Fear): String = c.resources.getStringArray(R.array.fear_reflections)[f.ordinal]
    fun exercise(c: Context, f: Fear): String = c.resources.getStringArray(R.array.fear_exercises)[f.ordinal]

    fun local(ms: Long, zone: ZoneId = ZoneId.systemDefault()) = Instant.ofEpochMilli(ms).atZone(zone)

    /** "17 Oct 2026". */
    fun date(c: Context, ms: Long): String = local(ms).format(DateTimeFormatter.ofPattern("d MMM yyyy", locale(c)))

    fun date(c: Context, d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale(c)))

    /** "17 Oct". */
    fun day(c: Context, ms: Long): String = local(ms).format(DateTimeFormatter.ofPattern("d MMM", locale(c)))

    fun clock(c: Context, minutes: Int): String =
        java.time.LocalTime.of(minutes / 60, minutes % 60).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale(c)))

    fun plural(c: Context, id: Int, n: Int): String = c.resources.getQuantityString(id, n, n)
}

@Composable
fun ctx(): Context = LocalContext.current

/** The gentle check on anything written: a worrying line opens the care dialog. */
object Care {
    val open = mutableStateOf(false)

    fun check(text: String) {
        if (Safety.worrying(text)) open.value = true
    }

    fun openHelplines(c: Context) {
        runCatching { c.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Safety.HELP_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
