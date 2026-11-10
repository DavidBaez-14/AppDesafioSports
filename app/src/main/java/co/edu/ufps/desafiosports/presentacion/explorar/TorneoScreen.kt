package co.edu.ufps.desafiosports.presentacion.explorar

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.AccionDelTorneo
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.EquipoInscrito
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FilaTabla
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.MiRelacion
import co.edu.ufps.desafiosports.domain.model.PartidoDelTorneo
import co.edu.ufps.desafiosports.domain.model.RelacionConEquipo
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.TorneoDetalle
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.presentacion.comun.Avatar
import co.edu.ufps.desafiosports.presentacion.comun.BloqueFecha
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.BotonPrimario
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.CifraDeCupos
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.presentacion.comun.inicialDe
import co.edu.ufps.desafiosports.presentacion.creador.icono
import co.edu.ufps.desafiosports.presentacion.creador.titulo
import co.edu.ufps.desafiosports.ui.deporte.iconoDeDeporte
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/** A dónde puede llevar el detalle del torneo. */
data class AccionesTorneo(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
    val onEntrar: () -> Unit = {},
    val onAbrirCreador: () -> Unit = {},
    val onAbrirArbitro: () -> Unit = {},
)

/** Detalle del torneo (MOV-0005). */
@Composable
fun TorneoScreen(
    onVolver: () -> Unit,
    onEntrar: () -> Unit,
    onAbrirCreador: (TorneoDetalle) -> Unit,
    onAbrirArbitro: () -> Unit,
    viewModel: TorneoViewModel = viewModel(factory = TorneoViewModel.Factory),
) {
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    val state = viewModel.state
    val torneo = state.torneo
    val acciones = AccionesTorneo(
        onReintentar = viewModel::cargar,
        onRefrescar = viewModel::refrescar,
        onAvisoMostrado = viewModel::onAvisoMostrado,
        onEntrar = onEntrar,
        onAbrirCreador = { torneo?.let(onAbrirCreador) },
        onAbrirArbitro = onAbrirArbitro,
    )
    TorneoContenido(state = state, hoy = LocalDate.now(), acciones = acciones, onVolver = onVolver)
}

/** Abre la hoja de compartir de Android con el nombre del torneo y su enlace (MOV-0005 · CA-6). */
private fun compartir(contexto: Context, torneo: TorneoDetalle) {
    val enlace = torneo.enlace ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, contexto.getString(R.string.crear_compartir_texto, torneo.nombre, enlace))
    }
    contexto.startActivity(Intent.createChooser(intent, null))
}

private enum class Pestana { EQUIPOS, PARTIDOS, TABLA, INFO }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TorneoContenido(state: TorneoState, hoy: LocalDate, acciones: AccionesTorneo, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        avisos.showSnackbar(aviso)
        acciones.onAvisoMostrado()
    }
    val contexto = LocalContext.current
    val torneo = state.torneo

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.torneo_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("torneo_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                actions = {
                    if (torneo?.enlace != null) {
                        IconButton(onClick = { compartir(contexto, torneo) }, modifier = Modifier.testTag("torneo_compartir")) {
                            Icon(painterResource(R.drawable.ic_share), contentDescription = stringResource(R.string.torneo_compartir))
                        }
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
            state.cargando && torneo == null -> Cargando(Modifier.padding(relleno))
            state.error != null && torneo == null -> EstadoError(
                state.error, acciones.onReintentar, Modifier.padding(relleno), testTagBoton = "torneo_reintentar",
            )
            torneo != null -> PullToRefreshBox(
                isRefreshing = state.refrescando,
                onRefresh = acciones.onRefrescar,
                modifier = Modifier
                    .padding(relleno)
                    .fillMaxSize(),
            ) {
                CuerpoDelTorneo(torneo, state.accion, hoy, acciones)
            }
        }
    }
}

