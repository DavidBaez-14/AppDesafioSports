package co.edu.ufps.desafiosports.presentacion.perfil

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
import co.edu.ufps.desafiosports.domain.model.EstadoMulta
import co.edu.ufps.desafiosports.domain.model.Multa
import co.edu.ufps.desafiosports.domain.model.PasoPorEquipo
import co.edu.ufps.desafiosports.domain.model.Perfil
import co.edu.ufps.desafiosports.domain.model.Puesto
import co.edu.ufps.desafiosports.domain.model.Tarjetas
import co.edu.ufps.desafiosports.presentacion.comun.Avatar
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.ChipEstado
import co.edu.ufps.desafiosports.presentacion.comun.EncabezadoBloque
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.EsqueletoDeLista
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.Tono
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.presentacion.inicio.Carga
import co.edu.ufps.desafiosports.ui.formato.Formatos
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/** Lo que Perfil le pide a la navegación y al ViewModel. */
data class AccionesPerfil(
    val onReintentar: () -> Unit = {},
    val onRefrescar: () -> Unit = {},
    val onCerrarSesion: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
    /** Solo en debug: el diagnóstico de conexión de MOV-0001. */
    val onAbrirDiagnostico: (() -> Unit)? = null,
    /** Aceptar una invitación pegando su enlace (MOV-0011). */
    val onAbrirInvitacion: () -> Unit = {},
)

/** Perfil (MOV-0004). Tercera pestaña de la pantalla principal. */
@Composable
fun PerfilScreen(
    onAbrirDiagnostico: (() -> Unit)?,
    onAbrirInvitacion: () -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory),
) {
    LifecycleResumeEffect(Unit) {
        viewModel.alVolver()
        onPauseOrDispose { }
    }
    val acciones = remember(viewModel, onAbrirDiagnostico, onAbrirInvitacion) {
        AccionesPerfil(
            onReintentar = viewModel::cargar,
            onRefrescar = viewModel::refrescar,
            onCerrarSesion = viewModel::cerrarSesion,
            onAvisoMostrado = viewModel::onAvisoMostrado,
            onAbrirDiagnostico = onAbrirDiagnostico,
            onAbrirInvitacion = onAbrirInvitacion,
        )
    }
    PerfilContenido(state = viewModel.state, hoy = LocalDate.now(), acciones = acciones)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PerfilContenido(state: PerfilState, hoy: LocalDate, acciones: AccionesPerfil, modifier: Modifier = Modifier) {
    val avisos = remember { SnackbarHostState() }
    LaunchedEffect(state.aviso) {
        val aviso = state.aviso ?: return@LaunchedEffect
        avisos.showSnackbar(aviso)
        acciones.onAvisoMostrado()
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.perfil_titulo)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(avisos) },
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
                contentPadding = PaddingValues(bottom = 32.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("perfil_lista"),
            ) {
                item(key = "encabezado") { Encabezado(state) }
                when (state.carga) {
                    Carga.CARGANDO -> item(key = "cargando") {
                        EsqueletoDeLista(Modifier.heightIn(max = 360.dp).testTag("perfil_cargando"), bloques = 2)
                    }
                    Carga.ERROR -> item(key = "error") {
                        val error = state.error ?: ErrorDePantalla(stringResource(R.string.estado_error))
                        EstadoVacio(
                            icono = if (error.sinConexion) R.drawable.ic_wifi_off else R.drawable.ic_error,
                            titulo = stringResource(if (error.sinConexion) R.string.estado_sin_conexion else R.string.estado_error),
                            detalle = error.mensaje,
                            accion = {
                                BotonContorno(stringResource(R.string.accion_reintentar), acciones.onReintentar, Modifier.testTag("perfil_reintentar"))
                            },
                        )
                    }
                    Carga.LISTO -> state.perfil?.let { perfil -> contenidoDelPerfil(state, perfil, hoy) }
                }
                // La cuenta va siempre, también con error: "Cerrar sesión" no puede desaparecer (CA-11).
                item(key = "titulo_cuenta") { EncabezadoBloque(stringResource(R.string.perfil_cuenta)) }
                item(key = "cuenta") { Cuenta(state, acciones) }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.contenidoDelPerfil(state: PerfilState, perfil: Perfil, hoy: LocalDate) {
    if (perfil.conCifras) {
        item(key = "cifras") { Cifras(perfil, Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) }
    }
    item(key = "titulo_trayectoria") { EncabezadoBloque(stringResource(R.string.perfil_trayectoria)) }
    if (perfil.trayectoria.isEmpty()) {
        item(key = "sin_trayectoria") {
            EstadoVacio(
                icono = R.drawable.ic_trophy,
                titulo = stringResource(R.string.perfil_trayectoria_vacia),
                modifier = Modifier.testTag("perfil_trayectoria_vacia"),
            )
        }
    } else {
        item(key = "trayectoria") {
            Panel(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                perfil.trayectoria.forEachIndexed { indice, paso ->
                    if (indice > 0) Divisor()
                    FilaDeTrayectoria(paso)
                }
            }
        }
    }
    if (state.conSanciones) {
        item(key = "titulo_sanciones") { EncabezadoBloque(stringResource(R.string.perfil_sanciones)) }
        item(key = "sanciones") {
            Panel(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp).testTag("perfil_sanciones")) {
                state.multas.forEachIndexed { indice, multa ->
                    if (indice > 0) Divisor()
                    FilaDeMulta(multa, hoy)
                }
                if (perfil.tarjetas.total > 0) {
                    if (state.multas.isNotEmpty()) Divisor()
                    FilaDeDato(stringResource(R.string.perfil_tarjetas_titulo), textoDeTarjetas(perfil.tarjetas))
                }
            }
        }
    }
}

// ── Piezas ──────────────────────────────────────────────────────────────────

@Composable
private fun Encabezado(state: PerfilState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
    ) {
        Avatar(state.iniciales.ifBlank { "·" }, tamano = 64.dp)
        Column(Modifier.weight(1f)) {
            Text(
                text = state.nombre.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .semantics { heading() }
                    .testTag("perfil_nombre"),
            )
            if (state.nombrePublico.isNotBlank()) {
                Text(
                    text = stringResource(R.string.perfil_nombre_publico, state.nombrePublico),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("perfil_nombre_publico"),
                )
            }
        }
    }
}

