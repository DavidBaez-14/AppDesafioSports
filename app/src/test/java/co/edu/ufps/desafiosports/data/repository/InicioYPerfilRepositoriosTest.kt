package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.api.CompeticionApi
import co.edu.ufps.desafiosports.data.remote.api.FinanzasApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoMulta
import co.edu.ufps.desafiosports.domain.model.EstadoTramite
import co.edu.ufps.desafiosports.domain.model.Puesto
import co.edu.ufps.desafiosports.domain.model.RolEnEquipo
import co.edu.ufps.desafiosports.domain.model.SeccionDeArbitro
import co.edu.ufps.desafiosports.domain.model.SeccionDeCreador
import co.edu.ufps.desafiosports.domain.model.Secciones
import co.edu.ufps.desafiosports.domain.model.Tarjetas
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
import java.time.LocalDateTime

/**
 * MOV-0004: el contrato de `/api/auth/me` (secciones), `yo/equipos`, `mi-perfil` y `multas/mias`.
 * Los JSON son los que respondió el backend desplegado el 14-nov-2026, recortados.
 */
class InicioYPerfilRepositoriosTest {

    private val servidor = MockWebServer()
    private lateinit var cuentas: CuentaRepositoryImpl
    private lateinit var equipos: MisEquiposRepositoryImpl
    private lateinit var perfiles: PerfilRepositoryImpl
    private lateinit var multas: MultasRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        val errores = ErroresHttp(ClienteHttp.json)
        cuentas = CuentaRepositoryImpl(retrofit.create(AuthApi::class.java), errores)
        equipos = MisEquiposRepositoryImpl(retrofit.create(CompeticionApi::class.java), errores)
        perfiles = PerfilRepositoryImpl(retrofit.create(CompeticionApi::class.java), errores)
        multas = MultasRepositoryImpl(retrofit.create(FinanzasApi::class.java), errores)
    }

    @After
    fun tearDown() {
        runCatching { servidor.shutdown() }
    }

    private fun responder(cuerpo: String, codigo: Int = 200) {
        servidor.enqueue(
            MockResponse().setResponseCode(codigo)
                .setHeader("Content-Type", if (codigo < 400) "application/json" else "application/problem+json")
                .setBody(cuerpo),
        )
    }

    private fun me(secciones: String) =
        """{"cuentaId":"c1","nombres":"Laura","apellidos":"Méndez","nombre":"Laura Méndez","correo":"admin01@prueba.local",
           "estado":"ACTIVA","roles":["ADMINISTRADOR","JUGADOR","SUPERADMIN"],"rolesPendientes":[],"vinculoPendiente":null$secciones}"""

    // ── /api/auth/me ────────────────────────────────────────────────────────

    @Test
    fun `CA-2 lee secciones de me con las dos`() = runTest {
        responder(
            me(
                ""","secciones":{"creador":{"torneos":7,"porRevisar":2},
                   "arbitro":{"partidosAsignados":5,"hoy":{"partidoId":"p1","local":"Halcones FC","visitante":"Pumas FC",
                   "fechaHora":"2026-11-14T16:00:00","estado":"PROGRAMADO"}}}""",
            ),
        )

        val secciones = cuentas.obtenerMiCuenta().getOrThrow().secciones

        assertEquals(Secciones(SeccionDeCreador(torneos = 7, porRevisar = 2), SeccionDeArbitro(partidosAsignados = 5)), secciones)
        assertFalse(secciones.ninguna)
    }

    @Test
    fun `CA-2 lee secciones de me con una sola`() = runTest {
        responder(me(""","secciones":{"creador":null,"arbitro":{"partidosAsignados":0,"hoy":null}}"""))

        val secciones = cuentas.obtenerMiCuenta().getOrThrow().secciones

        assertNull(secciones.creador)
        assertEquals(SeccionDeArbitro(0), secciones.arbitro)
    }

    @Test
    fun `CA-2 lee secciones de me con ninguna, o sin el campo`() = runTest {
        responder(me(""","secciones":{"creador":null,"arbitro":null}"""))
        responder(me(""))

        assertTrue(cuentas.obtenerMiCuenta().getOrThrow().secciones.ninguna)
        assertTrue(cuentas.obtenerMiCuenta().getOrThrow().secciones.ninguna)
    }

    // ── yo/equipos ──────────────────────────────────────────────────────────

    @Test
    fun `CA-5 convierte la respuesta de yo equipos`() = runTest {
        responder(
            """[{"equipoTorneoId":"0bec","equipoId":"c00c","equipo":"Halcones FC","torneoId":"5f58",
                 "torneo":"Liga Relámpago (prueba)","deporte":"FUTSAL","estadoTorneo":"EN_CURSO","rol":"DELEGADO",
                 "estadoInscripcion":"PENDIENTE_PAGO","motivoRechazo":null,"comprobante":"PENDIENTE_REVISION","numeroCamiseta":1,
                 "proximoPartido":{"partidoId":"3f90","rival":"Tiburones FC","local":false,
                                   "fechaHora":"2026-11-11T16:00:00","lugar":null,"estado":"PROGRAMADO"}},
                {"equipoTorneoId":"2205","equipo":"Halcones FC","torneoId":"6cdc","torneo":"Copa Voleibol","deporte":"VOLEIBOL",
                 "rol":"JUGADOR","estadoInscripcion":"APROBADO","comprobante":null,"proximoPartido":null}]""",
        )

        val lista = equipos.misEquipos().getOrThrow()

        assertEquals("/api/supercopa/yo/equipos", servidor.takeRequest().path)
        assertEquals(2, lista.size)
        val delega = lista[0]
        assertEquals("Halcones FC", delega.equipo)
        assertEquals("5f58", delega.torneoId)
        assertEquals("FUTSAL", delega.deporte)
        assertEquals(RolEnEquipo.DELEGADO, delega.rol)
        assertEquals(EstadoTramite.EN_REVISION, delega.tramite)
        assertEquals("Tiburones FC", delega.proximoPartido!!.rival)
        assertFalse(delega.proximoPartido!!.local)
        assertEquals(LocalDateTime.of(2026, 11, 11, 16, 0), delega.proximoPartido!!.fechaHora)
        assertNull(delega.proximoPartido!!.lugar)
        assertEquals(RolEnEquipo.JUGADOR, lista[1].rol)
        assertNull(lista[1].proximoPartido)
    }

    @Test
    fun `CA-12 un deporte o estado desconocido no falla`() = runTest {
        responder(
            """[{"equipoTorneoId":"x","equipo":"Remeros","torneoId":"t9","torneo":"Regata","deporte":"REMO","rol":"CAPITAN",
                 "estadoInscripcion":"EN_LISTA_DE_ESPERA","comprobante":"OTRO",
                 "proximoPartido":{"partidoId":"p","rival":null,"local":null,"fechaHora":"mañana","estado":"RARO"}},
                {"equipo":"Sin torneo"}]""",
        )

        val lista = equipos.misEquipos().getOrThrow()

        // El que no trae torneo no se puede abrir: se descarta.
        assertEquals(1, lista.size)
        assertEquals("REMO", lista[0].deporte)
        assertEquals(RolEnEquipo.JUGADOR, lista[0].rol)
        assertEquals(EstadoInscripcion.DESCONOCIDO, lista[0].estadoInscripcion)
        assertEquals(EstadoTramite.AL_DIA, lista[0].tramite)
        assertNull(lista[0].proximoPartido!!.fechaHora)
        assertNull(lista[0].proximoPartido!!.rival)
    }

    @Test
    fun `CA-11 un 403 de yo equipos llega como error tipado`() = runTest {
        responder("""{"status":403,"code":"ACCESO_DENEGADO","mensaje":"No tienes permiso."}""", 403)

        assertTrue(equipos.misEquipos().exceptionOrNull() is AppError.AccesoDenegado)
    }

    // ── mi-perfil ───────────────────────────────────────────────────────────

    @Test
    fun `CA-7 convierte mi perfil y pone el puesto en el renglon que coincide`() = runTest {
        responder(
            """{"personaId":"7e8f","nombre":"Juan Castro","alturaCm":183,"piernaHabil":"IZQUIERDA","posicion":"MEDIOCAMPISTA",
                "equipos":[{"id":"a1","nombre":"Halcones FC","torneo":"Copa Sistemas 2025","desde":"2025-03-01","hasta":"2025-06-01"},
                           {"id":"a1","nombre":"Halcones FC","torneo":"Liga Relámpago (prueba)","desde":"2026-11-10","hasta":null}],
                "resumen":{"partidosJugados":5,"goles":4,"tarjetas":{"amarillas":3,"azules":0,"rojas":1},"titulos":1},
                "titulos":[{"torneo":"Copa Sistemas 2025","equipo":"Halcones FC","puesto":"SUBCAMPEON"},
                           {"torneo":"Otra copa","equipo":"Halcones FC","puesto":"CUARTO"}],
                "partidos":[{"id":"5f0c","fecha":"2026-11-05T16:00:00","equipo":"Halcones FC","rival":"Tiburones FC","goles":2,"tarjetas":["AMARILLA"]}]}""",
        )

        val perfil = perfiles.miPerfil().getOrThrow()

        assertEquals("/api/supercopa/mi-perfil", servidor.takeRequest().path)
        assertEquals("Juan Castro", perfil.nombre)
        assertEquals(183, perfil.alturaCm)
        assertEquals("IZQUIERDA", perfil.piernaHabil)
        assertEquals(5, perfil.partidosJugados)
        assertEquals(4, perfil.goles)
        assertEquals(1, perfil.titulos)
        assertEquals(Tarjetas(3, 0, 1), perfil.tarjetas)
        assertEquals(listOf(Puesto.SUBCAMPEON, null), perfil.trayectoria.map { it.puesto })
        assertEquals("Copa Sistemas 2025", perfil.trayectoria[0].torneo)
        assertTrue(perfil.conCifras)
        assertTrue(perfil.conPerfilDeportivo)
    }

    @Test
    fun `CA-8 un perfil vacio llega sin cifras ni perfil deportivo`() = runTest {
        responder(
            """{"personaId":"b153","nombre":"Laura Méndez","alturaCm":null,"piernaHabil":null,"posicion":null,"equipos":[],
                "resumen":{"partidosJugados":0,"goles":0,"tarjetas":{"amarillas":0,"azules":0,"rojas":0},"titulos":0},
                "titulos":[],"partidos":[]}""",
        )

        val perfil = perfiles.miPerfil().getOrThrow()

        assertFalse(perfil.conCifras)
        assertFalse(perfil.conPerfilDeportivo)
        assertTrue(perfil.trayectoria.isEmpty())
        assertEquals(0, perfil.tarjetas.total)
    }

    // ── multas/mias ─────────────────────────────────────────────────────────

    @Test
    fun `CA-7 convierte las multas`() = runTest {
        responder(
            """[{"id":"d4e5","partidoId":"5f0c","equipoTorneoId":"c2d3","torneoId":"9a1b","tipoSancion":"ACUMULACION_AMARILLAS",
                 "monto":10000.00,"estado":"PENDIENTE","partidosSuspension":1,"partidosSuspensionRestantes":1,
                 "fechaGeneracion":"2026-11-05T17:40:00","fechaPago":null},
                {"id":"x","tipoSancion":"NUEVA","monto":null,"estado":"EN_COBRO","fechaGeneracion":null}]""",
        )

        val lista = multas.misMultas().getOrThrow()

        assertEquals("/api/finanzas/multas/mias", servidor.takeRequest().path)
        assertEquals("ACUMULACION_AMARILLAS", lista[0].tipoSancion)
        assertEquals(0, BigDecimal("10000").compareTo(lista[0].monto))
        assertEquals(EstadoMulta.PENDIENTE, lista[0].estado)
        assertEquals(LocalDateTime.of(2026, 11, 5, 17, 40), lista[0].fechaGeneracion)
        // CA-12: lo que la app no conoce llega con el caso genérico.
        assertEquals(EstadoMulta.DESCONOCIDO, lista[1].estado)
        assertEquals(0, BigDecimal.ZERO.compareTo(lista[1].monto))
    }
}