@Composable
private fun CuerpoDelTorneo(torneo: TorneoDetalle, accion: AccionDelTorneo?, hoy: LocalDate, acciones: AccionesTorneo) {
    // Solo las pestañas que tienen algo; la tabla aparece cuando ya hay partidos.
    val pestanas = buildList {
        add(Pestana.EQUIPOS)
        if (torneo.partidos.isNotEmpty()) add(Pestana.PARTIDOS)
        if (torneo.partidos.isNotEmpty() && torneo.tabla.isNotEmpty()) add(Pestana.TABLA)
        add(Pestana.INFO)
    }
    var elegida by rememberSaveable { mutableIntStateOf(0) }
    val pestana = pestanas.getOrElse(elegida) { Pestana.EQUIPOS }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("torneo_lista"),
    ) {
        item(key = "cabecera") { Cabecera(torneo, hoy, Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
        if (accion != null) {
            item(key = "franja") { FranjaDeAccion(torneo, accion, acciones, Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) }
        }
        item(key = "pestanas") {
            PrimaryTabRow(
                selectedTabIndex = pestanas.indexOf(pestana),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pestanas.indexOf(pestana), matchContentSize = true),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
                modifier = Modifier.padding(top = 16.dp),
            ) {
                pestanas.forEachIndexed { indice, p ->
                    Tab(
                        selected = p == pestana,
                        onClick = { elegida = indice },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = when (p) {
                                    Pestana.EQUIPOS -> stringResource(if (torneo.individual) R.string.torneo_tab_inscritos else R.string.torneo_tab_equipos)
                                    Pestana.PARTIDOS -> stringResource(R.string.torneo_tab_partidos)
                                    Pestana.TABLA -> stringResource(R.string.torneo_tab_tabla)
                                    Pestana.INFO -> stringResource(R.string.torneo_tab_info)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        },
                        modifier = Modifier.testTag("torneo_tab_${p.name.lowercase()}"),
                    )
                }
            }
        }
        when (pestana) {
            Pestana.EQUIPOS -> equipos(torneo)
            Pestana.PARTIDOS -> partidos(torneo.partidos, hoy)
            Pestana.TABLA -> tabla(torneo)
            Pestana.INFO -> item(key = "info") { Informacion(torneo) }
        }
    }
}

