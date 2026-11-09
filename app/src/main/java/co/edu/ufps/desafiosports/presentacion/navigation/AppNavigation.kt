package co.edu.ufps.desafiosports.presentacion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.ufps.desafiosports.BuildConfig
import co.edu.ufps.desafiosports.presentacion.acceso.ArranqueScreen
import co.edu.ufps.desafiosports.presentacion.acceso.LoginScreen
import co.edu.ufps.desafiosports.presentacion.acceso.RecuperarClaveScreen
import co.edu.ufps.desafiosports.presentacion.diagnostico.DiagnosticoScreen

/**
 * Grafo de la app (MOV-0002).
 *
 * Arranque → Login o inicio provisional (diagnóstico). Cerrar sesión desde cualquier lado
 * vuelve al login y borra la pila, así Atrás no regresa a una pantalla de sesión cerrada.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    raiz: RaizViewModel = viewModel(factory = RaizViewModel.Factory),
) {
    LaunchedEffect(raiz.sesionCerrada) {
        if (!raiz.sesionCerrada) return@LaunchedEffect
        val actual = navController.currentBackStackEntry?.destination
        val yaEnAcceso = actual?.hasRoute<Login>() == true || actual?.hasRoute<RecuperarClave>() == true
        // En el login no se navega: se perdería el aviso de error (por ejemplo, cuenta bloqueada).
        if (!yaEnAcceso) {
            navController.navigate(Login) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
        raiz.onSesionCerradaAtendida()
    }

    NavHost(navController = navController, startDestination = Arranque) {
        composable<Arranque> {
            ArranqueScreen(
                destino = raiz.destinoInicial,
                onDestino = { destino ->
                    val ruta: Any = when (destino) {
                        DestinoRaiz.LOGIN -> Login
                        DestinoRaiz.INICIO, DestinoRaiz.DIAGNOSTICO -> Diagnostico
                    }
                    navController.navigate(ruta) {
                        popUpTo<Arranque> { inclusive = true }
                    }
                },
            )
        }
        composable<Login> {
            LoginScreen(
                onIngresado = {
                    navController.navigate(Diagnostico) {
                        popUpTo<Login> { inclusive = true }
                    }
                },
                onRecuperarClave = { correo -> navController.navigate(RecuperarClave(correo)) },
                onAbrirDiagnostico = if (BuildConfig.DEBUG) {
                    { navController.navigate(Diagnostico) }
                } else {
                    null
                },
            )
        }
        composable<RecuperarClave> {
            RecuperarClaveScreen(onVolver = { navController.popBackStack() })
        }
        composable<Diagnostico> { DiagnosticoScreen() }
    }
}
