package co.edu.ufps.desafiosports.presentacion.navigation

import co.edu.ufps.desafiosports.domain.model.Configuracion
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0002 · CA-1 (arranque) y CA-10 (cerrar sesión vuelve al login). */
class RaizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()
    private val configCompleta = Configuracion("https://x.supabase.co", "sb_publishable_x", "http://10.0.2.2:8082/")

    private fun crearViewModel(configuracion: Configuracion = configCompleta) =
        RaizViewModel(configuracion, ObservarSesionUseCase(sesion))

    @Test
    fun `CA-1 mientras se lee la sesion guardada no hay destino`() {
        sesion.estado.value = EstadoSesion.Cargando

        assertNull(crearViewModel().destinoInicial)
    }

    @Test
    fun `CA-1 sin sesion va al login`() {
        sesion.estado.value = EstadoSesion.Inactiva

        assertEquals(DestinoRaiz.LOGIN, crearViewModel().destinoInicial)
    }

    @Test
    fun `CA-1 con sesion va al inicio`() {
        sesion.estado.value = EstadoSesion.Activa("jugador01@prueba.local")

        assertEquals(DestinoRaiz.INICIO, crearViewModel().destinoInicial)
    }

    @Test
    fun `CA-1 con una sesion que no se pudo renovar va al inicio igual`() {
        sesion.estado.value = EstadoSesion.SinRenovar

        assertEquals(DestinoRaiz.INICIO, crearViewModel().destinoInicial)
    }

    @Test
    fun `CA-1 decide en cuanto termina de leer la sesion`() {
        sesion.estado.value = EstadoSesion.Cargando
        val vm = crearViewModel()

        sesion.estado.value = EstadoSesion.Inactiva

        assertEquals(DestinoRaiz.LOGIN, vm.destinoInicial)
    }

    @Test
    fun `CA-1 el destino inicial no cambia cuando despues se inicia sesion`() {
        val vm = crearViewModel()

        sesion.estado.value = EstadoSesion.Activa("jugador01@prueba.local")

        assertEquals(DestinoRaiz.LOGIN, vm.destinoInicial)
    }

    @Test
    fun `CA-1 con la configuracion incompleta va al diagnostico`() {
        val vm = crearViewModel(Configuracion("", "", "http://10.0.2.2:8082/"))

        assertEquals(DestinoRaiz.DIAGNOSTICO, vm.destinoInicial)
    }

    @Test
    fun `CA-10 al cerrar una sesion activa avisa que hay que volver al login`() {
        sesion.estado.value = EstadoSesion.Activa("jugador01@prueba.local")
        val vm = crearViewModel()

        sesion.estado.value = EstadoSesion.Inactiva

        assertTrue(vm.sesionCerrada)
        vm.onSesionCerradaAtendida()
        assertFalse(vm.sesionCerrada)
    }

    @Test
    fun `CA-10 abrir la app sin sesion no cuenta como cierre`() {
        sesion.estado.value = EstadoSesion.Inactiva

        assertFalse(crearViewModel().sesionCerrada)
    }
}
