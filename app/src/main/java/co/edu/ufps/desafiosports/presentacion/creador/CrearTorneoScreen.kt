package co.edu.ufps.desafiosports.presentacion.creador

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.domain.model.BorradorTorneo
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.TorneoCreado
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.presentacion.acceso.AvisoErrorAnimado
import co.edu.ufps.desafiosports.presentacion.acceso.BotonPrincipal
import co.edu.ufps.desafiosports.presentacion.acceso.BotonSecundario
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.BotonPrimario
import co.edu.ufps.desafiosports.presentacion.comun.Cargando
import co.edu.ufps.desafiosports.presentacion.comun.EstadoError
import co.edu.ufps.desafiosports.ui.deporte.iconoDeDeporte
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import co.edu.ufps.desafiosports.ui.theme.coloresExtra
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo

/** Lo que "Crear torneo" le pide al ViewModel. */
data class AccionesCrearTorneo(
    val onReintentarDeportes: () -> Unit = {},
    val onDeporte: (Deporte) -> Unit = {},
    val onNombre: (String) -> Unit = {},
    val onFormato: (FormatoTorneo) -> Unit = {},
    val onCupos: (diferencia: Int) -> Unit = {},
    val onFecha: (LocalDate) -> Unit = {},
    val onVisibilidad: (Visibilidad) -> Unit = {},
    val onCobra: (Boolean) -> Unit = {},
    val onValor: (String) -> Unit = {},
    val onSiguiente: () -> Unit = {},
    val onAtras: () -> Unit = {},
    val onCerrar: () -> Unit = {},
    val onCancelarSalida: () -> Unit = {},
    val onSalir: () -> Unit = {},
    val onCompartir: (TorneoCreado) -> Unit = {},
    val onVerMisTorneos: () -> Unit = {},
)

/** Crear torneo en tres pasos (MOV-0007). */
@Composable
fun CrearTorneoScreen(
    onSalir: () -> Unit,
    onVerMisTorneos: () -> Unit,
    viewModel: CrearTorneoViewModel = viewModel(factory = CrearTorneoViewModel.Factory),
) {
    val contexto = LocalContext.current
    val state = viewModel.state
    val acciones = remember(viewModel, onSalir, onVerMisTorneos, contexto) {
        AccionesCrearTorneo(
            onReintentarDeportes = viewModel::cargarDeportes,
            onDeporte = viewModel::elegirDeporte,
            onNombre = viewModel::onNombreChange,
            onFormato = viewModel::elegirFormato,
            onCupos = viewModel::cambiarCupos,
            onFecha = viewModel::elegirFecha,
            onVisibilidad = viewModel::elegirVisibilidad,
            onCobra = viewModel::onCobraChange,
            onValor = viewModel::onValorChange,
            onSiguiente = viewModel::siguiente,
            onAtras = { if (viewModel.atras()) onSalir() },
            onCerrar = { if (viewModel.pedirSalida()) onSalir() },
            onCancelarSalida = viewModel::cancelarSalida,
            onSalir = onSalir,
            onCompartir = { torneo -> compartirTorneo(contexto, torneo) },
            onVerMisTorneos = onVerMisTorneos,
        )
    }
    // Con el torneo ya creado, Atrás no vuelve al formulario: va a "Mis torneos".
    BackHandler { if (state.creado != null) onVerMisTorneos() else acciones.onAtras() }
    CrearTorneoContenido(state = state, acciones = acciones)
}

