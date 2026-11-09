package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.DatosRegistro
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CompletarRegistroUseCase
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0003 · CA-8 a CA-11: Completar registro vista desde la pantalla. */
class CompletarRegistroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cuentas = FakeCuentaRepository()
    private val sesion = FakeSesionRepository()

    private fun crearViewModel() = CompletarRegistroViewModel(CompletarRegistroUseCase(cuentas), CerrarSesionUseCase(sesion))

    private fun CompletarRegistroViewModel.escribir(nombres: String = "Andrés", apellidos: String = "Peña") {
        onNombresChange(nombres)
        onApellidosChange(apellidos)
    }

    @Test
    fun `CA-8 nombres vacios muestran el error bajo nombres sin llamar al backend`() {
        val vm = crearViewModel()
        vm.escribir(nombres = " ")

        vm.terminar()

        assertNotNull(vm.state.errorNombres)
        assertNull(vm.state.errorApellidos)
        assertTrue(cuentas.registros.isEmpty())
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-8 documento a medias muestra el error bajo documento`() {
        val vm = crearViewModel()
        vm.escribir()
        vm.onTipoDocumentoChange(TipoDocumento.CC)

        vm.terminar()

        assertNotNull(vm.state.errorDocumento)
        assertTrue(cuentas.registros.isEmpty())
    }

    @Test
    fun `CA-8 escribir de nuevo borra el error del campo`() {
        val vm = crearViewModel()
        vm.escribir(nombres = "")
        vm.terminar()

        vm.onNombresChange("Andrés")

        assertNull(vm.state.errorNombres)
    }

    @Test
    fun `CA-9 datos validos registran y avisan que hay que ir al inicio`() {
        val vm = crearViewModel()
        vm.escribir()
        vm.onTipoDocumentoChange(TipoDocumento.TI)
        vm.onNumeroDocumentoChange("1090123456")

        vm.terminar()

        assertTrue(vm.state.registrado)
        assertEquals(listOf(DatosRegistro("Andrés", "Peña", TipoDocumento.TI, "1090123456")), cuentas.registros)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-9 al atender el registro el evento se apaga`() {
        val vm = crearViewModel()
        vm.escribir()
        vm.terminar()

        vm.onRegistroAtendido()

        assertFalse(vm.state.registrado)
    }

    @Test
    fun `CA-10 documento ya registrado muestra el mensaje del backend y se queda`() {
        val mensaje = "Ya existe una cuenta con este documento."
        cuentas.resultadoRegistro = Result.failure(AppError.Negocio("DOCUMENTO_REGISTRADO", mensaje))
        val vm = crearViewModel()
        vm.escribir()

        vm.terminar()

        assertEquals(mensaje, vm.state.error)
        assertFalse(vm.state.registrado)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-11 salir cierra la sesion`() {
        val vm = crearViewModel()

        vm.salir()

        assertEquals(1, sesion.cierres)
    }
}
