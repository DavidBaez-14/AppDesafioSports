package co.edu.ufps.desafiosports.presentacion.delegado

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.BotonPrimario
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.presentacion.creador.TituloConTorneo
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Lo que la pantalla le pide al ViewModel. */
data class AccionesInscribir(
    val onReintentar: () -> Unit = {},
    val onElegir: (EquipoPropio) -> Unit = {},
    val onNuevo: () -> Unit = {},
    val onNombre: (String) -> Unit = {},
    val onInscribir: () -> Unit = {},
)

/** Inscribir un equipo en un torneo (MOV-0014). */
@Composable
fun InscribirEquipoScreen(
    onVolver: () -> Unit,
    viewModel: InscribirEquipoViewModel = viewModel(factory = InscribirEquipoViewModel.Factory),
) {
    InscribirEquipoContenido(
        state = viewModel.state,
        acciones = AccionesInscribir(
            onReintentar = viewModel::cargar,
            onElegir = viewModel::elegir,
            onNuevo = viewModel::elegirNuevo,
            onNombre = viewModel::onNombre,
            onInscribir = viewModel::inscribir,
        ),
        onVolver = onVolver,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InscribirEquipoContenido(
    state: InscribirEquipoState,
    acciones: AccionesInscribir,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { TituloConTorneo(stringResource(R.string.inscribir_titulo), state.torneo) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("inscribir_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        val hecha = state.hecha
        when {
            hecha != null -> Confirmacion(hecha, state.cuota, onVolver, Modifier.padding(relleno))
            state.cargando -> Cargando(Modifier.padding(relleno))
            state.error != null -> EstadoError(state.error, acciones.onReintentar, Modifier.padding(relleno), testTagBoton = "inscribir_reintentar")
            else -> Formulario(state, acciones, Modifier.padding(relleno))
        }
    }
}

@Composable
private fun Formulario(state: InscribirEquipoState, acciones: AccionesInscribir, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.inscribir_explicacion),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Condiciones(state.cuota, state.exigeDocumento)

        EncabezadoBloque(stringResource(R.string.inscribir_que_equipo))
        Column(Modifier.selectableGroup()) {
            state.equipos.forEach { equipo ->
                OpcionDeEquipo(
                    texto = equipo.nombre,
                    elegida = state.elegido == equipo.id,
                    habilitada = !state.inscribiendo,
                    onElegir = { acciones.onElegir(equipo) },
                    modifier = Modifier.testTag("inscribir_equipo"),
                )
            }
            // Sin equipos, crear uno es la única opción: no hace falta el botón de radio.
            if (state.equipos.isNotEmpty()) {
                OpcionDeEquipo(
                    texto = stringResource(R.string.inscribir_nuevo),
                    elegida = state.nuevo,
                    habilitada = !state.inscribiendo,
                    onElegir = acciones.onNuevo,
                    modifier = Modifier.testTag("inscribir_nuevo"),
                )
            }
        }
        if (state.nuevo) {
            OutlinedTextField(
                value = state.nombreNuevo,
                onValueChange = acciones.onNombre,
                label = { Text(stringResource(R.string.inscribir_nombre)) },
                isError = state.errorNombre != null,
                supportingText = {
                    Text(state.errorNombre ?: stringResource(R.string.inscribir_nombre_ayuda))
                },
                singleLine = true,
                enabled = !state.inscribiendo,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("inscribir_nombre"),
            )
        }

        state.errorInscribir?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .testTag("inscribir_error"),
            )
        }
        BotonPrimario(
            texto = stringResource(R.string.inscribir_confirmar),
            onClick = acciones.onInscribir,
            habilitado = state.sePuedeInscribir,
            enProceso = state.inscribiendo,
            textoEnProceso = stringResource(R.string.inscribir_inscribiendo),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("inscribir_confirmar"),
        )
    }
}

/** La cuota y el documento, si aplican: lo que conviene saber antes de inscribir. */
@Composable
private fun Condiciones(cuota: Long, exigeDocumento: Boolean) {
    if (cuota <= 0 && !exigeDocumento) return
    Surface(
        color = colorDePanel(),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("inscribir_condiciones"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(16.dp)) {
            if (cuota > 0) {
                Text(stringResource(R.string.inscribir_cuota, Formatos.pesos(cuota)), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.inscribir_cuota_detalle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (exigeDocumento) {
                Text(
                    stringResource(R.string.inscribir_documento),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Una opción de selección única: la fila entera es el control. */
@Composable
private fun OpcionDeEquipo(
    texto: String,
    elegida: Boolean,
    habilitada: Boolean,
    onElegir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = elegida, enabled = habilitada, role = Role.RadioButton, onClick = onElegir)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        RadioButton(selected = elegida, onClick = null, enabled = habilitada)
        Text(texto, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Ya quedó inscrito: qué sigue. "Listo" vuelve al torneo, que muestra la inscripción. */
@Composable
private fun Confirmacion(hecha: InscripcionHecha, cuota: Long, onListo: () -> Unit, modifier: Modifier = Modifier) {
    val detalle = if (hecha.estado == EstadoInscripcion.APROBADO) {
        stringResource(R.string.inscribir_hecha_aprobada)
    } else if (cuota > 0) {
        stringResource(R.string.inscribir_hecha_cuota, Formatos.pesos(cuota))
    } else {
        stringResource(R.string.inscribir_hecha_revision)
    }
    EstadoVacio(
        icono = R.drawable.ic_check_circle,
        titulo = stringResource(R.string.inscribir_hecha_titulo, hecha.equipo),
        detalle = detalle,
        accion = { BotonContorno(stringResource(R.string.accion_listo), onListo, Modifier.testTag("inscribir_listo")) },
        modifier = modifier
            .padding(top = 32.dp)
            .testTag("inscribir_hecha"),
    )
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val equiposDeEjemplo = listOf(EquipoPropio("e1", "Halcones FC"), EquipoPropio("e2", "Real Código de Ingeniería de Sistemas"))

@Preview(name = "Inscribir · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Inscribir · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InscribirPreview() {
    DesafioSportsTheme {
        InscribirEquipoContenido(
            InscribirEquipoState(
                torneo = "Copa Prueba 2026", cuota = 50_000, exigeDocumento = true, cargando = false,
                equipos = equiposDeEjemplo, nuevo = true, nombreNuevo = "Pumas FC",
            ),
            AccionesInscribir(), {},
        )
    }
}

@Preview(name = "Inscribir · sin equipos", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InscribirSinEquiposPreview() {
    DesafioSportsTheme {
        InscribirEquipoContenido(InscribirEquipoState(torneo = "Liga Relámpago", cargando = false, nuevo = true), AccionesInscribir(), {})
    }
}

@Preview(name = "Inscribir · hecha", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InscribirHechaPreview() {
    DesafioSportsTheme {
        InscribirEquipoContenido(
            InscribirEquipoState(
                torneo = "Copa Prueba 2026", cuota = 50_000, cargando = false,
                hecha = InscripcionHecha("et1", "Halcones FC", EstadoInscripcion.PENDIENTE_PAGO),
            ),
            AccionesInscribir(), {},
        )
    }
}