/** Abre la hoja de compartir de Android con el nombre del torneo y su enlace. */
private fun compartirTorneo(contexto: Context, torneo: TorneoCreado) {
    val texto = contexto.getString(R.string.crear_compartir_texto, torneo.nombre, torneo.enlace)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(Intent.createChooser(intent, null))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CrearTorneoContenido(state: CrearTorneoState, acciones: AccionesCrearTorneo, modifier: Modifier = Modifier) {
    val creado = state.creado
    if (creado != null) {
        TorneoListo(creado, state.borrador, state.hoy, acciones, modifier)
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.crear_titulo)) },
                    navigationIcon = {
                        IconButton(onClick = acciones.onCerrar, modifier = Modifier.testTag("crear_cerrar")) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.crear_salir_sin_crear))
                        }
                    },
                    actions = {
                        Text(
                            text = stringResource(R.string.crear_paso, state.paso, BorradorTorneo.PASOS),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .testTag("crear_paso"),
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
                LinearProgressIndicator(
                    progress = { state.paso / BorradorTorneo.PASOS.toFloat() },
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    drawStopIndicator = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        bottomBar = {
            if (state.deportes != null) BarraDePasos(state, acciones)
        },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .imePadding(),
        ) {
            when {
                state.deportes != null -> Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                ) {
                    when (state.paso) {
                        1 -> PasoDeporte(state.deportes, state.borrador.deporte, acciones.onDeporte)
                        2 -> PasoNombreYFormato(state, acciones)
                        else -> PasoVisibilidad(state, acciones)
                    }
                }
                state.errorDeportes != null ->
                    EstadoError(state.errorDeportes, acciones.onReintentarDeportes, testTagBoton = "crear_reintentar")
                else -> Cargando()
            }
        }
    }

    if (state.confirmandoSalida) {
        AlertDialog(
            onDismissRequest = acciones.onCancelarSalida,
            title = { Text(stringResource(R.string.crear_salir_titulo)) },
            text = { Text(stringResource(R.string.crear_salir_detalle)) },
            confirmButton = {
                TextButton(onClick = acciones.onSalir, modifier = Modifier.testTag("crear_salir_confirmar")) {
                    Text(stringResource(R.string.crear_salir), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = acciones.onCancelarSalida, modifier = Modifier.testTag("crear_salir_cancelar")) {
                    Text(stringResource(R.string.crear_seguir), color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }
}

// ── Barra inferior ──────────────────────────────────────────────────────────

@Composable
private fun BarraDePasos(state: CrearTorneoState, acciones: AccionesCrearTorneo) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                AvisoErrorAnimado(state.error, Modifier.testTag("crear_error"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (state.paso > 1) {
                        BotonContorno(
                            texto = stringResource(R.string.accion_atras),
                            onClick = acciones.onAtras,
                            habilitado = !state.creando,
                            modifier = Modifier
                                .heightIn(min = 52.dp)
                                .testTag("crear_atras"),
                        )
                    }
                    BotonPrimario(
                        texto = stringResource(if (state.esUltimoPaso) R.string.crear_crear else R.string.crear_siguiente),
                        textoEnProceso = stringResource(R.string.crear_creando),
                        habilitado = state.puedeSeguir,
                        enProceso = state.creando,
                        onClick = acciones.onSiguiente,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("crear_siguiente"),
                    )
                }
            }
        }
    }
}

// ── Piezas comunes de los pasos ─────────────────────────────────────────────

@Composable
private fun Pregunta(titulo: String, detalle: String? = null) {
    Text(
        text = titulo.uppercase(),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(top = 20.dp)
            .semantics { heading() },
    )
    if (detalle != null) {
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    Spacer(Modifier.size(16.dp))
}

@Composable
private fun Subtitulo(texto: String) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(top = 20.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

/**
 * Opción de una lista de selección única, como tarjeta (MOV-0010): toda la tarjeta es el control.
 * La elegida sube un escalón de contenedor y lleva contorno en tinta y la marca de elegida, igual
 * que las tarjetas de deporte del paso 1.
 */
@Composable
private fun OpcionTarjeta(
    titulo: String,
    detalle: String,
    elegida: Boolean,
    onElegir: () -> Unit,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true,
    inicio: @Composable (color: Color) -> Unit,
) {
    val tinta = if (elegida) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        selected = elegida,
        onClick = onElegir,
        enabled = habilitada,
        shape = MaterialTheme.shapes.large,
        color = if (elegida) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (elegida) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .semantics { role = Role.RadioButton },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Box(Modifier.size(width = 48.dp, height = 40.dp), contentAlignment = Alignment.Center) { inicio(tinta) }
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(Modifier.size(20.dp)) {
                if (elegida) Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * El esquema de cada formato, dibujado: liga = todos contra todos (cada punto unido con todos),
 * eliminación = llaves que se cierran en un campeón, grupos = dos grupos que alimentan las llaves.
 * Es decorativo: el nombre y la explicación van al lado en texto.
 */
@Composable
private fun EsquemaFormato(formato: FormatoTorneo, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(width = 48.dp, height = 40.dp)) {
        val grosor = 2.dp.toPx()
        val punto = 3.dp.toPx()
        val w = size.width
        val h = size.height
        when (formato) {
            FormatoTorneo.LIGA -> {
                val r = minOf(w, h) / 2 - punto
                val centro = Offset(w / 2, h / 2)
                val puntos = (0 until 5).map { i ->
                    val angulo = Math.toRadians(-90.0 + i * 72.0)
                    Offset(centro.x + r * kotlin.math.cos(angulo).toFloat(), centro.y + r * kotlin.math.sin(angulo).toFloat())
                }
                for (i in puntos.indices) {
                    for (j in i + 1 until puntos.size) drawLine(color.copy(alpha = 0.55f), puntos[i], puntos[j], grosor * 0.6f)
                }
                puntos.forEach { drawCircle(color, punto, it) }
            }
            FormatoTorneo.ELIMINACION_DIRECTA -> llaves(color, grosor, x0 = 2.dp.toPx(), ancho = w - 6.dp.toPx(), alto = h)
            FormatoTorneo.GRUPOS_ELIMINATORIAS -> {
                val lado = h * 0.36f
                val izquierda = 1.dp.toPx()
                val arriba = h * 0.08f
                val abajo = h * 0.56f
                val radio = CornerRadius(3.dp.toPx())
                drawRoundRect(color, Offset(izquierda, arriba), Size(lado, lado), radio, style = Stroke(grosor))
                drawRoundRect(color, Offset(izquierda, abajo), Size(lado, lado), radio, style = Stroke(grosor))
                val xLlave = izquierda + lado + 8.dp.toPx()
                drawLine(color, Offset(izquierda + lado, arriba + lado / 2), Offset(xLlave, arriba + lado / 2), grosor)
                drawLine(color, Offset(izquierda + lado, abajo + lado / 2), Offset(xLlave, abajo + lado / 2), grosor)
                drawLine(color, Offset(xLlave, arriba + lado / 2), Offset(xLlave, abajo + lado / 2), grosor)
                drawLine(color, Offset(xLlave, h / 2), Offset(w - 4.dp.toPx(), h / 2), grosor)
                drawCircle(color, punto, Offset(w - 4.dp.toPx(), h / 2))
            }
        }
    }
}

/** Llaves de 4 a 2 a 1, de izquierda a derecha, con el campeón en un punto. */
private fun DrawScope.llaves(color: Color, grosor: Float, x0: Float, ancho: Float, alto: Float) {
    val filas = listOf(0.1f, 0.36f, 0.64f, 0.9f).map { it * alto }
    val x1 = x0 + ancho * 0.36f
    val x2 = x0 + ancho * 0.7f
    val x3 = x0 + ancho
    for ((uno, dos) in listOf(filas[0] to filas[1], filas[2] to filas[3])) {
        drawLine(color, Offset(x0, uno), Offset(x1, uno), grosor)
        drawLine(color, Offset(x0, dos), Offset(x1, dos), grosor)
        drawLine(color, Offset(x1, uno), Offset(x1, dos), grosor)
    }
    val m1 = (filas[0] + filas[1]) / 2
    val m2 = (filas[2] + filas[3]) / 2
    drawLine(color, Offset(x1, m1), Offset(x2, m1), grosor)
    drawLine(color, Offset(x1, m2), Offset(x2, m2), grosor)
    drawLine(color, Offset(x2, m1), Offset(x2, m2), grosor)
    drawLine(color, Offset(x2, alto / 2), Offset(x3, alto / 2), grosor)
    drawCircle(color, 3.dp.toPx(), Offset(x3, alto / 2))
}

// ── Paso 1 · deporte ────────────────────────────────────────────────────────

@Composable
private fun PasoDeporte(deportes: List<Deporte>, elegido: Deporte?, onDeporte: (Deporte) -> Unit) {
    Pregunta(stringResource(R.string.crear_p1_titulo), stringResource(R.string.crear_p1_detalle))
    if (deportes.isEmpty()) {
        Text(
            text = stringResource(R.string.crear_p1_sin_deportes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
        deportes.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                fila.forEach { deporte ->
                    TarjetaDeporte(
                        deporte = deporte,
                        elegido = elegido?.codigo == deporte.codigo,
                        onElegir = { onDeporte(deporte) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TarjetaDeporte(deporte: Deporte, elegido: Boolean, onElegir: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        selected = elegido,
        onClick = onElegir,
        shape = MaterialTheme.shapes.large,
        color = if (elegido) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (elegido) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
        modifier = modifier
            .heightIn(min = 112.dp)
            .semantics { role = Role.RadioButton }
            .testTag("deporte_${deporte.codigo.lowercase()}"),
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(iconoDeDeporte(deporte.codigo)), contentDescription = null, modifier = Modifier.size(30.dp))
                Spacer(Modifier.weight(1f))
                if (elegido) Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.padding(top = 20.dp)) {
                Text(deporte.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(if (deporte.tipo == TipoCompetencia.INDIVIDUAL) R.string.crear_individual else R.string.crear_por_equipos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Paso 2 · nombre y formato ───────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasoNombreYFormato(state: CrearTorneoState, acciones: AccionesCrearTorneo) {
    val borrador = state.borrador
    var eligiendoFecha by rememberSaveable { mutableStateOf(false) }
    val nombreCorto = borrador.nombre.isNotBlank() && !borrador.nombreValido

    Pregunta(stringResource(R.string.crear_p2_titulo))
    OutlinedTextField(
        value = borrador.nombre,
        onValueChange = acciones.onNombre,
        label = { Text(stringResource(R.string.crear_nombre)) },
        singleLine = true,
        enabled = !state.creando,
        isError = nombreCorto,
        supportingText = {
            Text(
                if (nombreCorto) stringResource(R.string.crear_nombre_corto)
                else stringResource(R.string.crear_nombre_ayuda, borrador.deporte?.nombre.orEmpty()),
            )
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crear_nombre"),
    )

    Subtitulo(stringResource(R.string.crear_formato))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
        FormatoTorneo.entries.forEach { formato ->
            val (titulo, detalle) = when (formato) {
                FormatoTorneo.LIGA -> R.string.formato_liga to R.string.formato_liga_detalle
                FormatoTorneo.ELIMINACION_DIRECTA -> R.string.formato_eliminacion to R.string.formato_eliminacion_detalle
                FormatoTorneo.GRUPOS_ELIMINATORIAS -> R.string.formato_grupos to R.string.formato_grupos_detalle
            }
            OpcionTarjeta(
                titulo = stringResource(titulo),
                detalle = stringResource(detalle),
                elegida = borrador.formato == formato,
                onElegir = { acciones.onFormato(formato) },
                habilitada = !state.creando,
                modifier = Modifier.testTag("formato_${formato.name.lowercase()}"),
            ) { color -> EsquemaFormato(formato, color) }
        }
    }

    Subtitulo(stringResource(R.string.crear_cupos))
    val individual = borrador.deporte?.tipo == TipoCompetencia.INDIVIDUAL
    val lecturaCupos = pluralStringResource(
        if (individual) R.plurals.crear_cupos_jugadores else R.plurals.crear_cupos_equipos, borrador.cupos, borrador.cupos,
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { stateDescription = lecturaCupos },
        ) {
            Text(
                text = borrador.cupos.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                modifier = Modifier.testTag("crear_cupos"),
            )
            Text(
                text = stringResource(
                    if (individual) R.string.crear_cupos_jugadores_rango else R.string.crear_cupos_equipos_rango,
                    borrador.cuposMinimos, borrador.cuposMaximos,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val coloresPaso = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
        FilledTonalIconButton(
            onClick = { acciones.onCupos(-1) },
            enabled = borrador.cupos > borrador.cuposMinimos && !state.creando,
            colors = coloresPaso,
            modifier = Modifier
                .size(48.dp)
                .testTag("crear_cupos_menos"),
        ) { Icon(painterResource(R.drawable.ic_remove), contentDescription = stringResource(R.string.crear_cupos_menos)) }
        Spacer(Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = { acciones.onCupos(1) },
            enabled = borrador.cupos < borrador.cuposMaximos && !state.creando,
            colors = coloresPaso,
            modifier = Modifier
                .size(48.dp)
                .testTag("crear_cupos_mas"),
        ) { Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.crear_cupos_mas)) }
    }

    Subtitulo(stringResource(R.string.crear_empieza))
    val fecha = borrador.fechaInicio
    Surface(
        onClick = { eligiendoFecha = true },
        enabled = !state.creando,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("crear_fecha"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = fecha?.let { Formatos.fecha(it, state.hoy) } ?: stringResource(R.string.crear_elegir_fecha),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(painterResource(R.drawable.ic_event), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (eligiendoFecha) {
        val hoyMillis = remember(state.hoy) { state.hoy.aMillis() }
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = (fecha ?: state.hoy).aMillis(),
            selectableDates = object : SelectableDates {
                // El backend no acepta fechas pasadas.
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= hoyMillis
                override fun isSelectableYear(year: Int): Boolean = year >= state.hoy.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { eligiendoFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let { acciones.onFecha(it.aFecha()) }
                        eligiendoFecha = false
                    },
                    enabled = estado.selectedDateMillis != null,
                    modifier = Modifier.testTag("crear_fecha_aceptar"),
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
}

/** El selector de fechas de Material trabaja en milisegundos UTC. */
private fun LocalDate.aMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.aFecha(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

// ── Paso 3 · visibilidad y cobro ────────────────────────────────────────────

@Composable
private fun PasoVisibilidad(state: CrearTorneoState, acciones: AccionesCrearTorneo) {
    val borrador = state.borrador
    Pregunta(stringResource(R.string.crear_p3_titulo), stringResource(R.string.crear_p3_detalle))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
        Visibilidad.entries.forEach { visibilidad ->
            OpcionTarjeta(
                titulo = stringResource(visibilidad.titulo()),
                detalle = stringResource(visibilidad.detalle()),
                elegida = borrador.visibilidad == visibilidad,
                onElegir = { acciones.onVisibilidad(visibilidad) },
                habilitada = !state.creando,
                modifier = Modifier.testTag("visibilidad_${visibilidad.name.lowercase()}"),
            ) { color ->
                Icon(painterResource(visibilidad.icono()), contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(value = borrador.cobraInscripcion, enabled = !state.creando, role = Role.Switch, onValueChange = acciones.onCobra)
            .padding(vertical = 8.dp)
            .testTag("crear_cobra"),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.crear_cobrar), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.crear_cobrar_detalle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = borrador.cobraInscripcion, onCheckedChange = null, enabled = !state.creando)
    }
    if (borrador.cobraInscripcion) {
        val valor = borrador.valorInscripcion
        OutlinedTextField(
            value = state.valorTexto,
            onValueChange = acciones.onValor,
            label = { Text(stringResource(R.string.crear_valor)) },
            prefix = { Text("$") },
            singleLine = true,
            enabled = !state.creando,
            supportingText = {
                Text(
                    if (valor != null && valor > 0) stringResource(R.string.crear_valor_lectura, Formatos.pesos(valor))
                    else stringResource(R.string.crear_valor_ayuda),
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("crear_valor"),
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(14.dp)) {
            Icon(painterResource(R.drawable.ic_info), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(stringResource(R.string.crear_despues), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

internal fun Visibilidad.titulo(): Int = when (this) {
    Visibilidad.PUBLICO -> R.string.visibilidad_publico
    Visibilidad.NO_LISTADO -> R.string.visibilidad_no_listado
    Visibilidad.PRIVADO -> R.string.visibilidad_privado
}

private fun Visibilidad.detalle(): Int = when (this) {
    Visibilidad.PUBLICO -> R.string.visibilidad_publico_detalle
    Visibilidad.NO_LISTADO -> R.string.visibilidad_no_listado_detalle
    Visibilidad.PRIVADO -> R.string.visibilidad_privado_detalle
}

@DrawableRes
internal fun Visibilidad.icono(): Int = when (this) {
    Visibilidad.PUBLICO -> R.drawable.ic_public
    Visibilidad.NO_LISTADO -> R.drawable.ic_link
    Visibilidad.PRIVADO -> R.drawable.ic_lock
}

// ── Torneo creado ───────────────────────────────────────────────────────────

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TorneoListo(
    torneo: TorneoCreado,
    borrador: BorradorTorneo,
    hoy: LocalDate,
    acciones: AccionesCrearTorneo,
    modifier: Modifier = Modifier,
) {
    val mensaje = when (torneo.visibilidad) {
        Visibilidad.PUBLICO -> stringResource(R.string.creado_publico)
        Visibilidad.NO_LISTADO -> stringResource(R.string.creado_no_listado)
        Visibilidad.PRIVADO -> stringResource(R.string.creado_privado)
        null -> stringResource(R.string.creado_generico)
    }
    val lectura = stringResource(R.string.creado_titulo)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(relleno).fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 48.dp)
                    .testTag("creado"),
            ) {
                Icon(
                    painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = MaterialTheme.coloresExtra.exito,
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = lectura.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        heading()
                        contentDescription = lectura
                    },
                )
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                FichaDelTorneo(torneo, borrador, hoy, Modifier.padding(vertical = 12.dp))
                if (torneo.enlace.isNotBlank()) {
                    BotonPrincipal(
                        texto = stringResource(R.string.creado_compartir),
                        textoEnProceso = stringResource(R.string.creado_compartir),
                        enProceso = false,
                        onClick = { acciones.onCompartir(torneo) },
                        modifier = Modifier.testTag("creado_compartir"),
                    )
                }
                BotonSecundario(
                    texto = stringResource(R.string.creado_ver_mis_torneos),
                    onClick = acciones.onVerMisTorneos,
                    modifier = Modifier.testTag("creado_mis_torneos"),
                )
            }
        }
    }
}

/**
 * La ficha del torneo recién creado (MOV-0010): el nombre en la condensada y lo que se eligió en
 * los tres pasos, para confirmarlo de un vistazo antes de compartir.
 */
@Composable
private fun FichaDelTorneo(torneo: TorneoCreado, borrador: BorradorTorneo, hoy: LocalDate, modifier: Modifier = Modifier) {
    val formato = when (borrador.formato) {
        FormatoTorneo.LIGA -> stringResource(R.string.formato_liga)
        FormatoTorneo.ELIMINACION_DIRECTA -> stringResource(R.string.formato_eliminacion)
        FormatoTorneo.GRUPOS_ELIMINATORIAS -> stringResource(R.string.formato_grupos)
        null -> null
    }
    val individual = borrador.deporte?.tipo == TipoCompetencia.INDIVIDUAL
    val cupos = pluralStringResource(
        if (individual) R.plurals.crear_cupos_jugadores else R.plurals.crear_cupos_equipos, borrador.cupos, borrador.cupos,
    )
    val visibilidad = (torneo.visibilidad ?: borrador.visibilidad)?.let { stringResource(it.titulo()) }
    val valor = borrador.valorInscripcion?.takeIf { borrador.cobraInscripcion && it > 0 }
    val filas = listOfNotNull(
        borrador.deporte?.let { stringResource(R.string.creado_ficha_deporte) to it.nombre },
        formato?.let { stringResource(R.string.creado_ficha_formato) to it },
        stringResource(R.string.creado_ficha_cupos) to cupos,
        borrador.fechaInicio?.let { stringResource(R.string.creado_ficha_empieza) to Formatos.fecha(it, hoy) },
        visibilidad?.let { stringResource(R.string.creado_ficha_visibilidad) to it },
        stringResource(R.string.creado_ficha_inscripcion) to (valor?.let { Formatos.pesos(it) } ?: stringResource(R.string.creado_ficha_gratis)),
    )
    Surface(
        color = colorDePanel(),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .testTag("creado_ficha"),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconoEnCirculo(iconoDeDeporte(torneo.deporte.ifBlank { borrador.deporte?.codigo.orEmpty() }))
                Text(
                    text = torneo.nombre.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filas.forEach { (etiqueta, texto) ->
                    Row(Modifier.semantics(mergeDescendants = true) { }) {
                        Text(
                            text = etiqueta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(texto, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 9)

private val deportesDeEjemplo = listOf(
    Deporte("FUTSAL", "Fútbol sala", TipoCompetencia.EQUIPO),
    Deporte("VOLEIBOL", "Voleibol", TipoCompetencia.EQUIPO),
    Deporte("BALONCESTO", "Baloncesto", TipoCompetencia.EQUIPO),
    Deporte("TENIS_MESA", "Tenis de mesa", TipoCompetencia.INDIVIDUAL),
)

private val borradorDeEjemplo = BorradorTorneo(
    deporte = deportesDeEjemplo.first(),
    nombre = "Relámpago de Fútbol sala",
    formato = FormatoTorneo.ELIMINACION_DIRECTA,
    cupos = 8,
    fechaInicio = HOY.plusDays(7),
    visibilidad = Visibilidad.PUBLICO,
    cobraInscripcion = true,
    valorInscripcion = 50_000,
)

@Preview(name = "Crear · paso 1 · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Crear · paso 1 · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CrearPaso1Preview() {
    DesafioSportsTheme {
        CrearTorneoContenido(
            CrearTorneoState(hoy = HOY, deportes = deportesDeEjemplo, cargandoDeportes = false, borrador = BorradorTorneo(deporte = deportesDeEjemplo.first())),
            AccionesCrearTorneo(),
        )
    }
}

@Preview(name = "Crear · paso 2", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 1000)
@Composable
private fun CrearPaso2Preview() {
    DesafioSportsTheme {
        CrearTorneoContenido(
            CrearTorneoState(hoy = HOY, paso = 2, deportes = deportesDeEjemplo, cargandoDeportes = false, borrador = borradorDeEjemplo),
            AccionesCrearTorneo(),
        )
    }
}

@Preview(name = "Crear · paso 3", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 1000)
@Composable
private fun CrearPaso3Preview() {
    DesafioSportsTheme {
        CrearTorneoContenido(
            CrearTorneoState(hoy = HOY, paso = 3, deportes = deportesDeEjemplo, cargandoDeportes = false, borrador = borradorDeEjemplo, valorTexto = "50000"),
            AccionesCrearTorneo(),
        )
    }
}

@Preview(name = "Crear · listo", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CrearListoPreview() {
    DesafioSportsTheme {
        CrearTorneoContenido(
            CrearTorneoState(hoy = HOY, creado = TorneoCreado("t1", "Relámpago de Fútbol sala", "FUTSAL", Visibilidad.PUBLICO, "https://desafio.sports/t/abc")),
            AccionesCrearTorneo(),
        )
    }
}
