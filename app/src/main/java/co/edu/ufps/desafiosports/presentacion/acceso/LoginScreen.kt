package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
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
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import co.edu.ufps.desafiosports.ui.theme.Logotipo

/**
 * Login con correo (MOV-0002 · A-2). Diseño: pantalla "Entrar" del prototipo aprobado.
 *
 * Crear cuenta, Google y Explorar sin cuenta llegan con sus specs: no se muestran como
 * botones muertos mientras tanto.
 */
@Composable
fun LoginScreen(
    onIngresado: () -> Unit,
    onRecuperarClave: (correo: String) -> Unit,
    onCrearCuenta: () -> Unit,
    onCompletarRegistro: () -> Unit,
    onAbrirDiagnostico: (() -> Unit)?,
    /** ID del cliente web de Google; null = la app no ofrece Google (MOV-0006 · CA-1). */
    googleWebClientId: String?,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val state = viewModel.state
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    LaunchedEffect(state.ingresado) {
        if (state.ingresado) {
            viewModel.onIngresoAtendido()
            onIngresado()
        }
    }
    LaunchedEffect(state.debeCompletarRegistro) {
        if (state.debeCompletarRegistro) {
            viewModel.onCompletarRegistroAtendido()
            onCompletarRegistro()
        }
    }
    LoginContenido(
        state = state,
        onCorreoChange = viewModel::onCorreoChange,
        onClaveChange = viewModel::onClaveChange,
        onAlternarClave = viewModel::onAlternarClave,
        onEntrar = viewModel::entrar,
        onRecuperarClave = { onRecuperarClave(state.correo.trim()) },
        onCrearCuenta = onCrearCuenta,
        onAbrirDiagnostico = onAbrirDiagnostico,
        mostrarGoogle = googleWebClientId != null,
        onGoogle = {
            if (googleWebClientId != null && viewModel.iniciarGoogle()) {
                alcance.launch { viewModel.onResultadoGoogle(pedirCuentaGoogle(contexto, googleWebClientId)) }
            }
        },
    )
}

