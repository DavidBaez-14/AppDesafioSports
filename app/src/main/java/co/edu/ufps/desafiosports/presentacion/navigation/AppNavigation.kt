package co.edu.ufps.desafiosports.presentacion.navigation

import co.edu.ufps.desafiosports.presentacion.delegado.InscribirEquipoScreen
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
import androidx.navigation.toRoute
import co.edu.ufps.desafiosports.presentacion.creador.ArbitrosTorneoScreen
import co.edu.ufps.desafiosports.presentacion.creador.CrearTorneoScreen
import co.edu.ufps.desafiosports.presentacion.creador.GestionTorneoScreen
import co.edu.ufps.desafiosports.presentacion.creador.PartidosTorneoScreen
import co.edu.ufps.desafiosports.presentacion.perfil.InvitacionScreen
import co.edu.ufps.desafiosports.presentacion.creador.InscripcionesScreen
import co.edu.ufps.desafiosports.presentacion.creador.SeccionCreadorScreen
import co.edu.ufps.desafiosports.presentacion.diagnostico.AccesoProvisional
import co.edu.ufps.desafiosports.presentacion.diagnostico.DiagnosticoScreen
import co.edu.ufps.desafiosports.presentacion.principal.PrincipalScreen
import co.edu.ufps.desafiosports.presentacion.explorar.ExplorarScreen
import co.edu.ufps.desafiosports.presentacion.explorar.TorneoScreen

/**
 * Grafo de la app (MOV-0002).
 *
 * Arranque → Login o la pantalla principal (MOV-0004). Cerrar sesión desde cualquier lado
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
                        DestinoRaiz.INICIO -> Principal
                        // Sin configuración completa no hay sesión posible: el diagnóstico dice qué falta.
                        DestinoRaiz.DIAGNOSTICO -> Diagnostico
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
                    navController.navigate(Principal) {
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
                onExplorar = { navController.navigate(Explorar(visitante = true)) },
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
                    navController.navigate(Principal) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }
        composable<Principal> {
            PrincipalScreen(
                onAbrirArbitro = { navController.navigate(SeccionArbitro) },
                onAbrirPlanilla = { partidoId -> navController.navigate(Planilla(partidoId)) },
                onAbrirCreador = { navController.navigate(SeccionCreador) },
                onAbrirInscripciones = { torneo -> navController.navigate(InscripcionesTorneo(torneo.id, torneo.nombre)) },
                onAbrirTorneo = { torneoId, enlaceToken -> navController.navigate(Torneo(torneoId, enlaceToken)) },
                onCrearTorneo = { navController.navigate(CrearTorneo) },
                // Sesión de una cuenta sin registro en el backend: no hay Inicio que mostrar (CA-10).
                onCompletarRegistro = {
                    navController.navigate(CompletarRegistro) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onAbrirDiagnostico = if (BuildConfig.DEBUG) {
                    { navController.navigate(Diagnostico) }
                } else {
                    null
                },
                onAbrirInvitacion = { navController.navigate(Invitacion) },
            )
        }
        composable<Explorar> {
            ExplorarScreen(
                onVolver = { navController.popBackStack() },
                onAbrirTorneo = { torneoId, enlaceToken -> navController.navigate(Torneo(torneoId, enlaceToken)) },
            )
        }
        composable<Torneo> {
            TorneoScreen(
                onVolver = { navController.popBackStack() },
                // Sin cuenta, "Entrar" vuelve al login que abrió Explorar (o lo abre si no está).
                onEntrar = {
                    if (!navController.popBackStack<Login>(inclusive = false)) {
                        navController.navigate(Login) { popUpTo(navController.graph.id) { inclusive = true } }
                    }
                },
                onAbrirCreador = { torneo -> navController.navigate(GestionTorneo(torneo.id, torneo.nombre)) },
                onAbrirArbitro = { navController.navigate(SeccionArbitro) },
                onInscribir = { torneo, enlaceToken ->
                    val cuota = if (torneo.cobraInscripcion) torneo.valorInscripcion?.toLong() ?: 0 else 0
                    navController.navigate(
                        InscribirEquipo(torneo.id, torneo.nombre, enlaceToken.orEmpty(), cuota, torneo.exigeDocumento),
                    )
                },
            )
        }
        composable<InscribirEquipo> {
            InscribirEquipoScreen(onVolver = { navController.popBackStack() })
        }
        composable<Diagnostico> {
            // Accesos directos a las secciones, útiles para probar sin pasar por Inicio.
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
                // "Por revisar" va directo a lo urgente; un torneo de la lista abre su gestión (MOV-0011).
                onRevisarTorneo = { torneo -> navController.navigate(InscripcionesTorneo(torneo.id, torneo.nombre)) },
                onAbrirTorneo = { torneo -> navController.navigate(GestionTorneo(torneo.id, torneo.nombre)) },
            )
        }
        composable<GestionTorneo> { entrada ->
            val ruta = entrada.toRoute<GestionTorneo>()
            GestionTorneoScreen(
                torneo = ruta.nombre,
                onVolver = { navController.popBackStack() },
                onInscripciones = { navController.navigate(InscripcionesTorneo(ruta.torneoId, ruta.nombre)) },
                onArbitros = { navController.navigate(ArbitrosTorneo(ruta.torneoId, ruta.nombre)) },
                onPartidos = { navController.navigate(PartidosTorneo(ruta.torneoId, ruta.nombre)) },
            )
        }
        composable<ArbitrosTorneo> {
            ArbitrosTorneoScreen(onVolver = { navController.popBackStack() })
        }
        composable<PartidosTorneo> { entrada ->
            val ruta = entrada.toRoute<PartidosTorneo>()
            PartidosTorneoScreen(
                onVolver = { navController.popBackStack() },
                onInvitarArbitros = { navController.navigate(ArbitrosTorneo(ruta.torneoId, ruta.nombre)) },
                onAbrirPlanilla = { partidoId -> navController.navigate(Planilla(partidoId)) },
            )
        }
        composable<Invitacion> {
            InvitacionScreen(onVolver = { navController.popBackStack() })
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
