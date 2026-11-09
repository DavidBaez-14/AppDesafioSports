package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.res.Configuration
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.ResultadoRegistro
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Crear cuenta (MOV-0003 · A-3). */
@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    onRevisarCorreo: (correo: String) -> Unit,
    onCompletarRegistro: () -> Unit,
    viewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.Factory),
) {
    val state = viewModel.state
    LaunchedEffect(state.destino) {
        when (state.destino) {
            ResultadoRegistro.CONFIRMAR_CORREO -> {
                viewModel.onDestinoAtendido()
                onRevisarCorreo(state.correo)
            }
            ResultadoRegistro.SESION_ACTIVA -> {
                viewModel.onDestinoAtendido()
                onCompletarRegistro()
            }
            null -> Unit
        }
    }
    RegistroContenido(
        state = state,
        onCorreoChange = viewModel::onCorreoChange,
        onClaveChange = viewModel::onClaveChange,
        onAlternarClave = viewModel::onAlternarClave,
        onCrearCuenta = viewModel::crearCuenta,
        onVolver = onVolver,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroContenido(
    state: RegistroState,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onAlternarClave: () -> Unit,
    onCrearCuenta: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val foco = LocalFocusManager.current
    val habilitado = !state.enProceso
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registro_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("registro_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
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
                    text = stringResource(R.string.registro_explicacion),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = state.correo,
                    onValueChange = onCorreoChange,
                    label = { Text(stringResource(R.string.login_correo)) },
                    singleLine = true,
                    enabled = habilitado,
                    isError = state.errorCorreo != null,
                    supportingText = state.errorCorreo?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onNext = { foco.moveFocus(FocusDirection.Down) }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.EmailAddress }
                        .testTag("registro_correo"),
                )
                OutlinedTextField(
                    value = state.clave,
                    onValueChange = onClaveChange,
                    label = { Text(stringResource(R.string.login_clave)) },
                    singleLine = true,
                    enabled = habilitado,
                    isError = state.errorClave != null,
                    supportingText = { Text(state.errorClave ?: stringResource(R.string.registro_ayuda_clave)) },
                    visualTransformation = if (state.claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onAlternarClave, modifier = Modifier.testTag("registro_ver_clave")) {
                            Icon(
                                painter = painterResource(if (state.claveVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                contentDescription = stringResource(if (state.claveVisible) R.string.login_ocultar_clave else R.string.login_mostrar_clave),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onDone = {
                        foco.clearFocus()
                        onCrearCuenta()
                    }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.NewPassword }
                        .testTag("registro_clave"),
                )
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    AvisoErrorAnimado(state.error, Modifier.testTag("registro_error"))
                    BotonPrincipal(
                        texto = stringResource(R.string.registro_crear),
                        textoEnProceso = stringResource(R.string.registro_creando),
                        enProceso = state.enProceso,
                        onClick = {
                            foco.clearFocus()
                            onCrearCuenta()
                        },
                        modifier = Modifier.testTag("registro_crear"),
                    )
                }
            }
        }
    }
}

@Preview(name = "Crear cuenta · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Crear cuenta · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegistroContenidoPreview() {
    DesafioSportsTheme {
        RegistroContenido(RegistroState(correo = "andres.pena@correo.co"), {}, {}, {}, {}, {})
    }
}
