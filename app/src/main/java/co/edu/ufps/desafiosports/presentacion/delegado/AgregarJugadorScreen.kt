package co.edu.ufps.desafiosports.presentacion.delegado

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.BorradorJugador
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.presentacion.comun.BotonPrimario
import co.edu.ufps.desafiosports.presentacion.creador.TituloConTorneo
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Lo que la pantalla le pide al ViewModel. */
data class AccionesAgregarJugador(
    val onTipo: (TipoDocumento) -> Unit = {},
    val onDocumento: (String) -> Unit = {},
    val onNombre: (String) -> Unit = {},
    val onCamiseta: (String) -> Unit = {},
    val onAgregar: () -> Unit = {},
    val onAvisoMostrado: () -> Unit = {},
)

/** Agregar jugadores al plantel (MOV-0015). */
@Composable
fun AgregarJugadorScreen(
    onVolver: () -> Unit,
    viewModel: AgregarJugadorViewModel = viewModel(factory = AgregarJugadorViewModel.Factory),
) {
    val acciones = remember(viewModel) {
        AccionesAgregarJugador(
            onTipo = viewModel::onTipo,
            onDocumento = viewModel::onDocumento,
            onNombre = viewModel::onNombre,
            onCamiseta = viewModel::onCamiseta,
            onAgregar = viewModel::agregar,
            onAvisoMostrado = viewModel::onAvisoMostrado,
        )
    }
    AgregarJugadorContenido(state = viewModel.state, acciones = acciones, onVolver = onVolver)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AgregarJugadorContenido(
    state: AgregarJugadorState,
    acciones: AccionesAgregarJugador,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avisos = remember { SnackbarHostState() }
    val textoAviso = state.aviso?.let { stringResource(R.string.agregar_aviso, it) }
    LaunchedEffect(state.aviso) {
        if (textoAviso == null) return@LaunchedEffect
        avisos.showSnackbar(textoAviso)
        acciones.onAvisoMostrado()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { TituloConTorneo(stringResource(R.string.agregar_titulo), state.equipo) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("agregar_atras")) {
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
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                stringResource(R.string.agregar_explicacion),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CamposDelDocumento(state, acciones)
            OutlinedTextField(
                value = state.borrador.nombre,
                onValueChange = acciones.onNombre,
                label = { Text(stringResource(R.string.agregar_nombre)) },
                isError = state.errorNombre != null,
                supportingText = { Text(state.errorNombre ?: stringResource(R.string.agregar_nombre_ayuda)) },
                singleLine = true,
                enabled = !state.agregando,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agregar_nombre"),
            )
            OutlinedTextField(
                value = state.borrador.camiseta,
                onValueChange = acciones.onCamiseta,
                label = { Text(stringResource(R.string.agregar_camiseta)) },
                isError = state.errorCamiseta != null,
                supportingText = { Text(state.errorCamiseta ?: stringResource(R.string.agregar_camiseta_ayuda)) },
                singleLine = true,
                enabled = !state.agregando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .width(200.dp)
                    .testTag("agregar_camiseta"),
            )
            state.errorGeneral?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .semantics { liveRegion = LiveRegionMode.Polite }
                        .testTag("agregar_error"),
                )
            }
            BotonPrimario(
                texto = stringResource(R.string.agregar_confirmar),
                onClick = acciones.onAgregar,
                habilitado = state.sePuedeAgregar,
                enProceso = state.agregando,
                textoEnProceso = stringResource(R.string.agregar_agregando),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agregar_confirmar"),
            )
            if (state.agregados.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.agregar_agregados, state.agregados.joinToString(", ")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("agregar_agregados"),
                )
            }
        }
    }
}

/** El tipo (por defecto CC) y el número. El número es opcional, salvo que el torneo lo exija. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CamposDelDocumento(state: AgregarJugadorState, acciones: AccionesAgregarJugador) {
    var menuAbierto by remember { mutableStateOf(false) }
    val habilitado = !state.agregando
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = menuAbierto,
            onExpandedChange = { if (habilitado) menuAbierto = it },
            modifier = Modifier.width(132.dp),
        ) {
            // El campo es angosto: muestra la sigla. El menú muestra el nombre completo.
            OutlinedTextField(
                value = siglaTipo(state.borrador.tipoDocumento),
                onValueChange = {},
                readOnly = true,
                enabled = habilitado,
                singleLine = true,
                label = { Text(stringResource(R.string.agregar_tipo)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, habilitado)
                    .testTag("agregar_tipo"),
            )
            ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }, matchAnchorWidth = false) {
                TipoDocumento.entries.forEach { tipo ->
                    DropdownMenuItem(
                        text = { Text(etiquetaTipo(tipo)) },
                        onClick = {
                            acciones.onTipo(tipo)
                            menuAbierto = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        OutlinedTextField(
            value = state.borrador.documento,
            onValueChange = acciones.onDocumento,
            label = {
                Text(stringResource(if (state.exigeDocumento) R.string.agregar_documento_exigido else R.string.agregar_documento))
            },
            isError = state.errorDocumento != null,
            supportingText = state.errorDocumento?.let { { Text(it) } },
            singleLine = true,
            enabled = habilitado,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false,
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .weight(1f)
                .testTag("agregar_documento"),
        )
    }
}

@Composable
private fun siglaTipo(tipo: TipoDocumento): String = stringResource(
    when (tipo) {
        TipoDocumento.CC -> R.string.tipo_documento_sigla_cc
        TipoDocumento.TI -> R.string.tipo_documento_sigla_ti
        TipoDocumento.CE -> R.string.tipo_documento_sigla_ce
        TipoDocumento.PPT -> R.string.tipo_documento_sigla_ppt
        TipoDocumento.PASAPORTE -> R.string.tipo_documento_sigla_pasaporte
    },
)

@Composable
private fun etiquetaTipo(tipo: TipoDocumento): String = stringResource(
    when (tipo) {
        TipoDocumento.CC -> R.string.tipo_documento_cc
        TipoDocumento.TI -> R.string.tipo_documento_ti
        TipoDocumento.CE -> R.string.tipo_documento_ce
        TipoDocumento.PPT -> R.string.tipo_documento_ppt
        TipoDocumento.PASAPORTE -> R.string.tipo_documento_pasaporte
    },
)

// ── Vistas previas ──────────────────────────────────────────────────────────

@Preview(name = "Agregar jugador · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Agregar jugador · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AgregarJugadorPreview() {
    DesafioSportsTheme {
        AgregarJugadorContenido(
            AgregarJugadorState(
                equipo = "Halcones FC",
                borrador = BorradorJugador(documento = "1090123456", nombre = "Juan Castro", camiseta = "10"),
                agregados = listOf("Sofía Moreno", "Mateo Jiménez"),
            ),
            AccionesAgregarJugador(), {},
        )
    }
}

@Preview(name = "Agregar jugador · errores", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AgregarJugadorErroresPreview() {
    DesafioSportsTheme {
        AgregarJugadorContenido(
            AgregarJugadorState(
                equipo = "Halcones FC", exigeDocumento = true,
                borrador = BorradorJugador(camiseta = "10"),
                errorDocumento = "Este torneo exige documento: escribe el del jugador.",
                errorCamiseta = "El 10 ya lo tiene Juan Castro.",
            ),
            AccionesAgregarJugador(), {},
        )
    }
}
