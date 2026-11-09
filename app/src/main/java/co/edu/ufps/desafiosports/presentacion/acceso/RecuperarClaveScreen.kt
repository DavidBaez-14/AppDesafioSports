package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Recuperar contraseña (MOV-0002 · N-3). La contraseña nueva se crea en la web, desde el enlace. */
@Composable
fun RecuperarClaveScreen(
    onVolver: () -> Unit,
    viewModel: RecuperarClaveViewModel = viewModel(factory = RecuperarClaveViewModel.Factory),
) {
    RecuperarClaveContenido(
        state = viewModel.state,
        onCorreoChange = viewModel::onCorreoChange,
        onEnviar = viewModel::enviar,
        onVolver = onVolver,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarClaveContenido(
    state: RecuperarClaveState,
    onCorreoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recuperar_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("recuperar_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { relleno ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(relleno)
                .imePadding(),
        ) {
            if (state.enviado) {
                Enviado(correo = state.correo, onVolver = onVolver)
            } else {
                Formulario(state = state, onCorreoChange = onCorreoChange, onEnviar = onEnviar)
            }
        }
    }
}

@Composable
private fun Formulario(state: RecuperarClaveState, onCorreoChange: (String) -> Unit, onEnviar: () -> Unit) {
    val foco = LocalFocusManager.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.recuperar_explicacion),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = state.correo,
            onValueChange = onCorreoChange,
            label = { Text(stringResource(R.string.login_correo)) },
            singleLine = true,
            enabled = !state.enProceso,
            isError = state.errorCorreo != null,
            supportingText = state.errorCorreo?.let { error -> { Text(error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send, autoCorrectEnabled = false),
            keyboardActions = KeyboardActions(onSend = {
                foco.clearFocus()
                onEnviar()
            }),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.EmailAddress }
                .testTag("recuperar_correo"),
        )
        Column(modifier = Modifier.padding(top = 4.dp)) {
            AvisoErrorAnimado(state.error, Modifier.testTag("recuperar_error"))
            BotonPrincipal(
                texto = stringResource(R.string.recuperar_enviar),
                textoEnProceso = stringResource(R.string.recuperar_enviando),
                enProceso = state.enProceso,
                onClick = {
                    foco.clearFocus()
                    onEnviar()
                },
                modifier = Modifier.testTag("recuperar_enviar"),
            )
        }
    }
}

@Composable
private fun Enviado(correo: String, onVolver: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .testTag("recuperar_enviado"),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mark_email_read),
            contentDescription = null,
            // Decorativo: el naranja queda solo para el botón "Volver a entrar".
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = stringResource(R.string.recuperar_enviado_titulo).uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.recuperar_enviado_detalle, correo),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        BotonPrincipal(
            texto = stringResource(R.string.recuperar_volver),
            textoEnProceso = stringResource(R.string.recuperar_volver),
            enProceso = false,
            onClick = onVolver,
            modifier = Modifier
                .padding(top = 16.dp)
                .testTag("recuperar_volver"),
        )
    }
}

@Preview(name = "Recuperar · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Recuperar · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RecuperarClaveContenidoPreview() {
    DesafioSportsTheme {
        RecuperarClaveContenido(RecuperarClaveState(correo = "andres.pena@correo.co"), {}, {}, {})
    }
}

@Preview(name = "Recuperar · enviado · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RecuperarClaveEnviadoPreview() {
    DesafioSportsTheme {
        RecuperarClaveContenido(RecuperarClaveState(correo = "andres.pena@correo.co", enviado = true), {}, {}, {})
    }
}
