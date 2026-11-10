package co.edu.ufps.desafiosports.presentacion.inicio

import android.content.res.Configuration
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.domain.model.EquipoEnPartido
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.presentacion.comun.Avatar
import co.edu.ufps.desafiosports.presentacion.comun.BloqueFecha
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import co.edu.ufps.desafiosports.ui.theme.Logotipo
import java.time.LocalDate
import java.time.LocalDateTime

/** Lo que Inicio le pide a la navegación y al ViewModel. */
data class AccionesInicio(
    val onAbrirArbitro: () -> Unit = {},
    val onAbrirPlanilla: (partidoId: String) -> Unit = {},
    val onAbrirCreador: () -> Unit = {},
    val onAbrirTorneo: (TorneoDeCreador) -> Unit = {},
    val onCrearTorneo: () -> Unit = {},
    val onReintentarArbitro: () -> Unit = {},
    val onReintentarCreador: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onCerrarSesion: () -> Unit = {},
    /** Solo en debug: el diagnóstico de conexión de MOV-0001. */
    val onAbrirDiagnostico: (() -> Unit)? = null,
)

/** Inicio provisional (MOV-0010). */
@Composable
fun InicioScreen(
    onAbrirArbitro: () -> Unit,
    onAbrirPlanilla: (String) -> Unit,
    onAbrirCreador: () -> Unit,
    onAbrirTorneo: (TorneoDeCreador) -> Unit,
    onCrearTorneo: () -> Unit,
    onAbrirDiagnostico: (() -> Unit)?,
    viewModel: InicioViewModel = viewModel(factory = InicioViewModel.Factory),
) {
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    val acciones = remember(viewModel, onAbrirArbitro, onAbrirPlanilla, onAbrirCreador, onAbrirTorneo, onCrearTorneo, onAbrirDiagnostico) {
        AccionesInicio(
            onAbrirArbitro = onAbrirArbitro,
            onAbrirPlanilla = onAbrirPlanilla,
            onAbrirCreador = onAbrirCreador,
            onAbrirTorneo = onAbrirTorneo,
            onCrearTorneo = onCrearTorneo,
            onReintentarArbitro = viewModel::reintentarArbitro,
            onReintentarCreador = viewModel::reintentarCreador,
            onRefrescar = viewModel::refrescar,
            onCerrarSesion = viewModel::cerrarSesion,
            onAbrirDiagnostico = onAbrirDiagnostico,
        )
    }
    InicioContenido(state = viewModel.state, hoy = LocalDate.now(), acciones = acciones)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InicioContenido(state: InicioState, hoy: LocalDate, acciones: AccionesInicio, modifier: Modifier = Modifier) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Logotipo(tamano = 26.sp, enUnaLinea = true) },
                actions = { MenuDeCuenta(state, acciones) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            // La acción principal de Inicio: el organizador llega en un toque (PRODUCT.md).
            ExtendedFloatingActionButton(
                onClick = acciones.onCrearTorneo,
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                text = { Text(stringResource(R.string.creador_crear_torneo)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier.testTag("inicio_crear_torneo"),
            )
        },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        PullToRefreshBox(
            isRefreshing = state.refrescando,
            onRefresh = acciones.onRefrescar,
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize(),
        ) {
            LazyColumn(
                // Aire para que el botón flotante no tape la última sección.
                contentPadding = PaddingValues(bottom = 104.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("inicio_lista"),
            ) {
                item(key = "saludo") {
                    val saludo = state.nombre?.let { stringResource(R.string.inicio_saludo, it) }
                        ?: stringResource(R.string.inicio_saludo_generico)
                    Text(
                        text = saludo.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                            .semantics { heading() }
                            .testTag("inicio_saludo"),
                    )
                }
                item(key = "titulo_secciones") { EncabezadoBloque(stringResource(R.string.inicio_tus_secciones)) }

                val arbitro = state.arbitro
                if (arbitro !is Seccion.Lista || arbitro.valor.visible) {
                    item(key = "arbitro") {
                        SeccionArbitro(arbitro, hoy, acciones, Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp))
                    }
                }
                item(key = "creador") {
                    SeccionCreador(state.creador, acciones, Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp))
                }
            }
        }
    }
}

/** La foto (por ahora, las iniciales) abre el menú de la cuenta. */
@Composable
private fun MenuDeCuenta(state: InicioState, acciones: AccionesInicio) {
    var abierto by remember { mutableStateOf(false) }
    val descripcion = stringResource(R.string.inicio_cuenta)
    Box {
        IconButton(
            onClick = { abierto = true },
            modifier = Modifier
                .padding(end = 4.dp)
                .semantics { contentDescription = descripcion }
                .testTag("inicio_cuenta"),
        ) {
            Avatar(state.iniciales.ifBlank { "·" }, tamano = 36.dp)
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.medium,
        ) {
            if (state.correo != null) {
                Text(
                    text = state.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .widthIn(max = 260.dp),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            acciones.onAbrirDiagnostico?.let { abrir ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.inicio_diagnostico)) },
                    onClick = {
                        abierto = false
                        abrir()
                    },
                    modifier = Modifier.testTag("inicio_diagnostico"),
                )
            }
            DropdownMenuItem(
                text = {
                    Text(stringResource(if (state.cerrandoSesion) R.string.inicio_cerrando_sesion else R.string.inicio_cerrar_sesion))
                },
                enabled = !state.cerrandoSesion,
                onClick = {
                    abierto = false
                    acciones.onCerrarSesion()
                },
                modifier = Modifier.testTag("inicio_cerrar_sesion"),
            )
        }
    }
}

