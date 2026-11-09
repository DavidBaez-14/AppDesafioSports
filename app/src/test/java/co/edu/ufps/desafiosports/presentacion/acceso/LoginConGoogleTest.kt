package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionConGoogleUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0006 · CA-2 a CA-7: Continuar con Google visto desde el login (ViewModel con fakes). */
class LoginConGoogleTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()
    private val cuentas = FakeCuentaRepository()

    private fun crearViewModel() = LoginViewModel(
        iniciarSesionUseCase = IniciarSesionUseCase(sesion),
        iniciarSesionConGoogleUseCase = IniciarSesionConGoogleUseCase(sesion),
        obtenerCuentaUseCase = ObtenerCuentaUseCase(cuentas),
        cerrarSesionUseCase = CerrarSesionUseCase(sesion),
    )

    private val token = ResultadoGoogle.Token(idToken = "id-token-de-google", nonce = "nonce-crudo")

    @Test
    fun `CA-2 al tocar el boton queda en proceso y un segundo toque no abre otro selector`() {
        val vm = crearViewModel()

        assertTrue(vm.iniciarGoogle())
        assertTrue(vm.state.enProceso)
        assertFalse(vm.iniciarGoogle())
    }

    @Test
    fun `CA-3 cancelar el selector deja el login disponible sin aviso`() {
        val vm = crearViewModel()
        vm.iniciarGoogle()

        vm.onResultadoGoogle(ResultadoGoogle.Cancelado)

        assertFalse(vm.state.enProceso)
        assertNull(vm.state.error)
        assertTrue(sesion.llamadasGoogle.isEmpty())
    }

    @Test
    fun `CA-4 un fallo del selector muestra el aviso`() {
        val vm = crearViewModel()
        vm.iniciarGoogle()

        vm.onResultadoGoogle(ResultadoGoogle.Fallo("No hay una cuenta de Google en este teléfono."))

        assertEquals("No hay una cuenta de Google en este teléfono.", vm.state.error)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-5 token valido de una cuenta registrada inicia sesion con el nonce crudo y entra`() {
        val vm = crearViewModel()
        vm.iniciarGoogle()

        vm.onResultadoGoogle(token)

        assertEquals(listOf("id-token-de-google" to "nonce-crudo"), sesion.llamadasGoogle)
        assertTrue(vm.state.ingresado)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-6 token valido de una cuenta sin registro va a completar registro`() {
        cuentas.resultado = Result.failure(AppError.CuentaNoRegistrada("Completa tu registro."))
        val vm = crearViewModel()
        vm.iniciarGoogle()

        vm.onResultadoGoogle(token)

        assertTrue(vm.state.debeCompletarRegistro)
        assertFalse(vm.state.ingresado)
        assertEquals(0, sesion.cierres)
    }

    @Test
    fun `CA-6 cuenta bloqueada muestra el aviso y cierra la sesion`() {
        cuentas.resultado = Result.failure(AppError.CuentaBloqueada("Tu cuenta está bloqueada."))
        val vm = crearViewModel()
        vm.iniciarGoogle()

        vm.onResultadoGoogle(token)

        assertEquals("Tu cuenta está bloqueada.", vm.state.error)
        assertEquals(1, sesion.cierres)
        assertFalse(vm.state.ingresado)
    }

    @Test
    fun `CA-7 si Supabase rechaza el token muestra el aviso y libera el login`() {
        sesion.resultadoGoogle = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()
        vm.iniciarGoogle()

        vm.onResultadoGoogle(token)

        assertEquals(AppError.SinConexion().mensaje, vm.state.error)
        assertFalse(vm.state.enProceso)
        assertFalse(vm.state.ingresado)
    }
}
