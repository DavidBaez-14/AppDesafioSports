package co.edu.ufps.desafiosports.presentacion.creador

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.InvitacionDeTorneo
import co.edu.ufps.desafiosports.domain.model.PersonalDeArbitraje
import co.edu.ufps.desafiosports.presentacion.comun.Avatar
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.presentacion.comun.inicialDe
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.time.LocalDate
import java.time.LocalDateTime

/** Lo que la pantalla de árbitros le pide al ViewModel. */
data class AccionesArbitros(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onPedirInvitacion: () -> Unit = {},
    val onCorreo: (String) -> Unit = {},
    val onCancelarInvitar: () -> Unit = {},
    val onConfirmarInvitar: () -> Unit = {},
    val onCompartir: (InvitacionDeTorneo) -> Unit = {},
    val onCancelarInvitacion: (InvitacionDeTorneo) -> Unit = {},
    val onTodosLosPartidos: (ArbitroDeTorneo, Boolean) -> Unit = { _, _ -> },
    val onPedirQuitar: (ArbitroDeTorneo) -> Unit = {},
    val onCancelarQuitar: () -> Unit = {},
    val onConfirmarQuitar: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
)

/** Árbitros de un torneo que administro e invitaciones pendientes (MOV-0011). */
@Composable
fun ArbitrosTorneoScreen(
    onVolver: () -> Unit,
    viewModel: ArbitrosTorneoViewModel = viewModel(factory = ArbitrosTorneoViewModel.Factory),
) {
    val contexto = LocalContext.current
    val state = viewModel.state
    val acciones = remember(viewModel, contexto, state.torneo) {
        AccionesArbitros(
            onReintentar = viewModel::cargar,
            onRefrescar = viewModel::refrescar,
            onPedirInvitacion = viewModel::pedirInvitacion,
            onCorreo = viewModel::onCorreoChange,
            onCancelarInvitar = viewModel::cancelarInvitar,
            onConfirmarInvitar = viewModel::confirmarInvitar,
            onCompartir = { compartirInvitacion(contexto, state.torneo, it) },
            onCancelarInvitacion = viewModel::cancelarInvitacion,
            onTodosLosPartidos = viewModel::cambiarTodosLosPartidos,
            onPedirQuitar = viewModel::pedirQuitar,
            onCancelarQuitar = viewModel::cancelarQuitar,
            onConfirmarQuitar = viewModel::confirmarQuitar,
            onAvisoMostrado = viewModel::onAvisoMostrado,
        )
    }
    // Recién creada: se ofrece compartir el enlace de una vez.
    LaunchedEffect(state.porCompartir) {
        val invitacion = state.porCompartir ?: return@LaunchedEffect
        compartirInvitacion(contexto, state.torneo, invitacion)
        viewModel.onCompartido()
    }
    ArbitrosTorneoContenido(state = state, hoy = LocalDate.now(), acciones = acciones, onVolver = onVolver)
}

