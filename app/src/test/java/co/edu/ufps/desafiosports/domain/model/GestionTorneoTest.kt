package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.competicion.GenerarPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.InvitarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPersonalDeArbitrajeUseCase
import co.edu.ufps.desafiosports.testutil.FakeGestionTorneoRepository
import co.edu.ufps.desafiosports.testutil.TOKEN_DE_PRUEBA
import co.edu.ufps.desafiosports.testutil.arbitroDePrueba
import co.edu.ufps.desafiosports.testutil.invitacionDePrueba
import co.edu.ufps.desafiosports.testutil.partidoDeTorneoDePrueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

/** MOV-0011: las reglas de la gestión de un torneo, en el dominio. */
class GestionTorneoTest {

    private val repositorio = FakeGestionTorneoRepository()

    // ── Árbitros ────────────────────────────────────────────────────────────

    @Test
    fun `CA-2 los arbitros van por nombre y las invitaciones con la mas reciente arriba`() = runTest {
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1, "Zoe"), arbitroDePrueba(2, "ana"), arbitroDePrueba(3, "Beto")))
        repositorio.invitaciones = Result.success(
            listOf(
                invitacionDePrueba("vieja", fecha = LocalDateTime.of(2026, 11, 1, 10, 0)),
                invitacionDePrueba("nueva", fecha = LocalDateTime.of(2026, 11, 15, 10, 0)),
            ),
        )

        val personal = ObtenerPersonalDeArbitrajeUseCase(repositorio)("t1").getOrThrow()

        assertEquals(listOf("ana", "Beto", "Zoe"), personal.arbitros.map { it.nombre })
        assertEquals(listOf("nueva", "vieja"), personal.invitaciones.map { it.id })
    }

    @Test
    fun `CA-3 el correo de la invitacion es opcional y uno mal escrito no viaja`() = runTest {
        val invitar = InvitarArbitroUseCase(repositorio)

        assertTrue(invitar("t1", "   ").isSuccess)
        assertTrue(invitar("t1", " Ana@Correo.com ").isSuccess)
        val fallo = invitar("t1", "ana@correo").exceptionOrNull()

        assertTrue(fallo is AppError.Validacion)
        assertEquals(listOf("invitar:t1:null", "invitar:t1:ana@correo.com"), repositorio.llamadas)
    }

    // ── Generar los partidos ────────────────────────────────────────────────

    @Test
    fun `CA-6 con menos de dos equipos aprobados no se puede generar`() {
        val estado = EstadoDePartidos(EstadoTorneo.PUBLICADO, equiposAprobados = 1, generados = false)

        assertEquals(BloqueoDePartidos.FaltanEquipos(1), estado.bloqueo)
        assertFalse(estado.puedeGenerar)
    }

    @Test
    fun `CA-6 un torneo en borrador o finalizado no genera partidos`() {
        assertEquals(BloqueoDePartidos.TorneoEnBorrador, EstadoDePartidos(EstadoTorneo.BORRADOR, 8, false).bloqueo)
        assertEquals(BloqueoDePartidos.TorneoFinalizado, EstadoDePartidos(EstadoTorneo.FINALIZADO, 8, false).bloqueo)
    }

    @Test
    fun `CA-6 con dos equipos aprobados se puede generar, y una sola vez`() {
        val listo = EstadoDePartidos(EstadoTorneo.PUBLICADO, equiposAprobados = 2, generados = false)

        assertNull(listo.bloqueo)
        assertTrue(listo.puedeGenerar)
        assertFalse(listo.copy(generados = true).puedeGenerar)
    }

    @Test
    fun `CA-7 solo con las inscripciones abiertas generar inicia el torneo`() {
        assertTrue(EstadoDePartidos(EstadoTorneo.PUBLICADO, 2, false).generarIniciaElTorneo)
        assertFalse(EstadoDePartidos(EstadoTorneo.EN_CURSO, 2, false).generarIniciaElTorneo)
    }

    @Test
    fun `CA-7 con el torneo publicado primero lo inicia y despues genera`() = runTest {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.PUBLICADO, 4, generados = false))

        assertTrue(GenerarPartidosUseCase(repositorio)("t1").isSuccess)

        assertEquals(listOf("estado:t1", "iniciar:t1", "generar:t1"), repositorio.llamadas)
    }

    @Test
    fun `CA-7 con el torneo en curso solo genera`() = runTest {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = false))

        assertTrue(GenerarPartidosUseCase(repositorio)("t1").isSuccess)

        assertEquals(listOf("estado:t1", "generar:t1"), repositorio.llamadas)
    }

    @Test
    fun `CA-7 si no se puede iniciar el torneo no se intenta generar`() = runTest {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.PUBLICADO, 4, generados = false))
        repositorio.resultadoIniciar = Result.failure(AppError.Negocio("REGLA_NEGOCIO", "Solo se puede iniciar un torneo en estado PUBLICADO."))

        val fallo = GenerarPartidosUseCase(repositorio)("t1").exceptionOrNull()

        assertEquals("Solo se puede iniciar un torneo en estado PUBLICADO.", (fallo as AppError).mensaje)
        assertFalse(repositorio.llamadas.contains("generar:t1"))
    }

    @Test
    fun `CA-7 si ya estan generados o faltan equipos no llama al backend para generar`() = runTest {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
        assertTrue(GenerarPartidosUseCase(repositorio)("t1").isSuccess)

        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.PUBLICADO, 1, generados = false))
        assertTrue(GenerarPartidosUseCase(repositorio)("t1").exceptionOrNull() is AppError.Validacion)

        assertEquals(listOf("estado:t1", "estado:t1"), repositorio.llamadas)
    }

    @Test
    fun `CA-6 sin partidos generados no se pide la lista`() = runTest {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.PUBLICADO, 3, generados = false))

        val resultado = ObtenerPartidosDeTorneoUseCase(repositorio)("t1").getOrThrow()

        assertTrue(resultado.partidos.isEmpty())
        assertEquals(listOf("estado:t1"), repositorio.llamadas)
    }

    // ── Partidos ────────────────────────────────────────────────────────────

    @Test
    fun `CA-8 una liga se agrupa por jornada y cada jornada por fecha`() {
        val grupos = listOf(
            partidoDeTorneoDePrueba("j2", jornada = 2),
            partidoDeTorneoDePrueba("j1-tarde", jornada = 1, fecha = LocalDateTime.of(2026, 11, 21, 16, 0)),
            partidoDeTorneoDePrueba("j1-sin-fecha", jornada = 1, fecha = null),
            partidoDeTorneoDePrueba("j1-manana", jornada = 1, fecha = LocalDateTime.of(2026, 11, 21, 8, 0)),
        ).porFaseYJornada()

        assertEquals(listOf(1, 2), grupos.map { it.jornada })
        assertEquals(listOf("j1-manana", "j1-tarde", "j1-sin-fecha"), grupos.first().partidos.map { it.id })
    }

    @Test
    fun `CA-8 los grupos van por jornada y letra, y las rondas en el orden en que se juegan`() {
        val grupos = listOf(
            partidoDeTorneoDePrueba("final", fase = FasePartido.FINAL, jornada = 4),
            partidoDeTorneoDePrueba("tercero", fase = FasePartido.TERCER_PUESTO, jornada = 4),
            partidoDeTorneoDePrueba("semi-1", fase = FasePartido.SEMIS, jornada = 3),
            partidoDeTorneoDePrueba("semi-2", fase = FasePartido.SEMIS, jornada = 3),
            partidoDeTorneoDePrueba("b1", jornada = 1, grupo = "B"),
            partidoDeTorneoDePrueba("a2", jornada = 2, grupo = "A"),
            partidoDeTorneoDePrueba("a1", jornada = 1, grupo = "A"),
        ).porFaseYJornada()

        assertEquals(
            listOf("GRUPOS|1|A", "GRUPOS|1|B", "GRUPOS|2|A", "SEMIS||", "TERCER_PUESTO||", "FINAL||"),
            grupos.map { it.clave },
        )
        assertEquals(listOf("semi-1", "semi-2"), grupos[3].partidos.map { it.id })
    }

    @Test
    fun `CA-8 una fase que la app no conoce no se pierde y va al final`() {
        val grupos = listOf(
            partidoDeTorneoDePrueba("raro", fase = FasePartido.desde("FASE_NUEVA"), faseCodigo = "FASE_NUEVA"),
            partidoDeTorneoDePrueba("final", fase = FasePartido.FINAL),
        ).porFaseYJornada()

        assertEquals(listOf("FINAL", "FASE_NUEVA"), grupos.map { it.faseCodigo })
        assertEquals(FasePartido.DESCONOCIDA, grupos.last().fase)
    }

    @Test
    fun `CA-9 un partido cerrado o en descanso ya no admite arbitros`() {
        assertTrue(partidoDeTorneoDePrueba("abierto").admiteArbitros)
        assertTrue(partidoDeTorneoDePrueba("jugando", estado = EstadoPartido.EN_CURSO).admiteArbitros)
        assertFalse(partidoDeTorneoDePrueba("cerrado", estado = EstadoPartido.FINALIZADO).admiteArbitros)
        assertFalse(partidoDeTorneoDePrueba("wo", estado = EstadoPartido.WO).admiteArbitros)
        assertFalse(partidoDeTorneoDePrueba("descansa", estado = EstadoPartido.DESCONOCIDO, estadoCodigo = "DESCANSO").admiteArbitros)
    }

    // ── Invitación recibida ─────────────────────────────────────────────────

    @Test
    fun `CA-11 el token sale del enlace completo o del codigo suelto`() {
        assertEquals(TOKEN_DE_PRUEBA, tokenDeInvitacion("https://desafio.test/invitacion/$TOKEN_DE_PRUEBA"))
        assertEquals(TOKEN_DE_PRUEBA, tokenDeInvitacion("  Te invito: https://desafio.test/invitacion/$TOKEN_DE_PRUEBA?x=1 "))
        assertEquals(TOKEN_DE_PRUEBA, tokenDeInvitacion(" $TOKEN_DE_PRUEBA\n"))
    }

    @Test
    fun `CA-11 lo que no parece una invitacion no da token`() {
        assertNull(tokenDeInvitacion(""))
        assertNull(tokenDeInvitacion("hola, quiero arbitrar"))
        assertNull(tokenDeInvitacion("https://desafio.test/t/$TOKEN_DE_PRUEBA"))
        assertNull(tokenDeInvitacion("https://desafio.test/invitacion/corto"))
    }
}
