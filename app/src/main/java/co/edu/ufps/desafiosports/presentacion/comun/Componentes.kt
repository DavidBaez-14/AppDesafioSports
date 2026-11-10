package co.edu.ufps.desafiosports.presentacion.comun

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.ui.theme.coloresExtra

// Piezas que comparten las pantallas con listas (MOV-0009 en adelante): encabezado de bloque,
// fila, chip de estado, columna de fecha y los estados vacío, error y cargando.

/** Un error que ocupa la pantalla: el texto para la persona y si fue por falta de conexión. */
data class ErrorDePantalla(val mensaje: String, val sinConexion: Boolean = false)

fun Throwable.aErrorDePantalla(): ErrorDePantalla =
    ErrorDePantalla(mensaje = mensajeParaMostrar(), sinConexion = this is AppError.SinConexion)

/** Título de un bloque de la lista ("HOY", "MIS TORNEOS"). TalkBack lo lee como encabezado. */
@Composable
fun EncabezadoBloque(titulo: String, modifier: Modifier = Modifier, fin: (@Composable () -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    ) {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        fin?.invoke()
    }
}

/**
 * Fila de lista: algo al inicio, título y detalle, y algo al final. Si lleva [onClick], toda la
 * fila es el botón. Con letra grande el título pasa a dos líneas en vez de recortarse.
 */
@Composable
fun Fila(
    titulo: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    inicio: (@Composable () -> Unit)? = null,
    fin: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(titulo, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = detalle?.let {
            { Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis) }
        },
        leadingContent = inicio,
        trailingContent = fin,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .heightIn(min = 72.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
    )
}

/** Flecha del final de una fila que lleva a otra pantalla. */
@Composable
fun FlechaFila() {
    Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null)
}

/** Icono dentro de un círculo, para el inicio de una fila. */
@Composable
fun IconoEnCirculo(@DrawableRes icono: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icono), contentDescription = null)
        }
    }
}

/** Qué dice el color de un [ChipEstado]. [AHORA] es el naranja: solo para lo que pasa hoy. */
enum class Tono { NEUTRO, INFO, EXITO, ADVERTENCIA, ERROR, AHORA }

/** Etiqueta corta de estado. El texto dice el estado; el color solo lo refuerza. */
@Composable
fun ChipEstado(texto: String, modifier: Modifier = Modifier, tono: Tono = Tono.NEUTRO, @DrawableRes icono: Int? = null) {
    val esquema = MaterialTheme.colorScheme
    val extra = MaterialTheme.coloresExtra
    val (fondo, tinta) = when (tono) {
        Tono.NEUTRO -> esquema.surfaceContainerHigh to esquema.onSurfaceVariant
        Tono.INFO -> esquema.tertiaryContainer to esquema.onTertiaryContainer
        Tono.EXITO -> extra.exitoContenedor to extra.exito
        Tono.ADVERTENCIA -> extra.advertenciaContenedor to extra.advertencia
        Tono.ERROR -> esquema.errorContainer to esquema.onErrorContainer
        Tono.AHORA -> esquema.primaryContainer to esquema.onPrimaryContainer
    }
    Surface(color = fondo, contentColor = tinta, shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .heightIn(min = 24.dp)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            if (icono != null) Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(14.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

/**
 * Columna de fecha al inicio de una fila: el día arriba, la hora en grande ("4:00") y debajo
 * "a. m." o "p. m.". Con [ahora] va en naranja (es lo que pasa hoy). TalkBack la lee de corrido.
 */
@Composable
fun BloqueFecha(
    dia: String,
    hora: String?,
    modifier: Modifier = Modifier,
    periodo: String? = null,
    ahora: Boolean = false,
) {
    val color = if (ahora) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val secundario = if (ahora) color else MaterialTheme.colorScheme.onSurfaceVariant
    val lectura = listOfNotNull(dia, hora, periodo).joinToString(" ")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clearAndSetSemantics { contentDescription = lectura },
    ) {
        Text(
            text = dia.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = secundario,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (hora != null) {
            Text(
                text = hora,
                style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                color = color,
                maxLines = 1,
            )
        }
        if (periodo != null) {
            Text(periodo, style = MaterialTheme.typography.labelSmall, color = secundario, maxLines = 1)
        }
    }
}

/** Pantalla o bloque sin datos: qué pasa y, si hay algo que hacer, el botón para hacerlo. */
@Composable
fun EstadoVacio(
    @DrawableRes icono: Int,
    titulo: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    accion: (@Composable () -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 36.dp),
    ) {
        Icon(
            painterResource(icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (detalle != null) {
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp),
            )
        }
        accion?.invoke()
    }
}

/** Error que ocupa la pantalla, con "Reintentar". Se desplaza si no cabe (letra grande). */
@Composable
fun EstadoError(
    error: ErrorDePantalla,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
    etiquetaBoton: String = stringResource(R.string.accion_reintentar),
    testTagBoton: String = "reintentar",
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        EstadoVacio(
            icono = if (error.sinConexion) R.drawable.ic_wifi_off else R.drawable.ic_error,
            titulo = stringResource(if (error.sinConexion) R.string.estado_sin_conexion else R.string.estado_error),
            detalle = error.mensaje,
            accion = { BotonContorno(etiquetaBoton, onReintentar, Modifier.testTag(testTagBoton)) },
        )
    }
}

/** Botón secundario del ancho de su texto: contorneado y en tinta, nunca naranja. */
@Composable
fun BotonContorno(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Botón principal que también puede estar **deshabilitado** (el paso aún no está completo).
 * Mientras trabaja conserva el naranja con el indicador, como pide DESIGN.md; deshabilitado se
 * apaga, para que se note que falta algo.
 */
@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    enProceso: Boolean = false,
    textoEnProceso: String = texto,
) {
    val colores = if (enProceso) {
        ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        ButtonDefaults.buttonColors()
    }
    Button(
        onClick = onClick,
        enabled = habilitado && !enProceso,
        colors = colores,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.heightIn(min = 52.dp),
    ) {
        if (enProceso) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
            )
            Text(textoEnProceso, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 10.dp))
        } else {
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Indicador centrado mientras llega la primera carga. */
@Composable
fun Cargando(modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.estado_cargando)
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .testTag("cargando")
                .semantics { contentDescription = descripcion },
        )
    }
}
