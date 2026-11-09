package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/**
 * Completar registro (MOV-0003 · N-2). Atrás del sistema equivale a "Salir": cierra la sesión,
 * porque una sesión sin registro no puede usar la app.
 */
@Composable
fun CompletarRegistroScreen(
    onRegistrado: () -> Unit,
    viewModel: CompletarRegistroViewModel = viewModel(factory = CompletarRegistroViewModel.Factory),
) {
    val state = viewModel.state
    LaunchedEffect(state.registrado) {
        if (state.registrado) {
            viewModel.onRegistroAtendido()
            onRegistrado()
        }
    }
    BackHandler(onBack = viewModel::salir)
    CompletarRegistroContenido(
        state = state,
        onNombresChange = viewModel::onNombresChange,
        onApellidosChange = viewModel::onApellidosChange,
        onTipoDocumentoChange = viewModel::onTipoDocumentoChange,
        onNumeroDocumentoChange = viewModel::onNumeroDocumentoChange,
        onTerminar = viewModel::terminar,
        onSalir = viewModel::salir,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletarRegistroContenido(
    state: CompletarRegistroState,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onTipoDocumentoChange: (TipoDocumento?) -> Unit,
    onNumeroDocumentoChange: (String) -> Unit,
    onTerminar: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val foco = LocalFocusManager.current
    val habilitado = !state.enProceso
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.completar_titulo)) },
                actions = {
                    TextButton(onClick = onSalir, modifier = Modifier.testTag("completar_salir")) {
                        Text(stringResource(R.string.completar_salir), color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { relleno ->
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(relleno).imePadding()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.completar_explicacion),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = state.nombres,
                    onValueChange = onNombresChange,
                    label = { Text(stringResource(R.string.completar_nombres)) },
                    singleLine = true,
                    enabled = habilitado,
                    isError = state.errorNombres != null,
                    supportingText = state.errorNombres?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { foco.moveFocus(FocusDirection.Down) }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.PersonFirstName }
                        .testTag("completar_nombres"),
                )
                OutlinedTextField(
                    value = state.apellidos,
                    onValueChange = onApellidosChange,
                    label = { Text(stringResource(R.string.completar_apellidos)) },
                    singleLine = true,
                    enabled = habilitado,
                    isError = state.errorApellidos != null,
                    supportingText = state.errorApellidos?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { foco.clearFocus() }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.PersonLastName }
                        .testTag("completar_apellidos"),
                )

                Text(
                    text = stringResource(R.string.completar_documento_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.completar_documento_explicacion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ExposedDropdownMenuBox(
                    expanded = menuAbierto,
                    onExpandedChange = { if (habilitado) menuAbierto = it },
                ) {
                    OutlinedTextField(
                        value = etiquetaTipo(state.tipoDocumento),
                        onValueChange = {},
                        readOnly = true,
                        enabled = habilitado,
                        label = { Text(stringResource(R.string.completar_tipo)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                        isError = state.errorDocumento != null && state.tipoDocumento == null,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, habilitado)
                            .fillMaxWidth()
                            .testTag("completar_tipo"),
                    )
                    ExposedDropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        (listOf<TipoDocumento?>(null) + TipoDocumento.entries).forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(etiquetaTipo(tipo)) },
                                onClick = {
                                    onTipoDocumentoChange(tipo)
                                    menuAbierto = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = state.numeroDocumento,
                    onValueChange = onNumeroDocumentoChange,
                    label = { Text(stringResource(R.string.completar_numero)) },
                    singleLine = true,
                    enabled = habilitado && state.tipoDocumento != null,
                    isError = state.errorDocumento != null,
                    supportingText = state.errorDocumento?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onDone = {
                        foco.clearFocus()
                        onTerminar()
                    }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("completar_numero"),
                )

                Column(modifier = Modifier.padding(top = 8.dp)) {
                    AvisoErrorAnimado(state.error, Modifier.testTag("completar_error"))
                    BotonPrincipal(
                        texto = stringResource(R.string.completar_terminar),
                        textoEnProceso = stringResource(R.string.completar_terminando),
                        enProceso = state.enProceso,
                        onClick = {
                            foco.clearFocus()
                            onTerminar()
                        },
                        modifier = Modifier.testTag("completar_terminar"),
                    )
                }
            }
        }
    }
}

@Composable
private fun etiquetaTipo(tipo: TipoDocumento?): String = stringResource(
    when (tipo) {
        null -> R.string.completar_tipo_ninguno
        TipoDocumento.CC -> R.string.tipo_documento_cc
        TipoDocumento.TI -> R.string.tipo_documento_ti
        TipoDocumento.CE -> R.string.tipo_documento_ce
        TipoDocumento.PPT -> R.string.tipo_documento_ppt
        TipoDocumento.PASAPORTE -> R.string.tipo_documento_pasaporte
    }
)

@Preview(name = "Completar registro · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Completar registro · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CompletarRegistroContenidoPreview() {
    DesafioSportsTheme {
        CompletarRegistroContenido(
            CompletarRegistroState(nombres = "Andrés", apellidos = "Peña Ruiz", tipoDocumento = TipoDocumento.CC, numeroDocumento = "1090123456"),
            {}, {}, {}, {}, {}, {},
        )
    }
}
