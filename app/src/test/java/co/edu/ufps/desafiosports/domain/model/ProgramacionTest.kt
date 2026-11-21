package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.competicion.GenerarPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ProgramarPartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.VolverAGenerarPartidosUseCase
import co.edu.ufps.desafiosports.testutil.FakeGestionTorneoRepository
import co.edu.ufps.desafiosports.testutil.partidoDeTorneoDePrueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** MOV-0013: programar un partido y volver a generar los partidos, en el dominio. */
class ProgramacionTest {

    private val repositorio = FakeGestionTorneoRepository()

    @Test
    fun `CA-1 el borrador arranca con la fecha, la hora y el lugar del partido`() {
        val partido = partidoDeTorneoDePrueba("p1", fecha = LocalDateTime.of(2026, 11, 21, 15, 30)).copy(lugar = "Cancha 2")

        val borrador = BorradorProgramacion.de(partido)

        assertEquals(LocalDate.of(2026, 11, 21), borrador.fecha)
        assertEquals(LocalTime.of(15, 30), borrador.hora)
        assertEquals("Cancha 2", borrador.lugar)
        assertEquals(LocalDateTime.of(2026, 11, 21, 15, 30), borrador.fechaHora)

        // Sin fecha ni lugar, el borrador queda vacío.
        val vacio = BorradorProgramacion.de(partidoDeTorneoDePrueba("p2", fecha = null))
        assertNull(vacio.fechaHora)
        assertEquals("", vacio.lugar)
    }

    @Test
    fun `CA-2 programar envia la fecha con su hora y el lugar sin espacios de sobra`() = runTest {
        val programar = ProgramarPartidoUseCase(repositorio)

        val resultado = programar(
            "p1",
            BorradorProgramacion(LocalDate.of(2026, 11, 21), LocalTime.of(15, 30), "  Coliseo UFPS, cancha 1  "),
        ).getOrThrow()

        assertEquals(listOf("programar:p1:2026-11-21T15:30:Coliseo UFPS, cancha 1"), repositorio.llamadas)
        assertEquals(LocalDateTime.of(2026, 11, 21, 15, 30), resultado.fecha)
        assertEquals("Coliseo UFPS, cancha 1", resultado.lugar)
    }

    @Test
    fun `CA-2 un lugar vacio viaja vacio, para borrarlo`() = runTest {
        ProgramarPartidoUseCase(repositorio)("p1", BorradorProgramacion(LocalDate.of(2026, 11, 21), LocalTime.of(8, 0), "   "))

        assertEquals(listOf("programar:p1:2026-11-21T08:00:"), repositorio.llamadas)
    }

    @Test
    fun `CA-3 el dia sin la hora, o la hora sin el dia, no viaja`() = runTest {
        val programar = ProgramarPartidoUseCase(repositorio)

        val sinHora = programar("p1", BorradorProgramacion(fecha = LocalDate.of(2026, 11, 21))).exceptionOrNull()
        val sinDia = programar("p1", BorradorProgramacion(hora = LocalTime.of(8, 0))).exceptionOrNull()

        assertEquals(ProgramarPartidoUseCase.MENSAJE_FECHA_INCOMPLETA, (sinHora as AppError.Validacion).mensaje)
        assertTrue(sinDia is AppError.Validacion)
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-3 un lugar de mas de 120 caracteres no viaja`() = runTest {
        val fallo = ProgramarPartidoUseCase(repositorio)(
            "p1", BorradorProgramacion(LocalDate.of(2026, 11, 21), LocalTime.of(8, 0), "x".repeat(121)),
        ).exceptionOrNull()

        assertEquals(ProgramarPartidoUseCase.MENSAJE_LUGAR, (fallo as AppError.Validacion).mensaje)
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-5 se pueden volver a generar mientras ninguno se haya jugado ni este en juego`() {
        val programados = listOf(
            partidoDeTorneoDePrueba("p1"),
            partidoDeTorneoDePrueba("p2", estado = EstadoPartido.APLAZADO),
            partidoDeTorneoDePrueba("p3", estadoCodigo = "DESCANSO", estado = EstadoPartido.DESCONOCIDO),
        )

        assertTrue(programados.sePuedenRegenerar)
        assertFalse(emptyList<PartidoDeTorneo>().sePuedenRegenerar)
        assertFalse((programados + partidoDeTorneoDePrueba("p4", estado = EstadoPartido.EN_CURSO)).sePuedenRegenerar)
        assertFalse((programados + partidoDeTorneoDePrueba("p4", estado = EstadoPartido.FINALIZADO)).sePuedenRegenerar)
        assertFalse((programados + partidoDeTorneoDePrueba("p4", estado = EstadoPartido.WO)).sePuedenRegenerar)
    }

    @Test
    fun `CA-6 volver a generar borra y despues genera con el torneo en curso`() = runTest {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
        repositorio.alBorrar = { repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = false)) }

        val resultado = VolverAGenerarPartidosUseCase(repositorio)("t1")

        assertTrue(resultado.isSuccess)
        assertEquals(listOf("borrar:t1", "estado:t1", "generar:t1"), repositorio.llamadas)
    }

    @Test
    fun `CA-6 si no se pueden borrar no se genera nada`() = runTest {
        repositorio.resultadoBorrar = Result.failure(
            AppError.Negocio("REGLA_NEGOCIO", "No se puede borrar el fixture: ya hay partidos jugados o en curso."),
        )

        val fallo = VolverAGenerarPartidosUseCase(repositorio, GenerarPartidosUseCase(repositorio))("t1").exceptionOrNull()

        assertEquals("No se puede borrar el fixture: ya hay partidos jugados o en curso.", (fallo as AppError).mensaje)
        assertEquals(listOf("borrar:t1"), repositorio.llamadas)
    }

    @Test
    fun `CA-6 si tras borrar ya no hay equipos suficientes, no se genera y se dice por que`() = runTest {
        repositorio.alBorrar = { repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 1, generados = false)) }

        val fallo = VolverAGenerarPartidosUseCase(repositorio)("t1").exceptionOrNull()

        assertEquals(GenerarPartidosUseCase.MENSAJE_NO_SE_PUEDE, (fallo as AppError.Validacion).mensaje)
        assertEquals(listOf("borrar:t1", "estado:t1"), repositorio.llamadas)
    }
}
