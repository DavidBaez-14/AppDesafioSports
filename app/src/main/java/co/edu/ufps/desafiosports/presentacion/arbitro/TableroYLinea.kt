package co.edu.ufps.desafiosports.presentacion.arbitro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.Lado
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.ui.deporte.MarcaDeEvento
import co.edu.ufps.desafiosports.ui.deporte.nombreDeEvento

// El tablero del partido y la línea de eventos de dos lados (MOV-0010).

/**
 * De qué lado de la línea va un evento (MOV-0010 · CA-7): por el equipo del torneo y, si el id no
 * coincide con ninguno, por el nombre. Lo que no se reconoce va al local, para no perderlo.
 */
fun ladoDelEvento(evento: EventoPartido, planilla: Planilla): Lado = when {
    evento.equipoTorneoId == planilla.visitante.equipoTorneoId -> Lado.VISITANTE
    evento.equipoTorneoId == planilla.local.equipoTorneoId -> Lado.LOCAL
    evento.equipoNombre.isNotBlank() && evento.equipoNombre == planilla.visitante.nombre -> Lado.VISITANTE
    else -> Lado.LOCAL
}

/**
 * Tablero del partido: el contexto arriba, las dos cifras grandes con su equipo debajo y el
 * estado. Es un escalón de contenedor, no una tarjeta con sombra. TalkBack lo lee de corrido.
 */
@Composable
internal fun Tablero(planilla: Planilla, modifier: Modifier = Modifier) {
    val partido = planilla.partido
    val marcador = planilla.marcador
    val local = partido.local?.nombre ?: stringResource(R.string.partido_equipo_por_definir)
    val visitante = partido.visitante?.nombre ?: stringResource(R.string.partido_equipo_por_definir)
    val contexto = listOfNotNull(partido.torneo, partido.fase).filter { it.isNotBlank() }.joinToString(" · ")
    val lectura = stringResource(R.string.planilla_marcador_lectura, local, marcador.local, visitante, marcador.visitante)
    val enJuego = partido.estado == EstadoPartido.EN_CURSO

    Surface(
        color = colorDePanel(),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 16.dp),
        ) {
            if (contexto.isNotBlank()) {
                Text(
                    text = contexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clearAndSetSemantics { contentDescription = lectura }
                    .testTag("planilla_marcador"),
            ) {
                LadoDelTablero(local, marcador.local, Modifier.weight(1f))
                Text(
                    text = "–",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp),
                )
                LadoDelTablero(visitante, marcador.visitante, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            when (partido.estado) {
                EstadoPartido.PROGRAMADO -> ChipEstado(stringResource(R.string.partido_programado), icono = R.drawable.ic_schedule)
                // Lo que pasa ahora: el único naranja del tablero.
                EstadoPartido.EN_CURSO -> ChipEstado(stringResource(R.string.partido_en_juego), tono = Tono.AHORA, icono = R.drawable.ic_radio_button_checked)
                EstadoPartido.FINALIZADO -> ChipEstado(stringResource(R.string.planilla_cerrado), tono = Tono.EXITO, icono = R.drawable.ic_task_alt)
                EstadoPartido.WO -> ChipEstado(stringResource(R.string.partido_wo))
                EstadoPartido.APLAZADO -> ChipEstado(stringResource(R.string.partido_aplazado), tono = Tono.ADVERTENCIA)
                EstadoPartido.DESCONOCIDO -> Unit
            }
            val lugar = partido.lugar
            if (!lugar.isNullOrBlank() && !enJuego) {
                Text(
                    text = lugar,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun LadoDelTablero(nombre: String, cifra: Int, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = cifra.toString(),
            // Barlow Condensed en display, con dígitos de ancho fijo para que no baile al cambiar.
            style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
        )
        Text(
            text = nombre.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = MaterialTheme.typography.headlineSmall.fontFamily, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

/**
 * Un evento en la línea de dos lados (MOV-0010 · CA-7): lo del local a la izquierda y lo del
 * visitante a la derecha, con el número de orden al centro sobre una línea vertical. El botón de
 * quitar queda en el borde de su lado, lejos del número.
 */
@Composable
internal fun EventoEnLinea(
    numero: Int,
    evento: EventoPartido,
    lado: Lado,
    primero: Boolean,
    ultimo: Boolean,
    sePuedeQuitar: Boolean,
    habilitado: Boolean,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tipo = nombreDeEvento(evento.tipo)
    val lectura = stringResource(R.string.planilla_evento_lectura, numero, tipo, evento.jugadorNombre, evento.equipoNombre)
    val colorLinea = MaterialTheme.colorScheme.outlineVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 4.dp),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (lado == Lado.LOCAL) {
                LadoDelEvento(evento, tipo, lectura, Lado.LOCAL, sePuedeQuitar, habilitado, numero, onQuitar)
            }
        }
        // El eje: una línea vertical que une los eventos, cortada arriba del primero y abajo del último.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(44.dp)
                .heightIn(min = 64.dp)
                .drawBehind {
                    val x = size.width / 2
                    val grosor = 2.dp.toPx()
                    val inicio = if (primero) size.height / 2 else 0f
                    val fin = if (ultimo) size.height / 2 else size.height
                    if (fin > inicio) drawLine(colorLinea, Offset(x, inicio), Offset(x, fin), grosor)
                },
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(28.dp)
                    .clearAndSetSemantics { },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(numero.toString(), style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"), maxLines = 1)
                }
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (lado == Lado.VISITANTE) {
                LadoDelEvento(evento, tipo, lectura, Lado.VISITANTE, sePuedeQuitar, habilitado, numero, onQuitar)
            }
        }
    }
}

@Composable
private fun LadoDelEvento(
    evento: EventoPartido,
    tipo: String,
    lectura: String,
    lado: Lado,
    sePuedeQuitar: Boolean,
    habilitado: Boolean,
    numero: Int,
    onQuitar: () -> Unit,
) {
    val local = lado == Lado.LOCAL
    val alinear = if (local) TextAlign.End else TextAlign.Start
    val quitar: @Composable () -> Unit = {
        if (sePuedeQuitar) {
            IconButton(onClick = onQuitar, enabled = habilitado, modifier = Modifier.testTag("evento_quitar")) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.planilla_quitar_evento, numero),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("evento_fila"),
    ) {
        if (local) quitar() else MarcaDeEvento(evento.tipo)
        Column(
            horizontalAlignment = if (local) Alignment.End else Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { contentDescription = lectura },
        ) {
            Text(
                text = evento.jugadorNombre,
                style = MaterialTheme.typography.titleSmall,
                textAlign = alinear,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = tipo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = alinear,
                maxLines = 1,
            )
        }
        if (local) MarcaDeEvento(evento.tipo) else quitar()
    }
}
