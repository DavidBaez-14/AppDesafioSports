package co.edu.ufps.desafiosports.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colores con significado que Material 3 no trae (MOV-0009): éxito, advertencia y las tarjetas.
 * Los mismos valores del prototipo aprobado. Las pantallas los leen con
 * `MaterialTheme.coloresExtra`, igual que leen `colorScheme`: nunca un valor suelto.
 */
@Immutable
data class ColoresExtra(
    val exito: Color,
    val exitoContenedor: Color,
    val advertencia: Color,
    val advertenciaContenedor: Color,
    val tarjetaAmarilla: Color,
    val tarjetaAzul: Color,
    val tarjetaRoja: Color,
)

internal val ColoresExtraOscuro = ColoresExtra(
    exito = Color(0xFF4ADE80),
    exitoContenedor = Color(0xFF0F2E1C),
    advertencia = Color(0xFFFACC15),
    advertenciaContenedor = Color(0xFF3A3000),
    tarjetaAmarilla = Color(0xFFFACC15),
    tarjetaAzul = Color(0xFF3B82F6),
    tarjetaRoja = Color(0xFFEF4444),
)

internal val ColoresExtraClaro = ColoresExtra(
    exito = Color(0xFF15803D),
    exitoContenedor = Color(0xFFDCFCE7),
    advertencia = Color(0xFF854D0E),
    advertenciaContenedor = Color(0xFFFEF3C7),
    // Las tarjetas son el objeto real: conservan su color en los dos temas.
    tarjetaAmarilla = Color(0xFFEAB308),
    tarjetaAzul = Color(0xFF2563EB),
    tarjetaRoja = Color(0xFFDC2626),
)

internal val LocalColoresExtra = staticCompositionLocalOf { ColoresExtraOscuro }

val MaterialTheme.coloresExtra: ColoresExtra
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresExtra.current
