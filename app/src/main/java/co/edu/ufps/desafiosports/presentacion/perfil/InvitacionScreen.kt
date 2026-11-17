package co.edu.ufps.desafiosports.presentacion.perfil

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.InvitacionRecibida
import co.edu.ufps.desafiosports.domain.model.RolInvitado
import co.edu.ufps.desafiosports.presentacion.comun.BotonContorno
import co.edu.ufps.desafiosports.presentacion.comun.BotonPrimario
import co.edu.ufps.desafiosports.presentacion.comun.EstadoVacio
import co.edu.ufps.desafiosports.presentacion.comun.colorDePanel
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Aceptar una invitación pegando su enlace (MOV-0011). */
@Composable
fun InvitacionScreen(
    onVolver: () -> Unit,
    viewModel: InvitacionViewModel = viewModel(factory = InvitacionViewModel.Factory),
) {
    InvitacionContenido(
        state = viewModel.state,
        onVolver = onVolver,
        onTexto = viewModel::onTextoChange,
        onBuscar = viewModel::buscar,
        onAceptar = viewModel::aceptar,
        onAceptarPendiente = viewModel::aceptarPendiente,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InvitacionContenido(
    state: InvitacionState,
    onVolver: () -> Unit,
    onTexto: (String) -> Unit,
    onBuscar: () -> Unit,
    onAceptar: () -> Unit,
    modifier: Modifier = Modifier,
    onAceptarPendiente: (InvitacionRecibida) -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invitacion_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("invitacion_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) { relleno ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (state.aceptada) {
                EstadoVacio(
                    icono = R.drawable.ic_check_circle,
                    titulo = stringResource(R.string.invitacion_aceptada_titulo),
                    detalle = stringResource(R.string.invitacion_aceptada_detalle),
                    accion = { BotonContorno(stringResource(R.string.accion_listo), onVolver, Modifier.testTag("invitacion_listo")) },
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .testTag("invitacion_aceptada"),
                )
                return@Column
            }

            // Las que le hicieron por su correo: se aceptan aquí mismo, sin enlace (MOV-0012).
            val elegida = state.invitacion
            state.pendientes.forEach { pendiente ->
                val esEsta = elegida?.token == pendiente.token
                VistaPrevia(pendiente, Modifier.testTag("invitacion_pendiente"))
                if (esEsta) {
                    state.errorAlAceptar?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("invitacion_error"),
                        )
                    }
                }
                BotonPrimario(
                    texto = stringResource(R.string.invitacion_aceptar),
                    onClick = { onAceptarPendiente(pendiente) },
                    habilitado = !state.aceptando && !state.buscando,
                    enProceso = state.aceptando && esEsta,
                    textoEnProceso = stringResource(R.string.invitacion_aceptando),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invitacion_aceptar_pendiente"),
                )
            }
            val hayPendientes = state.pendientes.isNotEmpty()
            if (hayPendientes) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                stringResource(if (hayPendientes) R.string.invitacion_explicacion_otra else R.string.invitacion_explicacion),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = state.texto,
                onValueChange = onTexto,
                label = { Text(stringResource(R.string.invitacion_enlace)) },
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it) } },
                enabled = !state.aceptando,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invitacion_enlace"),
            )

            // Una de las pendientes ya tiene su tarjeta arriba: aquí solo va la que se buscó con el enlace.
            val invitacion = state.invitacion?.takeIf { buscada -> state.pendientes.none { it.token == buscada.token } }
            if (invitacion == null) {
                // Mientras no hay invitación a la vista, buscar es la acción principal.
                BotonPrimario(
                    texto = stringResource(R.string.invitacion_buscar),
                    onClick = onBuscar,
                    habilitado = state.texto.isNotBlank() && !state.aceptando,
                    enProceso = state.buscando,
                    textoEnProceso = stringResource(R.string.invitacion_buscando),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invitacion_buscar"),
                )
            } else {
                VistaPrevia(invitacion)
                if (invitacion.pendiente) {
                    state.errorAlAceptar?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("invitacion_error"),
                        )
                    }
                    BotonPrimario(
                        texto = stringResource(R.string.invitacion_aceptar),
                        onClick = onAceptar,
                        enProceso = state.aceptando,
                        textoEnProceso = stringResource(R.string.invitacion_aceptando),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invitacion_aceptar"),
                    )
                }
            }
        }
    }
}

/** Qué es la invitación: el rol, el torneo, quién invita y para quién. */
@Composable
private fun VistaPrevia(invitacion: InvitacionRecibida, modifier: Modifier = Modifier) {
    val rol = when (invitacion.rol) {
        RolInvitado.ARBITRO -> stringResource(R.string.invitacion_rol_arbitro)
        RolInvitado.ADMINISTRADOR -> stringResource(R.string.invitacion_rol_admin)
        RolInvitado.DELEGADO -> stringResource(R.string.invitacion_rol_delegado)
        RolInvitado.DESCONOCIDO -> null
    }
    Surface(color = colorDePanel(), shape = MaterialTheme.shapes.large, modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .semantics(mergeDescendants = true) { }
                .padding(16.dp)
                .testTag("invitacion_vista"),
        ) {
            Text(
                text = rol?.let { stringResource(R.string.invitacion_te_invitan, it) }
                    ?: stringResource(R.string.invitacion_te_invitan_sin_rol),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            invitacion.torneo?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
            invitacion.invitadoPor?.let {
                Text(stringResource(R.string.invitacion_invita, it), style = MaterialTheme.typography.bodyMedium)
            }
            invitacion.paraCorreo?.let {
                Text(
                    stringResource(R.string.invitacion_para, it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!invitacion.pendiente) {
                Text(
                    stringResource(R.string.invitacion_no_vigente),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("invitacion_no_vigente"),
                )
            }
        }
    }
}

// ── Vistas previas ──────────────────────────────────────────────────────────

private val invitacionDeEjemplo = InvitacionRecibida(
    token = "abc", rol = RolInvitado.ARBITRO, torneo = "Copa Prueba 2026",
    invitadoPor = "Carlos Rojas", paraCorreo = "a***@correo.com", pendiente = true,
)

@Preview(name = "Invitación · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Invitación · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InvitacionPreview() {
    DesafioSportsTheme {
        InvitacionContenido(InvitacionState(texto = "https://desafio.test/invitacion/abc", invitacion = invitacionDeEjemplo), {}, {}, {}, {})
    }
}

@Preview(name = "Invitación · para ti", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InvitacionPendientePreview() {
    DesafioSportsTheme {
        InvitacionContenido(InvitacionState(pendientes = listOf(invitacionDeEjemplo.copy(paraCorreo = null))), {}, {}, {}, {})
    }
}

@Preview(name = "Invitación · aceptada", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InvitacionAceptadaPreview() {
    DesafioSportsTheme {
        InvitacionContenido(InvitacionState(aceptada = true), {}, {}, {}, {})
    }
}
