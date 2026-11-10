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
import co.edu.ufps.desafiosports.presentacion.acceso.CompletarRegistroScreen
import co.edu.ufps.desafiosports.presentacion.acceso.LoginScreen
import co.edu.ufps.desafiosports.presentacion.acceso.RecuperarClaveScreen
import co.edu.ufps.desafiosports.presentacion.acceso.RegistroScreen
import co.edu.ufps.desafiosports.presentacion.acceso.RevisaTuCorreoScreen
import androidx.compose.ui.res.stringResource
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.presentacion.arbitro.PlanillaScreen
import co.edu.ufps.desafiosports.presentacion.arbitro.SeccionArbitroScreen
import co.edu.ufps.desafiosports.presentacion.creador.CrearTorneoScreen
import co.edu.ufps.desafiosports.presentacion.creador.InscripcionesScreen
import co.edu.ufps.desafiosports.presentacion.creador.SeccionCreadorScreen
import co.edu.ufps.desafiosports.presentacion.diagnostico.AccesoProvisional
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
                onCrearCuenta = { navController.navigate(Registro) },
                onCompletarRegistro = {
                    navController.navigate(CompletarRegistro) {
                        popUpTo<Login> { inclusive = true }
                    }
                },
                onAbrirDiagnostico = if (BuildConfig.DEBUG) {
                    { navController.navigate(Diagnostico) }
                } else {
                    null
                },
                googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.ifBlank { null },
            )
        }
        composable<RecuperarClave> {
            RecuperarClaveScreen(onVolver = { navController.popBackStack() })
        }
        composable<Registro> {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                // Revisa tu correo reemplaza a Crear cuenta: Atrás o "Ir a entrar" vuelven al login.
                onRevisarCorreo = { correo ->
                    navController.navigate(RevisaTuCorreo(correo)) {
                        popUpTo<Registro> { inclusive = true }
                    }
                },
                onCompletarRegistro = {
                    navController.navigate(CompletarRegistro) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }
        composable<RevisaTuCorreo> {
            RevisaTuCorreoScreen(onIrAEntrar = { navController.popBackStack() })
        }
        composable<CompletarRegistro> {
            CompletarRegistroScreen(
                onRegistrado = {
                    navController.navigate(Diagnostico) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }
        composable<Diagnostico> {
            // Acceso provisional a las secciones mientras llega Inicio (MOV-0004).
            DiagnosticoScreen(
                accesos = listOf(
                    AccesoProvisional(stringResource(R.string.arbitro_titulo), "acceso_arbitro") {
                        navController.navigate(SeccionArbitro)
                    },
                    AccesoProvisional(stringResource(R.string.creador_titulo), "acceso_creador") {
                        navController.navigate(SeccionCreador)
                    },
                    AccesoProvisional(stringResource(R.string.creador_crear_torneo), "acceso_crear_torneo") {
                        navController.navigate(CrearTorneo)
                    },
                ),
            )
        }
        composable<SeccionCreador> {
            SeccionCreadorScreen(
                onVolver = { navController.popBackStack() },
                onCrearTorneo = { navController.navigate(CrearTorneo) },
                onAbrirTorneo = { torneo -> navController.navigate(InscripcionesTorneo(torneo.id, torneo.nombre)) },
            )
        }
        composable<CrearTorneo> {
            CrearTorneoScreen(
                onSalir = { navController.popBackStack() },
                // "Ver mis torneos" reemplaza el formulario: Atrás desde la sección no vuelve a él.
                // Si se entró desde la sección, se regresa a ella en vez de abrir otra encima.
                onVerMisTorneos = {
                    if (!navController.popBackStack<SeccionCreador>(inclusive = false)) {
                        navController.navigate(SeccionCreador) {
                            popUpTo<CrearTorneo> { inclusive = true }
                        }
                    }
                },
            )
        }
        composable<InscripcionesTorneo> {
            InscripcionesScreen(onVolver = { navController.popBackStack() })
        }
        composable<SeccionArbitro> {
            SeccionArbitroScreen(
                onVolver = { navController.popBackStack() },
                onAbrirPlanilla = { partidoId -> navController.navigate(Planilla(partidoId)) },
            )
        }
        composable<Planilla> {
            PlanillaScreen(onVolver = { navController.popBackStack() })
        }
    }
}
