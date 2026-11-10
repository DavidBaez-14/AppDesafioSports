package co.edu.ufps.desafiosports.presentacion.arbitro

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.EquipoEnPartido
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.PartidosDeArbitro
import co.edu.ufps.desafiosports.presentacion.comun.BloqueFecha
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.time.LocalDate
import java.time.LocalDateTime

/** Sección Árbitro (MOV-0009): los partidos que arbitra quien inició sesión. */
@Composable
fun SeccionArbitroScreen(
    onVolver: () -> Unit,
    onAbrirPlanilla: (partidoId: String) -> Unit,
    viewModel: SeccionArbitroViewModel = viewModel(factory = SeccionArbitroViewModel.Factory),
) {
    // Al regresar de la planilla, un partido puede haber cambiado de bloque.
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    SeccionArbitroContenido(
        state = viewModel.state,
        hoy = LocalDate.now(),
        onVolver = onVolver,
        onReintentar = viewModel::cargar,
        onRefrescar = viewModel::refrescar,
        onAbrirPartido = onAbrirPlanilla,
        onAvisoMostrado = viewModel::onAvisoMostrado,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SeccionArbitroContenido(
    state: SeccionArbitroState,
    hoy: LocalDate,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onRefrescar: () -> Unit,
    onAbrirPartido: (String) -> Unit,
    onAvisoMostrado: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        avisos.showSnackbar(aviso)
        onAvisoMostrado()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.arbitro_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("arbitro_atras")) {
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
        val partidos = state.partidos
        when {
            state.cargando && partidos == null -> Cargando(Modifier.padding(relleno))

            state.error != null && partidos == null ->
                EstadoError(state.error, onReintentar, Modifier.padding(relleno), testTagBoton = "arbitro_reintentar")

            partidos != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                if (partidos.vacio) {
                    // Desplazable para que "deslizar para actualizar" funcione también sin partidos.
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .testTag("arbitro_vacio"),
                    ) {
                        EstadoVacio(
                            icono = R.drawable.ic_sports,
                            titulo = stringResource(R.string.arbitro_vacio_titulo),
                            detalle = stringResource(R.string.arbitro_vacio_detalle),
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("arbitro_lista"),
                    ) {
                        bloque(R.string.arbitro_hoy, partidos.hoy, hoy, onAbrirPartido)
                        bloque(R.string.arbitro_proximos, partidos.proximos, hoy, onAbrirPartido)
                        bloque(R.string.arbitro_jugados, partidos.jugados, hoy, onAbrirPartido)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.bloque(
    titulo: Int,
    partidos: List<PartidoArbitro>,
    hoy: LocalDate,
    onAbrirPartido: (String) -> Unit,
) {
    if (partidos.isEmpty()) return
    item(key = "titulo_$titulo", contentType = "titulo") { EncabezadoBloque(stringResource(titulo)) }
    itemsIndexed(partidos, key = { _, partido -> partido.id }, contentType = { _, _ -> "partido" }) { indice, partido ->
        if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 16.dp))
        FilaPartido(partido, hoy, onAbrirPartido)
    }
}

@Composable
private fun FilaPartido(partido: PartidoArbitro, hoy: LocalDate, onAbrirPartido: (String) -> Unit) {
    val esHoy = partido.fechaHora?.toLocalDate() == hoy
    val cerrado = partido.estado.cerrado
    val enVivo = !cerrado && (esHoy || partido.estado == EstadoPartido.EN_CURSO)

    val titulo = if (partido.cruceDefinido) {
        stringResource(R.string.partido_cruce, partido.local?.nombre.orEmpty(), partido.visitante?.nombre.orEmpty())
    } else {
        stringResource(R.string.partido_cruce_por_definir)
    }
    val detalle = listOfNotNull(partido.torneo.takeIf { it.isNotBlank() }, partido.lugar).joinToString(" · ")

    Fila(
        titulo = titulo,
        detalle = detalle.ifBlank { null },
        inicio = {
            when {
                cerrado && esHoy -> Box(Modifier.width(72.dp), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_task_alt), contentDescription = null)
                }
                partido.fechaHora == null -> BloqueFecha(dia = stringResource(R.string.fecha_sin_fecha), hora = null)
                else -> BloqueFecha(
                    dia = Formatos.diaRelativo(
                        partido.fechaHora.toLocalDate(), hoy,
                        stringResource(R.string.fecha_hoy), stringResource(R.string.fecha_manana),
                    ),
                    // De un partido cerrado importa el día, no la hora.
                    hora = if (cerrado) null else Formatos.horaSinPeriodo(partido.fechaHora.toLocalTime()),
                    periodo = if (cerrado) null else Formatos.periodo(partido.fechaHora.toLocalTime()),
                    ahora = enVivo,
                )
            }
        },
        fin = {
            val marcador = partido.marcador
            when {
                partido.estado == EstadoPartido.FINALIZADO && marcador != null ->
                    ChipEstado(stringResource(R.string.partido_cerrado_marcador, marcador.local, marcador.visitante), tono = Tono.EXITO)
                partido.estado == EstadoPartido.FINALIZADO -> ChipEstado(stringResource(R.string.partido_cerrado), tono = Tono.EXITO)
                partido.estado == EstadoPartido.WO -> ChipEstado(stringResource(R.string.partido_wo))
                partido.estado == EstadoPartido.APLAZADO -> ChipEstado(stringResource(R.string.partido_aplazado), tono = Tono.ADVERTENCIA)
                enVivo && partido.cruceDefinido -> ChipEstado(stringResource(R.string.partido_planilla), tono = Tono.AHORA)
                partido.cruceDefinido -> FlechaFila()
            }
        },
        onClick = if (partido.cruceDefinido) ({ onAbrirPartido(partido.id) }) else null,
        modifier = Modifier.testTag("arbitro_partido"),
    )
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 12)

private fun partidoDeEjemplo(
    id: String,
    fechaHora: LocalDateTime?,
    estado: EstadoPartido = EstadoPartido.PROGRAMADO,
    marcador: Marcador? = null,
    conCruce: Boolean = true,
) = PartidoArbitro(
    id = id,
    torneoId = "t1",
    torneo = "Interfacultades Fútbol Sala",
    deporte = "FUTSAL",
    fase = "Grupo A · Fecha 3",
    local = if (conCruce) EquipoEnPartido("e1", "Sistemas") else null,
    visitante = if (conCruce) EquipoEnPartido("e2", "Civil") else null,
    fechaHora = fechaHora,
    lugar = "Cancha 2",
    estado = estado,
    marcador = marcador,
    asignado = true,
)

private val partidosDeEjemplo = PartidosDeArbitro(
    hoy = listOf(
        partidoDeEjemplo("1", HOY.atTime(14, 0), EstadoPartido.FINALIZADO, Marcador(3, 2)),
        partidoDeEjemplo("2", HOY.atTime(16, 0)),
    ),
    proximos = listOf(
        partidoDeEjemplo("3", HOY.plusDays(1).atTime(18, 30)),
        partidoDeEjemplo("4", HOY.plusDays(5).atTime(8, 0), conCruce = false),
        partidoDeEjemplo("5", null),
    ),
    jugados = listOf(partidoDeEjemplo("6", HOY.minusDays(3).atTime(16, 0), EstadoPartido.WO)),
)

@Preview(name = "Árbitro · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Árbitro · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SeccionArbitroPreview() {
    DesafioSportsTheme {
        SeccionArbitroContenido(SeccionArbitroState(cargando = false, partidos = partidosDeEjemplo), HOY, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Árbitro · vacío", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SeccionArbitroVaciaPreview() {
    DesafioSportsTheme {
        SeccionArbitroContenido(SeccionArbitroState(cargando = false, partidos = PartidosDeArbitro()), HOY, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Árbitro · sin conexión", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SeccionArbitroErrorPreview() {
    DesafioSportsTheme {
        SeccionArbitroContenido(
            SeccionArbitroState(cargando = false, error = ErrorDePantalla("No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.", sinConexion = true)),
            HOY, {}, {}, {}, {}, {},
        )
    }
}
