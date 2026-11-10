package co.edu.ufps.desafiosports.presentacion.explorar

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.TorneoResumen
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.CifraDeCupos
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.ui.deporte.iconoDeDeporte
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.time.LocalDate

/** Lo que Explorar le pide al ViewModel. */
data class AccionesExplorar(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onDeporte: (String?) -> Unit = {},
    val onBusqueda: (String) -> Unit = {},
    val onCargarMas: () -> Unit = {},
    val onAbrirTorneo: (String) -> Unit = {},
    val onPedirEnlace: () -> Unit = {},
    val onEnlace: (String) -> Unit = {},
    val onAbrirEnlace: () -> Unit = {},
    val onCancelarEnlace: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
)

/** Explorar torneos públicos (MOV-0005). */
@Composable
fun ExplorarScreen(
    onVolver: () -> Unit,
    onAbrirTorneo: (torneoId: String, enlaceToken: String) -> Unit,
    viewModel: ExplorarViewModel = viewModel(factory = ExplorarViewModel.Factory),
) {
    val state = viewModel.state
    LaunchedEffect(state.torneoPorAbrir) {
        val porAbrir = state.torneoPorAbrir ?: return@LaunchedEffect
        onAbrirTorneo(porAbrir.torneoId, porAbrir.enlaceToken)
        viewModel.onTorneoAbierto()
    }
    val acciones = remember(viewModel, onAbrirTorneo) {
        AccionesExplorar(
            onReintentar = viewModel::cargar,
            onRefrescar = viewModel::refrescar,
            onDeporte = viewModel::elegirDeporte,
            onBusqueda = viewModel::onBusquedaChange,
            onCargarMas = viewModel::cargarMas,
            onAbrirTorneo = { onAbrirTorneo(it, "") },
            onPedirEnlace = viewModel::pedirEnlace,
            onEnlace = viewModel::onEnlaceChange,
            onAbrirEnlace = viewModel::abrirEnlace,
            onCancelarEnlace = viewModel::cancelarEnlace,
            onAvisoMostrado = viewModel::onAvisoMostrado,
        )
    }
    ExplorarContenido(state = state, hoy = LocalDate.now(), acciones = acciones, onVolver = onVolver)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ExplorarContenido(state: ExplorarState, hoy: LocalDate, acciones: AccionesExplorar, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        avisos.showSnackbar(aviso)
        acciones.onAvisoMostrado()
    }
    val foco = LocalFocusManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.explorar_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("explorar_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                actions = {
                    IconButton(onClick = acciones.onPedirEnlace, modifier = Modifier.testTag("explorar_enlace")) {
                        Icon(painterResource(R.drawable.ic_link), contentDescription = stringResource(R.string.explorar_tengo_enlace))
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
        Column(
            Modifier
                .padding(relleno)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = state.busqueda,
                onValueChange = acciones.onBusqueda,
                placeholder = { Text(stringResource(R.string.explorar_buscar)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                trailingIcon = if (state.busqueda.isNotEmpty()) {
                    {
                        IconButton(onClick = { acciones.onBusqueda("") }) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.explorar_borrar_busqueda))
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("explorar_buscar"),
            )
            ChipsDeDeporte(state.deportes, state.deporte, acciones.onDeporte)
            Box(Modifier.weight(1f)) {
                val torneos = state.torneos
                when {
                    state.cargando && torneos == null -> Cargando()
                    state.error != null && torneos == null -> EstadoError(state.error, acciones.onReintentar, testTagBoton = "explorar_reintentar")
                    torneos != null -> PullToRefreshBox(
                        isRefreshing = state.refrescando,
                        onRefresh = acciones.onRefrescar,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (torneos.isEmpty()) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .testTag("explorar_vacio"),
                            ) {
                                EstadoVacio(
                                    icono = R.drawable.ic_search,
                                    titulo = stringResource(if (state.conFiltro) R.string.explorar_sin_resultados else R.string.explorar_vacio),
                                    detalle = stringResource(R.string.explorar_vacio_detalle),
                                    accion = { BotonContorno(stringResource(R.string.explorar_tengo_enlace), acciones.onPedirEnlace) },
                                    modifier = Modifier.padding(top = 24.dp),
                                )
                            }
                        } else {
                            ListaDeTorneos(torneos, state, hoy, acciones)
                        }
                    }
                }
            }
        }
    }

    if (state.pidiendoEnlace) DialogoDeEnlace(state, acciones)
}

@Composable
private fun ChipsDeDeporte(deportes: List<Deporte>, elegido: String?, onDeporte: (String?) -> Unit) {
    if (deportes.isEmpty()) return
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("explorar_deportes"),
    ) {
        item(key = "todos") {
            ChipDeporte(stringResource(R.string.explorar_todos), null, elegido == null) { onDeporte(null) }
        }
        items(deportes, key = { it.codigo }) { deporte ->
            ChipDeporte(deporte.nombre, iconoDeDeporte(deporte.codigo), elegido == deporte.codigo) { onDeporte(deporte.codigo) }
        }
    }
}

@Composable
private fun ChipDeporte(texto: String, icono: Int?, elegido: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = elegido,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = icono?.let { { Icon(painterResource(it), contentDescription = null, modifier = Modifier.size(18.dp)) } },
        shape = MaterialTheme.shapes.extraLarge,
        // Elegido sube a tinta: el naranja queda para la acción principal.
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.surface,
            selectedLeadingIconColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.heightIn(min = 40.dp),
    )
}

