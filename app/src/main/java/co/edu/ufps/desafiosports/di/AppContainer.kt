package co.edu.ufps.desafiosports.di

import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDelPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarCamisetaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.AgregarJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.InscribirEquipoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisEquiposBaseUseCase
import co.edu.ufps.desafiosports.domain.repository.DelegadoRepository
import co.edu.ufps.desafiosports.data.repository.DelegadoRepositoryImpl
import co.edu.ufps.desafiosports.data.remote.api.DelegadoApi
import co.edu.ufps.desafiosports.domain.use_case.competicion.VolverAGenerarPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ProgramarPartidoUseCase
import co.edu.ufps.desafiosports.BuildConfig
import co.edu.ufps.desafiosports.data.auth.ProveedorTokenSupabase
import co.edu.ufps.desafiosports.data.remote.api.ArbitrajeApi
import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.api.CreadorApi
import co.edu.ufps.desafiosports.data.repository.ArbitrajeRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.CreadorRepositoryImpl
import co.edu.ufps.desafiosports.domain.repository.CreadorRepository
import co.edu.ufps.desafiosports.domain.use_case.competicion.AprobarInscripcionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CrearTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerDeportesUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerInscripcionesUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RechazarInscripcionUseCase
import co.edu.ufps.desafiosports.domain.repository.ArbitrajeRepository
import co.edu.ufps.desafiosports.domain.use_case.competicion.AlinearEquipoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.AlinearJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CerrarPartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlanillaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDeAlineacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarEventoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RegistrarEventoUseCase
import co.edu.ufps.desafiosports.data.remote.api.SistemaApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.ProveedorToken
import co.edu.ufps.desafiosports.data.repository.CuentaRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.ServidorRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.SesionRepositoryImpl
import co.edu.ufps.desafiosports.domain.model.Configuracion
import co.edu.ufps.desafiosports.domain.repository.CuentaRepository
import co.edu.ufps.desafiosports.domain.repository.ServidorRepository
import co.edu.ufps.desafiosports.domain.repository.SesionRepository
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CompletarRegistroUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionConGoogleUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ReenviarConfirmacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.RegistrarseUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.RecuperarClaveUseCase
import co.edu.ufps.desafiosports.domain.use_case.sistema.ComprobarServidorUseCase
import co.edu.ufps.desafiosports.data.remote.api.GestionTorneoApi
import co.edu.ufps.desafiosports.data.repository.GestionTorneoRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.InvitacionesRepositoryImpl
import co.edu.ufps.desafiosports.domain.repository.GestionTorneoRepository
import co.edu.ufps.desafiosports.domain.repository.InvitacionesRepository
import co.edu.ufps.desafiosports.domain.use_case.competicion.AsignarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarTodosLosPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CancelarInvitacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.GenerarPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.InvitarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPersonalDeArbitrajeUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarArbitroDePartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.AceptarInvitacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerMisInvitacionesUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ReabrirPartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.VerInvitacionUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import co.edu.ufps.desafiosports.data.remote.api.TorneosApi
import co.edu.ufps.desafiosports.data.remote.api.CompeticionApi
import co.edu.ufps.desafiosports.data.remote.api.FinanzasApi
import co.edu.ufps.desafiosports.data.repository.MisEquiposRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.MultasRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.PerfilRepositoryImpl
import co.edu.ufps.desafiosports.domain.repository.MisEquiposRepository
import co.edu.ufps.desafiosports.domain.repository.MultasRepository
import co.edu.ufps.desafiosports.domain.repository.PerfilRepository
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMiPerfilUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisEquiposUseCase
import co.edu.ufps.desafiosports.domain.use_case.finanzas.ObtenerMisMultasUseCase
import co.edu.ufps.desafiosports.data.repository.TorneosRepositoryImpl
import co.edu.ufps.desafiosports.domain.repository.TorneosRepository
import co.edu.ufps.desafiosports.domain.use_case.competicion.AbrirTorneoPorEnlaceUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ExplorarTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerTorneoUseCase

/**
 * Inyección manual de dependencias: el único lugar donde se conectan las capas.
 *
 * Vive en la Application y cada ViewModel toma de aquí lo que necesita en su Factory.
 * Todo es `lazy`: nada se crea hasta que una pantalla lo pide.
 */
class AppContainer(val configuracion: Configuracion) {

