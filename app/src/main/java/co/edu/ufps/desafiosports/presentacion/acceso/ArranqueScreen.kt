package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.presentacion.navigation.DestinoRaiz
import co.edu.ufps.desafiosports.ui.theme.DesafioSportsTheme
import co.edu.ufps.desafiosports.ui.theme.Logotipo

/**
 * Arranque (MOV-0002 · A-1): el logotipo mientras se lee la sesión guardada. En cuanto hay
 * destino, navega. Suele durar menos de un instante: no lleva indicador de progreso.
 */
@Composable
fun ArranqueScreen(destino: DestinoRaiz?, onDestino: (DestinoRaiz) -> Unit) {
    LaunchedEffect(destino) {
        destino?.let(onDestino)
    }
    ArranqueContenido()
}

@Composable
fun ArranqueContenido(modifier: Modifier = Modifier) {
    val descripcion = stringResource(R.string.arranque_cargando)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = descripcion }
            .testTag("arranque"),
    ) {
        Logotipo()
    }
}

@Preview(name = "Arranque · oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Arranque · claro", showBackground = true)
@Composable
private fun ArranqueContenidoPreview() {
    DesafioSportsTheme { ArranqueContenido() }
}
