package co.edu.ufps.desafiosports.data.remote.red

import co.edu.ufps.desafiosports.testutil.FakeProveedorToken
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * MOV-0001 · CA-7, CA-8 y CA-9: el cliente HTTP se comporta como `src/api/http.js` de la web.
 * Usa el OkHttp real de la app contra un servidor falso (MockWebServer).
 */
class RedSesionTest {

    private val servidor = MockWebServer()
    private val proveedor = FakeProveedorToken(token = "token-1", tokenRenovado = "token-2")
    private lateinit var cliente: OkHttpClient

    @Before
    fun setUp() {
        servidor.start()
        cliente = ClienteHttp.okHttp(proveedor, ErroresHttp(ClienteHttp.json), registrarTrafico = false)
    }

    @After
    fun tearDown() {
        servidor.shutdown()
    }

    private fun pedir(ruta: String = "/api/auth/me"): Int =
        cliente.newCall(Request.Builder().url(servidor.url(ruta)).build()).execute().use { it.code }

    private fun responder(codigo: Int, cuerpo: String = "{}") {
        servidor.enqueue(
            MockResponse().setResponseCode(codigo)
                .setHeader("Content-Type", "application/problem+json")
                .setBody(cuerpo)
        )
    }

    @Test
    fun `CA-7 cada peticion lleva el Bearer de la sesion`() {
        responder(200)

        pedir()

        assertEquals("Bearer token-1", servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `CA-7 sin sesion no se envia Authorization`() {
        proveedor.token = null
        responder(200)

        pedir()

        assertNull(servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `CA-8 ante 401 renueva la sesion una vez y reintenta con el token nuevo`() {
        responder(401)
        responder(200)

        val codigo = pedir()

        assertEquals(200, codigo)
        assertEquals("Bearer token-1", servidor.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer token-2", servidor.takeRequest().getHeader("Authorization"))
        assertEquals(1, proveedor.renovaciones)
        assertFalse(proveedor.sesionCerrada)
    }

    @Test
    fun `CA-8 si el reintento vuelve a dar 401 no hay bucle y se cierra la sesion`() {
        responder(401)
        responder(401)

        val codigo = pedir()

        assertEquals(401, codigo)
        assertEquals(2, servidor.requestCount)
        assertEquals(1, proveedor.renovaciones)
        assertTrue(proveedor.sesionCerrada)
    }

    @Test
    fun `CA-8 si no se puede renovar no reintenta y cierra la sesion`() {
        proveedor.tokenRenovado = null
        responder(401)

        val codigo = pedir()

        assertEquals(401, codigo)
        assertEquals(1, servidor.requestCount)
        assertTrue(proveedor.sesionCerrada)
    }

    @Test
    fun `CA-8 un 401 sin sesion no intenta renovar`() {
        proveedor.token = null
        responder(401)

        pedir()

        assertEquals(0, proveedor.renovaciones)
        assertEquals(1, servidor.requestCount)
    }

    @Test
    fun `CA-9 un 403 CUENTA_BLOQUEADA cierra la sesion local`() {
        responder(403, """{"status":403,"code":"CUENTA_BLOQUEADA","mensaje":"Tu cuenta está bloqueada."}""")

        val codigo = pedir()

        assertEquals(403, codigo)
        assertTrue(proveedor.sesionCerrada)
    }

    @Test
    fun `CA-9 un 403 ACCESO_DENEGADO no cierra la sesion`() {
        responder(403, """{"status":403,"code":"ACCESO_DENEGADO","mensaje":"No tienes permisos para esta acción."}""")

        pedir()

        assertFalse(proveedor.sesionCerrada)
    }
}