@Composable
fun LoginContenido(
    state: LoginState,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onAlternarClave: () -> Unit,
    onEntrar: () -> Unit,
    onRecuperarClave: () -> Unit,
    onCrearCuenta: () -> Unit,
    onAbrirDiagnostico: (() -> Unit)?,
    modifier: Modifier = Modifier,
    mostrarGoogle: Boolean = false,
    onGoogle: () -> Unit = {},
) {
    val foco = LocalFocusManager.current
    val habilitado = !state.enProceso

    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(contentAlignment = Alignment.TopCenter, modifier = Modifier.safeDrawingPadding().imePadding()) {
            val altoVisible = maxHeight
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    // Ocupa al menos la pantalla para que los Spacer con peso repartan lo que sobra;
                    // con el teclado abierto o la letra grande no sobra nada y todo se desplaza.
                    .heightIn(min = altoVisible)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                Spacer(Modifier.height(32.dp))
                Logotipo(
                    // Herramienta de desarrollo (P16): pulsación larga abre el diagnóstico en debug.
                    // Sin pulsación larga no hay nada interactivo aquí: el logotipo no es un botón.
                    modifier = if (onAbrirDiagnostico != null) {
                        Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { onAbrirDiagnostico() }) }
                    } else {
                        Modifier
                    },
                )
                Text(
                    text = stringResource(R.string.login_bienvenida),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                // El logotipo queda arriba y el formulario baja hacia el pulgar cuando hay pantalla de sobra.
                Spacer(Modifier.weight(1f))

                OutlinedTextField(
                    value = state.correo,
                    onValueChange = onCorreoChange,
                    label = { Text(stringResource(R.string.login_correo)) },
                    singleLine = true,
                    enabled = habilitado,
                    isError = state.errorCorreo != null,
                    supportingText = state.errorCorreo?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(onNext = { foco.moveFocus(FocusDirection.Down) }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.EmailAddress }
                        .testTag("login_correo"),
                )

                OutlinedTextField(
                    value = state.clave,
                    onValueChange = onClaveChange,
                    label = { Text(stringResource(R.string.login_clave)) },
                    singleLine = true,
                    enabled = habilitado,
                    isError = state.errorClave != null,
                    supportingText = state.errorClave?.let { error -> { Text(error) } },
                    visualTransformation = if (state.claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onAlternarClave, modifier = Modifier.testTag("login_ver_clave")) {
                            Icon(
                                painter = painterResource(if (state.claveVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                                contentDescription = stringResource(
                                    if (state.claveVisible) R.string.login_ocultar_clave else R.string.login_mostrar_clave
                                ),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        autoCorrectEnabled = false,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        foco.clearFocus()
                        onEntrar()
                    }),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentType = ContentType.Password }
                        .testTag("login_clave"),
                )

                Column(modifier = Modifier.padding(top = 4.dp)) {
                    AvisoErrorAnimado(state.error, Modifier.testTag("login_error"))
                    BotonPrincipal(
                        texto = stringResource(R.string.login_entrar),
                        textoEnProceso = stringResource(R.string.login_entrando),
                        enProceso = state.enProceso,
                        onClick = {
                            foco.clearFocus()
                            onEntrar()
                        },
                        modifier = Modifier.testTag("login_entrar"),
                    )
                }

                if (mostrarGoogle) {
                    SeparadorO()
                    BotonGoogle(
                        onClick = {
                            foco.clearFocus()
                            onGoogle()
                        },
                        habilitado = habilitado,
                        modifier = Modifier.testTag("login_google"),
                    )
                }

                // Para quien llega por primera vez (MOV-0003): contorneado, sin competir con el naranja de Entrar.
                BotonSecundario(
                    texto = stringResource(R.string.login_crear_cuenta),
                    onClick = onCrearCuenta,
                    habilitado = habilitado,
                    modifier = Modifier.testTag("login_crear_cuenta"),
                )

                TextButton(
                    onClick = onRecuperarClave,
                    enabled = habilitado,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("login_olvide_clave"),
                ) {
                    Text(
                        text = stringResource(R.string.login_olvide_clave),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.weight(0.35f))
            }
        }
    }
}

/** Línea con una "o" en el centro: separa entrar con correo de las otras formas de entrar. */
@Composable
private fun SeparadorO() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(R.string.login_o), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** "Continuar con Google" con la "G" a color, como piden las guías de marca de Google (MOV-0006). */
@Composable
private fun BotonGoogle(onClick: () -> Unit, habilitado: Boolean, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
    ) {
        Icon(painterResource(R.drawable.ic_google), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(stringResource(R.string.login_google), style = MaterialTheme.typography.labelLarge)
    }
}

private val estadoDeEjemplo = LoginState(correo = "andres.pena@correo.co", clave = "contrasena")

@Preview(name = "Login con Google · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginContenidoGooglePreview() {
    DesafioSportsTheme {
        LoginContenido(estadoDeEjemplo, {}, {}, {}, {}, {}, {}, null, mostrarGoogle = true)
    }
}

@Preview(name = "Login · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Login · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginContenidoPreview() {
    DesafioSportsTheme {
        LoginContenido(estadoDeEjemplo, {}, {}, {}, {}, {}, {}, null)
    }
}

@Preview(name = "Login · error · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginContenidoErrorPreview() {
    DesafioSportsTheme {
        LoginContenido(estadoDeEjemplo.copy(error = "Correo o contraseña incorrectos."), {}, {}, {}, {}, {}, {}, null)
    }
}

@Preview(name = "Login · entrando · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginContenidoEntrandoPreview() {
    DesafioSportsTheme {
        LoginContenido(estadoDeEjemplo.copy(enProceso = true), {}, {}, {}, {}, {}, {}, null)
    }
}