/** Las tres cifras, como un marcador: el número en la condensada y debajo qué cuenta. */
@Composable
private fun Cifras(perfil: Perfil, modifier: Modifier = Modifier) {
    val lectura = stringResource(R.string.perfil_cifras_lectura, perfil.partidosJugados, perfil.goles, perfil.titulos)
    Surface(color = colorDePanel(), shape = MaterialTheme.shapes.large, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = lectura }
                .testTag("perfil_cifras")
                .padding(vertical = 16.dp),
        ) {
            Cifra(perfil.partidosJugados, stringResource(R.string.perfil_cifra_partidos), Modifier.weight(1f))
            Cifra(perfil.goles, stringResource(R.string.perfil_cifra_goles), Modifier.weight(1f))
            Cifra(perfil.titulos, stringResource(R.string.perfil_cifra_titulos), Modifier.weight(1f))
        }
    }
}

@Composable
private fun Cifra(valor: Int, etiqueta: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Panel(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Surface(color = colorDePanel(), shape = MaterialTheme.shapes.large, modifier = modifier.fillMaxWidth()) {
        Column { contenido() }
    }
}

@Composable
private fun Divisor() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
}

/** Un renglón de panel: título, detalle debajo y, si hay, un chip. Con letra grande el chip baja de línea. */
@Composable
private fun FilaDeDato(
    titulo: String,
    detalle: String?,
    modifier: Modifier = Modifier,
    chip: (@Composable () -> Unit)? = null,
    fin: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics(mergeDescendants = true) { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (detalle != null) {
                Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            chip?.invoke()
        }
        fin?.invoke()
    }
}

@Composable
private fun FilaDeTrayectoria(paso: PasoPorEquipo) {
    FilaDeDato(
        titulo = paso.torneo,
        detalle = paso.equipo,
        modifier = Modifier.testTag("perfil_paso"),
        chip = paso.puesto?.let { puesto ->
            {
                val (texto, tono) = when (puesto) {
                    Puesto.CAMPEON -> R.string.perfil_puesto_campeon to Tono.AHORA
                    Puesto.SUBCAMPEON -> R.string.perfil_puesto_subcampeon to Tono.NEUTRO
                    Puesto.TERCERO -> R.string.perfil_puesto_tercero to Tono.NEUTRO
                }
                ChipEstado(stringResource(texto), Modifier.padding(top = 4.dp), tono = tono, icono = R.drawable.ic_trophy)
            }
        },
    )
}

@Composable
private fun FilaDeMulta(multa: Multa, hoy: LocalDate) {
    val titulo = stringResource(
        when (multa.tipoSancion) {
            "AMARILLA" -> R.string.multa_amarilla
            "AZUL" -> R.string.multa_azul
            "ROJA" -> R.string.multa_roja
            "ACUMULACION_AMARILLAS" -> R.string.multa_acumulacion
            "ROJA_DIRECTA" -> R.string.multa_roja_directa
            else -> R.string.multa_generica
        },
    )
    val monto = Formatos.pesos(multa.monto)
    val detalle = multa.fechaGeneracion?.let { stringResource(R.string.multa_detalle, monto, Formatos.fecha(it.toLocalDate(), hoy)) } ?: monto
    FilaDeDato(
        titulo = titulo,
        detalle = detalle,
        modifier = Modifier.testTag("perfil_multa"),
        chip = {
            val (texto, tono) = when (multa.estado) {
                EstadoMulta.PENDIENTE -> R.string.multa_pendiente to Tono.ADVERTENCIA
                EstadoMulta.EN_REVISION -> R.string.multa_en_revision to Tono.INFO
                EstadoMulta.PAGADA -> R.string.multa_pagada to Tono.EXITO
                EstadoMulta.CONDONADA -> R.string.multa_condonada to Tono.NEUTRO
                EstadoMulta.DESCONOCIDO -> return@FilaDeDato
            }
            ChipEstado(stringResource(texto), Modifier.padding(top = 4.dp), tono = tono)
        },
    )
}

