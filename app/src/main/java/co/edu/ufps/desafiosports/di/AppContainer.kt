package co.edu.ufps.desafiosports.di

import co.edu.ufps.desafiosports.BuildConfig
import co.edu.ufps.desafiosports.data.auth.ProveedorTokenSupabase
import co.edu.ufps.desafiosports.data.remote.api.AuthApi
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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create

/**
 * Inyección manual de dependencias: el único lugar donde se conectan las capas.
 *
 * Es el patrón del profesor (StreamFlix armaba todo en MainActivity) pero centralizado:
 * vive en la Application y cada ViewModel toma de aquí lo que necesita en su Factory.
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
