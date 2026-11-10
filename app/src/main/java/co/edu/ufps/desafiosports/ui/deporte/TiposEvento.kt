package co.edu.ufps.desafiosports.ui.deporte

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.ui.theme.coloresExtra

/** Cómo se pinta un tipo de evento. Hoy solo existen los de fútbol sala. */
enum class AparienciaEvento { GOL, AMARILLA, AZUL, ROJA, GENERICO }

fun aparienciaDeEvento(codigo: String): AparienciaEvento = when (codigo) {
    "GOL" -> AparienciaEvento.GOL
    "AMARILLA" -> AparienciaEvento.AMARILLA
    "AZUL" -> AparienciaEvento.AZUL
    "ROJA" -> AparienciaEvento.ROJA
    else -> AparienciaEvento.GENERICO
}

/** El nombre visible del tipo de evento. Uno desconocido muestra su código en limpio ("Tiro libre"). */
@Composable
fun nombreDeEvento(codigo: String): String = when (aparienciaDeEvento(codigo)) {
    AparienciaEvento.GOL -> stringResource(R.string.evento_gol)
    AparienciaEvento.AMARILLA -> stringResource(R.string.evento_amarilla)
    AparienciaEvento.AZUL -> stringResource(R.string.evento_azul)
    AparienciaEvento.ROJA -> stringResource(R.string.evento_roja)
    AparienciaEvento.GENERICO -> codigoEnLimpio(codigo)
}

/** "TIRO_LIBRE" → "Tiro libre". */
fun codigoEnLimpio(codigo: String): String =
    codigo.trim().lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

/**
 * La marca del evento: una tarjeta de su color, o un icono para lo que no es tarjeta.
 * Es decorativa; el nombre del evento siempre va al lado en texto.
 */
@Composable
fun MarcaDeEvento(codigo: String, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.coloresExtra
    val colorTarjeta = when (aparienciaDeEvento(codigo)) {
        AparienciaEvento.AMARILLA -> colores.tarjetaAmarilla
        AparienciaEvento.AZUL -> colores.tarjetaAzul
        AparienciaEvento.ROJA -> colores.tarjetaRoja
        else -> null
    }
    Box(contentAlignment = Alignment.Center, modifier = modifier.size(24.dp)) {
        if (colorTarjeta != null) {
            Box(
                Modifier
                    .size(width = 16.dp, height = 22.dp)
                    .background(colorTarjeta, RoundedCornerShape(3.dp)),
            )
        } else {
            Icon(
                painter = painterResource(
                    if (aparienciaDeEvento(codigo) == AparienciaEvento.GOL) R.drawable.ic_sports_soccer else R.drawable.ic_flag,
                ),
                contentDescription = null,
                tint = LocalContentColor.current,
            )
        }
    }
}
