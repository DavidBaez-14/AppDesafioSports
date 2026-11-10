package co.edu.ufps.desafiosports.presentacion.creador

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.ui.deporte.iconoDeDeporte
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import co.edu.ufps.desafiosports.presentacion.comun.BarraDeLlenado
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.presentacion.comun.llenadoDeCupos
import co.edu.ufps.desafiosports.presentacion.comun.CifraDeCupos

/** Sección Creador (MOV-0008): mis torneos y lo que espera mi revisión. */
@Composable
fun SeccionCreadorScreen(
    onVolver: () -> Unit,
    onCrearTorneo: () -> Unit,
    onAbrirTorneo: (TorneoDeCreador) -> Unit,
    viewModel: SeccionCreadorViewModel = viewModel(factory = SeccionCreadorViewModel.Factory),
) {
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    SeccionCreadorContenido(
        state = viewModel.state,
        onVolver = onVolver,
        onReintentar = viewModel::cargar,
        onRefrescar = viewModel::refrescar,
        onCrearTorneo = onCrearTorneo,
        onAbrirTorneo = onAbrirTorneo,
        onAvisoMostrado = viewModel::onAvisoMostrado,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SeccionCreadorContenido(
    state: SeccionCreadorState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onRefrescar: () -> Unit,
    onCrearTorneo: () -> Unit,
    onAbrirTorneo: (TorneoDeCreador) -> Unit,
    onAvisoMostrado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        avisos.showSnackbar(aviso)
        onAvisoMostrado()
    }

    val torneos = state.torneos
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.creador_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("creador_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            // La acción principal de la sección: el único naranja de la pantalla.
            if (torneos != null) {
                ExtendedFloatingActionButton(
                    onClick = onCrearTorneo,
                    icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                    text = { Text(stringResource(R.string.creador_crear_torneo)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.testTag("creador_crear"),
                )
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        when {
            state.cargando && torneos == null -> Cargando(Modifier.padding(relleno))

            state.error != null && torneos == null ->
                EstadoError(state.error, onReintentar, Modifier.padding(relleno), testTagBoton = "creador_reintentar")

            torneos != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                if (torneos.isEmpty()) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("creador_vacio"),
                    ) {
                        EstadoVacio(
                            icono = R.drawable.ic_trophy,
                            titulo = stringResource(R.string.creador_vacio_titulo),
                            detalle = stringResource(R.string.creador_vacio_detalle),
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                } else {
                    LazyColumn(
                        // Deja aire para que el botón flotante no tape la última fila.
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("creador_lista"),
                    ) {
                        val porRevisar = state.porRevisar
                        if (porRevisar.isNotEmpty()) {
                            item(key = "titulo_revisar") { EncabezadoBloque(stringResource(R.string.creador_por_revisar)) }
                            itemsIndexed(porRevisar, key = { _, t -> "revisar_${t.id}" }) { indice, torneo ->
                                if (indice > 0) Separador()
                                FilaPorRevisar(torneo, onAbrirTorneo)
                            }
                        }
                        item(key = "titulo_torneos") { EncabezadoBloque(stringResource(R.string.creador_mis_torneos)) }
                        itemsIndexed(torneos, key = { _, t -> "torneo_${t.id}" }) { indice, torneo ->
                            if (indice > 0) Separador()
                            FilaTorneo(torneo, onAbrirTorneo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Separador() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 70.dp, end = 16.dp))
}

@Composable
private fun FilaPorRevisar(torneo: TorneoDeCreador, onAbrir: (TorneoDeCreador) -> Unit) {
    val partes = listOfNotNull(
        torneo.inscripcionesPendientes.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.creador_inscripciones_pendientes, it, it) },
        torneo.comprobantesPendientes.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.creador_comprobantes_pendientes, it, it) },
    )
    Fila(
        titulo = torneo.nombre,
        detalle = partes.joinToString(" · "),
        inicio = { IconoEnCirculo(R.drawable.ic_how_to_reg) },
        fin = {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Text(torneo.porRevisar.toString()) }
        },
        onClick = { onAbrir(torneo) },
        modifier = Modifier.testTag("creador_por_revisar"),
    )
}

/**
 * Un torneo de "Mis torneos": el deporte, el nombre, el estado con su color y cómo lo ven los
 * demás; a la derecha, los cupos ocupados en cifras de marcador con su barra de llenado (CA-8).
 */
@Composable
private fun FilaTorneo(torneo: TorneoDeCreador, onAbrir: (TorneoDeCreador) -> Unit) {
    val (estado, tono) = when (torneo.estado) {
        EstadoTorneo.BORRADOR -> stringResource(R.string.torneo_borrador) to Tono.ADVERTENCIA
        EstadoTorneo.PUBLICADO -> stringResource(R.string.torneo_inscripciones_abiertas) to Tono.INFO
        EstadoTorneo.EN_CURSO -> stringResource(R.string.torneo_en_curso) to Tono.EXITO
        EstadoTorneo.FINALIZADO -> stringResource(R.string.torneo_finalizado) to Tono.NEUTRO
        EstadoTorneo.DESCONOCIDO -> null to Tono.NEUTRO
    }
    val cupos = torneo.cupos
    val lecturaCupos = if (cupos != null) {
        stringResource(R.string.creador_inscritos_de_cupos, torneo.inscritos, cupos)
    } else {
        pluralStringResource(R.plurals.creador_inscritos, torneo.inscritos, torneo.inscritos)
    }
    val visibilidad = torneo.visibilidad
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable(role = Role.Button) { onAbrir(torneo) }
            .semantics(mergeDescendants = true) { }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("creador_torneo"),
    ) {
        IconoEnCirculo(iconoDeDeporte(torneo.deporte))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            Text(torneo.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (estado != null) ChipEstado(estado, tono = tono)
                if (visibilidad != null) {
                    Icon(
                        painterResource(visibilidad.icono()),
                        contentDescription = stringResource(visibilidad.titulo()),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        CifraDeCupos(torneo.inscritos, cupos, lecturaCupos)
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val torneosDeEjemplo = listOf(
    TorneoDeCreador("1", "Relámpago de Tenis de Mesa", EstadoTorneo.PUBLICADO, "TENIS_MESA", Visibilidad.PUBLICO, 16, 12, null, 3, 1),
    TorneoDeCreador("2", "Interfacultades Fútbol Sala", EstadoTorneo.EN_CURSO, "FUTSAL", Visibilidad.NO_LISTADO, 12, 12, null, 0, 0),
    TorneoDeCreador("3", "Copa de Ingeniería de Sistemas edición especial de fin de año", EstadoTorneo.FINALIZADO, "VOLEIBOL", Visibilidad.PRIVADO, null, 8, null, 0, 0),
)

@Preview(name = "Creador · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Creador · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SeccionCreadorPreview() {
    DesafioSportsTheme {
        SeccionCreadorContenido(SeccionCreadorState(cargando = false, torneos = torneosDeEjemplo), {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Creador · vacío", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SeccionCreadorVaciaPreview() {
    DesafioSportsTheme {
        SeccionCreadorContenido(SeccionCreadorState(cargando = false, torneos = emptyList()), {}, {}, {}, {}, {}, {})
    }
}
