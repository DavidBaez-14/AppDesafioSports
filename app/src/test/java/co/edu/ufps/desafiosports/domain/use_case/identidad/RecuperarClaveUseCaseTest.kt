package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0002 · CA-9: pedir el correo de recuperación, validando antes de llamar a Supabase. */
class RecuperarClaveUseCaseTest {

    private val repositorio = FakeSesionRepository()
    private val recuperar = RecuperarClaveUseCase(repositorio)

    @Test
    fun `CA-9 correo mal escrito falla en el campo correo sin llamar a Supabase`() = runTest {
        val error = recuperar("jugador01-prueba.local").exceptionOrNull()

        assertTrue(error is AppError.Validacion)
        assertEquals(CampoValidado.CORREO, (error as AppError.Validacion).campo)
        assertTrue(repositorio.llamadasRecuperacion.isEmpty())
    }

    @Test
    fun `CA-9 correo valido pide el enlace con el correo sin espacios`() = runTest {
        val resultado = recuperar("  jugador01@prueba.local ")

        assertTrue(resultado.isSuccess)
        assertEquals(listOf("jugador01@prueba.local"), repositorio.llamadasRecuperacion)
    }

    @Test
    fun `CA-9 el limite de envios de Supabase llega intacto a la pantalla`() = runTest {
        repositorio.resultadoRecuperacion = Result.failure(AppError.Negocio("over_email_send_rate_limit", "Espera"))

        val resultado = recuperar("jugador01@prueba.local")

        assertTrue(resultado.exceptionOrNull() is AppError.Negocio)
    }
}
