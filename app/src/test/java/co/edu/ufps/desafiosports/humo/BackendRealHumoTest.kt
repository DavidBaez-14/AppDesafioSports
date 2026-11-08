package co.edu.ufps.desafiosports.humo

import co.edu.ufps.desafiosports.BuildConfig
import co.edu.ufps.desafiosports.data.auth.ProveedorTokenSupabase
import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.api.SistemaApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.repository.CuentaRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.ServidorRepositoryImpl
import co.edu.ufps.desafiosports.data.repository.SesionRepositoryImpl
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.Rol
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemoryCodeVerifierCache
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.create

/**
 * Prueba de humo contra el backend y el Supabase REALES (MOV-0001 · CA-4, CA-6, CA-10).
 *
 * No corre en la suite normal: se salta si no defines DESAFIO_HUMO=1.
 * Requisitos: BackendCodeCup levantado y local.properties con SUPABASE_URL y SUPABASE_PUBLISHABLE_KEY.
 *
 *   DESAFIO_HUMO=1 HUMO_CLAVE=<SEED_PASSWORD del .env del backend> \
 *     ./gradlew testDebugUnitTest --tests "*BackendRealHumoTest*" --rerun
 *
 * Variables opcionales: HUMO_CORREO (por defecto jugador01@prueba.local) y
 * HUMO_API_URL (por defecto http://localhost:8082/, porque corre en el PC, no en el emulador).
 */
class BackendRealHumoTest {

    private val apiUrl = System.getenv("HUMO_API_URL") ?: "http://localhost:8082/"
    private val correo = System.getenv("HUMO_CORREO") ?: "jugador01@prueba.local"
    private val errores = ErroresHttp(ClienteHttp.json)

    @Before
    fun soloSiSePide() {
        assumeTrue("Prueba de humo desactivada: define DESAFIO_HUMO=1", System.getenv("DESAFIO_HUMO") == "1")
    }

    @Test
    fun `CA-4 el backend real responde UP en actuator health`() = runBlocking {
        val api: SistemaApi = ClienteHttp.retrofit(apiUrl, OkHttpClient()).create()

        val estado = ServidorRepositoryImpl(api, errores, apiUrl).comprobar().getOrThrow()

        println("[humo] $apiUrl actuator/health -> ${estado.estado}")
        assertTrue(estado.enLinea)
    }

    @Test
    fun `CA-10 sin sesion el backend real responde NO_AUTENTICADO con su mensaje`() = runBlocking {
        val api: AuthApi = ClienteHttp.retrofit(apiUrl, OkHttpClient()).create()

        val error = CuentaRepositoryImpl(api, errores).obtenerMiCuenta().exceptionOrNull()

        println("[humo] GET api/auth/me sin token -> ${error?.javaClass?.simpleName}: ${(error as? AppError)?.mensaje}")
        assertTrue(error is AppError.NoAutenticado)
    }

    @Test
    fun `CA-6 inicia sesion en Supabase y GET api auth me devuelve la cuenta`() = runBlocking {
        val clave = System.getenv("HUMO_CLAVE")
        assumeTrue("Falta HUMO_CLAVE", !clave.isNullOrBlank())
        assumeTrue("Falta configuración de Supabase en local.properties", BuildConfig.SUPABASE_URL.isNotBlank())

        // Mismo cliente que la app, pero con la sesión en memoria (en la JVM no hay SharedPreferences).
        val supabase = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY) {
            install(Auth) {
                enableLifecycleCallbacks = false
                autoLoadFromStorage = false
                alwaysAutoRefresh = false
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
        }
        val sesion = SesionRepositoryImpl(supabase.auth)
        val okHttp = ClienteHttp.okHttp(ProveedorTokenSupabase(supabase.auth), errores, registrarTrafico = false)
        val cuentas = CuentaRepositoryImpl(ClienteHttp.retrofit(apiUrl, okHttp).create(), errores)

        sesion.iniciarSesion(correo, clave!!).getOrThrow()
        val cuenta = cuentas.obtenerMiCuenta().getOrThrow()
        sesion.cerrarSesion()

        println("[humo] GET api/auth/me -> ${cuenta.nombre} <${cuenta.correo}> estado=${cuenta.estado} roles=${cuenta.roles}")
        assertEquals(correo, cuenta.correo)
        assertTrue(Rol.JUGADOR in cuenta.roles)
    }
}