// ── Secciones ───────────────────────────────────────────────────────────────

/** Un bloque de sección: un escalón de contenedor con la cabecera que lleva a la sección. */
@Composable
private fun BloqueSeccion(
    titulo: String,
    resumen: String?,
    @DrawableRes icono: Int,
    onAbrir: () -> Unit,
    modifier: Modifier = Modifier,
    fin: (@Composable () -> Unit)? = null,
    contenido: @Composable () -> Unit = {},
) {
    val lectura = stringResource(R.string.inicio_ver_seccion, titulo)
    Surface(
        color = colorDePanel(),
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .clickable(role = Role.Button, onClickLabel = lectura, onClick = onAbrir)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                IconoEnCirculo(icono)
                Column(Modifier.weight(1f)) {
                    Text(titulo.uppercase(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    if (resumen != null) {
                        Text(resumen, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                fin?.invoke()
                FlechaFila()
            }
            contenido()
        }
    }
}

@Composable
private fun SeccionArbitro(seccion: Seccion<ResumenArbitro>, hoy: LocalDate, acciones: AccionesInicio, modifier: Modifier = Modifier) {
    val resumen = (seccion as? Seccion.Lista)?.valor
    BloqueSeccion(
        titulo = stringResource(R.string.arbitro_titulo),
        resumen = resumen?.let {
            if (it.porJugar > 0) pluralStringResource(R.plurals.inicio_arbitro_por_jugar, it.porJugar, it.porJugar) else null
        },
        icono = R.drawable.ic_flag,
        onAbrir = acciones.onAbrirArbitro,
        modifier = modifier.testTag("inicio_arbitro"),
    ) {
        when (seccion) {
            Seccion.Cargando -> CargandoSeccion()
            is Seccion.Fallo -> FalloSeccion(seccion.error, acciones.onReintentarArbitro, "inicio_arbitro_reintentar")
            is Seccion.Lista -> {
                val proximo = seccion.valor.proximo
                if (proximo != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    ProximoPartido(proximo, hoy, acciones.onAbrirPlanilla)
                } else {
                    TextoDeSeccion(stringResource(R.string.inicio_arbitro_nada))
                }
            }
        }
    }
}

/**
 * El momento de Inicio: "Hoy 4:00 p. m. · Sistemas vs Civil", y un toque abre la planilla sin
 * pasar por la sección. La hora va en naranja solo si el partido es hoy.
 */
@Composable
private fun ProximoPartido(partido: PartidoArbitro, hoy: LocalDate, onAbrirPlanilla: (String) -> Unit) {
    val fecha = partido.fechaHora
    val esHoy = fecha?.toLocalDate() == hoy || partido.estado == EstadoPartido.EN_CURSO
    val cruce = if (partido.cruceDefinido) {
        stringResource(R.string.partido_cruce, partido.local?.nombre.orEmpty(), partido.visitante?.nombre.orEmpty())
    } else {
        stringResource(R.string.partido_cruce_por_definir)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .then(
                if (partido.cruceDefinido) Modifier.clickable(role = Role.Button) { onAbrirPlanilla(partido.id) } else Modifier,
            )
            .semantics(mergeDescendants = true) { }
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 14.dp)
            .testTag("inicio_proximo_partido"),
    ) {
        if (fecha == null) {
            BloqueFecha(dia = stringResource(R.string.fecha_sin_fecha), hora = null)
        } else {
            BloqueFecha(
                dia = Formatos.diaRelativo(fecha.toLocalDate(), hoy, stringResource(R.string.fecha_hoy), stringResource(R.string.fecha_manana)),
                hora = Formatos.horaSinPeriodo(fecha.toLocalTime()),
                periodo = Formatos.periodo(fecha.toLocalTime()),
                ahora = esHoy,
            )
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.inicio_arbitro_proximo),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(cruce, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val contexto = listOfNotNull(partido.torneo.takeIf { it.isNotBlank() }, partido.lugar).joinToString(" · ")
            if (contexto.isNotBlank()) {
                Text(
                    text = contexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (partido.cruceDefinido) {
            ChipEstado(
                texto = stringResource(R.string.partido_planilla),
                tono = if (esHoy) Tono.AHORA else Tono.NEUTRO,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun SeccionCreador(seccion: Seccion<ResumenCreador>, acciones: AccionesInicio, modifier: Modifier = Modifier) {
    val resumen = (seccion as? Seccion.Lista)?.valor
    val porRevisar = resumen?.porRevisar ?: 0
    BloqueSeccion(
        titulo = stringResource(R.string.creador_titulo),
        resumen = resumen?.takeIf { it.torneos.isNotEmpty() }?.let {
            pluralStringResource(R.plurals.inicio_creador_torneos, it.torneos.size, it.torneos.size)
        },
        icono = R.drawable.ic_trophy,
        onAbrir = acciones.onAbrirCreador,
        fin = if (porRevisar > 0) {
            { ChipEstado(pluralStringResource(R.plurals.inicio_creador_por_revisar, porRevisar, porRevisar), tono = Tono.AHORA) }
        } else {
            null
        },
        modifier = modifier.testTag("inicio_creador"),
    ) {
        when (seccion) {
            Seccion.Cargando -> CargandoSeccion()
            is Seccion.Fallo -> FalloSeccion(seccion.error, acciones.onReintentarCreador, "inicio_creador_reintentar")
            is Seccion.Lista -> {
                val valor = seccion.valor
                val destacado = valor.destacado
                when {
                    valor.torneos.isEmpty() -> TextoDeSeccion(stringResource(R.string.inicio_creador_vacio))
                    destacado != null && destacado.porRevisar > 0 -> {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        TorneoQueEspera(destacado, acciones.onAbrirTorneo)
                    }
                    else -> TextoDeSeccion(stringResource(R.string.inicio_creador_al_dia))
                }
            }
        }
    }
}

/** El torneo que más necesita al organizador: un toque lleva a sus inscripciones. */
@Composable
private fun TorneoQueEspera(torneo: TorneoDeCreador, onAbrir: (TorneoDeCreador) -> Unit) {
    val partes = listOfNotNull(
        torneo.inscripcionesPendientes.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.creador_inscripciones_pendientes, it, it) },
        torneo.comprobantesPendientes.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.creador_comprobantes_pendientes, it, it) },
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button) { onAbrir(torneo) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("inicio_torneo_espera"),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.inicio_creador_espera, torneo.nombre),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (partes.isNotEmpty()) {
                Text(partes.joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        FlechaFila()
    }
}

@Composable
private fun TextoDeSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 68.dp, end = 16.dp, bottom = 16.dp),
    )
}

@Composable
private fun CargandoSeccion() {
    val descripcion = stringResource(R.string.estado_cargando)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .semantics { contentDescription = descripcion },
        )
    }
}

/** El error de una sección no tapa las demás (CA-5): se muestra dentro de su bloque. */
@Composable
private fun FalloSeccion(error: ErrorDePantalla, onReintentar: () -> Unit, testTagBoton: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 68.dp, end = 16.dp, bottom = 16.dp),
    ) {
        Text(
            text = stringResource(if (error.sinConexion) R.string.estado_sin_conexion else R.string.estado_error),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
        Text(error.mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        BotonContorno(stringResource(R.string.accion_reintentar), onReintentar, Modifier.testTag(testTagBoton))
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 12)

private val proximoDeEjemplo = PartidoArbitro(
    id = "p1", torneoId = "t1", torneo = "Interfacultades Fútbol Sala", deporte = "FUTSAL", fase = "Fecha 3",
    local = EquipoEnPartido("e1", "Sistemas"), visitante = EquipoEnPartido("e2", "Civil"),
    fechaHora = LocalDateTime.of(2026, 11, 12, 16, 0), lugar = "Cancha 2",
    estado = EstadoPartido.PROGRAMADO, marcador = null, asignado = true,
)

private val torneosDeEjemplo = listOf(
    TorneoDeCreador("1", "Relámpago de Tenis de Mesa", EstadoTorneo.PUBLICADO, "TENIS_MESA", Visibilidad.PUBLICO, 16, 12, null, 3, 1),
    TorneoDeCreador("2", "Interfacultades Fútbol Sala", EstadoTorneo.EN_CURSO, "FUTSAL", Visibilidad.NO_LISTADO, 12, 12, null, 0, 0),
)

@Preview(name = "Inicio · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Inicio · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioPreview() {
    DesafioSportsTheme {
        InicioContenido(
            InicioState(
                nombre = "Carlos", iniciales = "CR", correo = "arbitro01@prueba.local",
                arbitro = Seccion.Lista(ResumenArbitro(proximoDeEjemplo, porJugar = 2, total = 4)),
                creador = Seccion.Lista(ResumenCreador(torneosDeEjemplo)),
            ),
            HOY, AccionesInicio(),
        )
    }
}

@Preview(name = "Inicio · cargando y con error", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioErrorPreview() {
    DesafioSportsTheme {
        InicioContenido(
            InicioState(
                nombre = null,
                arbitro = Seccion.Cargando,
                creador = Seccion.Fallo(ErrorDePantalla("No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.", sinConexion = true)),
            ),
            HOY, AccionesInicio(),
        )
    }
}
