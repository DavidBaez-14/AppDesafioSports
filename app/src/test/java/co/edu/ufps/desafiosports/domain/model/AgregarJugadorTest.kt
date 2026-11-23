package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.use_case.competicion.AgregarJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.MENSAJE_CAMISETA
import co.edu.ufps.desafiosports.domain.use_case.competicion.mensajeNumeroOcupado
import co.edu.ufps.desafiosports.testutil.FakeDelegadoRepository
import co.edu.ufps.desafiosports.testutil.miembro
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0015: qué viaja al agregar a alguien al plantel y qué no. */
class AgregarJugadorTest {

    private val repositorio = FakeDelegadoRepository()
    private val agregar = AgregarJugadorUseCase(repositorio)

    private suspend fun fallo(borrador: BorradorJugador, exigeDocumento: Boolean = false, plantel: Plantel? = null) =
        agregar("et1", borrador, exigeDocumento, plantel).exceptionOrNull() as AppError.Validacion

    @Test
    fun `CA-4 con documento viaja el documento con su tipo, sin espacios de sobra`() = runTest {
        agregar("et1", BorradorJugador(TipoDocumento.TI, documento = "  1090123 ", camiseta = " 7 "), exigeDocumento = false)
            .getOrThrow()

        assertEquals(listOf("agregar:et1:TI:1090123:null:7"), repositorio.llamadas)
    }

    @Test
    fun `CA-4 sin documento viaja solo el nombre, sin tipo ni numero`() = runTest {
        val agregado = agregar("et1", BorradorJugador(TipoDocumento.CE, nombre = "  Sofía Moreno  "), exigeDocumento = false)
            .getOrThrow()

        assertEquals(listOf("agregar:et1:null:null:Sofía Moreno:null"), repositorio.llamadas)
        assertEquals("Sofía Moreno", agregado.nombre)
    }

    @Test
    fun `CA-5 lo que se sabe antes de enviar no viaja y cada motivo dice su campo`() = runTest {
        val sinDocumento = fallo(BorradorJugador(nombre = "Sofía Moreno"), exigeDocumento = true)
        val documentoLargo = fallo(BorradorJugador(documento = "1".repeat(21)))
        val vacio = fallo(BorradorJugador(documento = "   ", nombre = "  "))
        val nombreLargo = fallo(BorradorJugador(nombre = "x".repeat(151)))

        assertEquals(CampoValidado.DOCUMENTO, sinDocumento.campo)
        assertEquals(AgregarJugadorUseCase.MENSAJE_DOCUMENTO_EXIGIDO, sinDocumento.mensaje)
        assertEquals(CampoValidado.DOCUMENTO, documentoLargo.campo)
        assertEquals(CampoValidado.NOMBRES, vacio.campo)
        assertEquals(AgregarJugadorUseCase.MENSAJE_NOMBRE, vacio.mensaje)
        assertEquals(CampoValidado.NOMBRES, nombreLargo.campo)
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-5 un numero fuera de 0 a 99 no viaja`() = runTest {
        val grande = fallo(BorradorJugador(nombre = "Sofía Moreno", camiseta = "100"))
        val letras = fallo(BorradorJugador(nombre = "Sofía Moreno", camiseta = "1a"))
        val negativo = fallo(BorradorJugador(nombre = "Sofía Moreno", camiseta = "-1"))

        listOf(grande, letras, negativo).forEach {
            assertEquals(CampoValidado.CAMISETA, it.campo)
            assertEquals(MENSAJE_CAMISETA, it.mensaje)
        }
        assertTrue(repositorio.llamadas.isEmpty())
    }

    @Test
    fun `CA-5 el numero de otro activo no viaja, y el de un retirado si`() = runTest {
        val plantel = Plantel(listOf(miembro("p1", "Juan Castro", 10), miembro("p2", "Mateo Jiménez", 7, activo = false)))

        val ocupado = fallo(BorradorJugador(nombre = "Sofía Moreno", camiseta = "10"), plantel = plantel)
        assertEquals(CampoValidado.CAMISETA, ocupado.campo)
        assertEquals(mensajeNumeroOcupado(10, "Juan Castro"), ocupado.mensaje)
        assertTrue(repositorio.llamadas.isEmpty())

        agregar("et1", BorradorJugador(nombre = "Sofía Moreno", camiseta = "7"), false, plantel).getOrThrow()
        assertEquals(listOf("agregar:et1:null:null:Sofía Moreno:7"), repositorio.llamadas)
    }
}