/** Abre la hoja de compartir de Android con el enlace de la invitación. */
private fun compartirInvitacion(contexto: Context, torneo: String, invitacion: InvitacionDeTorneo) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, contexto.getString(R.string.invitar_compartir_texto, torneo, invitacion.enlace))
    }
    contexto.startActivity(Intent.createChooser(intent, null))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ArbitrosTorneoContenido(
    state: ArbitrosState,
    hoy: LocalDate,
    acciones: AccionesArbitros,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    val textoAviso = when (val aviso = state.aviso) {
        is AvisoArbitros.Quitado -> stringResource(R.string.arbitros_aviso_quitado, aviso.nombre)
        AvisoArbitros.InvitacionCancelada -> stringResource(R.string.arbitros_aviso_invitacion_cancelada)
        is AvisoArbitros.Fallo -> aviso.mensaje
        null -> null
    }
    LaunchedEffect(state.aviso) {
        if (state.aviso == null) return@LaunchedEffect
        avisos.showSnackbar(textoAviso.orEmpty())
        acciones.onAvisoMostrado()
    }

    val personal = state.personal
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { TituloConTorneo(stringResource(R.string.gestion_arbitros), state.torneo) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("arbitros_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            // La acción principal de la pantalla: el único naranja.
            if (personal != null) {
                ExtendedFloatingActionButton(
                    onClick = acciones.onPedirInvitacion,
                    icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                    text = { Text(stringResource(R.string.arbitros_invitar)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.testTag("arbitros_invitar"),
                )
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        when {
            state.cargando && personal == null -> Cargando(Modifier.padding(relleno))

            state.error != null && personal == null ->
                if (state.sinPermiso) {
                    EstadoError(
                        state.error, onVolver, Modifier.padding(relleno),
                        etiquetaBoton = stringResource(R.string.accion_volver), testTagBoton = "arbitros_volver",
                    )
                } else {
                    EstadoError(state.error, acciones.onReintentar, Modifier.padding(relleno), testTagBoton = "arbitros_reintentar")
                }

            personal != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = acciones.onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                if (personal.vacio) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("arbitros_vacio"),
                    ) {
                        EstadoVacio(
                            icono = R.drawable.ic_sports,
                            titulo = stringResource(R.string.arbitros_vacio_titulo),
                            detalle = stringResource(R.string.arbitros_vacio_detalle),
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                } else {
                    LazyColumn(
                        // Deja aire para que el botón flotante no tape la última fila.
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("arbitros_lista"),
                    ) {
                        if (personal.arbitros.isNotEmpty()) {
                            item(key = "titulo_arbitros") { EncabezadoBloque(stringResource(R.string.arbitros_del_torneo)) }
                            itemsIndexed(personal.arbitros, key = { _, a -> "arbitro_${a.rolId}" }) { indice, arbitro ->
                                if (indice > 0) Separador()
                                FilaArbitro(arbitro, habilitada = state.ocupado == null, acciones)
                            }
                        }
                        if (personal.invitaciones.isNotEmpty()) {
                            item(key = "titulo_invitaciones") { EncabezadoBloque(stringResource(R.string.arbitros_invitaciones)) }
                            itemsIndexed(personal.invitaciones, key = { _, i -> "invitacion_${i.id}" }) { indice, invitacion ->
                                if (indice > 0) Separador()
                                FilaInvitacion(invitacion, hoy, habilitada = state.ocupado == null, acciones)
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.invitando) DialogoInvitar(state, acciones)

    state.quitando?.let { arbitro ->
        AlertDialog(
            onDismissRequest = acciones.onCancelarQuitar,
            // El diálogo es otra ventana: sin esto sus testTag no se ven desde fuera.
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            title = { Text(stringResource(R.string.arbitros_quitar_titulo, arbitro.nombre)) },
            text = { Text(stringResource(R.string.arbitros_quitar_detalle)) },
            confirmButton = {
                TextButton(onClick = acciones.onConfirmarQuitar, modifier = Modifier.testTag("arbitro_quitar_confirmar")) {
                    Text(stringResource(R.string.arbitro_quitar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = acciones.onCancelarQuitar) {
                    Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}

@Composable
private fun Separador() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 72.dp, end = 16.dp))
}

/** El árbitro, su interruptor de "todos los partidos" y "Quitar". */
@Composable
private fun FilaArbitro(arbitro: ArbitroDeTorneo, habilitada: Boolean, acciones: AccionesArbitros) {
    val lecturaTodos = stringResource(R.string.arbitro_todos_lectura, arbitro.nombre)
    val lecturaQuitar = stringResource(R.string.arbitro_quitar_lectura, arbitro.nombre)
    Column(Modifier.testTag("arbitro")) {
        Fila(
            titulo = arbitro.nombre,
            detalle = arbitro.correo?.takeIf { it != arbitro.nombre },
            inicio = { Avatar(inicialDe(arbitro.nombre)) },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = arbitro.todosLosPartidos,
                    enabled = habilitada,
                    role = Role.Switch,
                    onValueChange = { acciones.onTodosLosPartidos(arbitro, it) },
                )
                .semantics { contentDescription = lecturaTodos }
                .padding(start = 72.dp, end = 16.dp)
                .testTag("arbitro_todos"),
        ) {
            Text(
                stringResource(R.string.arbitro_todos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            // El toque lo recibe toda la fila.
            // En tinta y no en naranja: el naranja es de "Invitar árbitro".
            Switch(
                checked = arbitro.todosLosPartidos,
                onCheckedChange = null,
                enabled = habilitada,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
        ) {
            BotonContorno(
                texto = stringResource(R.string.arbitro_quitar),
                onClick = { acciones.onPedirQuitar(arbitro) },
                habilitado = habilitada,
                modifier = Modifier
                    .semantics { contentDescription = lecturaQuitar }
                    .testTag("arbitro_quitar"),
            )
        }
    }
}

@Composable
private fun FilaInvitacion(invitacion: InvitacionDeTorneo, hoy: LocalDate, habilitada: Boolean, acciones: AccionesArbitros) {
    Column(Modifier.testTag("invitacion")) {
        Fila(
            titulo = invitacion.correo ?: stringResource(R.string.invitacion_por_enlace),
            detalle = invitacion.fecha?.let { stringResource(R.string.invitacion_creada, Formatos.fecha(it.toLocalDate(), hoy)) },
            inicio = { IconoEnCirculo(R.drawable.ic_link) },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, end = 16.dp, bottom = 14.dp),
        ) {
            TextButton(
                onClick = { acciones.onCancelarInvitacion(invitacion) },
                enabled = habilitada,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("invitacion_cancelar"),
            ) {
                Text(stringResource(R.string.invitacion_cancelar), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (invitacion.enlace.isNotBlank()) {
                BotonContorno(
                    texto = stringResource(R.string.invitacion_compartir),
                    onClick = { acciones.onCompartir(invitacion) },
                    modifier = Modifier.testTag("invitacion_compartir"),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun DialogoInvitar(state: ArbitrosState, acciones: AccionesArbitros) {
    AlertDialog(
        onDismissRequest = acciones.onCancelarInvitar,
        // El diálogo es otra ventana: sin esto sus testTag no se ven desde fuera.
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        title = { Text(stringResource(R.string.arbitros_invitar)) },
        text = {
            OutlinedTextField(
                value = state.correo,
                onValueChange = acciones.onCorreo,
                label = { Text(stringResource(R.string.invitar_correo)) },
                isError = state.errorCorreo != null,
                supportingText = { Text(state.errorCorreo ?: stringResource(R.string.invitar_ayuda)) },
                singleLine = true,
                enabled = !state.enviandoInvitacion,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invitar_correo"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = acciones.onConfirmarInvitar,
                enabled = !state.enviandoInvitacion,
                modifier = Modifier.testTag("invitar_confirmar"),
            ) {
                Text(stringResource(if (state.enviandoInvitacion) R.string.invitar_creando else R.string.invitar_confirmar))
            }
        },
        dismissButton = {
            TextButton(onClick = acciones.onCancelarInvitar, enabled = !state.enviandoInvitacion) {
                Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 16)

private val personalDeEjemplo = PersonalDeArbitraje(
    arbitros = listOf(
        ArbitroDeTorneo("r1", "c1", "Ana Ruiz", "ana@correo.com", todosLosPartidos = true),
        ArbitroDeTorneo("r2", "c2", "Carlos Andrés Rojas Peñaranda de la Universidad", "carlos@correo.com", todosLosPartidos = false),
    ),
    invitaciones = listOf(
        InvitacionDeTorneo("i1", null, "https://desafio.test/invitacion/abc", LocalDateTime.of(2026, 11, 15, 10, 0)),
        InvitacionDeTorneo("i2", "nuevo@correo.com", "https://desafio.test/invitacion/def", LocalDateTime.of(2026, 11, 14, 10, 0)),
    ),
)

@Preview(name = "Árbitros · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Árbitros · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ArbitrosPreview() {
    DesafioSportsTheme {
        ArbitrosTorneoContenido(ArbitrosState("Copa Prueba 2026", cargando = false, personal = personalDeEjemplo), HOY, AccionesArbitros(), {})
    }
}

@Preview(name = "Árbitros · vacío", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ArbitrosVacioPreview() {
    DesafioSportsTheme {
        ArbitrosTorneoContenido(ArbitrosState("Copa Prueba 2026", cargando = false, personal = PersonalDeArbitraje()), HOY, AccionesArbitros(), {})
    }
}