@Composable
private fun ListaDeTorneos(torneos: List<TorneoResumen>, state: ExplorarState, hoy: LocalDate, acciones: AccionesExplorar) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("explorar_lista"),
    ) {
        itemsIndexed(torneos, key = { _, t -> t.id }) { indice, torneo ->
            if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 70.dp, end = 16.dp))
            FilaTorneo(torneo, hoy) { acciones.onAbrirTorneo(torneo.id) }
        }
        if (state.hayMas) {
            item(key = "mas") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    if (state.cargandoMas) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                    } else {
                        BotonContorno(stringResource(R.string.explorar_cargar_mas), acciones.onCargarMas, Modifier.testTag("explorar_mas"))
                    }
                }
            }
        }
    }
}

/** Un torneo de Explorar: deporte, nombre, estado, la fecha que importa y los cupos ocupados. */
@Composable
internal fun FilaTorneo(torneo: TorneoResumen, hoy: LocalDate, onClick: () -> Unit) {
    val cupos = torneo.cupos
    val lecturaCupos = if (cupos != null) {
        stringResource(R.string.creador_inscritos_de_cupos, torneo.inscritos, cupos)
    } else {
        pluralStringResource(R.plurals.creador_inscritos, torneo.inscritos, torneo.inscritos)
    }
    val fecha = when {
        torneo.inscripcionesAbiertas && torneo.cierreInscripciones != null ->
            stringResource(R.string.torneo_cierra, Formatos.fecha(torneo.cierreInscripciones, hoy))
        torneo.inicio != null && torneo.estado != EstadoTorneo.FINALIZADO ->
            stringResource(if (torneo.inicio.isAfter(hoy)) R.string.torneo_empieza else R.string.torneo_empezo, Formatos.fecha(torneo.inicio, hoy))
        else -> null
    }
    val detalle = listOfNotNull(torneo.deporteNombre.takeIf { it.isNotBlank() }, fecha).joinToString(" · ")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("explorar_torneo"),
    ) {
        IconoEnCirculo(iconoDeDeporte(torneo.deporte))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(torneo.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (detalle.isNotBlank()) {
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            ChipDeEstadoTorneo(torneo.estado, torneo.inscripcionesAbiertas)
        }
        CifraDeCupos(torneo.inscritos, cupos, lecturaCupos)
    }
}

@Composable
private fun DialogoDeEnlace(state: ExplorarState, acciones: AccionesExplorar) {
    AlertDialog(
        onDismissRequest = acciones.onCancelarEnlace,
        title = { Text(stringResource(R.string.explorar_enlace_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.explorar_enlace_detalle), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = state.enlace,
                    onValueChange = acciones.onEnlace,
                    label = { Text(stringResource(R.string.explorar_enlace_campo)) },
                    singleLine = true,
                    isError = state.errorEnlace != null,
                    supportingText = state.errorEnlace?.let { { Text(it) } },
                    enabled = !state.abriendoEnlace,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { acciones.onAbrirEnlace() }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enlace_campo"),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = acciones.onAbrirEnlace,
                enabled = state.enlace.isNotBlank() && !state.abriendoEnlace,
                modifier = Modifier.testTag("enlace_abrir"),
            ) { Text(stringResource(if (state.abriendoEnlace) R.string.explorar_enlace_abriendo else R.string.explorar_enlace_abrir)) }
        },
        dismissButton = {
            TextButton(onClick = acciones.onCancelarEnlace, enabled = !state.abriendoEnlace) {
                Text(stringResource(R.string.accion_cancelar), color = MaterialTheme.colorScheme.onSurface)
            }
        },
    )
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 10)

private val deportesDeEjemplo = listOf(
    Deporte("FUTSAL", "Fútbol sala", TipoCompetencia.EQUIPO),
    Deporte("VOLEIBOL", "Voleibol", TipoCompetencia.EQUIPO),
    Deporte("TENIS_MESA", "Tenis de mesa", TipoCompetencia.INDIVIDUAL),
)

private val torneosDeEjemplo = listOf(
    TorneoResumen("1", "Copa Prueba 2026", "FUTSAL", "Fútbol sala", FormatoTorneo.LIGA, EstadoTorneo.PUBLICADO, 16, 12, HOY.plusDays(4), HOY.plusDays(7), "Andrés Gómez", true),
    TorneoResumen("2", "Liga Relámpago", "FUTSAL", "Fútbol sala", FormatoTorneo.LIGA, EstadoTorneo.EN_CURSO, null, 8, null, HOY.minusDays(5), null, false),
    TorneoResumen("3", "Relámpago de Voleibol mixto de fin de año", "VOLEIBOL", "Voleibol", FormatoTorneo.ELIMINACION_DIRECTA, EstadoTorneo.PUBLICADO, 8, 8, null, HOY.plusDays(10), null, false),
)

@Preview(name = "Explorar · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Explorar · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ExplorarPreview() {
    DesafioSportsTheme {
        ExplorarContenido(
            ExplorarState(deportes = deportesDeEjemplo, torneos = torneosDeEjemplo, cargando = false, hayMas = true),
            HOY, AccionesExplorar(), {},
        )
    }
}

@Preview(name = "Explorar · sin resultados", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ExplorarVacioPreview() {
    DesafioSportsTheme {
        ExplorarContenido(ExplorarState(deportes = deportesDeEjemplo, busqueda = "copa", torneos = emptyList(), cargando = false), HOY, AccionesExplorar(), {})
    }
}
