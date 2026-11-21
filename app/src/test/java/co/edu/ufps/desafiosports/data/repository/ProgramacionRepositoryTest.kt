package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.GestionTorneoApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/** MOV-0013: programar un partido y borrar los partidos, en la capa data. */
class ProgramacionRepositoryTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: GestionTorneoRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        repositorio = GestionTorneoRepositoryImpl(retrofit.create(GestionTorneoApi::class.java), ErroresHttp(ClienteHttp.json))
    }

    @After
    fun tearDown() {
        runCatching { servidor.shutdown() }
    }

    private fun json(cuerpo: String, codigo: Int = 200) =
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo)

    private fun problema(codigo: Int, code: String, mensaje: String) =
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/problem+json")
            .setBody("""{"status":$codigo,"code":"$code","mensaje":"$mensaje"}""")

    @Test
    fun `CA-2 programar envia PUT con la fecha sin zona y el lugar, y lee como quedo`() = runTest {
        servidor.enqueue(
            json(
                """{"partidoId":"p1","torneoId":"t1","estado":"PROGRAMADO","fechaHora":"2026-11-21T15:30:00",
                    "lugar":"Coliseo UFPS, cancha 1","asignado":false}""",
            ),
        )

        val programacion = repositorio.programarPartido("p1", LocalDateTime.of(2026, 11, 21, 15, 30), "Coliseo UFPS, cancha 1")
            .getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("PUT", peticion.method)
        assertEquals("/api/supercopa/admin/partidos/p1/programacion", peticion.path)
        assertEquals("""{"fecha":"2026-11-21T15:30:00","lugar":"Coliseo UFPS, cancha 1"}""", peticion.body.readUtf8())
        assertEquals(LocalDateTime.of(2026, 11, 21, 15, 30), programacion.fecha)
        assertEquals("Coliseo UFPS, cancha 1", programacion.lugar)
    }

    @Test
    fun `CA-2 sin fecha no la envia, y un lugar vacio llega como sin lugar`() = runTest {
        servidor.enqueue(json("""{"partidoId":"p1","fechaHora":"2026-11-21T08:00:00","lugar":null}"""))

        val programacion = repositorio.programarPartido("p1", null, "").getOrThrow()

        assertEquals("""{"lugar":""}""", servidor.takeRequest().body.readUtf8())
        assertNull(programacion.lugar)
        assertEquals(LocalDateTime.of(2026, 11, 21, 8, 0), programacion.fecha)
    }

    @Test
    fun `CA-4 un partido que ya termino llega con su codigo y su mensaje`() = runTest {
        servidor.enqueue(problema(409, "PARTIDO_CERRADO", "El partido ya terminó: no se puede cambiar."))

        val fallo = repositorio.programarPartido("p1", LocalDateTime.of(2026, 11, 21, 8, 0), "").exceptionOrNull()

        assertEquals("PARTIDO_CERRADO", (fallo as AppError.Negocio).codigo)
        assertEquals("El partido ya terminó: no se puede cambiar.", fallo.mensaje)
    }

    @Test
    fun `CA-6 borrar los partidos envia DELETE al torneo y acepta el 204`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(204))
        servidor.enqueue(problema(400, "REGLA_NEGOCIO", "No se puede borrar el fixture: ya hay partidos jugados o en curso."))

        assertTrue(repositorio.borrarPartidos("t1").isSuccess)
        val fallo = repositorio.borrarPartidos("t1")

        val peticion = servidor.takeRequest()
        assertEquals("DELETE", peticion.method)
        assertEquals("/api/supercopa/admin/torneos/t1/fixture", peticion.path)
        assertFalse(fallo.isSuccess)
        assertEquals("No se puede borrar el fixture: ya hay partidos jugados o en curso.", (fallo.exceptionOrNull() as AppError).mensaje)
    }
}
