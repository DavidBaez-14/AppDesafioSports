package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.DelegadoApi
import co.edu.ufps.desafiosports.data.remote.red.ClienteHttp
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
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

/** MOV-0014 y MOV-0015: el contrato de la inscripción de un equipo y de su plantel, en la capa data. */
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

    // ── Plantel (MOV-0015) ──────────────────────────────────────────────────

    @Test
    fun `CA-2 el plantel trae activos y retirados, y el miembro sin persona se descarta`() = runTest {
        servidor.enqueue(
            json(
                """[
                  {"personaId":"p1","numeroDocumento":"9000000201","nombre":"Andrés Gómez","posicion":null,"numeroCamiseta":1,
                   "estado":"ACTIVO","desde":"2026-11-01","esDelegado":true,"torneoEstado":"EN_CURSO","tieneCuenta":true},
                  {"personaId":"p2","numeroDocumento":null,"nombre":"Sofía Moreno","numeroCamiseta":null,
                   "estado":"ACTIVO","esDelegado":false,"torneoEstado":"EN_CURSO","tieneCuenta":false},
                  {"personaId":"p3","nombre":"Mateo Jiménez","numeroCamiseta":7,"estado":"RETIRADO","esDelegado":false,
                   "torneoEstado":"EN_CURSO","tieneCuenta":true},
                  {"personaId":null,"nombre":"Roto","estado":"ACTIVO"}
                ]""",
            ),
        )

        val plantel = repositorio.plantel("et1").getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("GET", peticion.method)
        assertEquals("/api/supercopa/delegado/equipo-torneo/et1/miembros", peticion.path)
        assertEquals(listOf("p1", "p2", "p3"), plantel.map { it.personaId })
        val (andres, sofia, mateo) = plantel
        assertTrue(andres.esDelegado)
        assertEquals("9000000201", andres.numeroDocumento)
        assertEquals(1, andres.numeroCamiseta)
        assertFalse(andres.torneoFinalizado)
        assertFalse(sofia.tieneCuenta)
        assertNull(sofia.numeroDocumento)
        assertNull(sofia.numeroCamiseta)
        assertTrue(sofia.activo)
        assertFalse(mateo.activo)
    }

    @Test
    fun `CA-8 el plantel de un torneo finalizado llega marcado`() = runTest {
        servidor.enqueue(json("""[{"personaId":"p1","nombre":"Andrés Gómez","estado":"ACTIVO","torneoEstado":"FINALIZADO"}]"""))

        assertTrue(repositorio.plantel("et1").getOrThrow().single().torneoFinalizado)
    }

    @Test
    fun `CA-3 el 403 de quien no es el delegado llega con su codigo`() = runTest {
        servidor.enqueue(problema(403, "NO_DELEGADO_DE_LA_INSCRIPCION", "No eres el delegado de esta inscripción."))

        val error = repositorio.plantel("et1").exceptionOrNull() as AppError.Negocio

        assertEquals("NO_DELEGADO_DE_LA_INSCRIPCION", error.codigo)
        assertEquals("No eres el delegado de esta inscripción.", error.mensaje)
    }

    @Test
    fun `CA-4 agregar envia solo los campos escritos y lee al agregado`() = runTest {
        servidor.enqueue(
            json("""{"personaId":"p9","numeroDocumento":"1090","nombre":"Cuenta Real","numeroCamiseta":7,"estado":"ACTIVO","esDelegado":false}"""),
        )
        servidor.enqueue(json("""{"personaId":"p10","nombre":"Sofía Moreno","estado":"ACTIVO","tieneCuenta":false}"""))

        val conDocumento = repositorio.agregarJugador("et1", TipoDocumento.TI, "1090", null, 7).getOrThrow()
        val soloNombre = repositorio.agregarJugador("et1", null, null, "Sofía Moreno", null).getOrThrow()

        val primera = servidor.takeRequest()
        assertEquals("POST", primera.method)
        assertEquals("/api/supercopa/delegado/equipo-torneo/et1/miembros", primera.path)
        assertEquals("""{"numeroDocumento":"1090","tipoDocumento":"TI","numeroCamiseta":7}""", primera.body.readUtf8())
        assertEquals("""{"nombre":"Sofía Moreno"}""", servidor.takeRequest().body.readUtf8())
        // Con documento de alguien con cuenta, el nombre es el de su cuenta.
        assertEquals("Cuenta Real", conDocumento.nombre)
        assertEquals("p9", conDocumento.personaId)
        assertFalse(soloNombre.tieneCuenta)
    }

    @Test
    fun `CA-5 los rechazos al agregar llegan con su codigo y su mensaje`() = runTest {
        servidor.enqueue(problema(400, "DOCUMENTO_REQUERIDO", "Este torneo exige documento: escribe el número de documento del jugador."))
        servidor.enqueue(problema(409, "JUGADOR_BLOQUEADO", "Esta persona tiene la cuenta bloqueada: no puede entrar a un plantel."))
        servidor.enqueue(problema(400, "REGLA_NEGOCIO", "El jugador ya pertenece a un equipo en este torneo."))

        val documento = repositorio.agregarJugador("et1", null, null, "Sofía Moreno", null).exceptionOrNull() as AppError.Negocio
        val bloqueado = repositorio.agregarJugador("et1", TipoDocumento.CC, "1090", null, null).exceptionOrNull() as AppError.Negocio
        val otroEquipo = repositorio.agregarJugador("et1", TipoDocumento.CC, "1091", null, null).exceptionOrNull() as AppError.Negocio

        assertEquals("DOCUMENTO_REQUERIDO", documento.codigo)
        assertEquals("JUGADOR_BLOQUEADO", bloqueado.codigo)
        assertEquals("REGLA_NEGOCIO", otroEquipo.codigo)
        assertEquals("El jugador ya pertenece a un equipo en este torneo.", otroEquipo.mensaje)
    }

    @Test
    fun `CA-6 cambiar el numero usa PATCH, y sin numero envia un objeto vacio`() = runTest {
        servidor.enqueue(json("""{"personaId":"p2","nombre":"Juan Castro","numeroCamiseta":9,"estado":"ACTIVO"}"""))
        servidor.enqueue(json("""{"personaId":"p2","nombre":"Juan Castro","numeroCamiseta":null,"estado":"ACTIVO"}"""))

        val conNumero = repositorio.cambiarCamiseta("et1", "p2", 9).getOrThrow()
        val sinNumero = repositorio.cambiarCamiseta("et1", "p2", null).getOrThrow()

        val primera = servidor.takeRequest()
        assertEquals("PATCH", primera.method)
        assertEquals("/api/supercopa/delegado/equipo-torneo/et1/miembros/p2/camiseta", primera.path)
        assertEquals("""{"numeroCamiseta":9}""", primera.body.readUtf8())
        assertEquals("{}", servidor.takeRequest().body.readUtf8())
        assertEquals(9, conNumero.numeroCamiseta)
        assertNull(sinNumero.numeroCamiseta)
    }

    @Test
    fun `CA-7 quitar usa remover y lee al jugador ya retirado`() = runTest {
        servidor.enqueue(json("""{"personaId":"p2","nombre":"Juan Castro","numeroCamiseta":10,"estado":"RETIRADO"}"""))

        val retirado = repositorio.quitarJugador("et1", "p2").getOrThrow()

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/supercopa/delegado/equipo-torneo/et1/miembros/p2/remover", peticion.path)
        assertFalse(retirado.activo)
        assertEquals("p2", retirado.personaId)
    }
}
