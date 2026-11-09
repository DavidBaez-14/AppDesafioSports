package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.Nonce
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0006 · CA-5 y CA-8: el token de Google llega a Supabase con el nonce crudo. */
class IniciarSesionConGoogleUseCaseTest {

    private val repositorio = FakeSesionRepository()
    private val iniciar = IniciarSesionConGoogleUseCase(repositorio)

    @Test
    fun `CA-5 sin token no llama a Supabase`() = runTest {
        val resultado = iniciar("  ", "nonce")

        assertTrue(resultado.exceptionOrNull() is AppError)
        assertTrue(repositorio.llamadasGoogle.isEmpty())
    }

    @Test
    fun `CA-5 con token llama a Supabase con el token y el nonce crudo`() = runTest {
        val resultado = iniciar("id-token", "nonce-crudo")

        assertTrue(resultado.isSuccess)
        assertEquals(listOf("id-token" to "nonce-crudo"), repositorio.llamadasGoogle)
    }

    @Test
    fun `CA-8 el nonce es aleatorio en cada intento`() {
        val uno = Nonce.nuevo()
        val otro = Nonce.nuevo()

        assertTrue(uno.length >= 32)
        assertNotEquals(uno, otro)
    }

    @Test
    fun `CA-8 a Google va el SHA-256 del nonce en hexadecimal`() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Nonce.sha256("abc"))
    }
}
