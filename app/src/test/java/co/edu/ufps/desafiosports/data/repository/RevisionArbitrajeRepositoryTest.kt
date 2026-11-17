package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.ArbitrajeApi
import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
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

/** MOV-0012: lo nuevo del contrato (backend 0030), de punta a punta en la capa data. */
class RevisionArbitrajeRepositoryTest {

    private val servidor = MockWebServer()
    private lateinit var arbitraje: ArbitrajeRepositoryImpl
    private lateinit var invitaciones: InvitacionesRepositoryImpl

    @Before
    fun setUp() {
        servidor.start()
        val retrofit = ClienteHttp.retrofit(servidor.url("/").toString(), OkHttpClient())
        val errores = ErroresHttp(ClienteHttp.json)
        arbitraje = ArbitrajeRepositoryImpl(retrofit.create(ArbitrajeApi::class.java), errores)
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

    @Test
    fun `CA-1 mis invitaciones llegan con su token y la que no lo trae se descarta`() = runTest {
        servidor.enqueue(
            json(
                """
                [{"token":"tok-1","rol":"ARBITRO","ambito":"TORNEO","ambitoId":"t1",
                  "ambitoDescripcion":"Copa Prueba 2026","invitadoPor":"Laura Méndez","createdAt":"2026-11-16T10:00:00"},
                 {"token":"tok-2","rol":"ADMINISTRADOR","ambito":null,"ambitoDescripcion":null,"invitadoPor":""},
                 {"token":null,"rol":"ARBITRO"}]
                """,
            ),
        )

        val mias = invitaciones.mias().getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("GET", peticion.method)
        assertEquals("/api/auth/mis-invitaciones", peticion.path)
        assertEquals(listOf("tok-1", "tok-2"), mias.map { it.token })
        assertEquals(RolInvitado.ARBITRO, mias[0].rol)
        assertEquals("Copa Prueba 2026", mias[0].torneo)
        assertEquals("Laura Méndez", mias[0].invitadoPor)
        assertTrue(mias.all { it.pendiente })
        assertNull(mias[0].paraCorreo)
        // A la plataforma: sin torneo; y un nombre vacío no se muestra.
        assertNull(mias[1].torneo)
        assertNull(mias[1].invitadoPor)
    }

    @Test
    fun `CA-1 sin invitaciones devuelve una lista vacia, y un backend sin el endpoint es un fallo`() = runTest {
        servidor.enqueue(json("[]"))
        servidor.enqueue(problema(404, "NO_ENCONTRADO", "No encontrado."))

        assertTrue(invitaciones.mias().getOrThrow().isEmpty())
        assertTrue(invitaciones.mias().isFailure)
    }

    @Test
    fun `CA-3 y CA-6 la planilla trae quien esta expulsado y si se puede reabrir`() = runTest {
        servidor.enqueue(
            json(
                """
                {"partido":{"partidoId":"p1","estado":"FINALIZADO",
                            "local":{"equipoTorneoId":"e1","nombre":"Halcones FC"},
                            "visitante":{"equipoTorneoId":"e2","nombre":"Pumas FC"}},
                 "tiposEvento":["GOL","AMARILLA","AZUL","ROJA"],
                 "local":{"equipoTorneoId":"e1","nombre":"Halcones FC","jugadores":[
                    {"personaId":"j1","nombre":"Mateo Jiménez","numeroCamiseta":4,"alineado":true,"apto":true,"motivos":[],"expulsado":true},
                    {"personaId":"j2","nombre":"Juan Castro","numeroCamiseta":2,"alineado":true,"apto":true,"motivos":[],"expulsado":false},
                    {"personaId":"j3","nombre":"Sofía Moreno","numeroCamiseta":3,"alineado":false,"apto":false,"motivos":["MULTA_PENDIENTE"]}]},
                 "visitante":{"equipoTorneoId":"e2","nombre":"Pumas FC","jugadores":[]},
                 "eventos":[],"editable":false,"puedeReabrir":true}
                """,
            ),
        )

        val planilla = arbitraje.planilla("p1").getOrThrow()

        assertTrue(planilla.puedeReabrir)
        assertEquals(listOf(true, false, false), planilla.local.jugadores.map { it.expulsado })
        assertEquals(listOf("j2"), planilla.local.candidatos().map { it.personaId })
        assertEquals(listOf("MULTA_PENDIENTE"), planilla.local.jugadores[2].motivos)
    }

    @Test
    fun `CA-6 una planilla de un backend viejo no deja reabrir ni marca expulsados`() = runTest {
        servidor.enqueue(
            json(
                """{"partido":{"partidoId":"p1","estado":"FINALIZADO"},
                    "local":{"equipoTorneoId":"e1","nombre":"A","jugadores":[{"personaId":"j1","nombre":"Ana","alineado":true}]},
                    "editable":false}""",
            ),
        )

        val planilla = arbitraje.planilla("p1").getOrThrow()

        assertFalse(planilla.puedeReabrir)
        assertFalse(planilla.local.jugadores.single().expulsado)
    }

    @Test
    fun `CA-6 reabrir envia POST e ignora el partido que responde`() = runTest {
        servidor.enqueue(json("""{"id":"p1","estado":"EN_CURSO","golesLocal":2,"golesVisitante":0,"arbitros":[]}"""))
        servidor.enqueue(problema(403, "ACCESO_DENEGADO", "No tienes permisos para esta acción."))

        assertTrue(arbitraje.reabrirPartido("p1").isSuccess)
        val fallo = arbitraje.reabrirPartido("p1").exceptionOrNull()

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/supercopa/admin/partidos/p1/reabrir", peticion.path)
        assertEquals("No tienes permisos para esta acción.", (fallo as AppError).mensaje)
    }

    @Test
    fun `CA-4 los rechazos nuevos del backend llegan con su codigo y su mensaje`() = runTest {
        servidor.enqueue(problema(409, "JUGADOR_NO_ALINEADO", "Ese jugador no está en la alineación de su equipo. Alinéalo antes de anotarle algo."))
        servidor.enqueue(problema(409, "JUGADOR_EXPULSADO", "Ese jugador ya fue expulsado de este partido: no se le puede anotar nada más."))
        servidor.enqueue(problema(409, "JUGADOR_CON_EVENTOS", "Ese jugador ya tiene eventos en este partido. Quítalos antes de sacarlo de la alineación."))

        val sinAlinear = arbitraje.registrarEvento("p1", "j1", "e1", "GOL").exceptionOrNull() as AppError.Negocio
        val expulsado = arbitraje.registrarEvento("p1", "j1", "e1", "GOL").exceptionOrNull() as AppError.Negocio
        val conEventos = arbitraje.quitarDeAlineacion("p1", "j1").exceptionOrNull() as AppError.Negocio

        assertEquals("JUGADOR_NO_ALINEADO", sinAlinear.codigo)
        assertEquals("JUGADOR_EXPULSADO", expulsado.codigo)
        assertEquals("JUGADOR_CON_EVENTOS", conEventos.codigo)
        assertTrue(expulsado.mensaje.startsWith("Ese jugador ya fue expulsado"))
    }
}
