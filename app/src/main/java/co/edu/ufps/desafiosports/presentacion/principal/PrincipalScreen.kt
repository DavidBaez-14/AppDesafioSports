package co.edu.ufps.desafiosports.presentacion.principal

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.presentacion.explorar.ExplorarScreen
import co.edu.ufps.desafiosports.presentacion.inicio.InicioScreen
import co.edu.ufps.desafiosports.presentacion.perfil.PerfilScreen

/** Las pestañas de la barra inferior. El `name` se guarda al girar el teléfono. */
enum class Pestana(@StringRes val titulo: Int, @DrawableRes val icono: Int, val testTag: String) {
    INICIO(R.string.nav_inicio, R.drawable.ic_home, "nav_inicio"),
    EXPLORAR(R.string.nav_explorar, R.drawable.ic_search, "nav_explorar"),
    PERFIL(R.string.nav_perfil, R.drawable.ic_person, "nav_perfil"),
}

/**
 * La pantalla principal (MOV-0004): Inicio, Explorar y Perfil bajo una barra inferior.
 *
 * Es una sola ruta: la pestaña elegida se guarda aquí y no hay un grafo anidado. Atrás desde
 * Explorar o Perfil vuelve a Inicio; desde Inicio sale de la app.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PrincipalScreen(
    onAbrirArbitro: () -> Unit,
    onAbrirPlanilla: (String) -> Unit,
    onAbrirCreador: () -> Unit,
    onAbrirInscripciones: (TorneoDeCreador) -> Unit,
    onAbrirTorneo: (torneoId: String, enlaceToken: String) -> Unit,
    onCrearTorneo: () -> Unit,
    onCompletarRegistro: () -> Unit,
    onAbrirDiagnostico: (() -> Unit)?,
) {
    var pestana by rememberSaveable { mutableStateOf(Pestana.INICIO) }
    // Cada pestaña conserva dónde iba (la lista, lo que se estaba buscando) al cambiar a otra.
    val guardado = rememberSaveableStateHolder()

    BackHandler(enabled = pestana != Pestana.INICIO) { pestana = Pestana.INICIO }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Cada pestaña trae su propia barra superior y se ocupa de la barra de estado.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.semantics { testTagsAsResourceId = true },
            ) {
                Pestana.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = pestana == destino,
                        onClick = { pestana = destino },
                        icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                        label = { Text(stringResource(destino.titulo)) },
                        // Sin naranja: la barra no es la acción principal de ninguna pantalla.
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.testTag(destino.testTag),
                    )
                }
            }
        },
    ) { relleno ->
        Box(
            Modifier
                .padding(relleno)
                .consumeWindowInsets(relleno)
                .fillMaxSize(),
        ) {
            guardado.SaveableStateProvider(pestana.name) {
                when (pestana) {
                    Pestana.INICIO -> InicioScreen(
                        onAbrirArbitro = onAbrirArbitro,
                        onAbrirPlanilla = onAbrirPlanilla,
                        onAbrirCreador = onAbrirCreador,
                        onAbrirInscripciones = onAbrirInscripciones,
                        onAbrirTorneo = { torneoId -> onAbrirTorneo(torneoId, "") },
                        onCrearTorneo = onCrearTorneo,
                        onExplorar = { pestana = Pestana.EXPLORAR },
                        onCompletarRegistro = onCompletarRegistro,
                    )
                    Pestana.EXPLORAR -> ExplorarScreen(onVolver = null, onAbrirTorneo = onAbrirTorneo)
                    Pestana.PERFIL -> PerfilScreen(onAbrirDiagnostico = onAbrirDiagnostico)
                }
            }
        }
    }
}
