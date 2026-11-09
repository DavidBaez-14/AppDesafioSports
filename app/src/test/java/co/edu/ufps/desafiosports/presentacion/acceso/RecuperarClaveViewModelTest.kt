package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.identidad.RecuperarClaveUseCase
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0002 · CA-9: Recuperar contraseña vista desde la pantalla. */
class RecuperarClaveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()

    private fun crearViewModel(correo: String = CORREO) = RecuperarClaveViewModel(correo, RecuperarClaveUseCase(sesion))

    @Test
    fun `CA-9 trae el correo que se escribio en el login`() {
        assertEquals(CORREO, crearViewModel().state.correo)
    }

    @Test
    fun `CA-9 correo valido pide el enlace y muestra revisa tu correo`() {
        val vm = crearViewModel()

        vm.enviar()

        assertTrue(vm.state.enviado)
        assertFalse(vm.state.enProceso)
        assertEquals(listOf(CORREO), sesion.llamadasRecuperacion)
    }

    @Test
    fun `CA-9 correo mal escrito muestra el error en el campo sin llamar a Supabase`() {
        val vm = crearViewModel("jugador01-prueba.local")

        vm.enviar()

        assertNotNull(vm.state.errorCorreo)
        assertFalse(vm.state.enviado)
        assertTrue(sesion.llamadasRecuperacion.isEmpty())
    }

    @Test
    fun `CA-9 al superar el limite de envios muestra el aviso en espanol`() {
        val mensaje = "Ya pediste varios correos. Espera unos minutos y vuelve a intentarlo."
        sesion.resultadoRecuperacion = Result.failure(AppError.Negocio("over_email_send_rate_limit", mensaje))
        val vm = crearViewModel()

        vm.enviar()

        assertEquals(mensaje, vm.state.error)
        assertFalse(vm.state.enviado)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-9 sin conexion muestra el aviso y no marca como enviado`() {
        sesion.resultadoRecuperacion = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()

        vm.enviar()

        assertEquals(AppError.SinConexion().mensaje, vm.state.error)
        assertFalse(vm.state.enviado)
    }

    private companion object {
        const val CORREO = "jugador01@prueba.local"
    }
}
