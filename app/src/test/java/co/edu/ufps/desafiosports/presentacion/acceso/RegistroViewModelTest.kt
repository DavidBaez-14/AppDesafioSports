package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.model.ResultadoRegistro
import co.edu.ufps.desafiosports.domain.use_case.identidad.RegistrarseUseCase
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

/** MOV-0003 · CA-2 a CA-5: Crear cuenta vista desde la pantalla. */
class RegistroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()

    private fun crearViewModel() = RegistroViewModel(RegistrarseUseCase(sesion))

    private fun RegistroViewModel.escribir(correo: String = CORREO, clave: String = CLAVE) {
        onCorreoChange(correo)
        onClaveChange(clave)
    }

    @Test
    fun `CA-2 correo mal escrito muestra el error bajo el correo sin llamar a Supabase`() {
        val vm = crearViewModel()
        vm.escribir(correo = "nueva-cuenta.co")

        vm.crearCuenta()

        assertNotNull(vm.state.errorCorreo)
        assertNull(vm.state.errorClave)
        assertTrue(sesion.llamadasRegistro.isEmpty())
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-2 contrasena corta muestra el error bajo la contrasena`() {
        val vm = crearViewModel()
        vm.escribir(clave = "1234")

        vm.crearCuenta()

        assertNotNull(vm.state.errorClave)
        assertTrue(sesion.llamadasRegistro.isEmpty())
    }

    @Test
    fun `CA-3 con confirmacion de correo va a revisa tu correo`() {
        val vm = crearViewModel()
        vm.escribir()

        vm.crearCuenta()

        assertEquals(ResultadoRegistro.CONFIRMAR_CORREO, vm.state.destino)
        assertFalse(vm.state.enProceso)
        assertEquals("", vm.state.clave)
    }

    @Test
    fun `CA-4 si Supabase deja la sesion abierta va a completar registro`() {
        sesion.resultadoRegistro = Result.success(ResultadoRegistro.SESION_ACTIVA)
        val vm = crearViewModel()
        vm.escribir()

        vm.crearCuenta()

        assertEquals(ResultadoRegistro.SESION_ACTIVA, vm.state.destino)
    }

    @Test
    fun `CA-4 al atender el destino el evento se apaga`() {
        val vm = crearViewModel()
        vm.escribir()
        vm.crearCuenta()

        vm.onDestinoAtendido()

        assertNull(vm.state.destino)
    }

    @Test
    fun `CA-5 correo ya registrado muestra el aviso y se queda`() {
        val mensaje = "Ya existe una cuenta con este correo. Entra o recupera tu contraseña."
        sesion.resultadoRegistro = Result.failure(AppError.Negocio("user_already_exists", mensaje))
        val vm = crearViewModel()
        vm.escribir()

        vm.crearCuenta()

        assertEquals(mensaje, vm.state.error)
        assertNull(vm.state.destino)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-5 contrasena debil va bajo el campo contrasena`() {
        sesion.resultadoRegistro = Result.failure(AppError.Validacion("Esa contraseña es muy fácil de adivinar.", CampoValidado.CLAVE))
        val vm = crearViewModel()
        vm.escribir()

        vm.crearCuenta()

        assertEquals("Esa contraseña es muy fácil de adivinar.", vm.state.errorClave)
        assertNull(vm.state.destino)
    }

    @Test
    fun `CA-5 mientras espera un segundo toque no repite la llamada`() {
        val bloqueo = CompletableDeferred<Unit>()
        sesion.bloqueoRegistro = bloqueo
        val vm = crearViewModel()
        vm.escribir()

        vm.crearCuenta()
        assertTrue(vm.state.enProceso)
        vm.crearCuenta()
        bloqueo.complete(Unit)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, sesion.llamadasRegistro.size)
        assertEquals(ResultadoRegistro.CONFIRMAR_CORREO, vm.state.destino)
    }

    private companion object {
        const val CORREO = "nueva@cuenta.co"
        const val CLAVE = "clave-segura"
    }
}
