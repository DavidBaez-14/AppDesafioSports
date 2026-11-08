package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0001 · CA-5: validar antes de llamar a Supabase. */
class IniciarSesionUseCaseTest {

    private val repositorio = FakeSesionRepository()
    private val iniciarSesion = IniciarSesionUseCase(repositorio)

    @Test
    fun `CA-5 correo mal escrito falla sin llamar a Supabase`() = runTest {
        val resultado = iniciarSesion("jugador01-prueba.local", "clave-segura")

        assertTrue(resultado.exceptionOrNull() is AppError.Validacion)
        assertTrue(repositorio.llamadasInicio.isEmpty())
    }

    @Test
    fun `CA-5 contrasena de menos de 8 caracteres falla sin llamar a Supabase`() = runTest {
        val resultado = iniciarSesion("jugador01@prueba.local", "1234567")

        assertTrue(resultado.exceptionOrNull() is AppError.Validacion)
        assertTrue(repositorio.llamadasInicio.isEmpty())
    }

    @Test
    fun `CA-5 datos validos llaman a Supabase con el correo sin espacios`() = runTest {
        val resultado = iniciarSesion("  jugador01@prueba.local ", "12345678")

        assertTrue(resultado.isSuccess)
        assertEquals(listOf("jugador01@prueba.local" to "12345678"), repositorio.llamadasInicio)
    }

    @Test
    fun `CA-5 el error de Supabase llega intacto a la pantalla`() = runTest {
        repositorio.resultadoInicio = Result.failure(AppError.CredencialesInvalidas())

        val resultado = iniciarSesion("jugador01@prueba.local", "12345678")

        assertTrue(resultado.exceptionOrNull() is AppError.CredencialesInvalidas)
    }

    @Test
    fun `los correos del seed con dominio local son validos`() {
        assertTrue(IniciarSesionUseCase.esCorreoValido("admin01@prueba.local"))
        assertTrue(IniciarSesionUseCase.esCorreoValido("nombre.apellido+torneo@ufps.edu.co"))
    }
}
