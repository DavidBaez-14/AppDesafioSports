package co.edu.ufps.desafiosports.presentacion.arbitro

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.EquipoEnPartido
import co.edu.ufps.desafiosports.domain.model.EquipoPlanilla
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.JugadorPlanilla
import co.edu.ufps.desafiosports.domain.model.Lado
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.presentacion.acceso.BotonPrincipal
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.ui.deporte.MarcaDeEvento
import co.edu.ufps.desafiosports.ui.deporte.nombreDeEvento
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.time.LocalDateTime

/** Lo que la planilla le pide al ViewModel. Agrupado para no pasar una docena de parámetros sueltos. */
data class AccionesPlanilla(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onLado: (Lado) -> Unit = {},
    val onTipoEvento: (String) -> Unit = {},
    val onCerrarSeleccion: () -> Unit = {},
    val onJugador: (JugadorPlanilla) -> Unit = {},
    val onDeshacer: (eventoId: String) -> Unit = {},
    val onPedirQuitar: (EventoPartido) -> Unit = {},
    val onCancelarQuitar: () -> Unit = {},
    val onConfirmarQuitar: () -> Unit = {},
    val onAbrirAlineacion: () -> Unit = {},
    val onCerrarAlineacion: () -> Unit = {},
    val onLadoAlineacion: (Lado) -> Unit = {},
    val onCambiarAlineacion: (JugadorPlanilla) -> Unit = {},
    val onAlinearATodos: () -> Unit = {},
    val onPedirCierre: () -> Unit = {},
    val onCancelarCierre: () -> Unit = {},
    val onConfirmarCierre: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
)

