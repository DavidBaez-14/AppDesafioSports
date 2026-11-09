package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/** Revisa tu correo tras crear la cuenta (MOV-0003 · N-1). El enlace del correo abre la web. */
@Composable
fun RevisaTuCorreoScreen(
    onIrAEntrar: () -> Unit,
    viewModel: RevisaTuCorreoViewModel = viewModel(factory = RevisaTuCorreoViewModel.Factory),
) {
    RevisaTuCorreoContenido(state = viewModel.state, onReenviar = viewModel::reenviar, onIrAEntrar = onIrAEntrar)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisaTuCorreoContenido(
    state: RevisaTuCorreoState,
    onReenviar: () -> Unit,
    onIrAEntrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onIrAEntrar, modifier = Modifier.testTag("revisa_atras")) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.accion_atras))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { relleno ->
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.padding(relleno)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .testTag("revisa_tu_correo"),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mark_email_read),
                    contentDescription = null,
                    // Decorativo: el naranja queda para "Ir a entrar".
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp),
                )
                Text(
                    text = stringResource(R.string.revisa_titulo).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.revisa_detalle, state.correo),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.revisa_nota),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                if (state.reenviado) {
                    Text(
                        text = stringResource(R.string.revisa_reenviado),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .semantics { liveRegion = LiveRegionMode.Polite }
                            .testTag("revisa_reenviado"),
                    )
                }
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    AvisoErrorAnimado(state.error, Modifier.testTag("revisa_error"))
                    BotonPrincipal(
                        texto = stringResource(R.string.revisa_ir_a_entrar),
                        textoEnProceso = stringResource(R.string.revisa_ir_a_entrar),
                        enProceso = false,
                        onClick = onIrAEntrar,
                        modifier = Modifier.testTag("revisa_ir_a_entrar"),
                    )
                }
                TextButton(onClick = onReenviar, enabled = !state.enProceso, modifier = Modifier.testTag("revisa_reenviar")) {
                    Text(
                        text = stringResource(if (state.enProceso) R.string.revisa_reenviando else R.string.revisa_reenviar),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview(name = "Revisa tu correo · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Revisa tu correo · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RevisaTuCorreoContenidoPreview() {
    DesafioSportsTheme {
        RevisaTuCorreoContenido(RevisaTuCorreoState(correo = "andres.pena@correo.co", reenviado = true), {}, {})
    }
}
