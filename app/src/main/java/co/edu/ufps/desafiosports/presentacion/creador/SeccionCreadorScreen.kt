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
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 16.dp))
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

@Composable
private fun FilaTorneo(torneo: TorneoDeCreador, onAbrir: (TorneoDeCreador) -> Unit) {
    val estado = when (torneo.estado) {
        EstadoTorneo.BORRADOR -> stringResource(R.string.torneo_borrador)
        EstadoTorneo.PUBLICADO -> stringResource(R.string.torneo_inscripciones_abiertas)
        EstadoTorneo.EN_CURSO -> stringResource(R.string.torneo_en_curso)
        EstadoTorneo.FINALIZADO -> stringResource(R.string.torneo_finalizado)
        EstadoTorneo.DESCONOCIDO -> null
    }
    val cupos = torneo.cupos
    val inscritos = if (cupos != null) {
        stringResource(R.string.creador_inscritos_de_cupos, torneo.inscritos, cupos)
    } else {
        pluralStringResource(R.plurals.creador_inscritos, torneo.inscritos, torneo.inscritos)
    }
    Fila(
        titulo = torneo.nombre,
        detalle = listOfNotNull(estado, inscritos).joinToString(" · "),
        inicio = { IconoEnCirculo(iconoDeDeporte(torneo.deporte)) },
        fin = {
            val visibilidad = torneo.visibilidad
            if (visibilidad != null) ChipEstado(stringResource(visibilidad.titulo()), icono = visibilidad.icono()) else FlechaFila()
        },
        onClick = { onAbrir(torneo) },
        modifier = Modifier.testTag("creador_torneo"),
    )
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