/** Planilla del partido (MOV-0009). */
@Composable
fun PlanillaScreen(
    onVolver: () -> Unit,
    viewModel: PlanillaViewModel = viewModel(factory = PlanillaViewModel.Factory),
) {
    val acciones = remember(viewModel) {
        AccionesPlanilla(
            onReintentar = viewModel::cargar,
            onRefrescar = viewModel::refrescar,
            onLado = viewModel::elegirLado,
            onTipoEvento = viewModel::abrirSeleccion,
            onCerrarSeleccion = viewModel::cerrarSeleccion,
            onJugador = viewModel::registrar,
            onDeshacer = viewModel::deshacer,
            onPedirQuitar = viewModel::pedirQuitar,
            onCancelarQuitar = viewModel::cancelarQuitar,
            onConfirmarQuitar = viewModel::confirmarQuitar,
            onAbrirAlineacion = viewModel::abrirAlineacion,
            onCerrarAlineacion = viewModel::cerrarAlineacion,
            onLadoAlineacion = viewModel::elegirLadoAlineacion,
            onCambiarAlineacion = viewModel::cambiarAlineacion,
            onAlinearATodos = viewModel::alinearATodos,
            onPedirCierre = viewModel::pedirCierre,
            onCancelarCierre = viewModel::cancelarCierre,
            onConfirmarCierre = viewModel::confirmarCierre,
            onAvisoMostrado = viewModel::onAvisoMostrado,
        )
    }
    PlanillaContenido(state = viewModel.state, acciones = acciones, onVolver = onVolver)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PlanillaContenido(
    state: PlanillaState,
    acciones: AccionesPlanilla,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    val textoAviso = state.aviso?.let { textoDeAviso(it) }
    val textoDeshacer = stringResource(R.string.accion_deshacer)
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        val evento = (aviso as? AvisoPlanilla.EventoRegistrado)?.evento
        val resultado = avisos.showSnackbar(
            message = textoAviso.orEmpty(),
            actionLabel = if (evento != null) textoDeshacer else null,
            duration = if (evento != null) SnackbarDuration.Long else SnackbarDuration.Short,
        )
        acciones.onAvisoMostrado()
        if (evento != null && resultado == SnackbarResult.ActionPerformed) acciones.onDeshacer(evento.id)
    }

    val planilla = state.planilla
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.planilla_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("planilla_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (state.editable) BarraCerrar(cerrando = state.cerrando, onCerrar = acciones.onPedirCierre)
        },
        snackbarHost = { SnackbarHost(avisos) },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        when {
            state.cargando && planilla == null -> Cargando(Modifier.padding(relleno))

            state.error != null && planilla == null ->
                if (state.sinPermiso) {
                    EstadoError(
                        state.error, onVolver, Modifier.padding(relleno),
                        etiquetaBoton = stringResource(R.string.accion_volver), testTagBoton = "planilla_volver",
                    )
                } else {
                    EstadoError(state.error, acciones.onReintentar, Modifier.padding(relleno), testTagBoton = "planilla_reintentar")
                }

            planilla != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = acciones.onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                CuerpoPlanilla(state, planilla, acciones)
            }
        }
    }

    if (planilla != null) {
        state.tipoEnSeleccion?.let { tipo ->
            JugadorDelEventoHoja(
                tipo = tipo,
                equipo = planilla.equipo(state.lado),
                onElegir = acciones.onJugador,
                onCerrar = acciones.onCerrarSeleccion,
            )
        }
        if (state.alineacionAbierta) {
            AlineacionHoja(
                planilla = planilla,
                lado = state.ladoAlineacion,
                editable = state.editable,
                enAccion = state.enAccion,
                onLado = acciones.onLadoAlineacion,
                onCambiar = acciones.onCambiarAlineacion,
                onTodos = acciones.onAlinearATodos,
                onCerrar = acciones.onCerrarAlineacion,
            )
        }
        state.eventoPorQuitar?.let { evento ->
            AlertDialog(
                onDismissRequest = acciones.onCancelarQuitar,
                title = { Text(stringResource(R.string.planilla_quitar_titulo)) },
                text = {
                    Text(stringResource(R.string.planilla_quitar_detalle, nombreDeEvento(evento.tipo), evento.jugadorNombre, evento.equipoNombre))
                },
                confirmButton = {
                    TextButton(onClick = acciones.onConfirmarQuitar, modifier = Modifier.testTag("quitar_confirmar")) {
                        Text(stringResource(R.string.planilla_quitar), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = acciones.onCancelarQuitar, modifier = Modifier.testTag("quitar_cancelar")) {
                        Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                    }
                },
            )
        }
        if (state.confirmandoCierre) {
            val marcador = planilla.marcador
            AlertDialog(
                onDismissRequest = acciones.onCancelarCierre,
                title = { Text(stringResource(R.string.planilla_cerrar_titulo, marcador.local, marcador.visitante)) },
                text = { Text(stringResource(R.string.planilla_cerrar_detalle)) },
                confirmButton = {
                    TextButton(onClick = acciones.onConfirmarCierre, modifier = Modifier.testTag("cerrar_confirmar")) {
                        Text(stringResource(R.string.planilla_cerrar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = acciones.onCancelarCierre, modifier = Modifier.testTag("cerrar_cancelar")) {
                        Text(stringResource(R.string.planilla_seguir), color = MaterialTheme.colorScheme.onSurface)
                    }
                },
            )
        }
    }
}

@Composable
private fun textoDeAviso(aviso: AvisoPlanilla): String = when (aviso) {
    is AvisoPlanilla.EventoRegistrado ->
        stringResource(R.string.planilla_aviso_registrado, nombreDeEvento(aviso.evento.tipo), aviso.evento.jugadorNombre)
    AvisoPlanilla.EventoQuitado -> stringResource(R.string.planilla_aviso_quitado)
    AvisoPlanilla.PartidoCerrado -> stringResource(R.string.planilla_aviso_cerrado)
    AvisoPlanilla.SinActualizar -> stringResource(R.string.planilla_aviso_sin_actualizar)
    is AvisoPlanilla.Fallo -> aviso.mensaje
}

@Composable
private fun CuerpoPlanilla(state: PlanillaState, planilla: Planilla, acciones: AccionesPlanilla) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("planilla_lista"),
    ) {
        item(key = "encabezado") { Tablero(planilla, Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)) }

        if (state.editable) {
            item(key = "registro") {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp)) {
                    Text(
                        text = stringResource(R.string.planilla_anotar).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .semantics { heading() },
                    )
                    SelectorDeEquipo(planilla, state.lado, acciones.onLado)
                    TecladoDeEventos(planilla.tiposEvento, habilitado = !state.enAccion, onTipo = acciones.onTipoEvento)
                }
            }
        }

        if (planilla.partido.cruceDefinido) {
            item(key = "alineacion") {
                Fila(
                    titulo = stringResource(R.string.planilla_alineacion),
                    detalle = stringResource(
                        R.string.planilla_alineacion_resumen,
                        planilla.local.alineados, planilla.local.nombre,
                        planilla.visitante.alineados, planilla.visitante.nombre,
                    ),
                    inicio = { IconoEnCirculo(R.drawable.ic_groups) },
                    fin = { FlechaFila() },
                    onClick = acciones.onAbrirAlineacion,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .testTag("planilla_alineacion"),
                )
            }
        }

        item(key = "titulo_eventos") {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.planilla_eventos).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
            }
        }
        if (planilla.eventos.isNotEmpty()) {
            item(key = "lados") { RotulosDeLados(planilla) }
        }
        if (planilla.eventos.isEmpty()) {
            item(key = "sin_eventos") {
                Text(
                    text = stringResource(if (state.editable) R.string.planilla_sin_eventos else R.string.planilla_sin_eventos_lectura),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 20.dp)
                        .testTag("planilla_sin_eventos"),
                )
            }
        } else {
            itemsIndexed(planilla.eventos, key = { _, evento -> evento.id }) { indice, evento ->
                EventoEnLinea(
                    numero = indice + 1,
                    evento = evento,
                    lado = ladoDelEvento(evento, planilla),
                    primero = indice == 0,
                    ultimo = indice == planilla.eventos.lastIndex,
                    sePuedeQuitar = state.editable,
                    habilitado = !state.enAccion,
                    onQuitar = { acciones.onPedirQuitar(evento) },
                )
            }
        }
        item(key = "aire") { Spacer(Modifier.size(24.dp)) }
    }
}

