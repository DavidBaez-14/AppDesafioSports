package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.TorneosApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.Visibilidad
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
import java.math.BigDecimal
import java.time.LocalDate

/** MOV-0005: el contrato de Explorar y del detalle público del torneo (backend 0029). */
class TorneosRepositoryImplTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: TorneosRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        repositorio = TorneosRepositoryImpl(retrofit.create(TorneosApi::class.java), ErroresHttp(ClienteHttp.json))
    }

    @After
    fun tearDown() {
        runCatching { servidor.shutdown() }
    }

    private fun json(cuerpo: String) =
        MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(cuerpo)

    @Test
    fun `CA-1 y CA-2 explorar envia el filtro y lee la pagina`() = runTest {
        servidor.enqueue(
            json(
                """
                {"contenido":[
                  {"id":"t1","nombre":"Copa Prueba 2026","deporte":"FUTSAL","deporteNombre":"Fútbol sala","formato":"LIGA",
                   "estado":"PUBLICADO","cupos":null,"inscritos":2,"cierreInscripciones":null,"inicio":"2026-11-17",
                   "organizador":null,"inscripcionesAbiertas":true},
                  {"id":null,"nombre":"Sin id"}],
                 "pagina":0,"tamano":20,"totalElementos":3,"totalPaginas":2}
                """.trimIndent(),
            ),
        )

        val pagina = repositorio.explorar("FUTSAL", "  copa ", 0).getOrThrow()

        val pedido = servidor.takeRequest()
        assertEquals("/api/public/torneos", pedido.requestUrl!!.encodedPath)
        assertEquals("FUTSAL", pedido.requestUrl!!.queryParameter("deporte"))
        assertEquals("copa", pedido.requestUrl!!.queryParameter("q"))
        assertEquals("0", pedido.requestUrl!!.queryParameter("page"))
        assertEquals("20", pedido.requestUrl!!.queryParameter("size"))
        // El que no trae id se descarta: no se podría abrir.
        assertEquals(1, pagina.contenido.size)
        assertTrue(pagina.hayMas)
        val t = pagina.contenido.single()
        assertEquals("Copa Prueba 2026", t.nombre)
        assertEquals(FormatoTorneo.LIGA, t.formato)
        assertEquals(EstadoTorneo.PUBLICADO, t.estado)
        assertNull(t.cupos)
        assertEquals(LocalDate.of(2026, 11, 17), t.inicio)
        assertTrue(t.inscripcionesAbiertas)
    }

    @Test
    fun `CA-2 sin filtro no se envian deporte ni busqueda`() = runTest {
        servidor.enqueue(json("""{"contenido":[],"pagina":0,"totalPaginas":0}"""))

        repositorio.explorar(null, "   ", 0).getOrThrow()

        val url = servidor.takeRequest().requestUrl!!
        assertNull(url.queryParameter("deporte"))
        assertNull(url.queryParameter("q"))
    }

    @Test
    fun `CA-4 el detalle trae equipos, partidos, tabla y la relacion de la persona`() = runTest {
        servidor.enqueue(
            json(
                """
                {"id":"t1","nombre":"Copa Prueba 2026","edicion":2026,
                 "deporte":{"codigo":"FUTSAL","nombre":"Fútbol sala","tipoCompetencia":"EQUIPO"},
                 "formato":"LIGA","estado":"EN_CURSO","visibilidad":"NO_LISTADO","fechaInicio":"2026-11-17",
                 "cupos":16,"inscritos":2,"inscripcionesAbiertas":false,"cobraInscripcion":true,"valorInscripcion":50000.0,
                 "exigeDocumento":true,"reglas":"  ","organizador":"Andrés Gómez","enlace":"http://x/t/abc",
                 "equipos":[{"equipoTorneoId":"e1","nombre":"Halcones FC","miembros":6}],
                 "tabla":{"GLOBAL":[{"posicion":2,"equipoTorneoId":"e2","equipoNombre":"Tiburones FC","pts":0,"pj":1},
                                    {"posicion":1,"equipoTorneoId":"e1","equipoNombre":"Halcones FC","pts":3,"pj":1,"pg":1,"dg":1}]},
                 "partidos":[{"partidoId":"p1","fase":null,"jornada":1,"local":{"nombre":"Halcones FC"},"visitante":{"nombre":"Tiburones FC"},
                              "fechaHora":"2026-11-18T16:00:00","estado":"FINALIZADO","marcador":{"local":2,"visitante":1}}],
                 "premios":[{"nombre":"CAMPEON","titulo":"Campeón","ganadorEquipo":"Halcones FC"}],
                 "miRelacion":{"administra":false,"arbitra":false,
                   "delegadoDe":{"equipoTorneoId":"e1","equipo":"Halcones FC","estadoInscripcion":"APROBADO","comprobante":"APROBADO"}}}
                """.trimIndent(),
            ),
        )

        val t = repositorio.torneo("t1").getOrThrow()

        assertEquals("/api/public/torneos/t1", servidor.takeRequest().path)
        assertEquals(TipoCompetencia.EQUIPO, t.deporte!!.tipo)
        assertEquals(Visibilidad.NO_LISTADO, t.visibilidad)
        assertEquals(EstadoTorneo.EN_CURSO, t.estado)
        assertEquals(0, BigDecimal("50000").compareTo(t.valorInscripcion))
        assertNull(t.reglas)
        assertEquals(listOf("Halcones FC", "Tiburones FC"), t.tabla.getValue("GLOBAL").map { it.equipo })
        val partido = t.partidos.single()
        assertEquals("Fecha 1", partido.fase)
        assertEquals(EstadoPartido.FINALIZADO, partido.estado)
        assertEquals(Marcador(2, 1), partido.marcador)
        assertEquals("Halcones FC", t.premios.single().ganador)
        assertEquals(EstadoInscripcion.APROBADO, t.miRelacion!!.delegadoDe!!.estadoInscripcion)
        assertNull(t.miRelacion!!.juegaEn)
    }

    @Test
    fun `CA-3 por enlace pide la ruta del token`() = runTest {
        servidor.enqueue(json("""{"id":"t1","nombre":"Privado con enlace","inscripcionesAbiertas":true}"""))

        val t = repositorio.torneoPorEnlace("abc123").getOrThrow()

        assertEquals("/api/public/t/abc123", servidor.takeRequest().path)
        assertNull(t.miRelacion)
        assertFalse(t.individual)
    }

    @Test
    fun `un torneo que no existe o no se puede ver llega como no encontrado`() = runTest {
        servidor.enqueue(
            MockResponse().setResponseCode(404).setHeader("Content-Type", "application/problem+json")
                .setBody("""{"status":404,"code":"NO_ENCONTRADO","mensaje":"No encontramos ese torneo."}"""),
        )

        val error = repositorio.torneo("privado").exceptionOrNull()

        assertTrue(error is AppError.NoEncontrado)
        assertEquals("No encontramos ese torneo.", (error as AppError).mensaje)
    }
}
