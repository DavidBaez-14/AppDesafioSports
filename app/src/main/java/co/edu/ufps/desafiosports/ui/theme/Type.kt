package co.edu.ufps.desafiosports.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.edu.ufps.desafiosports.R

// Tipografía de marca (MOV-0002). Fuentes empaquetadas en res/font (OFL, ver docs/licencias):
// no dependen de Google Play como las fuentes descargables.
//  - Barlow Condensed: display y headline (títulos, marcadores) y el logotipo.
//  - Barlow: title, body y label (texto, botones, campos).
// Tamaños y espaciados: la escala de Material 3, siempre en sp.

internal val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
    Font(R.font.barlow_condensed_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
)

internal val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
)

private val base = Typography()

internal val Tipografia = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold),
    displayMedium = base.displayMedium.copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold),
    displaySmall = base.displaySmall.copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold),
    headlineLarge = base.headlineLarge.copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Barlow, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = Barlow, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = Barlow, fontWeight = FontWeight.SemiBold),
    // El espaciado de Material 3 (0,25–0,5 sp) está pensado para Roboto; en Barlow separa
    // demasiado las letras del texto corrido. Se reduce sin llegar a cero en los tamaños chicos.
    bodyLarge = base.bodyLarge.copy(fontFamily = Barlow, letterSpacing = 0.15.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = Barlow, letterSpacing = 0.1.sp),
    bodySmall = base.bodySmall.copy(fontFamily = Barlow, letterSpacing = 0.2.sp),
    labelLarge = base.labelLarge.copy(fontFamily = Barlow, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = Barlow, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
    labelSmall = base.labelSmall.copy(fontFamily = Barlow, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp),
)
