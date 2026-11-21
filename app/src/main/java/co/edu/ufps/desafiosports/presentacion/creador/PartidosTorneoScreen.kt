package co.edu.ufps.desafiosports.presentacion.creador

import java.time.ZoneOffset
import java.time.LocalTime
import java.time.Instant
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.annotation.DrawableRes
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.BloqueoDePartidos
import co.edu.ufps.desafiosports.domain.model.EstadoDePartidos
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FasePartido
import co.edu.ufps.desafiosports.domain.model.GrupoDePartidos
import co.edu.ufps.desafiosports.domain.model.PartidoDeTorneo
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.BotonPrimario
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.time.LocalDate
import java.time.LocalDateTime

/** Lo que la pantalla de partidos le pide al ViewModel. */
data class AccionesPartidos(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onGenerar: () -> Unit = {},
    val onCancelarGenerar: () -> Unit = {},
    val onConfirmarGenerar: () -> Unit = {},
    val onAbrirPartido: (PartidoDeTorneo) -> Unit = {},
    val onCerrarAsignacion: () -> Unit = {},
    val onAlternarArbitro: (ArbitroDeTorneo) -> Unit = {},
    val onReintentarArbitros: () -> Unit = {},
    val onInvitarArbitros: () -> Unit = {},
    /** Quien organiza el torneo opera la planilla de cualquier partido (MOV-0012). */
    val onAbrirPlanilla: (partidoId: String) -> Unit = {},
    // Programar y volver a generar (MOV-0013)
    val onProgramar: (PartidoDeTorneo) -> Unit = {},
    val onFechaProgramacion: (LocalDate) -> Unit = {},
    val onHoraProgramacion: (LocalTime) -> Unit = {},
    val onLugarProgramacion: (String) -> Unit = {},
    val onGuardarProgramacion: () -> Unit = {},
    val onCancelarProgramacion: () -> Unit = {},
    val onRegenerar: () -> Unit = {},
    val onCancelarRegenerar: () -> Unit = {},
    val onConfirmarRegenerar: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
)

