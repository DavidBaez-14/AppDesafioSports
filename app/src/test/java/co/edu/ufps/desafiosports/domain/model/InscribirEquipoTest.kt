package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.competicion.InscribirEquipoUseCase
import co.edu.ufps.desafiosports.testutil.FakeDelegadoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0014: qué equipo se inscribe y qué no viaja. */
class InscribirEquipoTest {

    private val repositorio = FakeDelegadoRepository()
    private val inscribir = InscribirEquipoUseCase(repositorio)
    private val propios = listOf(EquipoPropio("e1", "Halcones FC"), EquipoPropio("e2", "Pumas FC"))

    @Test
    fun `CA-3 un equipo propio viaja por su id, con el enlace si lo hay`() = runTest {
        inscribir("t1", EquipoAInscribir.Existente("e2"), "tok-enlace", propios).getOrThrow()

        assertEquals(listOf("inscribir:t1:e2:null:tok-enlace"), repositorio.llamadas)
    }

    @Test
    fun `CA-4 un equipo nuevo viaja por su nombre, sin espacios de sobra`() = runTest {
        val hecha = inscribir("t1", EquipoAInscribir.Nuevo("  Tiburones FC "), null, propios).getOrThrow()

        assertEquals(listOf("inscribir:t1:null:Tiburones FC:null"), repositorio.llamadas)
        assertEquals("Tiburones FC", hecha.equipo)
        assertEquals(EstadoInscripcion.PENDIENTE_PAGO, hecha.estado)
    }

    @Test
    fun `CA-5 un nombre vacio o de mas de 150 caracteres no viaja`() = runTest {
        val vacio = inscribir("t1", EquipoAInscribir.Nuevo("   "), null, propios).exceptionOrNull()
        val largo = inscribir("t1", EquipoAInscribir.Nuevo("x".repeat(151)), null, propios).exceptionOrNull()

        assertEquals(InscribirEquipoUseCase.MENSAJE_NOMBRE_VACIO, (vacio as AppError.Validacion).mensaje)
        assertEquals(InscribirEquipoUseCase.MENSAJE_NOMBRE_LARGO, (largo as AppError.Validacion).mensaje)
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-5 el nombre de un equipo que ya tiene, sin importar mayusculas, no viaja`() = runTest {
        val fallo = inscribir("t1", EquipoAInscribir.Nuevo("halcones fc"), null, propios).exceptionOrNull()

        assertEquals(InscribirEquipoUseCase.mensajeRepetido("halcones fc"), (fallo as AppError.Validacion).mensaje)
        assertTrue(repositorio.llamadas.isEmpty())
    }
}
