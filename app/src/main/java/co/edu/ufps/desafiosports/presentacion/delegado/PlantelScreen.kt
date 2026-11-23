package co.edu.ufps.desafiosports.presentacion.delegado

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.MiembroDePlantel
import co.edu.ufps.desafiosports.domain.model.Plantel
import co.edu.ufps.desafiosports.presentacion.arbitro.NumeroCamiseta
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.creador.TituloConTorneo
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Lo que la pantalla del plantel le pide al ViewModel y a la navegación. */
data class AccionesPlantel(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onAgregar: () -> Unit = {},
    val onJugador: (MiembroDePlantel) -> Unit = {},
    val onCamiseta: (String) -> Unit = {},
    val onGuardar: () -> Unit = {},
    val onCerrarEdicion: () -> Unit = {},
    val onPedirQuitar: () -> Unit = {},
    val onCancelarQuitar: () -> Unit = {},
    val onConfirmarQuitar: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
)

/** El plantel de un equipo en un torneo, para su delegado (MOV-0015). */
@Composable
fun PlantelScreen(
    onVolver: () -> Unit,
    onAgregar: () -> Unit,
    viewModel: PlantelViewModel = viewModel(factory = PlantelViewModel.Factory),
) {
    // Al volver de agregar jugadores, el plantel se pide de nuevo.
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    val acciones = remember(viewModel, onAgregar) {
        AccionesPlantel(
            onReintentar = viewModel::cargar,
            onRefrescar = viewModel::refrescar,
            onAgregar = onAgregar,
            onJugador = viewModel::abrirJugador,
            onCamiseta = viewModel::onCamiseta,
            onGuardar = viewModel::guardarCamiseta,
            onCerrarEdicion = viewModel::cerrarEdicion,
            onPedirQuitar = viewModel::pedirQuitar,
            onCancelarQuitar = viewModel::cancelarQuitar,
            onConfirmarQuitar = viewModel::confirmarQuitar,
            onAvisoMostrado = viewModel::onAvisoMostrado,
        )
    }
    PlantelContenido(state = viewModel.state, acciones = acciones, onVolver = onVolver)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PlantelContenido(
    state: PlantelState,
    acciones: AccionesPlantel,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    val textoAviso = when (val aviso = state.aviso) {
        is AvisoPlantel.Quitado -> stringResource(R.string.plantel_aviso_quitado, aviso.nombre)
        is AvisoPlantel.Fallo -> aviso.mensaje
        null -> null
    }
    LaunchedEffect(state.aviso) {
        if (textoAviso == null) return@LaunchedEffect
        avisos.showSnackbar(textoAviso)
        acciones.onAvisoMostrado()
    }

    val plantel = state.plantel
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    TituloConTorneo(
                        stringResource(R.string.plantel_titulo),
                        listOf(state.equipo, state.torneo).filter { it.isNotBlank() }.joinToString(" · "),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("plantel_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            if (state.editable) {
                ExtendedFloatingActionButton(
                    onClick = acciones.onAgregar,
                    icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                    text = { Text(stringResource(R.string.plantel_agregar)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                    modifier = Modifier.testTag("plantel_agregar"),
                )
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        when {
            state.cargando && plantel == null -> Cargando(Modifier.padding(relleno))

            state.error != null && plantel == null ->
                if (state.sinPermiso) {
                    EstadoError(
                        state.error, onVolver, Modifier.padding(relleno),
                        etiquetaBoton = stringResource(R.string.accion_volver), testTagBoton = "plantel_volver",
                    )
                } else {
                    EstadoError(state.error, acciones.onReintentar, Modifier.padding(relleno), testTagBoton = "plantel_reintentar")
                }

            plantel != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = acciones.onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                ListaDePlantel(plantel, state.editable, acciones)
            }
        }
    }

    state.miembroEditando?.let { miembro ->
        if (state.confirmandoQuitar) {
            DialogoQuitar(miembro, state.guardando, acciones)
        } else {
            DialogoJugador(miembro, state, acciones)
        }
    }
}

@Composable
private fun ListaDePlantel(plantel: Plantel, editable: Boolean, acciones: AccionesPlantel) {
    val activos = plantel.activos
    val retirados = plantel.retirados
    LazyColumn(
        // Aire para que el botón flotante no tape la última fila.
        contentPadding = PaddingValues(bottom = 104.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("plantel_lista"),
    ) {
        item(key = "resumen") {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = pluralStringResource(R.plurals.plantel_jugadores, activos.size, activos.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag("plantel_resumen"),
                )
                Text(
                    text = stringResource(if (editable) R.string.plantel_ayuda else R.string.plantel_cerrado),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (activos.isEmpty()) {
            item(key = "vacio") {
                EstadoVacio(
                    icono = R.drawable.ic_groups,
                    titulo = stringResource(R.string.plantel_vacio),
                    detalle = stringResource(R.string.plantel_vacio_detalle),
                )
            }
        }
        items(activos, key = { it.personaId }) { miembro ->
            Fila(
                titulo = miembro.nombre,
                detalle = detalleDe(miembro),
                inicio = { NumeroCamiseta(miembro.numeroCamiseta) },
                onClick = if (editable) ({ acciones.onJugador(miembro) }) else null,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("plantel_jugador"),
            )
        }
        if (retirados.isNotEmpty()) {
            item(key = "titulo_retirados") { EncabezadoBloque(stringResource(R.string.plantel_retirados)) }
            items(retirados, key = { "retirado_${it.personaId}" }) { miembro ->
                Fila(
                    titulo = miembro.nombre,
                    detalle = stringResource(R.string.plantel_retirado),
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .testTag("plantel_retirado"),
                )
            }
        }
    }
}

/** "Delegado · Sin cuenta · Doc. 1090…": lo que el delegado necesita saber de cada uno. */
@Composable
private fun detalleDe(miembro: MiembroDePlantel): String? = listOfNotNull(
    if (miembro.esDelegado) stringResource(R.string.planilla_delegado) else null,
    if (!miembro.tieneCuenta) stringResource(R.string.plantel_sin_cuenta) else null,
    miembro.numeroDocumento?.let { stringResource(R.string.planilla_documento, it) },
).joinToString(" · ").ifBlank { null }

/** Cambiar el número del jugador o quitarlo del plantel. El error va dentro del diálogo. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DialogoJugador(miembro: MiembroDePlantel, state: PlantelState, acciones: AccionesPlantel) {
    AlertDialog(
        onDismissRequest = acciones.onCerrarEdicion,
        // El diálogo es otra ventana: sin esto sus testTag no se ven desde fuera.
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("dialogo_jugador"),
        title = { Text(miembro.nombre) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.camisetaTexto,
                    onValueChange = acciones.onCamiseta,
                    label = { Text(stringResource(R.string.plantel_camiseta)) },
                    isError = state.errorEdicion != null,
                    supportingText = { Text(state.errorEdicion ?: stringResource(R.string.plantel_camiseta_ayuda)) },
                    singleLine = true,
                    enabled = !state.guardando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("plantel_camiseta"),
                )
                if (!miembro.esDelegado) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    TextButton(
                        onClick = acciones.onPedirQuitar,
                        enabled = !state.guardando,
                        modifier = Modifier.testTag("plantel_quitar"),
                    ) {
                        Text(stringResource(R.string.plantel_quitar), color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Text(
                        stringResource(R.string.plantel_delegado_fijo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = acciones.onGuardar, enabled = !state.guardando, modifier = Modifier.testTag("plantel_guardar")) {
                Text(stringResource(if (state.guardando) R.string.plantel_guardando else R.string.plantel_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = acciones.onCerrarEdicion, enabled = !state.guardando) {
                Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DialogoQuitar(miembro: MiembroDePlantel, quitando: Boolean, acciones: AccionesPlantel) {
    AlertDialog(
        onDismissRequest = acciones.onCancelarQuitar,
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        title = { Text(stringResource(R.string.plantel_quitar_titulo, miembro.nombre)) },
        text = { Text(stringResource(R.string.plantel_quitar_detalle)) },
        confirmButton = {
            TextButton(onClick = acciones.onConfirmarQuitar, enabled = !quitando, modifier = Modifier.testTag("plantel_quitar_confirmar")) {
                Text(
                    stringResource(if (quitando) R.string.plantel_quitando else R.string.plantel_quitar_si),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = acciones.onCancelarQuitar, enabled = !quitando, modifier = Modifier.testTag("plantel_quitar_cancelar")) {
                Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private fun miembroDeEjemplo(
    id: String,
    nombre: String,
    numero: Int?,
    activo: Boolean = true,
    delegado: Boolean = false,
    cuenta: Boolean = true,
    documento: String? = null,
) = MiembroDePlantel(id, nombre, documento, numero, activo, delegado, cuenta, torneoFinalizado = false)

private val plantelDeEjemplo = Plantel(
    listOf(
        miembroDeEjemplo("1", "Andrés Gómez", 1, delegado = true, documento = "9000000201"),
        miembroDeEjemplo("2", "Juan Castro", 10, documento = "9000000301"),
        miembroDeEjemplo("3", "Sofía Moreno", null, cuenta = false),
        miembroDeEjemplo("4", "Mateo Jiménez", 7, activo = false),
    ),
)

@Preview(name = "Plantel · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Plantel · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlantelPreview() {
    DesafioSportsTheme {
        PlantelContenido(
            PlantelState(equipo = "Halcones FC", torneo = "Copa Prueba 2026", cargando = false, plantel = plantelDeEjemplo),
            AccionesPlantel(), {},
        )
    }
}

@Preview(name = "Plantel · editar", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlantelEditarPreview() {
    DesafioSportsTheme {
        PlantelContenido(
            PlantelState(
                equipo = "Halcones FC", torneo = "Copa Prueba 2026", cargando = false, plantel = plantelDeEjemplo,
                editando = "2", camisetaTexto = "10",
            ),
            AccionesPlantel(), {},
        )
    }
}
