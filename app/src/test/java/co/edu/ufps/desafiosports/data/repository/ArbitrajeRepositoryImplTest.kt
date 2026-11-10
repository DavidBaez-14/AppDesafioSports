package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.ArbitrajeApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.Marcador
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

/** MOV-0009: el contrato de la sección Árbitro y la planilla, de punta a punta en la capa data. */
class ArbitrajeRepositoryImplTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: ArbitrajeRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        repositorio = ArbitrajeRepositoryImpl(retrofit.create(ArbitrajeApi::class.java), ErroresHttp(ClienteHttp.json))
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
    fun `CA-1 convierte la lista de partidos con sus fechas, estados y marcador`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"partidoId":"p1","torneoId":"t1","torneo":"Liga Relámpago (prueba)","deporte":"FUTSAL",
                  "fase":"Fecha 1","faseCodigo":"GRUPOS","jornada":1,"grupo":null,
                  "local":{"equipoTorneoId":"e1","nombre":"Halcones FC"},
                  "visitante":{"equipoTorneoId":"e2","nombre":"Tiburones FC"},
                  "fechaHora":"2026-11-12T16:00:00","lugar":"Cancha 1","estado":"FINALIZADO",
                  "marcador":{"local":3,"visitante":2},"asignado":true},
                 {"partidoId":"p2","torneoId":"t1","torneo":"Liga Relámpago (prueba)","deporte":"FUTSAL",
                  "fase":"Final","local":null,"visitante":null,"fechaHora":null,"lugar":"",
                  "estado":"PROGRAMADO","marcador":null,"asignado":false}]
                """.trimIndent(),
            ),
        )

        val partidos = repositorio.misPartidos().getOrThrow()

        assertEquals("/api/supercopa/arbitro/partidos", servidor.takeRequest().path)
        val cerrado = partidos[0]
        assertEquals("p1", cerrado.id)
        assertEquals("Halcones FC", cerrado.local?.nombre)
        assertEquals(LocalDateTime.of(2026, 11, 12, 16, 0), cerrado.fechaHora)
        assertEquals(EstadoPartido.FINALIZADO, cerrado.estado)
        assertEquals(Marcador(3, 2), cerrado.marcador)
        assertTrue(cerrado.asignado)

        val porDefinir = partidos[1]
        assertFalse(porDefinir.cruceDefinido)
        assertNull(porDefinir.fechaHora)
        assertNull("un lugar vacío se trata como sin lugar", porDefinir.lugar)
        assertNull(porDefinir.marcador)
    }

    @Test
    fun `CA-2 sin partidos devuelve una lista vacia`() = runTest {
        servidor.enqueue(json("[]"))

        assertTrue(repositorio.misPartidos().getOrThrow().isEmpty())
    }

    @Test
    fun `CA-13 un estado desconocido o una fecha ilegible no fallan`() = runTest {
        servidor.enqueue(
            json("""[{"partidoId":"p1","estado":"SUSPENDIDO_POR_LLUVIA","fechaHora":"mañana a las 4","campoNuevo":{"x":1}}]"""),
        )

        val partido = repositorio.misPartidos().getOrThrow().single()

        assertEquals(EstadoPartido.DESCONOCIDO, partido.estado)
        assertNull(partido.fechaHora)
        assertEquals("", partido.torneo)
    }

    @Test
    fun `CA-4 la planilla llega con planteles, eventos en orden y tipos de evento`() = runTest {
        servidor.enqueue(
            json(
                """
                {"partido":{"partidoId":"p1","torneo":"Liga Relámpago (prueba)","fase":"Fecha 1",
                   "local":{"equipoTorneoId":"e1","nombre":"Halcones FC"},
                   "visitante":{"equipoTorneoId":"e2","nombre":"Tiburones FC"},
                   "fechaHora":"2026-11-12T16:00:00","estado":"EN_CURSO","marcador":{"local":1,"visitante":0}},
                 "tiposEvento":["GOL","AMARILLA","AZUL","ROJA","TIRO_LIBRE"],
                 "local":{"equipoTorneoId":"e1","nombre":"Halcones FC","jugadores":[
                   {"personaId":"j1","nombre":"Juan Castro","numeroCamiseta":10,"numeroDocumento":"9000000301",
                    "esDelegado":false,"alineado":true,"goles":1,"apto":true,"motivos":[]},
                   {"personaId":"j2","nombre":"Mateo Jiménez","numeroCamiseta":null,"numeroDocumento":null,
                    "esDelegado":true,"alineado":false,"goles":0,"apto":false,"motivos":["Multa pendiente"]}]},
                 "visitante":{"equipoTorneoId":"e2","nombre":"Tiburones FC","jugadores":[]},
                 "eventos":[
                   {"id":"ev2","orden":2,"personaId":"j1","jugadorNombre":"Juan Castro","equipoTorneoId":"e1","equipoNombre":"Halcones FC","tipoEvento":"AMARILLA","createdAt":"2026-11-12T16:20:00"},
                   {"id":"ev1","orden":1,"personaId":"j1","jugadorNombre":"Juan Castro","equipoTorneoId":"e1","equipoNombre":"Halcones FC","tipoEvento":"GOL","createdAt":"2026-11-12T16:10:00"}],
                 "editable":true}
                """.trimIndent(),
            ),
        )

        val planilla = repositorio.planilla("p1").getOrThrow()

        assertEquals("/api/supercopa/arbitro/partidos/p1/planilla", servidor.takeRequest().path)
        assertTrue(planilla.editable)
        assertEquals(Marcador(1, 0), planilla.marcador)
        assertEquals(listOf("GOL", "AMARILLA", "AZUL", "ROJA", "TIRO_LIBRE"), planilla.tiposEvento)
        assertEquals(listOf("ev1", "ev2"), planilla.eventos.map { it.id })
        val (juan, mateo) = planilla.local.jugadores
        assertEquals(10, juan.numeroCamiseta)
        assertEquals("9000000301", juan.numeroDocumento)
        assertTrue(juan.alineado)
        assertFalse(mateo.apto)
        assertTrue(mateo.esDelegado)
        assertEquals(listOf("Multa pendiente"), mateo.motivos)
        assertNull(mateo.numeroCamiseta)
        assertTrue(planilla.visitante.jugadores.isEmpty())
    }

    @Test
    fun `CA-10 una planilla sin el campo editable es de solo lectura`() = runTest {
        servidor.enqueue(json("""{"partido":{"partidoId":"p1","estado":"FINALIZADO"},"tiposEvento":null,"eventos":null}"""))

        val planilla = repositorio.planilla("p1").getOrThrow()

        assertFalse(planilla.editable)
        assertEquals(Marcador(0, 0), planilla.marcador)
        assertTrue(planilla.tiposEvento.isEmpty())
        assertTrue(planilla.eventos.isEmpty())
        assertFalse(planilla.partido.cruceDefinido)
    }

    @Test
    fun `CA-4 registrar evento envia POST con el jugador, el equipo y el tipo`() = runTest {
        servidor.enqueue(
            json("""{"id":"ev9","orden":4,"personaId":"j1","jugadorNombre":"Juan Castro","equipoTorneoId":"e1","equipoNombre":"Halcones FC","tipoEvento":"GOL"}"""),
        )

        val evento = repositorio.registrarEvento("p1", "j1", "e1", "GOL").getOrThrow()

        val pedido = servidor.takeRequest()
        assertEquals("POST", pedido.method)
        assertEquals("/api/supercopa/admin/partidos/p1/eventos", pedido.path)
        assertEquals("""{"personaId":"j1","equipoTorneoId":"e1","tipoEvento":"GOL"}""", pedido.body.readUtf8())
        assertEquals("ev9", evento.id)
        assertEquals(4, evento.orden)
        assertEquals("GOL", evento.tipo)
    }

    @Test
    fun `CA-5 quitar evento envia DELETE y acepta el 204 sin cuerpo`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(204))

        val resultado = repositorio.quitarEvento("p1", "ev9")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("DELETE", pedido.method)
        assertEquals("/api/supercopa/admin/partidos/p1/eventos/ev9", pedido.path)
    }

    @Test
    fun `CA-7 alinear envia POST y acepta el 200 sin cuerpo`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(200))

        val resultado = repositorio.alinear("p1", "j2", "e1")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("POST", pedido.method)
        assertEquals("/api/supercopa/admin/partidos/p1/alineacion", pedido.path)
        assertEquals("""{"personaId":"j2","equipoTorneoId":"e1"}""", pedido.body.readUtf8())
    }

    @Test
    fun `CA-7 quitar de la alineacion envia DELETE con la persona`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(204))

        val resultado = repositorio.quitarDeAlineacion("p1", "j2")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("DELETE", pedido.method)
        assertEquals("/api/supercopa/admin/partidos/p1/alineacion/j2", pedido.path)
    }

    @Test
    fun `CA-7 alinear a quien no es apto llega como error de negocio con el motivo`() = runTest {
        servidor.enqueue(problema(400, "REGLA_NEGOCIO", "Jugador no apto: Multa pendiente"))

        val error = repositorio.alinear("p1", "j2", "e1").exceptionOrNull()

        assertTrue(error is AppError.Negocio)
        assertEquals("REGLA_NEGOCIO", (error as AppError.Negocio).codigo)
        assertEquals("Jugador no apto: Multa pendiente", error.mensaje)
    }

    @Test
    fun `CA-8 alinear a todos envia el equipo`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(200))

        val resultado = repositorio.alinearEquipo("p1", "e2")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("/api/supercopa/admin/partidos/p1/alineacion/add-all", pedido.path)
        assertEquals("""{"equipoTorneoId":"e2"}""", pedido.body.readUtf8())
    }

    @Test
    fun `CA-9 cerrar envia POST e ignora el partido que responde`() = runTest {
        servidor.enqueue(json("""{"id":"p1","estado":"FINALIZADO","golesLocal":3,"golesVisitante":2,"arbitros":[]}"""))

        val resultado = repositorio.cerrarPartido("p1")

        val pedido = servidor.takeRequest()
        assertTrue(resultado.isSuccess)
        assertEquals("POST", pedido.method)
        assertEquals("/api/supercopa/admin/partidos/p1/cerrar", pedido.path)
    }

    @Test
    fun `CA-12 un 403 NO_ARBITRO_DEL_PARTIDO llega con su codigo y el mensaje del backend`() = runTest {
        servidor.enqueue(problema(403, "NO_ARBITRO_DEL_PARTIDO", "No tienes permisos para arbitrar este partido."))

        val error = repositorio.planilla("p1").exceptionOrNull()

        assertTrue(error is AppError.Negocio)
        assertEquals("NO_ARBITRO_DEL_PARTIDO", (error as AppError.Negocio).codigo)
        assertEquals("No tienes permisos para arbitrar este partido.", error.mensaje)
    }

    @Test
    fun `CA-15 sin servidor devuelve SinConexion`() = runTest {
        servidor.shutdown()

        assertTrue(repositorio.planilla("p1").exceptionOrNull() is AppError.SinConexion)
        assertTrue(repositorio.cerrarPartido("p1").exceptionOrNull() is AppError.SinConexion)
    }
}
