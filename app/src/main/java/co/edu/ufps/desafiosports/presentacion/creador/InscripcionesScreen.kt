package co.edu.ufps.desafiosports.presentacion.creador

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.Inscripcion
import co.edu.ufps.desafiosports.domain.model.InscripcionesDeTorneo
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import co.edu.ufps.desafiosports.presentacion.comun.Avatar
import co.edu.ufps.desafiosports.presentacion.comun.inicialDe

/** Inscripciones de un torneo que administro: aprobar y rechazar (MOV-0008). */
@Composable
fun InscripcionesScreen(
    onVolver: () -> Unit,
    viewModel: InscripcionesViewModel = viewModel(factory = InscripcionesViewModel.Factory),
) {
    InscripcionesContenido(
        state = viewModel.state,
        hoy = LocalDate.now(),
        onVolver = onVolver,
        onReintentar = viewModel::cargar,
        onRefrescar = viewModel::refrescar,
        onAprobar = viewModel::aprobar,
        onPedirRechazo = viewModel::pedirRechazo,
        onMotivo = viewModel::onMotivoChange,
        onCancelarRechazo = viewModel::cancelarRechazo,
        onConfirmarRechazo = viewModel::confirmarRechazo,
        onAvisoMostrado = viewModel::onAvisoMostrado,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InscripcionesContenido(
    state: InscripcionesState,
    hoy: LocalDate,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onRefrescar: () -> Unit,
    onAprobar: (Inscripcion) -> Unit,
    onPedirRechazo: (Inscripcion) -> Unit,
    onMotivo: (String) -> Unit,
    onCancelarRechazo: () -> Unit,
    onConfirmarRechazo: () -> Unit,
    onAvisoMostrado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    val textoAviso = when (val aviso = state.aviso) {
        is AvisoInscripciones.Aprobada -> stringResource(R.string.inscripciones_aviso_aprobada, aviso.equipo)
        is AvisoInscripciones.Rechazada -> stringResource(R.string.inscripciones_aviso_rechazada, aviso.equipo)
        AvisoInscripciones.SinActualizar -> stringResource(R.string.inscripciones_aviso_sin_actualizar)
        is AvisoInscripciones.Fallo -> aviso.mensaje
        null -> null
    }
    LaunchedEffect(state.aviso) {
        if (state.aviso == null) return@LaunchedEffect
        avisos.showSnackbar(textoAviso.orEmpty())
        onAvisoMostrado()
    }

    val inscripciones = state.inscripciones
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.inscripciones_titulo), maxLines = 1)
                        if (state.torneo.isNotBlank()) {
                            Text(
                                text = state.torneo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("inscripciones_atras")) {
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
            state.cargando && inscripciones == null -> Cargando(Modifier.padding(relleno))

            state.error != null && inscripciones == null ->
                if (state.sinPermiso) {
                    EstadoError(
                        state.error, onVolver, Modifier.padding(relleno),
                        etiquetaBoton = stringResource(R.string.accion_volver), testTagBoton = "inscripciones_volver",
                    )
                } else {
                    EstadoError(state.error, onReintentar, Modifier.padding(relleno), testTagBoton = "inscripciones_reintentar")
                }

            inscripciones != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                if (inscripciones.vacio) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("inscripciones_vacio"),
                    ) {
                        EstadoVacio(
                            icono = R.drawable.ic_inbox,
                            titulo = stringResource(R.string.inscripciones_vacio_titulo),
                            detalle = stringResource(R.string.inscripciones_vacio_detalle),
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("inscripciones_lista"),
                    ) {
                        bloque(R.string.inscripciones_por_aprobar, inscripciones.porAprobar) { inscripcion ->
                            FilaPorAprobar(inscripcion, hoy, ocupada = state.enAccion == inscripcion.id, habilitada = state.enAccion == null, onAprobar, onPedirRechazo)
                        }
                        bloque(R.string.inscripciones_aprobadas, inscripciones.aprobadas) { inscripcion ->
                            Fila(
                                titulo = inscripcion.equipo,
                                inicio = { Avatar(inicialDe(inscripcion.equipo)) },
                                detalle = inscripcion.delegado?.let { stringResource(R.string.inscripciones_delegado, it) },
                                fin = { ChipEstado(stringResource(R.string.inscripcion_aprobada), tono = Tono.EXITO) },
                                modifier = Modifier.testTag("inscripcion_aprobada"),
                            )
                        }
                        bloque(R.string.inscripciones_rechazadas, inscripciones.rechazadas) { inscripcion ->
                            val expulsado = inscripcion.estado == EstadoInscripcion.EXPULSADO
                            Fila(
                                titulo = inscripcion.equipo,
                                inicio = { Avatar(inicialDe(inscripcion.equipo)) },
                                detalle = inscripcion.motivoRechazo?.let { stringResource(R.string.inscripciones_motivo, it) }
                                    ?: inscripcion.delegado?.let { stringResource(R.string.inscripciones_delegado, it) },
                                fin = {
                                    ChipEstado(
                                        stringResource(if (expulsado) R.string.inscripcion_expulsado else R.string.inscripcion_rechazada),
                                        tono = Tono.ERROR,
                                    )
                                },
                                modifier = Modifier.testTag("inscripcion_rechazada"),
                            )
                        }
                    }
                }
            }
        }
    }

    state.rechazando?.let { inscripcion ->
        AlertDialog(
            onDismissRequest = onCancelarRechazo,
            title = { Text(stringResource(R.string.inscripciones_rechazar_titulo, inscripcion.equipo)) },
            text = {
                OutlinedTextField(
                    value = state.motivo,
                    onValueChange = onMotivo,
                    label = { Text(stringResource(R.string.inscripciones_motivo_etiqueta)) },
                    isError = state.errorMotivo != null,
                    supportingText = { Text(state.errorMotivo ?: stringResource(R.string.inscripciones_motivo_ayuda)) },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rechazo_motivo"),
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmarRechazo, modifier = Modifier.testTag("rechazo_confirmar")) {
                    Text(stringResource(R.string.inscripciones_rechazar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarRechazo, modifier = Modifier.testTag("rechazo_cancelar")) {
                    Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}

private fun LazyListScope.bloque(
    titulo: Int,
    inscripciones: List<Inscripcion>,
    fila: @Composable (Inscripcion) -> Unit,
) {
    if (inscripciones.isEmpty()) return
    item(key = "titulo_$titulo") { EncabezadoBloque(stringResource(titulo)) }
    itemsIndexed(inscripciones, key = { _, inscripcion -> "${titulo}_${inscripcion.id}" }) { indice, inscripcion ->
        if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 72.dp, end = 16.dp))
        fila(inscripcion)
    }
}

@Composable
private fun FilaPorAprobar(
    inscripcion: Inscripcion,
    hoy: LocalDate,
    ocupada: Boolean,
    habilitada: Boolean,
    onAprobar: (Inscripcion) -> Unit,
    onPedirRechazo: (Inscripcion) -> Unit,
) {
    val detalle = listOfNotNull(
        inscripcion.delegado?.let { stringResource(R.string.inscripciones_delegado, it) },
        inscripcion.fecha?.let { Formatos.fecha(it.toLocalDate(), hoy) },
        inscripcion.monto?.takeIf { it.signum() > 0 }?.let { Formatos.pesos(it) },
    ).joinToString(" · ")
    Column(Modifier.testTag("inscripcion_pendiente")) {
        Fila(titulo = inscripcion.equipo, detalle = detalle.ifBlank { null }, inicio = { Avatar(inicialDe(inscripcion.equipo)) })
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, end = 16.dp, bottom = 14.dp),
        ) {
            if (ocupada) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            BotonContorno(
                texto = stringResource(R.string.inscripciones_rechazar),
                onClick = { onPedirRechazo(inscripcion) },
                habilitado = habilitada,
                modifier = Modifier.testTag("inscripcion_rechazar"),
            )
            // Tonal y no naranja: hay un "Aprobar" por fila, y el naranja es de una sola acción.
            FilledTonalButton(
                onClick = { onAprobar(inscripcion) },
                enabled = habilitada,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("inscripcion_aprobar"),
            ) {
                Text(stringResource(R.string.inscripciones_aprobar), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 9)

private fun inscripcionDeEjemplo(id: String, equipo: String, estado: EstadoInscripcion, motivo: String? = null) = Inscripcion(
    id = id,
    equipo = equipo,
    delegado = "Andrés Gómez",
    estado = estado,
    motivoRechazo = motivo,
    fecha = LocalDateTime.of(2026, 11, 7, 10, 0),
    monto = BigDecimal("50000"),
)

private val inscripcionesDeEjemplo = InscripcionesDeTorneo(
    porAprobar = listOf(
        inscripcionDeEjemplo("1", "Halcones FC", EstadoInscripcion.PENDIENTE_PAGO),
        inscripcionDeEjemplo("2", "Real Código de Ingeniería de Sistemas", EstadoInscripcion.PENDIENTE_PAGO),
    ),
    aprobadas = listOf(inscripcionDeEjemplo("3", "Tiburones FC", EstadoInscripcion.APROBADO)),
    rechazadas = listOf(inscripcionDeEjemplo("4", "Pumas FC", EstadoInscripcion.RECHAZADO, "El comprobante no corresponde a este torneo.")),
)

@Preview(name = "Inscripciones · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Inscripciones · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InscripcionesPreview() {
    DesafioSportsTheme {
        InscripcionesContenido(
            InscripcionesState(torneo = "Copa Prueba 2026", cargando = false, inscripciones = inscripcionesDeEjemplo, enAccion = "1"),
            HOY, {}, {}, {}, {}, {}, {}, {}, {}, {},
        )
    }
}

@Preview(name = "Inscripciones · rechazar", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InscripcionesRechazarPreview() {
    DesafioSportsTheme {
        InscripcionesContenido(
            InscripcionesState(
                torneo = "Copa Prueba 2026", cargando = false, inscripciones = inscripcionesDeEjemplo,
                rechazando = inscripcionesDeEjemplo.porAprobar.first(), errorMotivo = InscripcionesViewModel.MENSAJE_MOTIVO,
            ),
            HOY, {}, {}, {}, {}, {}, {}, {}, {}, {},
        )
    }
}
