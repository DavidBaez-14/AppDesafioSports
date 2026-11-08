package co.edu.ufps.desafiosports.presentacion.diagnostico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.model.Rol
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Pantalla con estado: pide su ViewModel y le pasa el estado a la versión sin estado. */
@Composable
fun DiagnosticoScreen(viewModel: DiagnosticoViewModel = viewModel(factory = DiagnosticoViewModel.Factory)) {
    DiagnosticoContenido(
        state = viewModel.state,
        onCorreoChange = viewModel::onCorreoChange,
        onClaveChange = viewModel::onClaveChange,
        onReintentar = viewModel::comprobarServidor,
        onIniciarSesion = viewModel::iniciarSesion,
        onConsultarCuenta = viewModel::consultarCuenta,
        onCerrarSesion = viewModel::cerrarSesion,
    )
}

/** Pantalla sin estado: solo dibuja. Es la que usan los @Preview y los tests de UI. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DiagnosticoContenido(
    state: DiagnosticoState,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onReintentar: () -> Unit,
    onIniciarSesion: () -> Unit,
    onConsultarCuenta: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    Scaffold(
        // Expone los testTag como resource-id: permite automatizar con adb/uiautomator.
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = { TopAppBar(title = { Text(stringResource(R.string.diagnostico_titulo)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.diagnostico_aviso),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (state.faltantes.isNotEmpty()) {
                Tarjeta(titulo = stringResource(R.string.diagnostico_config_titulo)) {
                    Text(
                        text = stringResource(R.string.diagnostico_config_detalle, state.faltantes.joinToString()),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("config_incompleta"),
                    )
                }
            }

            TarjetaServidor(state, onReintentar)
            TarjetaSesion(state, onCorreoChange, onClaveChange, onIniciarSesion, onConsultarCuenta, onCerrarSesion)
            state.cuenta?.let { TarjetaCuenta(it) }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("diagnostico_error"),
                )
            }
        }
    }
}

@Composable
private fun TarjetaServidor(state: DiagnosticoState, onReintentar: () -> Unit) {
    Tarjeta(titulo = stringResource(R.string.diagnostico_servidor_titulo)) {
        Text(stringResource(R.string.diagnostico_servidor_url, state.apiBaseUrl))
        val estado = when (val servidor = state.servidor) {
            EstadoComprobacion.Comprobando -> stringResource(R.string.diagnostico_servidor_comprobando)
            is EstadoComprobacion.EnLinea -> stringResource(R.string.diagnostico_servidor_ok, servidor.estado)
            is EstadoComprobacion.Fallo -> servidor.mensaje
        }
        Text(
            text = estado,
            color = if (state.servidor is EstadoComprobacion.Fallo) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("estado_servidor"),
        )
        OutlinedButton(onClick = onReintentar, modifier = Modifier.testTag("boton_reintentar")) {
            Text(stringResource(R.string.diagnostico_reintentar))
        }
    }
}

@Composable
private fun TarjetaSesion(
    state: DiagnosticoState,
    onCorreoChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onConsultarCuenta: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    Tarjeta(titulo = stringResource(R.string.diagnostico_sesion_titulo)) {
        when (val sesion = state.sesion) {
            EstadoSesion.Cargando -> Text(stringResource(R.string.diagnostico_sesion_cargando))

            is EstadoSesion.Activa, EstadoSesion.SinRenovar -> {
                val correo = (sesion as? EstadoSesion.Activa)?.correo.orEmpty()
                Text(
                    text = stringResource(R.string.diagnostico_sesion_activa, correo),
                    modifier = Modifier.testTag("estado_sesion"),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onConsultarCuenta,
                        enabled = !state.enProceso,
                        modifier = Modifier.testTag("boton_consultar"),
                    ) { Text(stringResource(R.string.diagnostico_consultar_cuenta)) }
                    OutlinedButton(
                        onClick = onCerrarSesion,
                        enabled = !state.enProceso,
                        modifier = Modifier.testTag("boton_cerrar"),
                    ) { Text(stringResource(R.string.diagnostico_cerrar_sesion)) }
                }
            }

            EstadoSesion.Inactiva -> {
                Text(
                    text = stringResource(R.string.diagnostico_sesion_inactiva),
                    modifier = Modifier.testTag("estado_sesion"),
                )
                OutlinedTextField(
                    value = state.correo,
                    onValueChange = onCorreoChange,
                    label = { Text(stringResource(R.string.diagnostico_correo)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("campo_correo"),
                )
                OutlinedTextField(
                    value = state.clave,
                    onValueChange = onClaveChange,
                    label = { Text(stringResource(R.string.diagnostico_clave)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("campo_clave"),
                )
                Button(
                    onClick = onIniciarSesion,
                    enabled = !state.enProceso,
                    modifier = Modifier.fillMaxWidth().testTag("boton_iniciar"),
                ) {
                    if (state.enProceso) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.diagnostico_iniciar_sesion))
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCuenta(cuenta: Cuenta) {
    val ninguno = stringResource(R.string.diagnostico_ninguno)
    Tarjeta(titulo = stringResource(R.string.diagnostico_cuenta_titulo), modifier = Modifier.testTag("tarjeta_cuenta")) {
        Text(stringResource(R.string.diagnostico_cuenta_nombre, cuenta.nombre))
        Text(stringResource(R.string.diagnostico_cuenta_correo, cuenta.correo))
        Text(stringResource(R.string.diagnostico_cuenta_estado, cuenta.estado))
        Text(stringResource(R.string.diagnostico_cuenta_roles, cuenta.roles.joinToString().ifEmpty { ninguno }))
        Text(stringResource(R.string.diagnostico_cuenta_pendientes, cuenta.rolesPendientes.joinToString().ifEmpty { ninguno }))
    }
}

@Composable
private fun Tarjeta(titulo: String, modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

private val estadoDeEjemplo = DiagnosticoState(
    apiBaseUrl = "http://10.0.2.2:8082/",
    servidor = EstadoComprobacion.EnLinea("UP"),
    sesion = EstadoSesion.Activa("jugador01@prueba.local"),
    cuenta = Cuenta(
        id = "1",
        nombres = "Jugador",
        apellidos = "Uno",
        nombre = "Jugador Uno",
        correo = "jugador01@prueba.local",
        estado = "ACTIVA",
        roles = setOf(Rol.JUGADOR),
        rolesPendientes = emptySet(),
    ),
)

@Preview(name = "Oscuro", showBackground = true, backgroundColor = 0xFF04070F)
@Composable
private fun DiagnosticoOscuroPreview() {
    DesafioSportsTheme(oscuro = true) {
        DiagnosticoContenido(estadoDeEjemplo, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Claro sin sesión", showBackground = true)
@Composable
private fun DiagnosticoClaroPreview() {
    DesafioSportsTheme(oscuro = false) {
        DiagnosticoContenido(
            estadoDeEjemplo.copy(sesion = EstadoSesion.Inactiva, cuenta = null, servidor = EstadoComprobacion.Fallo("No hay conexión con el servidor.")),
            {}, {}, {}, {}, {}, {},
        )
    }
}