/** Partidos de un torneo que administro: generarlos y ponerles árbitros (MOV-0011). */
@Composable
fun PartidosTorneoScreen(
    onVolver: () -> Unit,
    onInvitarArbitros: () -> Unit,
    onAbrirPlanilla: (partidoId: String) -> Unit,
    viewModel: PartidosTorneoViewModel = viewModel(factory = PartidosTorneoViewModel.Factory),
) {
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    val acciones = remember(viewModel, onInvitarArbitros, onAbrirPlanilla) {
        AccionesPartidos(
            onReintentar = viewModel::cargar,
            onRefrescar = viewModel::refrescar,
            onGenerar = viewModel::pedirGenerar,
            onCancelarGenerar = viewModel::cancelarGenerar,
            onConfirmarGenerar = viewModel::confirmarGenerar,
            onAbrirPartido = viewModel::abrirAsignacion,
            onCerrarAsignacion = viewModel::cerrarAsignacion,
            onAlternarArbitro = viewModel::alternarArbitro,
            onReintentarArbitros = viewModel::cargarArbitros,
            onInvitarArbitros = {
                viewModel.cerrarAsignacion()
                onInvitarArbitros()
            },
            onAbrirPlanilla = { partidoId ->
                viewModel.cerrarAsignacion()
                onAbrirPlanilla(partidoId)
            },
            onProgramar = viewModel::abrirProgramacion,
            onFechaProgramacion = viewModel::onFechaProgramacion,
            onHoraProgramacion = viewModel::onHoraProgramacion,
            onLugarProgramacion = viewModel::onLugarProgramacion,
            onGuardarProgramacion = viewModel::guardarProgramacion,
            onCancelarProgramacion = viewModel::cancelarProgramacion,
            onRegenerar = viewModel::pedirRegenerar,
            onCancelarRegenerar = viewModel::cancelarRegenerar,
            onConfirmarRegenerar = viewModel::confirmarRegenerar,
            onAvisoMostrado = viewModel::onAvisoMostrado,
        )
    }
    PartidosTorneoContenido(state = viewModel.state, hoy = LocalDate.now(), acciones = acciones, onVolver = onVolver)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PartidosTorneoContenido(
    state: PartidosState,
    hoy: LocalDate,
    acciones: AccionesPartidos,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        avisos.showSnackbar(aviso)
        acciones.onAvisoMostrado()
    }

    val estado = state.estado
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { TituloConTorneo(stringResource(R.string.gestion_partidos), state.torneo) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("partidos_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(avisos) },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        when {
            state.cargando && estado == null -> Cargando(Modifier.padding(relleno))

            state.error != null && estado == null ->
                if (state.sinPermiso) {
                    EstadoError(
                        state.error, onVolver, Modifier.padding(relleno),
                        etiquetaBoton = stringResource(R.string.accion_volver), testTagBoton = "partidos_volver",
                    )
                } else {
                    EstadoError(state.error, acciones.onReintentar, Modifier.padding(relleno), testTagBoton = "partidos_reintentar")
                }

            estado != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = acciones.onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                if (state.partidos.isEmpty()) {
                    SinPartidos(estado, state.generando, acciones.onGenerar)
                } else {
                    val grupos = state.grupos
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("partidos_lista"),
                    ) {
                        grupos.forEach { grupo ->
                            item(key = "titulo_${grupo.clave}") { EncabezadoBloque(tituloDeGrupo(grupo)) }
                            grupo.partidos.forEachIndexed { indice, partido ->
                                item(key = "partido_${partido.id}") {
                                    if (indice > 0) {
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                    }
                                    FilaPartido(
                                        partido, hoy, sinArbitro = state.sinArbitro(partido),
                                        onAbrir = acciones.onAbrirPartido, onPlanilla = acciones.onAbrirPlanilla,
                                    )
                                }
                            }
                        }
                        item(key = "regenerar") {
                            PieRegenerar(sePuede = state.sePuedenRegenerar, generando = state.generando, onRegenerar = acciones.onRegenerar)
                        }
                    }
                }
            }
        }
    }

    if (state.confirmandoGenerar) {
        AlertDialog(
            onDismissRequest = acciones.onCancelarGenerar,
            // El diálogo es otra ventana: sin esto sus testTag no se ven desde fuera.
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            title = { Text(stringResource(R.string.partidos_confirmar_titulo)) },
            text = { Text(stringResource(R.string.partidos_confirmar_detalle)) },
            confirmButton = {
                TextButton(onClick = acciones.onConfirmarGenerar, modifier = Modifier.testTag("partidos_generar_confirmar")) {
                    Text(stringResource(R.string.partidos_confirmar))
                }
            },
            dismissButton = {
                TextButton(onClick = acciones.onCancelarGenerar) {
                    Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }

    if (state.confirmandoRegenerar) {
        AlertDialog(
            onDismissRequest = acciones.onCancelarRegenerar,
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            title = { Text(stringResource(R.string.partidos_regenerar_titulo)) },
            text = { Text(stringResource(R.string.partidos_regenerar_aviso)) },
            confirmButton = {
                TextButton(onClick = acciones.onConfirmarRegenerar, modifier = Modifier.testTag("partidos_regenerar_confirmar")) {
                    // Borra lo que hay: va en el color de error, no en naranja.
                    Text(stringResource(R.string.partidos_regenerar_confirmar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = acciones.onCancelarRegenerar, modifier = Modifier.testTag("partidos_regenerar_cancelar")) {
                    Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }

    state.partidoAsignando?.let { partido -> HojaDeArbitros(partido, state, acciones) }
    state.partidoProgramando?.let { partido -> DialogoProgramacion(partido, state, hoy, acciones) }
}

/**
 * Al final de la lista (MOV-0013): volver a generar los partidos mientras ninguno se haya jugado.
 * Cuando ya no se puede, se dice por qué.
 */
@Composable
private fun PieRegenerar(sePuede: Boolean, generando: Boolean, onRegenerar: () -> Unit) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
    )
    if (sePuede) {
        Fila(
            titulo = stringResource(if (generando) R.string.partidos_regenerando else R.string.partidos_regenerar),
            detalle = stringResource(R.string.partidos_regenerar_detalle),
            onClick = if (generando) null else onRegenerar,
            modifier = Modifier.testTag("partidos_regenerar"),
        )
    } else {
        Text(
            text = stringResource(R.string.partidos_regenerar_bloqueado),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(16.dp)
                .testTag("partidos_regenerar_bloqueado"),
        )
    }
}

/** "Sáb 21 · 3:30 p. m. · Cancha 2", con "Sin fecha" y "Sin lugar" cuando falta algo. */
@Composable
private fun textoDeProgramacion(partido: PartidoDeTorneo, hoy: LocalDate): String = listOf(
    partido.fecha?.let { "${Formatos.fecha(it.toLocalDate(), hoy)} · ${Formatos.hora(it)}" } ?: stringResource(R.string.fecha_sin_fecha),
    partido.lugar ?: stringResource(R.string.programar_sin_lugar),
).joinToString(" · ")

/**
 * Fecha, hora y lugar de un partido (MOV-0013). El día y la hora se eligen con los selectores de
 * Material; el lugar se escribe. El error va dentro del diálogo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun DialogoProgramacion(partido: PartidoDeTorneo, state: PartidosState, hoy: LocalDate, acciones: AccionesPartidos) {
    val borrador = state.borradorProgramacion
    val guardando = state.guardandoProgramacion
    var eligiendoFecha by remember { mutableStateOf(false) }
    var eligiendoHora by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = acciones.onCancelarProgramacion,
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("dialogo_programar"),
        title = { Text(stringResource(R.string.programar_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = cruceDe(partido),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CampoQueAbre(
                    etiqueta = stringResource(R.string.programar_dia),
                    valor = borrador.fecha?.let { Formatos.fecha(it, hoy) } ?: stringResource(R.string.programar_elegir_dia),
                    icono = R.drawable.ic_event,
                    habilitado = !guardando,
                    onClick = { eligiendoFecha = true },
                    modifier = Modifier.testTag("programar_fecha"),
                )
                CampoQueAbre(
                    etiqueta = stringResource(R.string.programar_hora),
                    valor = borrador.hora?.let { Formatos.hora(it) } ?: stringResource(R.string.programar_elegir_hora),
                    icono = R.drawable.ic_schedule,
                    habilitado = !guardando,
                    onClick = { eligiendoHora = true },
                    modifier = Modifier.testTag("programar_hora"),
                )
                OutlinedTextField(
                    value = borrador.lugar,
                    onValueChange = acciones.onLugarProgramacion,
                    label = { Text(stringResource(R.string.programar_lugar)) },
                    placeholder = { Text(stringResource(R.string.programar_lugar_ejemplo)) },
                    singleLine = true,
                    enabled = !guardando,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("programar_lugar"),
                )
                state.errorProgramacion?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("programar_error"),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = acciones.onGuardarProgramacion, enabled = !guardando, modifier = Modifier.testTag("programar_guardar")) {
                Text(stringResource(if (guardando) R.string.programar_guardando else R.string.programar_guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = acciones.onCancelarProgramacion, enabled = !guardando) {
                Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )

    if (eligiendoFecha) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = (borrador.fecha ?: hoy).aMillisUtc())
        DatePickerDialog(
            onDismissRequest = { eligiendoFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let { acciones.onFechaProgramacion(it.aFechaUtc()) }
                        eligiendoFecha = false
                    },
                    enabled = estado.selectedDateMillis != null,
                    modifier = Modifier.testTag("programar_fecha_aceptar"),
                ) { Text(stringResource(R.string.accion_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { eligiendoFecha = false }) {
                    Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        ) {
            DatePicker(state = estado)
        }
    }

    if (eligiendoHora) {
        val inicial = borrador.hora ?: LocalTime.of(HORA_POR_DEFECTO, 0)
        val estado = rememberTimePickerState(initialHour = inicial.hour, initialMinute = inicial.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { eligiendoHora = false },
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            title = { Text(stringResource(R.string.programar_hora)) },
            text = { TimePicker(state = estado) },
            confirmButton = {
                TextButton(
                    onClick = {
                        acciones.onHoraProgramacion(LocalTime.of(estado.hour, estado.minute))
                        eligiendoHora = false
                    },
                    modifier = Modifier.testTag("programar_hora_aceptar"),
                ) { Text(stringResource(R.string.accion_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { eligiendoHora = false }) {
                    Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}

/** Un campo que no se escribe: tocarlo abre un selector. */
@Composable
private fun CampoQueAbre(
    etiqueta: String,
    valor: String,
    @DrawableRes icono: Int,
    habilitado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            onClick = onClick,
            enabled = habilitado,
            shape = MaterialTheme.shapes.small,
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$etiqueta: $valor" },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Text(valor, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(painterResource(icono), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** La hora que propone el selector cuando el partido aún no tiene una. */
private const val HORA_POR_DEFECTO = 8

/** El selector de fechas de Material trabaja en milisegundos UTC. */
private fun LocalDate.aMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.aFechaUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/** El torneo aún no tiene partidos: cuántos equipos hay y el botón para generarlos. */
@Composable
private fun SinPartidos(estado: EstadoDePartidos, generando: Boolean, onGenerar: () -> Unit) {
    val motivo = when (val bloqueo = estado.bloqueo) {
        is BloqueoDePartidos.FaltanEquipos -> stringResource(R.string.partidos_faltan_equipos, bloqueo.aprobados)
        BloqueoDePartidos.TorneoEnBorrador -> stringResource(R.string.partidos_torneo_borrador)
        BloqueoDePartidos.TorneoFinalizado -> stringResource(R.string.partidos_torneo_finalizado)
        null -> null
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("partidos_vacio"),
    ) {
        EstadoVacio(
            icono = R.drawable.ic_event,
            titulo = stringResource(R.string.partidos_vacio_titulo),
            detalle = stringResource(R.string.partidos_vacio_detalle),
            modifier = Modifier.padding(top = 48.dp),
        )
        Text(
            text = motivo ?: pluralStringResource(R.plurals.partidos_equipos_aprobados, estado.equiposAprobados, estado.equiposAprobados),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .testTag("partidos_motivo"),
        )
        Spacer(Modifier.height(20.dp))
        BotonPrimario(
            texto = stringResource(R.string.partidos_generar),
            onClick = onGenerar,
            habilitado = estado.puedeGenerar,
            enProceso = generando,
            textoEnProceso = stringResource(R.string.partidos_generando),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .testTag("partidos_generar"),
        )
    }
}

@Composable
private fun tituloDeGrupo(grupo: GrupoDePartidos): String {
    val jornada = grupo.jornada
    val letra = grupo.grupo
    return when (grupo.fase) {
        FasePartido.GRUPOS -> when {
            letra != null && jornada != null -> stringResource(R.string.partidos_grupo_jornada, letra, jornada)
            jornada != null -> stringResource(R.string.partidos_jornada, jornada)
            letra != null -> stringResource(R.string.partidos_grupo, letra)
            else -> stringResource(R.string.partidos_fase_grupos)
        }
        FasePartido.REPECHAJE -> stringResource(R.string.fase_repechaje)
        FasePartido.OCTAVOS -> stringResource(R.string.fase_octavos)
        FasePartido.CUARTOS -> stringResource(R.string.fase_cuartos)
        FasePartido.SEMIS -> stringResource(R.string.fase_semis)
        FasePartido.TERCER_PUESTO -> stringResource(R.string.fase_tercer_puesto)
        FasePartido.FINAL -> stringResource(R.string.fase_final)
        // Una fase que la app no conoce se muestra legible: "FASE_NUEVA" → "Fase nueva".
        FasePartido.DESCONOCIDA -> grupo.faseCodigo.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
            .ifBlank { stringResource(R.string.gestion_partidos) }
    }
}

@Composable
private fun cruceDe(partido: PartidoDeTorneo): String {
    val porDefinir = stringResource(R.string.partido_equipo_por_definir)
    return stringResource(R.string.partido_cruce, partido.local ?: porDefinir, partido.visitante ?: porDefinir)
}

@Composable
private fun FilaPartido(
    partido: PartidoDeTorneo,
    hoy: LocalDate,
    sinArbitro: Boolean,
    onAbrir: (PartidoDeTorneo) -> Unit,
    onPlanilla: (partidoId: String) -> Unit,
) {
    val cuando = partido.fecha?.let { "${Formatos.fecha(it.toLocalDate(), hoy)} · ${Formatos.hora(it)}" }
    val detalle = listOfNotNull(
        cuando ?: stringResource(R.string.fecha_sin_fecha),
        partido.lugar,
        partido.arbitros.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.nombre },
    ).joinToString(" · ")
    val (chip, tono) = when {
        partido.descansa -> stringResource(R.string.partido_descansa) to Tono.NEUTRO
        partido.estado == EstadoPartido.FINALIZADO ->
            stringResource(R.string.partido_marcador, partido.golesLocal, partido.golesVisitante) to Tono.NEUTRO
        partido.estado == EstadoPartido.WO -> stringResource(R.string.partido_wo) to Tono.NEUTRO
        partido.estado == EstadoPartido.EN_CURSO -> stringResource(R.string.partido_en_juego) to Tono.EXITO
        partido.estado == EstadoPartido.APLAZADO -> stringResource(R.string.partido_aplazado) to Tono.ADVERTENCIA
        sinArbitro -> stringResource(R.string.partido_sin_arbitro) to Tono.ADVERTENCIA
        else -> null to Tono.NEUTRO
    }
    Fila(
        titulo = cruceDe(partido),
        detalle = detalle,
        fin = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (chip != null) ChipEstado(chip, tono = tono)
                if (partido.admiteArbitros || partido.tienePlanilla) FlechaFila()
            }
        },
        // Abierto: la hoja con sus árbitros y su planilla. Cerrado ya no admite árbitros: va
        // directo a la planilla, que quien organiza puede reabrir (MOV-0012).
        onClick = when {
            partido.admiteArbitros -> ({ onAbrir(partido) })
            partido.tienePlanilla -> ({ onPlanilla(partido.id) })
            else -> null
        },
        modifier = Modifier.testTag("partido"),
    )
}

/**
 * Hoja para poner árbitros a un partido. Cada casilla cambia al tocarla; el error va dentro de
 * la hoja porque los avisos de la pantalla quedan tapados por ella.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun HojaDeArbitros(partido: PartidoDeTorneo, state: PartidosState, acciones: AccionesPartidos) {
    ModalBottomSheet(
        onDismissRequest = acciones.onCerrarAsignacion,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        // La hoja es otra ventana: sin esto sus testTag no se ven desde fuera.
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("hoja_arbitros"),
    ) {
        Text(
            text = stringResource(R.string.partido_hoja_titulo).uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .semantics { heading() },
        )
        Text(
            text = cruceDe(partido),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
        )
        if (state.cargandoArbitros || state.enviando.isNotEmpty()) {
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }
        state.errorAsignacion?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .testTag("asignar_error"),
            )
        }
        val arbitros = state.arbitros
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            if (partido.tienePlanilla) {
                Fila(
                    titulo = stringResource(R.string.partido_abrir_planilla),
                    detalle = stringResource(R.string.partido_abrir_planilla_detalle),
                    fin = { FlechaFila() },
                    onClick = { acciones.onAbrirPlanilla(partido.id) },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .testTag("partido_planilla"),
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            Fila(
                titulo = stringResource(R.string.programar_fila),
                detalle = textoDeProgramacion(partido, LocalDate.now()),
                fin = { FlechaFila() },
                onClick = { acciones.onProgramar(partido) },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .testTag("partido_programar"),
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
            Text(
                text = stringResource(R.string.asignar_titulo),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp)
                    .semantics { heading() },
            )
            when {
                arbitros == null -> if (!state.cargandoArbitros) {
                    BotonContorno(
                        stringResource(R.string.accion_reintentar), acciones.onReintentarArbitros,
                        Modifier
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .testTag("asignar_reintentar"),
                    )
                }

                arbitros.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.asignar_sin_arbitros),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                    BotonContorno(
                        stringResource(R.string.asignar_invitar), acciones.onInvitarArbitros,
                        Modifier
                            .padding(horizontal = 24.dp)
                            .testTag("asignar_invitar"),
                    )
                }

                else -> arbitros.forEach { arbitro ->
                    val asignado = arbitro.todosLosPartidos || partido.arbitros.any { it.cuentaId == arbitro.cuentaId }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .toggleable(
                                value = asignado,
                                // Quien arbitra todos los partidos no se asigna uno por uno.
                                enabled = !arbitro.todosLosPartidos,
                                role = Role.Checkbox,
                                onValueChange = { acciones.onAlternarArbitro(arbitro) },
                            )
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                            .testTag("asignar_arbitro"),
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(arbitro.nombre, style = MaterialTheme.typography.bodyLarge)
                            if (arbitro.todosLosPartidos) {
                                Text(
                                    stringResource(R.string.arbitro_todos),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Checkbox(checked = asignado, onCheckedChange = null, enabled = !arbitro.todosLosPartidos)
                    }
                }
            }
        }
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 16)

private fun partidoDeEjemplo(
    id: String,
    local: String?,
    visitante: String?,
    fase: FasePartido = FasePartido.GRUPOS,
    jornada: Int? = 1,
    estado: EstadoPartido = EstadoPartido.PROGRAMADO,
    arbitros: List<ArbitroAsignado> = emptyList(),
) = PartidoDeTorneo(
    id = id, fecha = LocalDateTime.of(2026, 11, 21, 8, 0), estado = estado, estadoCodigo = estado.name,
    local = local, visitante = visitante, golesLocal = 3, golesVisitante = 1,
    fase = fase, faseCodigo = fase.name, jornada = jornada, grupo = null, lugar = "Cancha 1", arbitros = arbitros,
)

private val partidosDeEjemplo = listOf(
    partidoDeEjemplo("1", "Halcones FC", "Tiburones FC", estado = EstadoPartido.FINALIZADO),
    partidoDeEjemplo("2", "Real Código de Ingeniería de Sistemas", "Pumas FC", arbitros = listOf(ArbitroAsignado("c1", "Ana Ruiz"))),
    partidoDeEjemplo("3", "Halcones FC", "Pumas FC", jornada = 2),
    partidoDeEjemplo("4", null, null, fase = FasePartido.FINAL, jornada = 3),
)

@Preview(name = "Partidos · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Partidos · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PartidosPreview() {
    DesafioSportsTheme {
        PartidosTorneoContenido(
            PartidosState(
                "Copa Prueba 2026", cargando = false,
                estado = EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true),
                partidos = partidosDeEjemplo, arbitros = emptyList(),
            ),
            HOY, AccionesPartidos(), {},
        )
    }
}

@Preview(name = "Partidos · sin generar", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PartidosVacioPreview() {
    DesafioSportsTheme {
        PartidosTorneoContenido(
            PartidosState("Copa Prueba 2026", cargando = false, estado = EstadoDePartidos(EstadoTorneo.PUBLICADO, 1, generados = false)),
            HOY, AccionesPartidos(), {},
        )
    }
}
