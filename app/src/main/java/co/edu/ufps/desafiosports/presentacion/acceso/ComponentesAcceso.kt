package co.edu.ufps.desafiosports.presentacion.acceso

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import co.edu.ufps.desafiosports.R

/**
 * Aviso de error bajo el formulario. TalkBack lo anuncia al aparecer (región viva).
 */
@Composable
internal fun AvisoError(mensaje: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Icon(painterResource(R.drawable.ic_error), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(mensaje, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * El aviso de error entra y sale desplegándose (el único movimiento de estas pantallas).
 * Mientras se va conserva el último texto, para no encogerse vacío. Deja 12 dp debajo,
 * pegado al botón que lo sigue. Con "Quitar animaciones" del sistema, Compose lo hace instantáneo.
 */
@Composable
internal fun AvisoErrorAnimado(mensaje: String?, modifier: Modifier = Modifier) {
    var ultimo by remember { mutableStateOf(mensaje.orEmpty()) }
    LaunchedEffect(mensaje) {
        if (mensaje != null) ultimo = mensaje
    }
    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = modifier,
    ) {
        AvisoError(mensaje ?: ultimo, Modifier.padding(bottom = 12.dp))
    }
}

/**
 * Botón principal de los formularios de acceso. Mientras trabaja muestra el progreso y no
 * acepta toques, pero conserva el color de marca (un botón gris parece roto, no ocupado).
 */
@Composable
internal fun BotonPrincipal(
    texto: String,
    textoEnProceso: String,
    enProceso: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !enProceso,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
    ) {
        if (enProceso) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(textoEnProceso, style = MaterialTheme.typography.labelLarge)
        } else {
            Text(texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}
