package co.edu.ufps.desafiosports.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.edu.ufps.desafiosports.R

/**
 * Logotipo tipográfico provisional (MOV-0002): "DESAFÍO" sobre "SPORTS", Barlow Condensed
 * ExtraBold Itálica, con "SPORTS" en el color primario. El logo definitivo lo reemplaza aquí.
 *
 * TalkBack lo lee una vez, como "Desafío Sports", y como encabezado.
 */
@Composable
fun Logotipo(
    modifier: Modifier = Modifier,
    tamano: TextUnit = 46.sp,
) {
    val nombre = stringResource(R.string.app_name)
    val linea1 = stringResource(R.string.logotipo_linea_1).uppercase()
    val linea2 = stringResource(R.string.logotipo_linea_2).uppercase()
    Text(
        text = buildAnnotatedString {
            append(linea1)
            append('\n')
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append(linea2) }
        },
        style = TextStyle(
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = tamano,
            lineHeight = tamano * 0.95f,
            letterSpacing = 0.01.em,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = nombre
            heading()
        },
    )
}
