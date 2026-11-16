package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.api.GestionTorneoApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FasePartido
import co.edu.ufps.desafiosports.domain.model.RolInvitado
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

/** MOV-0011: el contrato de árbitros, partidos e invitaciones, en la capa data. */
class GestionRepositoryImplTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: GestionTorneoRepositoryImpl
    private lateinit var invitaciones: InvitacionesRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        val errores = ErroresHttp(ClienteHttp.json)
        repositorio = GestionTorneoRepositoryImpl(retrofit.create(GestionTorneoApi::class.java), errores)
        invitaciones = InvitacionesRepositoryImpl(retrofit.create(AuthApi::class.java), errores)
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

    // ── Árbitros ────────────────────────────────────────────────────────────

    @Test
    fun `CA-2 de los roles del torneo solo quedan los arbitros`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"id":"r0","cuentaId":"c0","nombre":"Carlos Rojas","correo":"carlos@correo.com","rol":"ADMIN","todosLosPartidos":null,"esTuyo":true},
                 {"id":"r1","cuentaId":"c1","nombre":"Ana Ruiz","correo":"ana@correo.com","rol":"ARBITRO","todosLosPartidos":true},
                 {"id":"r2","cuentaId":"c2","nombre":null,"correo":"sin.nombre@correo.com","rol":"ARBITRO","todosLosPartidos":null},
                 {"id":null,"cuentaId":"c3","nombre":"Sin rol","rol":"ARBITRO"}]
                """.trimIndent(),
            ),
        )

        val arbitros = repositorio.arbitros("t1").getOrThrow()

        assertEquals("/api/supercopa/admin/torneos/t1/roles", servidor.takeRequest().path)
        assertEquals(listOf("r1", "r2"), arbitros.map { it.rolId })
        assertTrue(arbitros[0].todosLosPartidos)
        // Una cuenta sin persona no tiene nombre: se muestra su correo.
        assertEquals("sin.nombre@correo.com", arbitros[1].nombre)
        assertFalse(arbitros[1].todosLosPartidos)
    }

    @Test
    fun `CA-2 de las invitaciones del torneo solo quedan las de arbitro pendientes`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"id":"i1","correo":null,"rol":"ARBITRO","estado":"PENDIENTE","enlace":"https://web/invitacion/abc","createdAt":"2026-11-16T10:00:00"},
                 {"id":"i2","correo":"otro@correo.com","rol":"ADMINISTRADOR","estado":"PENDIENTE","enlace":"https://web/invitacion/def"},
                 {"id":"i3","correo":"equipo@correo.com","rol":"DELEGADO","estado":"PENDIENTE","enlace":"https://web/invitacion/ghi"},
                 {"id":"i4","correo":"viejo@correo.com","rol":"ARBITRO","estado":"ACEPTADA","enlace":"https://web/invitacion/jkl"}]
                """.trimIndent(),
            ),
        )

        val pendientes = repositorio.invitaciones("t1").getOrThrow()

        assertEquals("/api/supercopa/admin/torneos/t1/invitaciones", servidor.takeRequest().path)
        assertEquals(listOf("i1"), pendientes.map { it.id })
        assertNull(pendientes.single().correo)
        assertEquals("https://web/invitacion/abc", pendientes.single().enlace)
        assertEquals(LocalDateTime.of(2026, 11, 16, 10, 0), pendientes.single().fecha)
    }

    @Test
    fun `CA-3 invitar por enlace envia solo el rol y devuelve el enlace`() = runTest {
        servidor.enqueue(json("""{"id":"i9","correo":null,"rol":"ARBITRO","estado":"PENDIENTE","enlace":"https://web/invitacion/xyz"}""", 201))

        val invitacion = repositorio.invitarArbitro("t1", null).getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/supercopa/admin/torneos/t1/invitaciones", peticion.path)
        assertEquals("""{"rol":"ARBITRO"}""", peticion.body.readUtf8())
        assertEquals("https://web/invitacion/xyz", invitacion.enlace)
    }

    @Test
    fun `CA-3 invitar con correo lo envia, y una duplicada llega con su mensaje`() = runTest {
        servidor.enqueue(problema(409, "INVITACION_DUPLICADA", "Ya hay una invitación pendiente para ese correo y rol."))

        val fallo = repositorio.invitarArbitro("t1", "ana@correo.com").exceptionOrNull()

        assertEquals("""{"rol":"ARBITRO","correo":"ana@correo.com"}""", servidor.takeRequest().body.readUtf8())
        assertEquals("INVITACION_DUPLICADA", (fallo as AppError.Negocio).codigo)
        assertEquals("Ya hay una invitación pendiente para ese correo y rol.", fallo.mensaje)
    }

    @Test
    fun `CA-4 todos los partidos envia PUT con el valor`() = runTest {
        servidor.enqueue(json("""{"id":"r1","cuentaId":"c1","rol":"ARBITRO","todosLosPartidos":true}"""))

        assertTrue(repositorio.cambiarTodosLosPartidos("t1", "c1", true).isSuccess)

        val peticion = servidor.takeRequest()
        assertEquals("PUT", peticion.method)
        assertEquals("/api/supercopa/admin/torneos/t1/arbitros/c1/todos-los-partidos", peticion.path)
        assertEquals("""{"todosLosPartidos":true}""", peticion.body.readUtf8())
    }

    @Test
    fun `CA-5 quitar un arbitro y cancelar una invitacion envian DELETE y aceptan el 204`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(204))
        servidor.enqueue(MockResponse().setResponseCode(204))

        assertTrue(repositorio.quitarArbitro("t1", "r1").isSuccess)
        assertTrue(repositorio.cancelarInvitacion("t1", "i1").isSuccess)

        val quitar = servidor.takeRequest()
        assertEquals("DELETE", quitar.method)
        assertEquals("/api/supercopa/admin/torneos/t1/roles/r1", quitar.path)
        val cancelar = servidor.takeRequest()
        assertEquals("DELETE", cancelar.method)
        assertEquals("/api/supercopa/admin/torneos/t1/invitaciones/i1", cancelar.path)
    }

    // ── Partidos ────────────────────────────────────────────────────────────

    @Test
    fun `CA-6 la configuracion dice el estado, los equipos aprobados y si hay partidos`() = runTest {
        servidor.enqueue(
            json("""{"torneoId":"t1","estado":"PUBLICADO","formato":"LIGA","equiposAprobados":4,"fixtureGenerado":false,"rondasPlayoff":[]}"""),
        )

        val estado = repositorio.estadoDePartidos("t1").getOrThrow()

        assertEquals("/api/supercopa/admin/torneos/t1/configuracion", servidor.takeRequest().path)
        assertEquals(EstadoTorneo.PUBLICADO, estado.estadoTorneo)
        assertEquals(4, estado.equiposAprobados)
        assertFalse(estado.generados)
        assertTrue(estado.puedeGenerar)
    }

    @Test
    fun `CA-7 iniciar y generar envian POST sin cuerpo e ignoran lo que responden`() = runTest {
        servidor.enqueue(json("""{"id":"t1","estado":"EN_CURSO"}"""))
        servidor.enqueue(json("""[{"id":"p1","estado":"PROGRAMADO"}]"""))

        assertTrue(repositorio.iniciarTorneo("t1").isSuccess)
        assertTrue(repositorio.generarPartidos("t1").isSuccess)

        val iniciar = servidor.takeRequest()
        assertEquals("POST", iniciar.method)
        assertEquals("/api/supercopa/admin/torneos/t1/iniciar", iniciar.path)
        val generar = servidor.takeRequest()
        assertEquals("POST", generar.method)
        assertEquals("/api/supercopa/admin/torneos/t1/fixture", generar.path)
    }

    @Test
    fun `CA-7 una regla del backend al generar llega con su mensaje`() = runTest {
        servidor.enqueue(problema(400, "REGLA_NEGOCIO", "Se necesitan al menos 2 equipos APROBADOS para generar fixture."))

        val fallo = repositorio.generarPartidos("t1").exceptionOrNull()

        assertEquals("Se necesitan al menos 2 equipos APROBADOS para generar fixture.", (fallo as AppError).mensaje)
    }

    @Test
    fun `CA-8 los partidos llegan con fase, jornada, equipos por definir y sus arbitros`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"id":"p1","fecha":"2026-11-21T08:00:00","estado":"PROGRAMADO","localNombre":"Halcones FC","visitanteNombre":"Tiburones FC",
                  "golesLocal":0,"golesVisitante":0,"fase":"GRUPOS","jornada":1,"grupo":"A","torneoId":"t1","lugar":"Cancha 1",
                  "arbitros":[{"cuentaId":"c1","nombre":"Ana Ruiz","asignadoEn":"2026-11-16T10:00:00"},{"cuentaId":null,"nombre":"Roto"}]},
                 {"id":"p2","fecha":null,"estado":"DESCANSO","localNombre":null,"visitanteNombre":null,"fase":"FINAL","jornada":3,"grupo":null,"arbitros":null},
                 {"id":"p3","estado":"FINALIZADO","localNombre":"A","visitanteNombre":"B","golesLocal":3,"golesVisitante":1,"fase":"FASE_NUEVA"}]
                """.trimIndent(),
            ),
        )

        val partidos = repositorio.partidos("t1").getOrThrow()

        assertEquals("/api/supercopa/admin/torneos/t1/partidos", servidor.takeRequest().path)
        val primero = partidos[0]
        assertEquals(LocalDateTime.of(2026, 11, 21, 8, 0), primero.fecha)
        assertEquals(FasePartido.GRUPOS, primero.fase)
        assertEquals(1, primero.jornada)
        assertEquals("A", primero.grupo)
        assertEquals("Cancha 1", primero.lugar)
        assertEquals(listOf("c1"), primero.arbitros.map { it.cuentaId })

        val porDefinir = partidos[1]
        assertNull(porDefinir.local)
        assertNull(porDefinir.visitante)
        assertNull(porDefinir.fecha)
        assertTrue(porDefinir.descansa)
        assertTrue(porDefinir.arbitros.isEmpty())

        val raro = partidos[2]
        assertEquals(EstadoPartido.FINALIZADO, raro.estado)
        assertEquals(3, raro.golesLocal)
        // Una fase que la app no conoce no cierra la app: conserva su código.
        assertEquals(FasePartido.DESCONOCIDA, raro.fase)
        assertEquals("FASE_NUEVA", raro.faseCodigo)
    }

    @Test
    fun `CA-9 asignar envia la cuenta y responde los arbitros del partido`() = runTest {
        servidor.enqueue(json("""[{"cuentaId":"c1","nombre":"Ana Ruiz"},{"cuentaId":"c2","nombre":"Beto Díaz"}]"""))

        val arbitros = repositorio.asignarArbitro("p1", "c2").getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/supercopa/admin/partidos/p1/arbitros", peticion.path)
        assertEquals("""{"cuentaId":"c2"}""", peticion.body.readUtf8())
        assertEquals(listOf("Ana Ruiz", "Beto Díaz"), arbitros.map { it.nombre })
    }

    @Test
    fun `CA-9 quitar del partido envia DELETE, y quien juega en el partido no puede arbitrarlo`() = runTest {
        servidor.enqueue(json("[]"))
        servidor.enqueue(problema(409, "ARBITRO_EN_PLANTILLA", "Esa persona juega en uno de los equipos del partido: no puede arbitrarlo."))

        assertTrue(repositorio.quitarArbitroDePartido("p1", "c1").getOrThrow().isEmpty())
        val fallo = repositorio.asignarArbitro("p1", "c3").exceptionOrNull()

        val quitar = servidor.takeRequest()
        assertEquals("DELETE", quitar.method)
        assertEquals("/api/supercopa/admin/partidos/p1/arbitros/c1", quitar.path)
        assertEquals("ARBITRO_EN_PLANTILLA", (fallo as AppError.Negocio).codigo)
    }

    @Test
    fun `CA-13 un torneo ajeno responde 403 con su mensaje y sin servidor es SinConexion`() = runTest {
        servidor.enqueue(problema(403, "NO_ADMIN_DEL_TORNEO", "No administras este torneo."))

        val ajeno = repositorio.arbitros("ajeno").exceptionOrNull()
        servidor.shutdown()
        val sinRed = repositorio.partidos("t1").exceptionOrNull()

        assertEquals("No administras este torneo.", (ajeno as AppError).mensaje)
        assertTrue(sinRed is AppError.SinConexion)
    }

    // ── Invitación recibida ─────────────────────────────────────────────────

    @Test
    fun `CA-11 la vista previa trae el rol, el torneo y quien invita`() = runTest {
        servidor.enqueue(
            json(
                """{"rol":"ARBITRO","ambito":"TORNEO","ambitoId":"t1","ambitoDescripcion":"Copa Prueba 2026",
                   "estado":"PENDIENTE","invitadoPor":"Carlos Rojas","paraCorreo":"a***@correo.com"}""",
            ),
        )
        servidor.enqueue(json("""{"rol":"ROL_NUEVO","ambito":null,"ambitoDescripcion":null,"estado":"ACEPTADA"}"""))
        servidor.enqueue(problema(404, "INVITACION_NO_ENCONTRADA", "Invitación no encontrada."))

        val vigente = invitaciones.ver("tok-1").getOrThrow()
        val usada = invitaciones.ver("tok-2").getOrThrow()
        val fallo = invitaciones.ver("tok-3").exceptionOrNull()

        assertEquals("/api/auth/invitaciones/tok-1", servidor.takeRequest().path)
        assertEquals("tok-1", vigente.token)
        assertEquals(RolInvitado.ARBITRO, vigente.rol)
        assertEquals("Copa Prueba 2026", vigente.torneo)
        assertEquals("Carlos Rojas", vigente.invitadoPor)
        assertEquals("a***@correo.com", vigente.paraCorreo)
        assertTrue(vigente.pendiente)

        assertEquals(RolInvitado.DESCONOCIDO, usada.rol)
        assertNull(usada.torneo)
        assertFalse(usada.pendiente)
        assertEquals("Invitación no encontrada.", (fallo as AppError).mensaje)
    }

    @Test
    fun `CA-12 aceptar envia POST al token, y otro correo llega con su mensaje`() = runTest {
        servidor.enqueue(json("""{"cuentaId":"c1","correo":"ana@correo.com","roles":["JUGADOR"]}"""))
        servidor.enqueue(problema(403, "INVITACION_OTRO_CORREO", "Esta invitación es para otro correo. Inicia sesión con el correo invitado."))

        assertTrue(invitaciones.aceptar("tok-1").isSuccess)
        val fallo = invitaciones.aceptar("tok-1").exceptionOrNull()

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/auth/invitaciones/tok-1/aceptar", peticion.path)
        assertEquals("Esta invitación es para otro correo. Inicia sesión con el correo invitado.", (fallo as AppError).mensaje)
    }
}
