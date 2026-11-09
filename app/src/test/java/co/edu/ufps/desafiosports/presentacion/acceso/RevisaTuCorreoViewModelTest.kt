package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.identidad.ReenviarConfirmacionUseCase
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0003 · CA-6: reenviar el correo de confirmación. */
class RevisaTuCorreoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()

    private fun crearViewModel() = RevisaTuCorreoViewModel(CORREO, ReenviarConfirmacionUseCase(sesion))

    @Test
    fun `CA-6 muestra el correo con el que se creo la cuenta`() {
        assertEquals(CORREO, crearViewModel().state.correo)
    }

    @Test
    fun `CA-6 reenviar pide otro envio y lo confirma`() {
        val vm = crearViewModel()

        vm.reenviar()

        assertTrue(vm.state.reenviado)
        assertFalse(vm.state.enProceso)
        assertEquals(listOf(CORREO), sesion.llamadasReenvio)
    }

    @Test
    fun `CA-6 con el limite de envios muestra el aviso y no lo confirma`() {
        val mensaje = "Ya pediste varios correos. Espera unos minutos y vuelve a intentarlo."
        sesion.resultadoReenvio = Result.failure(AppError.Negocio("over_email_send_rate_limit", mensaje))
        val vm = crearViewModel()

        vm.reenviar()

        assertEquals(mensaje, vm.state.error)
        assertFalse(vm.state.reenviado)
    }

    private companion object {
        const val CORREO = "nueva@cuenta.co"
    }
}
