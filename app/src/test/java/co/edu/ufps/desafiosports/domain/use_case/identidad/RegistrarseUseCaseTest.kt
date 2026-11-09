package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0003 · CA-2: validar antes de crear la cuenta en Supabase. */
class RegistrarseUseCaseTest {

    private val repositorio = FakeSesionRepository()
    private val registrarse = RegistrarseUseCase(repositorio)

    @Test
    fun `CA-2 correo mal escrito falla en el campo correo sin llamar a Supabase`() = runTest {
        val error = registrarse("nueva-cuenta.co", "clave-segura").exceptionOrNull()

        assertEquals(CampoValidado.CORREO, (error as AppError.Validacion).campo)
        assertTrue(repositorio.llamadasRegistro.isEmpty())
    }

    @Test
    fun `CA-2 contrasena corta falla en el campo contrasena sin llamar a Supabase`() = runTest {
        val error = registrarse("nueva@cuenta.co", "1234567").exceptionOrNull()

        assertEquals(CampoValidado.CLAVE, (error as AppError.Validacion).campo)
        assertTrue(repositorio.llamadasRegistro.isEmpty())
    }

    @Test
    fun `CA-2 datos validos crean la cuenta con el correo sin espacios`() = runTest {
        val resultado = registrarse("  nueva@cuenta.co ", "clave-segura")

        assertTrue(resultado.isSuccess)
        assertEquals(listOf("nueva@cuenta.co" to "clave-segura"), repositorio.llamadasRegistro)
    }
}