    // ── Sesión: Supabase Auth (solo el módulo de Auth) ─────────────────────
    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            // Si falta configuración se usan valores inválidos a propósito: la app abre y
            // el diagnóstico avisa qué falta, en vez de cerrarse al arrancar.
            supabaseUrl = configuracion.supabaseUrl.ifBlank { "https://sin-configurar.invalid" },
            supabaseKey = configuracion.supabaseClavePublicable.ifBlank { "sin-configurar" },
        ) {
            install(Auth)
        }
    }

    private val proveedorToken: ProveedorToken by lazy { ProveedorTokenSupabase(supabase.auth) }

    // ── Red: BackendCodeCup ────────────────────────────────────────────────
    private val erroresHttp: ErroresHttp by lazy { ErroresHttp(ClienteHttp.json) }

    private val okHttp: OkHttpClient by lazy {
        ClienteHttp.okHttp(proveedorToken, erroresHttp, registrarTrafico = BuildConfig.DEBUG)
    }

    private val retrofit: Retrofit by lazy { ClienteHttp.retrofit(configuracion.apiBaseUrl, okHttp) }

    private val authApi: AuthApi by lazy { retrofit.create() }
    private val sistemaApi: SistemaApi by lazy { retrofit.create() }
    private val arbitrajeApi: ArbitrajeApi by lazy { retrofit.create() }
    private val creadorApi: CreadorApi by lazy { retrofit.create() }
    private val torneosApi: TorneosApi by lazy { retrofit.create() }
    private val delegadoApi: DelegadoApi by lazy { retrofit.create() }
    private val competicionApi: CompeticionApi by lazy { retrofit.create() }
    private val finanzasApi: FinanzasApi by lazy { retrofit.create() }
    private val gestionTorneoApi: GestionTorneoApi by lazy { retrofit.create() }

    // ── Repositorios ───────────────────────────────────────────────────────
    val sesionRepository: SesionRepository by lazy { SesionRepositoryImpl(supabase.auth) }
    val cuentaRepository: CuentaRepository by lazy { CuentaRepositoryImpl(authApi, erroresHttp) }
    val servidorRepository: ServidorRepository by lazy {
        ServidorRepositoryImpl(sistemaApi, erroresHttp, ClienteHttp.normalizarBaseUrl(configuracion.apiBaseUrl))
    }

    // ── Casos de uso ───────────────────────────────────────────────────────
    val iniciarSesion by lazy { IniciarSesionUseCase(sesionRepository) }
    val iniciarSesionConGoogle by lazy { IniciarSesionConGoogleUseCase(sesionRepository) }
    val recuperarClave by lazy { RecuperarClaveUseCase(sesionRepository) }
    val registrarse by lazy { RegistrarseUseCase(sesionRepository) }
    val reenviarConfirmacion by lazy { ReenviarConfirmacionUseCase(sesionRepository) }
    val completarRegistro by lazy { CompletarRegistroUseCase(cuentaRepository) }
    val cerrarSesion by lazy { CerrarSesionUseCase(sesionRepository) }
    val observarSesion by lazy { ObservarSesionUseCase(sesionRepository) }
    val obtenerCuenta by lazy { ObtenerCuentaUseCase(cuentaRepository) }
    val comprobarServidor by lazy { ComprobarServidorUseCase(servidorRepository) }

    // Sección Árbitro y planilla (MOV-0009)
    val arbitrajeRepository: ArbitrajeRepository by lazy { ArbitrajeRepositoryImpl(arbitrajeApi, erroresHttp) }
    val obtenerPartidosDeArbitro by lazy { ObtenerPartidosDeArbitroUseCase(arbitrajeRepository) }
    val obtenerPlanilla by lazy { ObtenerPlanillaUseCase(arbitrajeRepository) }
    val registrarEvento by lazy { RegistrarEventoUseCase(arbitrajeRepository) }
    val quitarEvento by lazy { QuitarEventoUseCase(arbitrajeRepository) }
    val alinearJugador by lazy { AlinearJugadorUseCase(arbitrajeRepository) }
    val quitarDeAlineacion by lazy { QuitarDeAlineacionUseCase(arbitrajeRepository) }
    val alinearEquipo by lazy { AlinearEquipoUseCase(arbitrajeRepository) }
    val cerrarPartido by lazy { CerrarPartidoUseCase(arbitrajeRepository) }
    val reabrirPartido by lazy { ReabrirPartidoUseCase(arbitrajeRepository) }

    // Crear torneo y sección Creador (MOV-0007 y MOV-0008)
    val creadorRepository: CreadorRepository by lazy { CreadorRepositoryImpl(creadorApi, erroresHttp) }
    val obtenerDeportes by lazy { ObtenerDeportesUseCase(creadorRepository) }
    val crearTorneo by lazy { CrearTorneoUseCase(creadorRepository) }
    val obtenerMisTorneos by lazy { ObtenerMisTorneosUseCase(creadorRepository) }
    val obtenerInscripciones by lazy { ObtenerInscripcionesUseCase(creadorRepository) }
    val aprobarInscripcion by lazy { AprobarInscripcionUseCase(creadorRepository) }
    val rechazarInscripcion by lazy { RechazarInscripcionUseCase(creadorRepository) }

    // Explorar y detalle del torneo (MOV-0005)
    val torneosRepository: TorneosRepository by lazy { TorneosRepositoryImpl(torneosApi, erroresHttp) }
    val explorarTorneos by lazy { ExplorarTorneosUseCase(torneosRepository) }
    val obtenerTorneo by lazy { ObtenerTorneoUseCase(torneosRepository) }

    // Inscribir un equipo (MOV-0014)
    val delegadoRepository: DelegadoRepository by lazy { DelegadoRepositoryImpl(delegadoApi, erroresHttp) }
    val obtenerMisEquiposBase by lazy { ObtenerMisEquiposBaseUseCase(delegadoRepository) }
    val inscribirEquipo by lazy { InscribirEquipoUseCase(delegadoRepository) }

    // Plantel (MOV-0015)
    val obtenerPlantel by lazy { ObtenerPlantelUseCase(delegadoRepository) }
    val agregarJugador by lazy { AgregarJugadorUseCase(delegadoRepository) }
    val cambiarCamiseta by lazy { CambiarCamisetaUseCase(delegadoRepository) }
    val quitarDelPlantel by lazy { QuitarDelPlantelUseCase(delegadoRepository) }
    val abrirTorneoPorEnlace by lazy { AbrirTorneoPorEnlaceUseCase(torneosRepository) }

    // Inicio y Perfil (MOV-0004)
    val misEquiposRepository: MisEquiposRepository by lazy { MisEquiposRepositoryImpl(competicionApi, erroresHttp) }
    val perfilRepository: PerfilRepository by lazy { PerfilRepositoryImpl(competicionApi, erroresHttp) }
    val multasRepository: MultasRepository by lazy { MultasRepositoryImpl(finanzasApi, erroresHttp) }
    val obtenerMisEquipos by lazy { ObtenerMisEquiposUseCase(misEquiposRepository) }
    val obtenerMiPerfil by lazy { ObtenerMiPerfilUseCase(perfilRepository) }
    val obtenerMisMultas by lazy { ObtenerMisMultasUseCase(multasRepository) }

    // Gestión del torneo: árbitros, partidos e invitaciones (MOV-0011)
    val gestionTorneoRepository: GestionTorneoRepository by lazy { GestionTorneoRepositoryImpl(gestionTorneoApi, erroresHttp) }
    val invitacionesRepository: InvitacionesRepository by lazy { InvitacionesRepositoryImpl(authApi, erroresHttp) }
    val obtenerPersonalDeArbitraje by lazy { ObtenerPersonalDeArbitrajeUseCase(gestionTorneoRepository) }
    val invitarArbitro by lazy { InvitarArbitroUseCase(gestionTorneoRepository) }
    val cancelarInvitacion by lazy { CancelarInvitacionUseCase(gestionTorneoRepository) }
    val quitarArbitro by lazy { QuitarArbitroUseCase(gestionTorneoRepository) }
    val cambiarTodosLosPartidos by lazy { CambiarTodosLosPartidosUseCase(gestionTorneoRepository) }
    val obtenerPartidosDeTorneo by lazy { ObtenerPartidosDeTorneoUseCase(gestionTorneoRepository) }
    val generarPartidos by lazy { GenerarPartidosUseCase(gestionTorneoRepository) }
    val asignarArbitro by lazy { AsignarArbitroUseCase(gestionTorneoRepository) }
    val quitarArbitroDePartido by lazy { QuitarArbitroDePartidoUseCase(gestionTorneoRepository) }
    val programarPartido by lazy { ProgramarPartidoUseCase(gestionTorneoRepository) }
    val volverAGenerarPartidos by lazy { VolverAGenerarPartidosUseCase(gestionTorneoRepository, generarPartidos) }
    val verInvitacion by lazy { VerInvitacionUseCase(invitacionesRepository) }
    val aceptarInvitacion by lazy { AceptarInvitacionUseCase(invitacionesRepository) }
    val obtenerMisInvitaciones by lazy { ObtenerMisInvitacionesUseCase(invitacionesRepository) }

    companion object {
        /** Lee la configuración que Gradle copió de local.properties a BuildConfig. */
        fun configuracionDesdeBuild() = Configuracion(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseClavePublicable = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
            apiBaseUrl = BuildConfig.API_BASE_URL,
            googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
        )
    }
}
