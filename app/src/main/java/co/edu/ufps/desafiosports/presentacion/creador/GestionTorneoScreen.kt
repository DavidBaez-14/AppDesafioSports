package co.edu.ufps.desafiosports.presentacion.creador

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.presentacion.comun.Fila
import co.edu.ufps.desafiosports.presentacion.comun.FlechaFila
import co.edu.ufps.desafiosports.presentacion.comun.IconoEnCirculo
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme

/**
 * Las tres puertas de un torneo que administro: inscripciones, árbitros y partidos (MOV-0011).
 * No pide nada al backend: cada pantalla carga lo suyo.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GestionTorneoScreen(
    torneo: String,
    onVolver: () -> Unit,
    onInscripciones: () -> Unit,
    onArbitros: () -> Unit,
    onPartidos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { TituloConTorneo(stringResource(R.string.gestion_titulo), torneo) },
                navigationIcon = {
                    IconButton(onClick = onVolver, modifier = Modifier.testTag("gestion_atras")) {
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
            Modifier
                .padding(relleno)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Puerta(R.drawable.ic_how_to_reg, R.string.inscripciones_titulo, R.string.gestion_inscripciones_detalle, "gestion_inscripciones", onInscripciones)
            Separador()
            Puerta(R.drawable.ic_sports, R.string.gestion_arbitros, R.string.gestion_arbitros_detalle, "gestion_arbitros", onArbitros)
            Separador()
            Puerta(R.drawable.ic_event, R.string.gestion_partidos, R.string.gestion_partidos_detalle, "gestion_partidos", onPartidos)
        }
    }
}

/** Título de una pantalla de gestión con el nombre del torneo debajo. */
@Composable
internal fun TituloConTorneo(titulo: String, torneo: String) {
    Column {
        Text(titulo, maxLines = 1)
        if (torneo.isNotBlank()) {
            Text(
                text = torneo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Puerta(@DrawableRes icono: Int, titulo: Int, detalle: Int, tag: String, onClick: () -> Unit) {
    Fila(
        titulo = stringResource(titulo),
        detalle = stringResource(detalle),
        inicio = { IconoEnCirculo(icono) },
        fin = { FlechaFila() },
        onClick = onClick,
        modifier = Modifier.testTag(tag),
    )
}

@Composable
private fun Separador() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(start = 72.dp, end = 16.dp))
}

@Preview(name = "Gestión · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Gestión · claro", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun GestionTorneoPreview() {
    DesafioSportsTheme {
        GestionTorneoScreen("Copa Prueba 2026", {}, {}, {}, {})
    }
}