/** Los nombres de los dos equipos sobre la línea: de qué lado va cada uno. */
@Composable
private fun RotulosDeLados(planilla: Planilla) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clearAndSetSemantics { },
    ) {
        Text(
            text = planilla.local.nombre,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        Spacer(Modifier.width(44.dp))
        Text(
            text = planilla.visitante.nombre,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )
    }
}

/** Los tipos de evento del deporte, de a cuatro por fila. Los manda el backend. */
@Composable
private fun TecladoDeEventos(tipos: List<String>, habilitado: Boolean, onTipo: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
        tipos.chunked(4).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { tipo ->
                    Surface(
                        onClick = { onTipo(tipo) },
                        enabled = habilitado,
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 84.dp)
                            .testTag("evento_${tipo.lowercase()}"),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                        ) {
                            MarcaDeEvento(tipo)
                            Text(
                                text = nombreDeEvento(tipo),
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                // Completa la fila para que los botones conserven su ancho.
                repeat(4 - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Barra fija con la acción principal de la planilla. */
@Composable
private fun BarraCerrar(cerrando: Boolean, onCerrar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            BotonPrincipal(
                texto = stringResource(R.string.planilla_cerrar),
                textoEnProceso = stringResource(R.string.planilla_cerrando),
                enProceso = cerrando,
                onClick = onCerrar,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("planilla_cerrar"),
            )
        }
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private fun jugadorDeEjemplo(id: String, nombre: String, numero: Int?, alineado: Boolean = true, apto: Boolean = true) =
    JugadorPlanilla(
        personaId = id, nombre = nombre, numeroCamiseta = numero, numeroDocumento = "10901234$id", esDelegado = false,
        alineado = alineado, goles = 0, apto = apto, motivos = if (apto) emptyList() else listOf("Suspendido por tarjeta roja"),
    )

internal val planillaDeEjemplo = Planilla(
    partido = PartidoArbitro(
        id = "p1", torneoId = "t1", torneo = "Interfacultades Fútbol Sala", deporte = "FUTSAL", fase = "Grupo A · Fecha 3",
        local = EquipoEnPartido("e1", "Sistemas"), visitante = EquipoEnPartido("e2", "Ingeniería Civil"),
        fechaHora = LocalDateTime.of(2026, 11, 12, 16, 0), lugar = "Cancha 2",
        estado = EstadoPartido.EN_CURSO, marcador = Marcador(2, 1), asignado = true,
    ),
    tiposEvento = listOf("GOL", "AMARILLA", "AZUL", "ROJA"),
    local = EquipoPlanilla(
        "e1", "Sistemas",
        listOf(
            jugadorDeEjemplo("1", "Juan Castro", 10),
            jugadorDeEjemplo("2", "Mateo Jiménez", 7),
            jugadorDeEjemplo("3", "Sebastián Pardo", null, alineado = false, apto = false),
        ),
    ),
    visitante = EquipoPlanilla("e2", "Ingeniería Civil", listOf(jugadorDeEjemplo("4", "Nicolás Ortiz", 9))),
    eventos = listOf(
        EventoPartido("ev1", 1, "1", "Juan Castro", "e1", "Sistemas", "GOL"),
        EventoPartido("ev2", 2, "4", "Nicolás Ortiz", "e2", "Ingeniería Civil", "AMARILLA"),
        EventoPartido("ev3", 3, "4", "Nicolás Ortiz", "e2", "Ingeniería Civil", "GOL"),
        EventoPartido("ev4", 4, "2", "Mateo Jiménez", "e1", "Sistemas", "GOL"),
    ),
    editable = true,
)

@Preview(name = "Planilla · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Planilla · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlanillaPreview() {
    DesafioSportsTheme {
        PlanillaContenido(PlanillaState(cargando = false, planilla = planillaDeEjemplo), AccionesPlanilla(), {})
    }
}

@Preview(name = "Planilla · cerrada", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlanillaCerradaPreview() {
    DesafioSportsTheme {
        PlanillaContenido(
            PlanillaState(
                cargando = false,
                planilla = planillaDeEjemplo.copy(
                    partido = planillaDeEjemplo.partido.copy(estado = EstadoPartido.FINALIZADO),
                    editable = false,
                ),
            ),
            AccionesPlanilla(), {},
        )
    }
}
