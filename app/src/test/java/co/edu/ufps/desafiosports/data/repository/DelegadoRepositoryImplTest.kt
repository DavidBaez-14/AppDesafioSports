package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.DelegadoApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** MOV-0014: el contrato de la inscripción de un equipo, en la capa data. */
class DelegadoRepositoryImplTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: DelegadoRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        repositorio = DelegadoRepositoryImpl(retrofit.create(DelegadoApi::class.java), ErroresHttp(ClienteHttp.json))
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
    fun `CA-2 mis equipos llegan en el orden del backend y el que no tiene id se descarta`() = runTest {
        servidor.enqueue(json("""[{"id":"e2","nombre":"Pumas FC"},{"id":"e1","nombre":"Halcones FC"},{"id":"","nombre":"Roto"}]"""))

        val equipos = repositorio.misEquipos().getOrThrow()

        assertEquals("/api/supercopa/delegado/equipos", servidor.takeRequest().path)
        assertEquals(listOf("e2", "e1"), equipos.map { it.id })
        assertEquals("Pumas FC", equipos.first().nombre)
    }

    @Test
    fun `CA-3 inscribir un equipo propio envia solo su id y lee la inscripcion`() = runTest {
        servidor.enqueue(
            json(
                """{"id":"et1","torneoId":"t1","torneoNombre":"Copa Prueba 2026","equipoId":"e1","equipoNombre":"Halcones FC",
                    "estadoInscripcion":"PENDIENTE_PAGO","montoInscripcion":50000}""",
            ),
        )

        val hecha = repositorio.inscribir("t1", "e1", null, null).getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/supercopa/delegado/torneos/t1/inscribir", peticion.path)
        assertEquals("""{"equipoId":"e1"}""", peticion.body.readUtf8())
        assertEquals("et1", hecha.equipoTorneoId)
        assertEquals("Halcones FC", hecha.equipo)
        assertEquals(EstadoInscripcion.PENDIENTE_PAGO, hecha.estado)
    }

    @Test
    fun `CA-4 un equipo nuevo viaja por su nombre y con el enlace del torneo`() = runTest {
        servidor.enqueue(json("""{"id":"et2","equipoNombre":null,"estadoInscripcion":"PENDIENTE_PAGO"}"""))

        val hecha = repositorio.inscribir("t1", null, "Tiburones FC", "tok-enlace").getOrThrow()

        assertEquals("""{"nombreEquipo":"Tiburones FC","enlaceToken":"tok-enlace"}""", servidor.takeRequest().body.readUtf8())
        // Sin nombre en la respuesta, se usa el que se escribió.
        assertEquals("Tiburones FC", hecha.equipo)
    }

    @Test
    fun `CA-6 los rechazos llegan con su codigo y su mensaje`() = runTest {
        servidor.enqueue(problema(409, "YA_INSCRITO", "Ya tienes una inscripcion para este torneo."))
        servidor.enqueue(problema(409, "EQUIPO_DUPLICADO", "Ya tienes un equipo llamado Pumas FC."))
        servidor.enqueue(problema(409, "CUPOS_AGOTADOS", "Este torneo ya no tiene cupos."))

        val yaInscrito = repositorio.inscribir("t1", "e1", null, null).exceptionOrNull() as AppError.Negocio
        val duplicado = repositorio.inscribir("t1", null, "Pumas FC", null).exceptionOrNull() as AppError.Negocio
        val sinCupos = repositorio.inscribir("t1", "e1", null, null).exceptionOrNull() as AppError.Negocio

        assertEquals("YA_INSCRITO", yaInscrito.codigo)
        assertEquals("Ya tienes una inscripcion para este torneo.", yaInscrito.mensaje)
        assertEquals("EQUIPO_DUPLICADO", duplicado.codigo)
        assertTrue(duplicado.mensaje.contains("Pumas FC"))
        assertEquals("CUPOS_AGOTADOS", sinCupos.codigo)
    }
}
