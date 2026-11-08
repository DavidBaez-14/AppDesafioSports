package co.edu.ufps.desafiosports.presentacion.diagnostico

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.Configuracion
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.domain.use_case.sistema.ComprobarServidorUseCase
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeServidorRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.cuentaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0001 · CA-3, CA-4, CA-6 y CA-10 vistos desde la pantalla (ViewModel con fakes). */
class DiagnosticoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()
    private val cuentas = FakeCuentaRepository()
    private val servidor = FakeServidorRepository()
    private val configCompleta = Configuracion("https://x.supabase.co", "sb_publishable_x", "http://10.0.2.2:8082/")

    private fun crearViewModel(configuracion: Configuracion = configCompleta) = DiagnosticoViewModel(
        configuracion = configuracion,
        comprobarServidorUseCase = ComprobarServidorUseCase(servidor),
        observarSesionUseCase = ObservarSesionUseCase(sesion),
        iniciarSesionUseCase = IniciarSesionUseCase(sesion),
        obtenerCuentaUseCase = ObtenerCuentaUseCase(cuentas),
        cerrarSesionUseCase = CerrarSesionUseCase(sesion),
    )

    @Test
    fun `CA-4 al abrir comprueba el servidor y muestra que esta en linea`() {
        val vm = crearViewModel()

        assertEquals(EstadoComprobacion.EnLinea("UP"), vm.state.servidor)
    }

    @Test
    fun `CA-4 con el servidor caido muestra el mensaje de sin conexion`() {
        servidor.resultado = Result.failure(AppError.SinConexion())

        val vm = crearViewModel()

        val estado = vm.state.servidor
        assertTrue(estado is EstadoComprobacion.Fallo)
        assertEquals(AppError.SinConexion().mensaje, (estado as EstadoComprobacion.Fallo).mensaje)
    }

    @Test
    fun `CA-3 con configuracion incompleta lo avisa y no toca la sesion`() {
        val vm = crearViewModel(Configuracion("", "", "http://10.0.2.2:8082/"))

        assertEquals(listOf("SUPABASE_URL", "SUPABASE_PUBLISHABLE_KEY"), vm.state.faltantes)
        assertEquals(EstadoSesion.Inactiva, vm.state.sesion)
        assertFalse(sesion.fueObservada)
    }

    @Test
    fun `CA-6 iniciar sesion consulta la cuenta y la muestra`() {
        val vm = crearViewModel()
        vm.onCorreoChange("jugador01@prueba.local")
        vm.onClaveChange("12345678")

        vm.iniciarSesion()

        assertEquals(EstadoSesion.Activa("jugador01@prueba.local"), vm.state.sesion)
        assertEquals(cuentaDePrueba(), vm.state.cuenta)
        assertEquals("", vm.state.clave)
        assertNull(vm.state.error)
        assertFalse(vm.state.enProceso)
    }

    @Test
    fun `CA-10 un error del backend se muestra con su mensaje`() {
        cuentas.resultado = Result.failure(AppError.CuentaNoRegistrada("Completa tu registro para continuar."))
        val vm = crearViewModel()
        vm.onCorreoChange("jugador22@prueba.local")
        vm.onClaveChange("12345678")

        vm.iniciarSesion()

        assertNull(vm.state.cuenta)
        assertEquals("Completa tu registro para continuar.", vm.state.error)
    }

    @Test
    fun `CA-5 credenciales invalidas no inician sesion`() {
        sesion.resultadoInicio = Result.failure(AppError.CredencialesInvalidas())
        val vm = crearViewModel()
        vm.onCorreoChange("jugador01@prueba.local")
        vm.onClaveChange("clave-equivocada")

        vm.iniciarSesion()

        assertEquals(EstadoSesion.Inactiva, vm.state.sesion)
        assertEquals(AppError.CredencialesInvalidas().mensaje, vm.state.error)
        assertEquals(0, cuentas.llamadas)
    }

    @Test
    fun `cerrar sesion limpia la cuenta`() {
        val vm = crearViewModel()
        vm.onCorreoChange("jugador01@prueba.local")
        vm.onClaveChange("12345678")
        vm.iniciarSesion()

        vm.cerrarSesion()

        assertEquals(EstadoSesion.Inactiva, vm.state.sesion)
        assertNull(vm.state.cuenta)
    }
}
