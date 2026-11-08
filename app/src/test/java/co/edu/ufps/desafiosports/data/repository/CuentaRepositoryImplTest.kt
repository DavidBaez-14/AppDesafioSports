package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.Rol
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** MOV-0001 · CA-6, CA-10 y CA-11: `GET /api/auth/me` de punta a punta en la capa data. */
class CuentaRepositoryImplTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: CuentaRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        repositorio = CuentaRepositoryImpl(retrofit.create(AuthApi::class.java), ErroresHttp(ClienteHttp.json))
    }

    @After
    fun tearDown() {
        runCatching { servidor.shutdown() }
    }

    @Test
    fun `CA-6 me devuelve la cuenta con sus roles e ignora lo que no conoce`() = runTest {
        servidor.enqueue(
            MockResponse().setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {"cuentaId":"c1","personaId":"p1","nombres":"Jugador","apellidos":"Uno",
                     "nombre":"Jugador Uno","correo":"jugador01@prueba.local","tipoDocumento":"CC",
                     "numeroDocumento":"9000000301","cedula":"9000000301","estado":"ACTIVA",
                     "roles":["JUGADOR","DELEGADO","ROL_FUTURO"],"rolesPendientes":["ARBITRO"],
                     "campoNuevo":123}
                    """.trimIndent()
                )
        )

        val cuenta = repositorio.obtenerMiCuenta().getOrThrow()

        assertEquals("/api/auth/me", servidor.takeRequest().path)
        assertEquals("Jugador Uno", cuenta.nombre)
        assertEquals("ACTIVA", cuenta.estado)
        assertEquals(setOf(Rol.JUGADOR, Rol.DELEGADO), cuenta.roles)
        assertEquals(setOf(Rol.ARBITRO), cuenta.rolesPendientes)
    }

    @Test
    fun `CA-10 un 404 CUENTA_NO_REGISTRADA llega como error tipado con el mensaje del backend`() = runTest {
        servidor.enqueue(
            MockResponse().setResponseCode(404)
                .setHeader("Content-Type", "application/problem+json")
                .setBody("""{"status":404,"code":"CUENTA_NO_REGISTRADA","mensaje":"Completa tu registro para continuar."}""")
        )

        val error = repositorio.obtenerMiCuenta().exceptionOrNull()

        assertTrue(error is AppError.CuentaNoRegistrada)
        assertEquals("Completa tu registro para continuar.", (error as AppError).mensaje)
    }

    @Test
    fun `CA-11 sin servidor devuelve SinConexion y no revienta`() = runTest {
        servidor.shutdown()

        val error = repositorio.obtenerMiCuenta().exceptionOrNull()

        assertTrue(error is AppError.SinConexion)
    }
}
