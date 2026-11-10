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
        TorneoListo(creado, acciones, modifier)
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

/** Opción de una lista de selección única: toda la fila es el control. */
@Composable
private fun OpcionRadio(
    titulo: String,
    detalle: String,
    elegida: Boolean,
    onElegir: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icono: Int? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .selectable(selected = elegida, role = Role.RadioButton, onClick = onElegir)
            .padding(vertical = 8.dp),
    ) {
        RadioButton(selected = elegida, onClick = null)
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (icono != null) {
            Icon(painterResource(icono), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
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
    Column(Modifier.selectableGroup()) {
        FormatoTorneo.entries.forEach { formato ->
            val (titulo, detalle) = when (formato) {
                FormatoTorneo.LIGA -> R.string.formato_liga to R.string.formato_liga_detalle
                FormatoTorneo.ELIMINACION_DIRECTA -> R.string.formato_eliminacion to R.string.formato_eliminacion_detalle
                FormatoTorneo.GRUPOS_ELIMINATORIAS -> R.string.formato_grupos to R.string.formato_grupos_detalle
            }
            OpcionRadio(
                titulo = stringResource(titulo),
                detalle = stringResource(detalle),
                elegida = borrador.formato == formato,
                onElegir = { acciones.onFormato(formato) },
                modifier = Modifier.testTag("formato_${formato.name.lowercase()}"),
            )
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
        IconButton(
            onClick = { acciones.onCupos(-1) },
            enabled = borrador.cupos > borrador.cuposMinimos && !state.creando,
            modifier = Modifier.testTag("crear_cupos_menos"),
        ) { Icon(painterResource(R.drawable.ic_remove), contentDescription = stringResource(R.string.crear_cupos_menos)) }
        IconButton(
            onClick = { acciones.onCupos(1) },
            enabled = borrador.cupos < borrador.cuposMaximos && !state.creando,
            modifier = Modifier.testTag("crear_cupos_mas"),
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
    Column(Modifier.selectableGroup()) {
        Visibilidad.entries.forEach { visibilidad ->
            OpcionRadio(
                titulo = stringResource(visibilidad.titulo()),
                detalle = stringResource(visibilidad.detalle()),
                icono = visibilidad.icono(),
                elegida = borrador.visibilidad == visibilidad,
                onElegir = { acciones.onVisibilidad(visibilidad) },
                modifier = Modifier.testTag("visibilidad_${visibilidad.name.lowercase()}"),
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
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
private fun TorneoListo(torneo: TorneoCreado, acciones: AccionesCrearTorneo, modifier: Modifier = Modifier) {
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
                Text(torneo.nombre, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(8.dp))
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