/** Nombre, deporte, estado y los datos clave en un panel. */
@Composable
private fun Cabecera(torneo: TorneoDetalle, hoy: LocalDate, modifier: Modifier = Modifier) {
    val cupos = torneo.cupos
    val lecturaCupos = if (cupos != null) {
        stringResource(R.string.creador_inscritos_de_cupos, torneo.inscritos, cupos)
    } else {
        pluralStringResource(R.plurals.creador_inscritos, torneo.inscritos, torneo.inscritos)
    }
    Surface(color = colorDePanel(), shape = MaterialTheme.shapes.large, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconoEnCirculo(iconoDeDeporte(torneo.deporte?.codigo))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = torneo.nombre.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    val sub = listOfNotNull(torneo.deporte?.nombre, torneo.organizador?.let { stringResource(R.string.torneo_organiza, it) })
                    if (sub.isNotEmpty()) {
                        Text(sub.joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                CifraDeCupos(torneo.inscritos, cupos, lecturaCupos)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                ChipDeEstadoTorneo(torneo.estado, torneo.inscripcionesAbiertas)
                torneo.visibilidad?.let { ChipEstado(stringResource(it.titulo()), icono = it.icono()) }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                torneo.fechaInicio?.let { Dato(stringResource(R.string.creado_ficha_empieza), Formatos.fecha(it, hoy)) }
                torneo.cierreInscripciones?.let { Dato(stringResource(R.string.torneo_cierre), Formatos.fecha(it, hoy)) }
                nombreDeFormato(torneo.formato)?.let { Dato(stringResource(R.string.creado_ficha_formato), it) }
                Dato(
                    stringResource(R.string.creado_ficha_inscripcion),
                    torneo.valorInscripcion?.takeIf { torneo.cobraInscripcion && it.signum() > 0 }?.let { Formatos.pesos(it) }
                        ?: stringResource(R.string.creado_ficha_gratis),
                )
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(Modifier.semantics(mergeDescendants = true) { }) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.End)
    }
}

@Composable
private fun nombreDeFormato(formato: FormatoTorneo?): String? = when (formato) {
    FormatoTorneo.LIGA -> stringResource(R.string.formato_liga)
    FormatoTorneo.ELIMINACION_DIRECTA -> stringResource(R.string.formato_eliminacion)
    FormatoTorneo.GRUPOS_ELIMINATORIAS -> stringResource(R.string.formato_grupos)
    null -> null
}

/** El estado de un torneo con su color. "Inscripciones abiertas" es información, no "lo de ahora". */
@Composable
internal fun ChipDeEstadoTorneo(estado: EstadoTorneo, inscripcionesAbiertas: Boolean) {
    when {
        estado == EstadoTorneo.PUBLICADO && inscripcionesAbiertas ->
            ChipEstado(stringResource(R.string.torneo_inscripciones_abiertas), tono = Tono.INFO)
        estado == EstadoTorneo.PUBLICADO -> ChipEstado(stringResource(R.string.torneo_inscripciones_cerradas))
        estado == EstadoTorneo.EN_CURSO -> ChipEstado(stringResource(R.string.torneo_en_curso), tono = Tono.EXITO)
        estado == EstadoTorneo.FINALIZADO -> ChipEstado(stringResource(R.string.torneo_finalizado))
        estado == EstadoTorneo.BORRADOR -> ChipEstado(stringResource(R.string.torneo_borrador), tono = Tono.ADVERTENCIA)
        else -> Unit
    }
}

/** El estado de una inscripción con su color. */
@Composable
internal fun ChipDeInscripcion(estado: EstadoInscripcion, modifier: Modifier = Modifier) {
    when (estado) {
        EstadoInscripcion.PENDIENTE_PAGO -> ChipEstado(stringResource(R.string.inscripcion_pendiente), modifier, tono = Tono.ADVERTENCIA)
        EstadoInscripcion.APROBADO -> ChipEstado(stringResource(R.string.inscripcion_aprobada), modifier, tono = Tono.EXITO)
        EstadoInscripcion.RECHAZADO -> ChipEstado(stringResource(R.string.inscripcion_rechazada), modifier, tono = Tono.ERROR)
        EstadoInscripcion.EXPULSADO -> ChipEstado(stringResource(R.string.inscripcion_expulsado), modifier, tono = Tono.ERROR)
        EstadoInscripcion.DESCONOCIDO -> Unit
    }
}

/**
 * La franja de acciones (MOV-0005 · CA-4): lo que la persona es o puede hacer en este torneo.
 * Los botones "Inscribir mi equipo" y "Unirme a un equipo" llegan con MOV-0011 y MOV-0012: hasta
 * entonces la franja informa, sin botones que no lleven a ningún lado.
 */
@Composable
private fun FranjaDeAccion(torneo: TorneoDetalle, accion: AccionDelTorneo, acciones: AccionesTorneo, modifier: Modifier = Modifier) {
    Surface(
        color = colorDePanel(),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .testTag("torneo_franja"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
            when (accion) {
                AccionDelTorneo.EntrarParaInscribirse -> {
                    TextoDeFranja(stringResource(R.string.torneo_franja_entrar))
                    BotonPrimario(stringResource(R.string.torneo_entrar), acciones.onEntrar, Modifier.fillMaxWidth().testTag("torneo_entrar"))
                }
                AccionDelTorneo.Inscribirse -> {
                    TituloDeFranja(stringResource(R.string.torneo_franja_juegas))
                    TextoDeFranja(
                        if (torneo.cobraInscripcion && torneo.valorInscripcion != null && torneo.valorInscripcion.signum() > 0) {
                            stringResource(R.string.torneo_franja_abiertas_cobro, Formatos.pesos(torneo.valorInscripcion))
                        } else {
                            stringResource(R.string.torneo_franja_abiertas)
                        },
                    )
                }
                is AccionDelTorneo.Delegado -> {
                    FilaDeRelacion(stringResource(R.string.torneo_franja_delegas, accion.relacion.equipo), accion.relacion.estadoInscripcion)
                    if (accion.relacion.estadoInscripcion == EstadoInscripcion.PENDIENTE_PAGO) {
                        TextoDeFranja(stringResource(R.string.torneo_franja_pendiente))
                    }
                }
                is AccionDelTorneo.Juega -> {
                    FilaDeRelacion(stringResource(R.string.torneo_franja_juegas_en, accion.relacion.equipo), accion.relacion.estadoInscripcion)
                }
                AccionDelTorneo.Administra -> {
                    TextoDeFranja(stringResource(R.string.torneo_franja_administras))
                    BotonContorno(stringResource(R.string.torneo_abrir_creador), acciones.onAbrirCreador, Modifier.fillMaxWidth().testTag("torneo_abrir_creador"))
                }
                AccionDelTorneo.Arbitra -> {
                    TextoDeFranja(stringResource(R.string.torneo_franja_arbitras))
                    BotonContorno(stringResource(R.string.torneo_abrir_arbitro), acciones.onAbrirArbitro, Modifier.fillMaxWidth().testTag("torneo_abrir_arbitro"))
                }
                AccionDelTorneo.IndividualPendiente -> TextoDeFranja(stringResource(R.string.torneo_franja_individual))
                AccionDelTorneo.Cerradas -> TextoDeFranja(
                    stringResource(if (torneo.estado == EstadoTorneo.PUBLICADO) R.string.torneo_franja_cerradas else R.string.torneo_franja_ya_empezo),
                )
            }
        }
    }
}

@Composable
private fun TituloDeFranja(texto: String) {
    Text(texto.uppercase(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
}

@Composable
private fun TextoDeFranja(texto: String) {
    Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun FilaDeRelacion(texto: String, estado: EstadoInscripcion) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        ChipDeInscripcion(estado, Modifier.padding(start = 8.dp))
    }
}

// ── Pestañas ────────────────────────────────────────────────────────────────

private fun LazyListScope.equipos(torneo: TorneoDetalle) {
    if (torneo.equipos.isEmpty()) {
        item(key = "sin_equipos") {
            Text(
                text = stringResource(if (torneo.individual) R.string.torneo_sin_inscritos else R.string.torneo_sin_equipos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 28.dp),
            )
        }
        return
    }
    itemsIndexed(torneo.equipos, key = { _, e -> "equipo_${e.equipoTorneoId}" }) { indice, equipo ->
        if (indice > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 72.dp, end = 16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .semantics(mergeDescendants = true) { }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("torneo_equipo"),
        ) {
            Avatar(inicialDe(equipo.nombre))
            Text(equipo.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!torneo.individual) {
                Text(
                    pluralStringResource(R.plurals.torneo_jugadores, equipo.miembros, equipo.miembros),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun LazyListScope.partidos(partidos: List<PartidoDelTorneo>, hoy: LocalDate) {
    itemsIndexed(partidos, key = { _, p -> "partido_${p.id}" }) { indice, partido ->
        val faseAnterior = partidos.getOrNull(indice - 1)?.fase
        if (partido.fase.isNotBlank() && partido.fase != faseAnterior) {
            Text(
                text = partido.fase.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
                    .semantics { heading() },
            )
        } else if (indice > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 88.dp, end = 16.dp))
        }
        FilaPartidoDelTorneo(partido, hoy)
    }
}

/** Un partido del torneo como en un fixture: fecha y los dos equipos uno sobre otro con su cifra. */
@Composable
private fun FilaPartidoDelTorneo(partido: PartidoDelTorneo, hoy: LocalDate) {
    val fecha = partido.fechaHora
    val cerrado = partido.estado.cerrado
    val cifras = partido.marcador?.takeIf { partido.estado != EstadoPartido.PROGRAMADO }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .semantics(mergeDescendants = true) { }
            .padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
    ) {
        if (fecha == null) {
            BloqueFecha(dia = stringResource(R.string.fecha_sin_fecha), hora = null)
        } else {
            BloqueFecha(
                dia = Formatos.diaRelativo(fecha.toLocalDate(), hoy, stringResource(R.string.fecha_hoy), stringResource(R.string.fecha_manana)),
                hora = if (cerrado) null else Formatos.horaSinPeriodo(fecha.toLocalTime()),
                periodo = if (cerrado) null else Formatos.periodo(fecha.toLocalTime()),
                ahora = !cerrado && (fecha.toLocalDate() == hoy || partido.estado == EstadoPartido.EN_CURSO),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            val porDefinir = stringResource(R.string.partido_equipo_por_definir)
            LineaDeEquipo(partido.local ?: porDefinir, cifras?.local, atenuado = cifras != null && cifras.visitante > cifras.local)
            LineaDeEquipo(partido.visitante ?: porDefinir, cifras?.visitante, atenuado = cifras != null && cifras.local > cifras.visitante)
            partido.lugar?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        when (partido.estado) {
            EstadoPartido.EN_CURSO -> ChipEstado(stringResource(R.string.partido_en_juego), Modifier.padding(start = 8.dp), tono = Tono.AHORA)
            EstadoPartido.WO -> ChipEstado(stringResource(R.string.partido_wo), Modifier.padding(start = 8.dp))
            EstadoPartido.APLAZADO -> ChipEstado(stringResource(R.string.partido_aplazado), Modifier.padding(start = 8.dp), tono = Tono.ADVERTENCIA)
            else -> Unit
        }
    }
}

@Composable
private fun LineaDeEquipo(nombre: String, cifra: Int?, atenuado: Boolean) {
    val color = if (atenuado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(nombre, style = MaterialTheme.typography.titleMedium, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (cifra != null) {
            Text(
                text = cifra.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                ),
                color = color,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

/**
 * La tabla de posiciones. Las columnas son las comunes (PJ, G, P, Pts); el empate y la diferencia
 * solo en fútbol sala, donde existen.
 */
private fun LazyListScope.tabla(torneo: TorneoDetalle) {
    val conEmpates = torneo.deporte?.codigo == "FUTSAL"
    torneo.tabla.forEach { (grupo, filas) ->
        item(key = "grupo_$grupo") {
            if (torneo.tabla.size > 1) {
                Text(
                    text = stringResource(R.string.torneo_grupo, grupo).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                )
            }
            FilaDeTabla(
                posicion = "#", equipo = stringResource(R.string.torneo_tabla_equipo),
                cifras = columnas(conEmpates).map { stringResource(it.first) }, encabezado = true,
            )
        }
        itemsIndexed(filas, key = { _, f -> "fila_${grupo}_${f.equipoTorneoId}" }) { _, fila ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            FilaDeTabla(fila.posicion.toString(), fila.equipo, columnas(conEmpates).map { it.second(fila).toString() }, encabezado = false)
        }
    }
}

private fun columnas(conEmpates: Boolean): List<Pair<Int, (FilaTabla) -> Int>> = buildList {
    add(R.string.torneo_tabla_pj to { f: FilaTabla -> f.jugados })
    add(R.string.torneo_tabla_g to { f: FilaTabla -> f.ganados })
    if (conEmpates) add(R.string.torneo_tabla_e to { f: FilaTabla -> f.empatados })
    add(R.string.torneo_tabla_p to { f: FilaTabla -> f.perdidos })
    if (conEmpates) add(R.string.torneo_tabla_dg to { f: FilaTabla -> f.diferencia })
    add(R.string.torneo_tabla_pts to { f: FilaTabla -> f.puntos })
}

@Composable
private fun FilaDeTabla(posicion: String, equipo: String, cifras: List<String>, encabezado: Boolean) {
    val estilo = if (encabezado) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
    val color = if (encabezado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (encabezado) 36.dp else 48.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(posicion, style = estilo, color = color, modifier = Modifier.width(28.dp))
        Text(equipo, style = if (encabezado) estilo else MaterialTheme.typography.titleSmall, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        cifras.forEachIndexed { i, cifra ->
            val ultima = i == cifras.lastIndex
            Text(
                text = cifra,
                style = if (ultima && !encabezado) estilo.copy(fontWeight = FontWeight.Bold) else estilo,
                color = color,
                textAlign = TextAlign.End,
                modifier = Modifier.width(if (ultima) 40.dp else 32.dp),
            )
        }
    }
}

@Composable
private fun Informacion(torneo: TorneoDetalle) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        BloqueDeInfo(stringResource(R.string.torneo_reglas), torneo.reglas ?: stringResource(R.string.torneo_sin_reglas))
        if (torneo.premios.isNotEmpty()) {
            BloqueDeInfo(
                stringResource(R.string.torneo_premios),
                torneo.premios.joinToString("\n") { p -> if (p.ganador != null) "${p.nombre}: ${p.ganador}" else p.nombre },
            )
        }
        if (torneo.exigeDocumento) BloqueDeInfo(stringResource(R.string.torneo_documento), stringResource(R.string.torneo_documento_detalle))
    }
}

@Composable
private fun BloqueDeInfo(titulo: String, texto: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo.uppercase(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.semantics { heading() })
        Text(texto, style = MaterialTheme.typography.bodyLarge)
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 10)

internal val torneoDeEjemplo = TorneoDetalle(
    id = "t1", nombre = "Copa Prueba 2026", deporte = Deporte("FUTSAL", "Fútbol sala", TipoCompetencia.EQUIPO),
    formato = FormatoTorneo.LIGA, estado = EstadoTorneo.PUBLICADO, visibilidad = Visibilidad.PUBLICO,
    fechaInicio = HOY.plusDays(7), fechaFin = null, cierreInscripciones = HOY.plusDays(4), cupos = 16, inscritos = 2,
    inscripcionesAbiertas = true, cobraInscripcion = true, valorInscripcion = BigDecimal("50000"), exigeDocumento = false,
    reglas = null, organizador = "Andrés Gómez", enlace = "https://desafio.sports/t/abc",
    equipos = listOf(EquipoInscrito("e1", "Halcones FC", 6), EquipoInscrito("e2", "Tiburones FC", 6)),
    tabla = emptyMap(),
    partidos = listOf(
        PartidoDelTorneo("p1", "Fecha 1", "Halcones FC", "Tiburones FC", LocalDateTime.of(2026, 11, 3, 16, 0), "Cancha 1", EstadoPartido.FINALIZADO, Marcador(2, 1)),
    ),
    premios = emptyList(),
    miRelacion = MiRelacion(administra = false, arbitra = false, delegadoDe = null, juegaEn = null),
)

@Preview(name = "Torneo · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 900)
@Preview(name = "Torneo · claro", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun TorneoPreview() {
    DesafioSportsTheme {
        TorneoContenido(TorneoState(cargando = false, torneo = torneoDeEjemplo, conSesion = true), HOY, AccionesTorneo(), {})
    }
}

@Preview(name = "Torneo · delegado", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun TorneoDelegadoPreview() {
    DesafioSportsTheme {
        val relacion = RelacionConEquipo("e1", "Halcones FC", EstadoInscripcion.PENDIENTE_PAGO, null)
        TorneoContenido(
            TorneoState(cargando = false, conSesion = true, torneo = torneoDeEjemplo.copy(miRelacion = MiRelacion(false, false, relacion, null))),
            HOY, AccionesTorneo(), {},
        )
    }
}

@Preview(name = "Torneo · error", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TorneoErrorPreview() {
    DesafioSportsTheme {
        TorneoContenido(TorneoState(cargando = false, error = ErrorDePantalla("No encontramos ese torneo.")), HOY, AccionesTorneo(), {})
    }
}
