package co.edu.ufps.desafiosports.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Tema PROVISIONAL con los tokens del Design System, mapeados a roles de Material 3.
// Decisiones pendientes (docs/plan-ui-impeccable.md): tema claro u oscuro, tipografía y contraste del naranja.
//
// Sin color dinámico (Material You): taparía los colores de la marca.
// Texto oscuro sobre el naranja: el blanco sobre #FF5500 no llega al contraste AA (3:1).

private val EsquemaOscuro = darkColorScheme(
    primary = Naranja500,
    onPrimary = Navy900,
    primaryContainer = Naranja700,
    onPrimaryContainer = Naranja50,
    secondary = Azul400,
    onSecondary = Navy900,
    secondaryContainer = Azul700,
    onSecondaryContainer = Azul200,
    background = Navy900,
    onBackground = Blanco,
    surface = Navy800,
    onSurface = Blanco,
    surfaceVariant = Navy600,
    onSurfaceVariant = Gris200,
    surfaceContainerLowest = Navy900,
    surfaceContainerLow = Navy800,
    surfaceContainer = Navy700,
    surfaceContainerHigh = Navy600,
    surfaceContainerHighest = Navy500,
    outline = Navy400,
    outlineVariant = Navy600,
    error = Rojo500,
    onError = Navy900,
)

private val EsquemaClaro = lightColorScheme(
    primary = Naranja700,
    onPrimary = Blanco,
    primaryContainer = Naranja100,
    onPrimaryContainer = Navy900,
    secondary = Azul500,
    onSecondary = Blanco,
    background = Navy50,
    onBackground = Navy900,
    surface = Blanco,
    onSurface = Navy900,
    surfaceVariant = Navy100,
    onSurfaceVariant = Navy500,
    outline = Navy300,
    error = Rojo700,
    onError = Blanco,
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
