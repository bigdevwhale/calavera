package app.calavera.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.calavera.R

/**
 * The night of the first of November: an indigo sky, candlelight, marigold orange, the fuchsia,
 * turquoise and lime of papel picado, and the cream of bone and sugar.
 */
@Immutable
data class Palette(
    val bg: Color,
    val bg2: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val faint: Color,
    val marigold: Color,
    val marigoldSoft: Color,
    val onMarigold: Color,
    val fuchsia: Color,
    val fuchsiaSoft: Color,
    val turquoise: Color,
    val turquoiseSoft: Color,
    val lime: Color,
    val violet: Color,
    val bone: Color,
    val boneShade: Color,
    val socket: Color,
    val flame: Color,
    val danger: Color,
)

val NightPalette = Palette(
    bg = Color(0xFF1E1733),
    bg2 = Color(0xFF261D42),
    surface = Color(0xFF2C2350),
    surface2 = Color(0xFF382D63),
    line = Color(0xFF4A3D7A),
    ink = Color(0xFFFBF1DE),
    muted = Color(0xFFC9BCDB),
    faint = Color(0xFF8F82AE),
    marigold = Color(0xFFF7A21B),
    marigoldSoft = Color(0xFF4D3A3A),
    onMarigold = Color(0xFF2A1606),
    fuchsia = Color(0xFFE8418E),
    fuchsiaSoft = Color(0xFF4F2756),
    turquoise = Color(0xFF26C6B3),
    turquoiseSoft = Color(0xFF214A5C),
    lime = Color(0xFFB9D83C),
    violet = Color(0xFF9B6BEA),
    bone = Color(0xFFF6EEDD),
    boneShade = Color(0xFFBDAE95),
    socket = Color(0xFF1A1330),
    flame = Color(0xFFFFD46B),
    danger = Color(0xFFFF7A7A),
)

val LocalPalette = staticCompositionLocalOf { NightPalette }

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

/** Painted sign letters, like the name over a cantina door. */
val Lobster = FontFamily(Font(R.font.lobster, FontWeight.Normal))

/** Handwriting, for calaveritas, memories and Catrina's notes. */
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Normal))

object U {
    val c: Palette @Composable get() = LocalPalette.current

    val RXl = RoundedCornerShape(26.dp)
    val RLg = RoundedCornerShape(20.dp)
    val RMd = RoundedCornerShape(14.dp)
    val RSm = RoundedCornerShape(10.dp)
}

object UType {
    val Display: TextStyle @Composable get() = TextStyle(fontFamily = Lobster, fontSize = 34.sp, lineHeight = 40.sp, color = U.c.ink)
    val Title: TextStyle @Composable get() = TextStyle(fontFamily = Lobster, fontSize = 26.sp, lineHeight = 32.sp, color = U.c.ink)
    val Name: TextStyle @Composable get() = TextStyle(fontFamily = Lobster, fontSize = 20.sp, lineHeight = 25.sp, color = U.c.ink)
    val Sub: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontSize = 14.5.sp, lineHeight = 21.sp, color = U.c.muted)
    val Eyebrow: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, letterSpacing = 0.14.em, color = U.c.marigold)
    val Strong: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = U.c.ink)
    val Body: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontSize = 15.sp, lineHeight = 22.sp, color = U.c.ink)
    val Muted: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontSize = 13.sp, lineHeight = 18.sp, color = U.c.muted)
    val Small: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = U.c.faint)
    val Hand: TextStyle @Composable get() = TextStyle(fontFamily = app.calavera.ui.theme.Hand, fontSize = 23.sp, lineHeight = 27.sp, color = U.c.ink)
    val Number: TextStyle @Composable get() = TextStyle(fontFamily = Lobster, fontSize = 24.sp, color = U.c.ink)
    val Button: TextStyle @Composable get() = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}

@Composable
fun CalaveraTheme(content: @Composable () -> Unit) {
    val p = NightPalette
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = p.marigold,
                onPrimary = p.onMarigold,
                primaryContainer = p.marigoldSoft,
                onPrimaryContainer = p.ink,
                secondary = p.fuchsia,
                secondaryContainer = p.fuchsiaSoft,
                onSecondaryContainer = p.ink,
                background = p.bg,
                surface = p.surface,
                surfaceContainer = p.surface,
                surfaceContainerHigh = p.surface,
                surfaceContainerHighest = p.surface2,
                surfaceContainerLow = p.surface2,
                onBackground = p.ink,
                onSurface = p.ink,
                onSurfaceVariant = p.muted,
                outline = p.line,
                outlineVariant = p.line,
                error = p.danger,
            ),
            typography = MaterialTheme.typography.copy(
                bodyLarge = UType.Body,
                bodyMedium = UType.Body.copy(fontSize = 14.sp),
                labelLarge = UType.Button,
            ),
            content = content,
        )
    }
}
