package co.edu.ufps.desafiosports.presentacion.arbitro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.EquipoPlanilla
import co.edu.ufps.desafiosports.domain.model.JugadorPlanilla
import co.edu.ufps.desafiosports.domain.model.Lado
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.ui.deporte.nombreDeEvento

/** Selector de equipo de la planilla: dos botones segmentados con el nombre de cada equipo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectorDeEquipo(
    planilla: Planilla,
    elegido: Lado,
    onElegir: (Lado) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefijo: String = "equipo",
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        Lado.entries.forEachIndexed { indice, lado ->
            SegmentedButton(
                selected = elegido == lado,
                onClick = { onElegir(lado) },
                shape = SegmentedButtonDefaults.itemShape(index = indice, count = Lado.entries.size),
                label = {
                    Text(planilla.equipo(lado).nombre, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("${tagPrefijo}_${lado.name.lowercase()}"),
            )
        }
    }
}

/** El número de camiseta en un círculo; una raya si el jugador no tiene. */
@Composable
internal fun NumeroCamiseta(numero: Int?, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(numero?.toString() ?: "–", style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
    }
}

/**
 * Hoja de alineación (MOV-0009 · CA-7 y CA-8): quién juega de cada equipo. Quien no es apto
 * aparece con el motivo y no se puede marcar. Con [editable] en false solo se consulta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlineacionHoja(
    planilla: Planilla,
    lado: Lado,
    editable: Boolean,
    enAccion: Boolean,
    onLado: (Lado) -> Unit,
    onCambiar: (JugadorPlanilla) -> Unit,
    onTodos: () -> Unit,
    onCerrar: () -> Unit,
) {
    val equipo = planilla.equipo(lado)
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.testTag("hoja_alineacion"),
    ) {
        Text(
            text = stringResource(R.string.planilla_alineacion).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .semantics { heading() },
        )
        SelectorDeEquipo(
            planilla = planilla,
            elegido = lado,
            onElegir = onLado,
            tagPrefijo = "alineacion_equipo",
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 4.dp),
        ) {
            Text(
                text = pluralStringResource(R.plurals.planilla_alineados, equipo.alineados, equipo.alineados, equipo.jugadores.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (editable && equipo.jugadores.isNotEmpty()) {
                TextButton(onClick = onTodos, enabled = !enAccion, modifier = Modifier.testTag("alineacion_todos")) {
                    Icon(painterResource(R.drawable.ic_done_all), contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        stringResource(R.string.planilla_alinear_todos),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
        if (equipo.jugadores.isEmpty()) {
            EstadoVacio(
                icono = R.drawable.ic_groups,
                titulo = stringResource(R.string.planilla_plantel_vacio),
                detalle = stringResource(R.string.planilla_plantel_vacio_detalle),
            )
        } else {
            LazyColumn(modifier = Modifier.testTag("alineacion_lista")) {
                items(equipo.jugadores, key = { it.personaId }) { jugador ->
                    FilaAlineacion(jugador, editable = editable, enAccion = enAccion, onCambiar = { onCambiar(jugador) })
                }
            }
        }
    }
}

@Composable
private fun FilaAlineacion(jugador: JugadorPlanilla, editable: Boolean, enAccion: Boolean, onCambiar: () -> Unit) {
    // A quien no es apto solo se le puede quitar (si ya estaba alineado), nunca agregar.
    val sePuedeCambiar = editable && !enAccion && (jugador.apto || jugador.alineado)
    val noApto = stringResource(R.string.planilla_no_apto)
    val detalle = when {
        !jugador.apto -> if (jugador.motivos.isEmpty()) noApto else stringResource(R.string.planilla_no_apto_motivo, jugador.motivos.joinToString(", "))
        else -> listOfNotNull(
            jugador.numeroDocumento?.let { stringResource(R.string.planilla_documento, it) },
            if (jugador.esDelegado) stringResource(R.string.planilla_delegado) else null,
        ).joinToString(" · ").ifBlank { null }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(
                if (editable) {
                    Modifier.toggleable(value = jugador.alineado, enabled = sePuedeCambiar, role = Role.Checkbox, onValueChange = { onCambiar() })
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag("alineacion_jugador"),
    ) {
        NumeroCamiseta(jugador.numeroCamiseta)
        Column(modifier = Modifier.weight(1f)) {
            Text(jugador.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (detalle != null) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (jugador.apto) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
            }
        }
        when {
            // La fila entera es el control; la casilla solo muestra el estado.
            editable -> Checkbox(checked = jugador.alineado, onCheckedChange = null, enabled = sePuedeCambiar)
            jugador.alineado -> Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = stringResource(R.string.planilla_alineado),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            !jugador.apto -> Icon(painterResource(R.drawable.ic_block), contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }
    }
}

/** Hoja para elegir a quién se le anota el evento (MOV-0009 · CA-4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun JugadorDelEventoHoja(
    tipo: String,
    equipo: EquipoPlanilla,
    onElegir: (JugadorPlanilla) -> Unit,
    onCerrar: () -> Unit,
) {
    val candidatos = equipo.candidatos()
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.testTag("hoja_jugador"),
    ) {
        Text(
            text = stringResource(R.string.planilla_evento_de, nombreDeEvento(tipo), equipo.nombre).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .semantics { heading() },
        )
        Text(
            text = stringResource(R.string.planilla_quien),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 4.dp),
        )
        if (candidatos.isEmpty()) {
            EstadoVacio(
                icono = R.drawable.ic_groups,
                titulo = stringResource(R.string.planilla_sin_candidatos),
                detalle = stringResource(R.string.planilla_sin_candidatos_detalle),
            )
        } else {
            LazyColumn(modifier = Modifier.testTag("jugador_lista")) {
                items(candidatos, key = { it.personaId }) { jugador ->
                    Fila(
                        titulo = jugador.nombre,
                        detalle = if (jugador.alineado) null else stringResource(R.string.planilla_sin_alinear),
                        inicio = { NumeroCamiseta(jugador.numeroCamiseta) },
                        onClick = { onElegir(jugador) },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("jugador_opcion"),
                    )
                }
            }
        }
    }
}