@Composable
private fun textoDeTarjetas(tarjetas: Tarjetas): String = stringResource(
    R.string.perfil_tarjetas,
    pluralStringResource(R.plurals.perfil_amarillas, tarjetas.amarillas, tarjetas.amarillas),
    pluralStringResource(R.plurals.perfil_azules, tarjetas.azules, tarjetas.azules),
    pluralStringResource(R.plurals.perfil_rojas, tarjetas.rojas, tarjetas.rojas),
)

/** "Mediocampista · Pierna izquierda · 183 cm", o "Sin completar". Solo lectura (editarlo es otra spec). */
@Composable
private fun textoDelPerfilDeportivo(perfil: Perfil?): String {
    if (perfil == null || !perfil.conPerfilDeportivo) return stringResource(R.string.perfil_deportivo_sin_completar)
    val pierna = when (perfil.piernaHabil) {
        null -> null
        "DERECHA" -> stringResource(R.string.perfil_pierna_derecha)
        "IZQUIERDA" -> stringResource(R.string.perfil_pierna_izquierda)
        "AMBIDIESTRO" -> stringResource(R.string.perfil_pierna_ambas)
        else -> comoTexto(perfil.piernaHabil)
    }
    return listOfNotNull(
        perfil.posicion?.let { comoTexto(it) },
        pierna,
        perfil.alturaCm?.let { stringResource(R.string.perfil_altura, it) },
    ).joinToString(" · ")
}

/** Un código del backend que la app no traduce se muestra legible: "MEDIO_CAMPISTA" → "Medio campista". */
private fun comoTexto(codigo: String): String =
    codigo.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@Composable
private fun Cuenta(state: PerfilState, acciones: AccionesPerfil) {
    Panel(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
        FilaDeDato(
            titulo = stringResource(R.string.perfil_deportivo),
            detalle = textoDelPerfilDeportivo(state.perfil),
            modifier = Modifier.testTag("perfil_deportivo"),
        )
        Divisor()
        FilaDeDato(
            titulo = stringResource(R.string.perfil_invitacion),
            detalle = null,
            fin = { FlechaFila() },
            modifier = Modifier
                .clickable(role = Role.Button, onClick = acciones.onAbrirInvitacion)
                .testTag("perfil_invitacion"),
        )
        acciones.onAbrirDiagnostico?.let { abrir ->
            Divisor()
            FilaDeDato(
                titulo = stringResource(R.string.inicio_diagnostico),
                detalle = null,
                fin = { FlechaFila() },
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = abrir)
                    .testTag("perfil_diagnostico"),
            )
        }
        Divisor()
        FilaDeDato(
            titulo = stringResource(if (state.cerrandoSesion) R.string.inicio_cerrando_sesion else R.string.inicio_cerrar_sesion),
            detalle = state.correo.ifBlank { null },
            modifier = Modifier
                .clickable(role = Role.Button, enabled = !state.cerrandoSesion, onClick = acciones.onCerrarSesion)
                .testTag("perfil_cerrar_sesion"),
        )
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val HOY: LocalDate = LocalDate.of(2026, 11, 12)

private val perfilDeEjemplo = Perfil(
    nombre = "Juan Castro", alturaCm = 183, piernaHabil = "IZQUIERDA", posicion = "MEDIOCAMPISTA",
    trayectoria = listOf(
        PasoPorEquipo("Halcones FC", "Copa Sistemas 2025", Puesto.CAMPEON),
        PasoPorEquipo("Halcones FC", "Liga Relámpago (prueba)", null),
    ),
    partidosJugados = 12, goles = 7, titulos = 1, tarjetas = Tarjetas(3, 0, 1),
)

private val multasDeEjemplo = listOf(
    Multa("m1", "ACUMULACION_AMARILLAS", BigDecimal("10000"), EstadoMulta.PENDIENTE, LocalDateTime.of(2026, 11, 5, 17, 40)),
    Multa("m2", "ROJA", BigDecimal("20000"), EstadoMulta.PAGADA, LocalDateTime.of(2026, 10, 20, 17, 40)),
)

@Preview(name = "Perfil · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 1100)
@Preview(name = "Perfil · claro", showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun PerfilPreview() {
    DesafioSportsTheme {
        PerfilContenido(
            PerfilState(
                nombre = "Juan Castro", nombrePublico = "Juan C.", iniciales = "JC", correo = "jugador01@prueba.local",
                carga = Carga.LISTO, perfil = perfilDeEjemplo, multas = multasDeEjemplo,
            ),
            HOY, AccionesPerfil(onAbrirDiagnostico = {}),
        )
    }
}

@Preview(name = "Perfil · sin conexión", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PerfilErrorPreview() {
    DesafioSportsTheme {
        PerfilContenido(
            PerfilState(
                nombre = "Juan Castro", nombrePublico = "Juan C.", iniciales = "JC", carga = Carga.ERROR,
                error = ErrorDePantalla("No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.", sinConexion = true),
            ),
            HOY, AccionesPerfil(),
        )
    }
}
