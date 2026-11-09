package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0002 · CA-2 a CA-8: el login visto desde la pantalla (ViewModel con fakes). */
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()
    private val cuentas = FakeCuentaRepository()

    private fun crearViewModel() = LoginViewModel(
        iniciarSesionUseCase = IniciarSesionUseCase(sesion),
        obtenerCuentaUseCase = ObtenerCuentaUseCase(cuentas),
        cerrarSesionUseCase = CerrarSesionUseCase(sesion),
    )

    private fun LoginViewModel.escribir(correo: String = CORREO, clave: String = CLAVE) {
        onCorreoChange(correo)
        onClaveChange(clave)
    }

    @Test
    fun `CA-2 correo mal escrito muestra el error bajo el correo sin llamar a Supabase`() {
        val vm = crearViewModel()
        vm.escribir(correo = "jugador01-prueba.local")

        vm.entrar()

        assertNotNull(vm.state.errorCorreo)
        assertNull(vm.state.errorClave)
        assertNull(vm.state.error)
        assertFalse(vm.state.enProceso)
        assertTrue(sesion.llamadasInicio.isEmpty())
    }

    @Test
    fun `CA-2 contrasena corta muestra el error bajo la contrasena sin llamar a Supabase`() {
        val vm = crearViewModel()
        vm.escribir(clave = "1234567")

        vm.entrar()

        assertNotNull(vm.state.errorClave)
        assertNull(vm.state.errorCorreo)
        assertTrue(sesion.llamadasInicio.isEmpty())
    }

    @Test
    fun `CA-2 escribir de nuevo en el campo borra su error`() {
        val vm = crearViewModel()
        vm.escribir(correo = "jugador01-prueba.local")
        vm.entrar()

        vm.onCorreoChange(CORREO)

        assertNull(vm.state.errorCorreo)
    }

    @Test
    fun `CA-3 credenciales rechazadas muestran el aviso y conservan el correo`() {
        sesion.resultadoInicio = Result.failure(AppError.CredencialesInvalidas())
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertEquals(AppError.CredencialesInvalidas().mensaje, vm.state.error)
        assertEquals(CORREO, vm.state.correo)
        assertFalse(vm.state.ingresado)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-4 credenciales validas y cuenta registrada entran y borran la contrasena`() {
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertTrue(vm.state.ingresado)
        assertEquals("", vm.state.clave)
        assertNull(vm.state.error)
        assertFalse(vm.state.enProceso)
        assertEquals(1, cuentas.llamadas)
    }

    @Test
    fun `CA-4 al atender el ingreso el evento se apaga`() {
        val vm = crearViewModel()
        vm.escribir()
        vm.entrar()

        vm.onIngresoAtendido()

        assertFalse(vm.state.ingresado)
    }

    @Test
    fun `MOV-0003 CA-7 cuenta sin registro completo va a completar registro sin cerrar la sesion`() {
        cuentas.resultado = Result.failure(AppError.CuentaNoRegistrada("Completa tu registro para continuar."))
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertTrue(vm.state.debeCompletarRegistro)
        assertFalse(vm.state.ingresado)
        assertNull(vm.state.error)
        assertEquals(0, sesion.cierres)
        assertEquals(EstadoSesion.Activa(CORREO), sesion.estado.value)
    }

    @Test
    fun `MOV-0003 CA-7 al atender el aviso de completar registro el evento se apaga`() {
        cuentas.resultado = Result.failure(AppError.CuentaNoRegistrada("Completa tu registro."))
        val vm = crearViewModel()
        vm.escribir()
        vm.entrar()

        vm.onCompletarRegistroAtendido()

        assertFalse(vm.state.debeCompletarRegistro)
    }

    @Test
    fun `CA-5 cuenta bloqueada cierra la sesion y muestra el mensaje del backend`() {
        cuentas.resultado = Result.failure(AppError.CuentaBloqueada("Tu cuenta está bloqueada."))
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertEquals("Tu cuenta está bloqueada.", vm.state.error)
        assertFalse(vm.state.ingresado)
        assertEquals(1, sesion.cierres)
    }

    @Test
    fun `CA-5 si el backend no responde despues de Supabase entra igual`() {
        cuentas.resultado = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertTrue(vm.state.ingresado)
        assertEquals(0, sesion.cierres)
    }

    @Test
    fun `CA-6 sin conexion muestra el aviso y se puede reintentar`() {
        sesion.resultadoInicio = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertEquals(AppError.SinConexion().mensaje, vm.state.error)
        assertFalse(vm.state.enProceso)

        sesion.resultadoInicio = Result.success(Unit)
        vm.entrar()

        assertTrue(vm.state.ingresado)
        assertEquals(2, sesion.llamadasInicio.size)
    }

    @Test
    fun `CA-7 mientras espera la respuesta un segundo toque no repite la llamada`() {
        val bloqueo = CompletableDeferred<Unit>()
        sesion.bloqueoInicio = bloqueo
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()
        assertTrue(vm.state.enProceso)
        vm.entrar()
        assertEquals(1, sesion.llamadasInicio.size)

        bloqueo.complete(Unit)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.ingresado)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-8 correo sin confirmar muestra el mensaje de confirmar el correo`() {
        sesion.resultadoInicio = Result.failure(AppError.CorreoNoConfirmado())
        val vm = crearViewModel()
        vm.escribir()

        vm.entrar()

        assertEquals(AppError.CorreoNoConfirmado().mensaje, vm.state.error)
        assertFalse(vm.state.ingresado)
    }

    private companion object {
        const val CORREO = "jugador01@prueba.local"
        const val CLAVE = "clave-segura"
    }
}
