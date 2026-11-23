package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarCamisetaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDelPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.mensajeNumeroOcupado
import co.edu.ufps.desafiosports.testutil.FakeDelegadoRepository
import co.edu.ufps.desafiosports.testutil.miembro
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0015: cambiar el número de un jugador y quitarlo del plantel. */
class CamisetaYQuitarTest {

    private val repositorio = FakeDelegadoRepository()
    private val cambiar = CambiarCamisetaUseCase(repositorio)
    private val quitar = QuitarDelPlantelUseCase(repositorio)

    private val delegado = miembro("p1", "Andrés Gómez", 1, delegado = true)
    private val juan = miembro("p2", "Juan Castro", 10)
    private val sofia = miembro("p3", "Sofía Moreno", null)
    private val plantel = Plantel(listOf(delegado, juan, sofia))

    @Test
    fun `CA-6 cambiar el numero envia el nuevo, y vacio se lo quita`() = runTest {
        val conNumero = cambiar("et1", sofia, " 8 ", plantel).getOrThrow()
        val sinNumero = cambiar("et1", juan, "", plantel).getOrThrow()

        assertEquals(listOf("camiseta:et1:p3:8", "camiseta:et1:p2:null"), repositorio.llamadas)
        assertEquals(8, conNumero.numeroCamiseta)
        assertEquals(null, sinNumero.numeroCamiseta)
    }

    @Test
    fun `CA-6 si el numero no cambio no viaja`() = runTest {
        val igual = cambiar("et1", juan, "10", plantel).getOrThrow()
        val sigueSinNumero = cambiar("et1", sofia, "  ", plantel).getOrThrow()

        assertEquals(juan, igual)
        assertEquals(sofia, sigueSinNumero)
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-6 el numero de otro activo o uno fuera de rango no viaja`() = runTest {
        val ocupado = cambiar("et1", sofia, "10", plantel).exceptionOrNull() as AppError.Validacion
        val fueraDeRango = cambiar("et1", sofia, "100", plantel).exceptionOrNull() as AppError.Validacion

        assertEquals(mensajeNumeroOcupado(10, "Juan Castro"), ocupado.mensaje)
        assertEquals(CampoValidado.CAMISETA, ocupado.campo)
        assertEquals(CampoValidado.CAMISETA, fueraDeRango.campo)
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-7 al delegado no se le quita, y a los demas si`() = runTest {
        val delDelegado = quitar("et1", delegado).exceptionOrNull() as AppError.Validacion
        assertEquals(QuitarDelPlantelUseCase.MENSAJE_DELEGADO, delDelegado.mensaje)
        assertTrue(repositorio.llamadas.isEmpty())

        repositorio.plantel = Result.success(plantel.miembros)
        val retirado = quitar("et1", juan).getOrThrow()

        assertEquals(listOf("quitar:et1:p2"), repositorio.llamadas)
        assertFalse(retirado.activo)
    }
}
