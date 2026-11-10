package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.CreadorApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.NuevoTorneo
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.domain.use_case.competicion.RechazarInscripcionUseCase
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/** MOV-0007 y MOV-0008: el contrato de crear torneo y de la sección Creador, en la capa data. */
class CreadorRepositoryImplTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: CreadorRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        repositorio = CreadorRepositoryImpl(retrofit.create(CreadorApi::class.java), ErroresHttp(ClienteHttp.json))
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

    private val torneo = NuevoTorneo(
        nombre = "Relámpago de Tenis de Mesa",
        deporte = "TENIS_MESA",
        formato = FormatoTorneo.ELIMINACION_DIRECTA,
        cupos = 16,
        fechaInicio = LocalDate.of(2026, 11, 21),
        visibilidad = Visibilidad.PUBLICO,
        cobraInscripcion = true,
        valorInscripcion = 5000,
    )

    @Test
    fun `MOV-0007 CA-1 el catalogo de deportes llega con su tipo y descarta lo que no sirve`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"codigo":"FUTSAL","nombre":"Fútbol sala","tipoCompetencia":"EQUIPO"},
                 {"codigo":"TENIS_MESA","nombre":"Tenis de mesa","tipoCompetencia":"INDIVIDUAL"},
                 {"codigo":"AJEDREZ","nombre":null,"tipoCompetencia":"POR_PAREJAS"},
                 {"codigo":"","nombre":"Sin código","tipoCompetencia":"EQUIPO"}]
                """.trimIndent(),
            ),
        )

        val deportes = repositorio.deportes().getOrThrow()

        assertEquals("/api/public/deportes", servidor.takeRequest().path)
        assertEquals(listOf("FUTSAL", "TENIS_MESA", "AJEDREZ"), deportes.map { it.codigo })
        assertEquals(TipoCompetencia.INDIVIDUAL, deportes[1].tipo)
        // Un deporte sin nombre muestra su código; un tipo desconocido se trata como por equipos.
        assertEquals("AJEDREZ", deportes[2].nombre)
        assertEquals(TipoCompetencia.EQUIPO, deportes[2].tipo)
    }

    @Test
    fun `MOV-0007 CA-2 crear envia POST con todos los campos y devuelve el enlace`() = runTest {
        servidor.enqueue(
            json(
                """{"id":"t9","nombre":"Relámpago de Tenis de Mesa","deporte":"TENIS_MESA","formato":"ELIMINACION_DIRECTA",
                    "estado":"PUBLICADO","visibilidad":"PUBLICO","cupos":16,"fechaInicio":"2026-11-21",
                    "valorInscripcion":5000.00,"enlaceToken":"abc","enlace":"http://localhost:5173/t/abc"}""",
                codigo = 201,
            ),
        )

        val creado = repositorio.crearTorneo(torneo).getOrThrow()

        val pedido = servidor.takeRequest()
        assertEquals("POST", pedido.method)
        assertEquals("/api/supercopa/creador/torneos", pedido.path)
        assertEquals(
            """{"nombre":"Relámpago de Tenis de Mesa","deporte":"TENIS_MESA","formato":"ELIMINACION_DIRECTA","cupos":16,""" +
                """"fechaInicio":"2026-11-21","visibilidad":"PUBLICO","cobraInscripcion":true,"valorInscripcion":5000}""",
            pedido.body.readUtf8(),
        )
        assertEquals("t9", creado.id)
        assertEquals(Visibilidad.PUBLICO, creado.visibilidad)
        assertEquals("http://localhost:5173/t/abc", creado.enlace)
    }

    @Test
    fun `MOV-0007 CA-2 si no cobra, el valor de la inscripcion no viaja`() = runTest {
        servidor.enqueue(json("""{"id":"t9","nombre":"x","visibilidad":"PRIVADO","enlace":""}""", codigo = 201))

        repositorio.crearTorneo(torneo.copy(cobraInscripcion = false, valorInscripcion = null, visibilidad = Visibilidad.PRIVADO)).getOrThrow()

        val cuerpo = servidor.takeRequest().body.readUtf8()
        assertTrue(cuerpo.contains(""""cobraInscripcion":false"""))
        assertTrue("sin valorInscripcion en el cuerpo: $cuerpo", !cuerpo.contains("valorInscripcion"))
    }

    @Test
    fun `MOV-0007 CA-3 un error de validacion llega con su codigo y el mensaje del backend`() = runTest {
        servidor.enqueue(problema(400, "FECHA_INICIO_PASADA", "La fecha de inicio no puede ser anterior a hoy."))

        val error = repositorio.crearTorneo(torneo).exceptionOrNull()

        assertTrue(error is AppError.Negocio)
        assertEquals("FECHA_INICIO_PASADA", (error as AppError.Negocio).codigo)
        assertEquals("La fecha de inicio no puede ser anterior a hoy.", error.mensaje)
    }

    @Test
    fun `MOV-0008 CA-1 mis torneos llegan con sus pendientes, cupos y visibilidad`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"id":"t1","nombre":"Copa Prueba 2026","edicion":2026,"estado":"PUBLICADO","formato":"LIGA",
                  "fechaInicio":"2026-11-16","deporte":"FUTSAL","visibilidad":"PUBLICO","cupos":16,
                  "enlace":"http://localhost:5173/t/abc","inscritos":2,
                  "pendientes":{"inscripciones":3,"comprobantes":1},"creadoEn":"2026-11-09T10:00:00"},
                 {"id":"t2","nombre":"Torneo viejo","estado":"ARCHIVADO","deporte":"FUTSAL","visibilidad":"SECRETO",
                  "cupos":null,"enlace":"","inscritos":0,"pendientes":null}]
                """.trimIndent(),
            ),
        )

        val torneos = repositorio.misTorneos().getOrThrow()

        assertEquals("/api/supercopa/creador/torneos", servidor.takeRequest().path)
        val copa = torneos[0]
        assertEquals(EstadoTorneo.PUBLICADO, copa.estado)
        assertEquals(Visibilidad.PUBLICO, copa.visibilidad)
        assertEquals(16, copa.cupos)
        assertEquals(2, copa.inscritos)
        assertEquals(4, copa.porRevisar)

        // Lo que la app no conoce no la cierra: estado genérico, sin visibilidad y sin pendientes.
        val viejo = torneos[1]
        assertEquals(EstadoTorneo.DESCONOCIDO, viejo.estado)
        assertNull(viejo.visibilidad)
        assertNull(viejo.cupos)
        assertNull(viejo.enlace)
        assertEquals(0, viejo.porRevisar)
    }

    @Test
    fun `MOV-0008 CA-2 las inscripciones llegan con equipo, delegado, estado, fecha y monto`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"id":"et1","torneoId":"t1","torneoNombre":"Copa Prueba 2026","equipoId":"e1","equipoNombre":"Halcones FC",
                  "delegadoPersonaId":"p1","delegadoNombre":"Andrés Gómez","estadoInscripcion":"PENDIENTE_PAGO",
                  "aprobadoPor":null,"motivoRechazo":null,"fechaInscripcion":"2026-11-07T10:00:00","montoInscripcion":50000.00},
                 {"id":"et2","equipoNombre":"Pumas FC","delegadoNombre":"","estadoInscripcion":"RECHAZADO",
                  "motivoRechazo":"Pago incompleto","fechaInscripcion":null,"montoInscripcion":null}]
                """.trimIndent(),
            ),
        )

        val inscripciones = repositorio.inscripciones("t1").getOrThrow()

        assertEquals("/api/supercopa/admin/torneos/t1/inscripciones", servidor.takeRequest().path)
        val pendiente = inscripciones[0]
        assertEquals("et1", pendiente.id)
        assertEquals("Halcones FC", pendiente.equipo)
        assertEquals("Andrés Gómez", pendiente.delegado)
        assertEquals(EstadoInscripcion.PENDIENTE_PAGO, pendiente.estado)
        assertEquals(LocalDateTime.of(2026, 11, 7, 10, 0), pendiente.fecha)
        assertEquals(0, BigDecimal("50000").compareTo(pendiente.monto))

        val rechazada = inscripciones[1]
        assertEquals(EstadoInscripcion.RECHAZADO, rechazada.estado)
        assertEquals("Pago incompleto", rechazada.motivoRechazo)
        assertNull(rechazada.delegado)
        assertNull(rechazada.fecha)
        assertNull(rechazada.monto)
    }

    @Test
    fun `MOV-0008 CA-3 aprobar envia POST sin cuerpo e ignora la inscripcion que responde`() = runTest {
        servidor.enqueue(json("""{"id":"et1","estadoInscripcion":"APROBADO"}"""))

        val resultado = repositorio.aprobarInscripcion("t1", "et1")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("POST", pedido.method)
        assertEquals("/api/supercopa/admin/torneos/t1/inscripciones/et1/aprobar", pedido.path)
    }

    @Test
    fun `MOV-0008 CA-3 rechazar envia el motivo`() = runTest {
        servidor.enqueue(json("""{"id":"et1","estadoInscripcion":"RECHAZADO"}"""))

        val resultado = repositorio.rechazarInscripcion("t1", "et1", "El comprobante no corresponde.")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("/api/supercopa/admin/torneos/t1/inscripciones/et1/rechazar", pedido.path)
        assertEquals("""{"motivo":"El comprobante no corresponde."}""", pedido.body.readUtf8())
    }

    @Test
    fun `MOV-0008 CA-3 el caso de uso no llama al backend sin motivo y lo envia limpio`() = runTest {
        val rechazar = RechazarInscripcionUseCase(repositorio)

        val error = rechazar("t1", "et1", "  ").exceptionOrNull()

        assertTrue(error is AppError.Validacion)
        assertEquals(0, servidor.requestCount)

        servidor.enqueue(json("""{"id":"et1"}"""))
        rechazar("t1", "et1", "  Pago incompleto  ").getOrThrow()
        assertEquals("""{"motivo":"Pago incompleto"}""", servidor.takeRequest().body.readUtf8())
    }

    @Test
    fun `MOV-0008 CA-4 un torneo ajeno responde 403 NO_ADMIN_DEL_TORNEO con su mensaje`() = runTest {
        servidor.enqueue(problema(403, "NO_ADMIN_DEL_TORNEO", "No administras este torneo."))

        val error = repositorio.inscripciones("ajeno").exceptionOrNull()

        assertTrue(error is AppError.Negocio)
        assertEquals("NO_ADMIN_DEL_TORNEO", (error as AppError.Negocio).codigo)
    }

    @Test
    fun `MOV-0008 CA-6 sin servidor devuelve SinConexion`() = runTest {
        servidor.shutdown()

        assertTrue(repositorio.misTorneos().exceptionOrNull() is AppError.SinConexion)
        assertTrue(repositorio.deportes().exceptionOrNull() is AppError.SinConexion)
    }
}
