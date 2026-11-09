package co.edu.ufps.desafiosports.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Tema de marca (MOV-0002). Sin color dinámico (Material You): taparía los colores de la marca.
// El oscuro es el principal; la app sigue al tema del teléfono.

private val EsquemaOscuro = darkColorScheme(
    primary = OscuroNaranja,
    onPrimary = OscuroFondo,
    primaryContainer = OscuroNaranjaContenedor,
    onPrimaryContainer = OscuroSobreNaranjaContenedor,
    inversePrimary = ClaroNaranja,
    secondary = OscuroTintaSecundaria,
    onSecondary = OscuroContenedor,
    secondaryContainer = OscuroContenedorMaximo,
    onSecondaryContainer = OscuroTinta,
    tertiary = OscuroAzul,
    onTertiary = OscuroFondo,
    tertiaryContainer = OscuroAzulContenedor,
    onTertiaryContainer = OscuroAzulSobreContenedor,
    background = OscuroFondo,
    onBackground = OscuroTinta,
    surface = OscuroFondo,
    onSurface = OscuroTinta,
    surfaceVariant = OscuroContenedorAlto,
    onSurfaceVariant = OscuroTintaSecundaria,
    surfaceTint = OscuroContenedorMaximo,
    inverseSurface = OscuroTinta,
    inverseOnSurface = OscuroContenedor,
    error = OscuroError,
    onError = OscuroFondo,
    errorContainer = OscuroErrorContenedor,
    onErrorContainer = OscuroSobreErrorContenedor,
    outline = Borde,
    outlineVariant = OscuroContenedorMaximo,
    scrim = Negro,
    surfaceBright = OscuroContenedorMaximo,
    surfaceDim = OscuroFondo,
    surfaceContainerLowest = OscuroContenedorMinimo,
    surfaceContainerLow = OscuroContenedorBajo,
    surfaceContainer = OscuroContenedor,
    surfaceContainerHigh = OscuroContenedorAlto,
    surfaceContainerHighest = OscuroContenedorMaximo,
)

private val EsquemaClaro = lightColorScheme(
    primary = ClaroNaranja,
    onPrimary = Blanco,
    primaryContainer = ClaroNaranjaContenedor,
    onPrimaryContainer = ClaroSobreNaranjaContenedor,
    inversePrimary = OscuroNaranja,
    secondary = ClaroTintaSecundaria,
    onSecondary = Blanco,
    secondaryContainer = ClaroContenedorMaximo,
    onSecondaryContainer = ClaroTinta,
    tertiary = ClaroAzul,
    onTertiary = Blanco,
    tertiaryContainer = ClaroAzulContenedor,
    onTertiaryContainer = ClaroAzulSobreContenedor,
    background = ClaroFondo,
    onBackground = ClaroTinta,
    surface = ClaroFondo,
    onSurface = ClaroTinta,
    surfaceVariant = ClaroContenedorAlto,
    onSurfaceVariant = ClaroTintaSecundaria,
    surfaceTint = Borde,
    inverseSurface = OscuroContenedorAlto,
    inverseOnSurface = OscuroTinta,
    error = ClaroError,
    onError = Blanco,
    errorContainer = ClaroErrorContenedor,
    onErrorContainer = ClaroSobreErrorContenedor,
    outline = Borde,
    outlineVariant = ClaroContenedorMaximo,
    scrim = Negro,
    surfaceBright = ClaroContenedorMinimo,
    surfaceDim = ClaroContenedorMaximo,
    surfaceContainerLowest = ClaroContenedorMinimo,
    surfaceContainerLow = ClaroContenedorBajo,
    surfaceContainer = ClaroContenedor,
    surfaceContainerHigh = ClaroContenedorAlto,
    surfaceContainerHighest = ClaroContenedorMaximo,
)

@Composable
fun DesafioSportsTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content,
    )
}
