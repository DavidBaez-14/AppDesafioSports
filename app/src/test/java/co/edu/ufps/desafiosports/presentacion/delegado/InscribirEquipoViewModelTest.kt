package co.edu.ufps.desafiosports.presentacion.delegado

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.use_case.competicion.InscribirEquipoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisEquiposBaseUseCase
import co.edu.ufps.desafiosports.testutil.FakeDelegadoRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0014: inscribir un equipo, visto desde la pantalla. */
class InscribirEquipoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeDelegadoRepository()
    private val halcones = EquipoPropio("e1", "Halcones FC")
    private val pumas = EquipoPropio("e2", "Pumas FC")

    private fun crear(enlaceToken: String? = null) = InscribirEquipoViewModel(
        torneoId = "t1",
        torneoNombre = "Copa Prueba 2026",
        enlaceToken = enlaceToken,
        cuota = 50_000,
        exigeDocumento = false,
        obtenerMisEquiposBaseUseCase = ObtenerMisEquiposBaseUseCase(repositorio),
        inscribirEquipoUseCase = InscribirEquipoUseCase(repositorio),
    )

    private fun inscripciones() = repositorio.llamadas.filter { it.startsWith("inscribir") }

    @Test
    fun `CA-2 con un solo equipo queda elegido y se puede inscribir de una`() {
        repositorio.equipos = Result.success(listOf(halcones))
        val vm = crear()

        assertFalse(vm.state.cargando)
        assertEquals("e1", vm.state.elegido)
        assertFalse(vm.state.nuevo)
        assertTrue(vm.state.sePuedeInscribir)
    }

    @Test
    fun `CA-2 con varios equipos no se elige ninguno hasta que la persona toca uno`() {
        repositorio.equipos = Result.success(listOf(pumas, halcones))
        val vm = crear()

        assertNull(vm.state.elegido)
        assertFalse(vm.state.sePuedeInscribir)
        vm.inscribir()
        assertTrue(inscripciones().isEmpty())

        vm.elegir(halcones)
        assertTrue(vm.state.sePuedeInscribir)
    }

    @Test
    fun `CA-2 sin equipos arranca en crear uno nuevo, y sin nombre no se puede inscribir`() {
        val vm = crear()

        assertTrue(vm.state.nuevo)
        assertFalse(vm.state.sePuedeInscribir)
        vm.onNombre("Tiburones FC")
        assertTrue(vm.state.sePuedeInscribir)
    }

    @Test
    fun `CA-2 si no cargan los equipos se ofrece reintentar y se recupera`() {
        repositorio.equipos = Result.failure(AppError.SinConexion())
        val vm = crear()

        assertNotNull(vm.state.error)
        assertTrue(vm.state.error!!.sinConexion)
        assertFalse(vm.state.sePuedeInscribir)

        repositorio.equipos = Result.success(listOf(halcones))
        vm.cargar()

        assertNull(vm.state.error)
        assertEquals("e1", vm.state.elegido)
    }

    @Test
    fun `CA-3 inscribir el equipo elegido muestra la confirmacion, con el enlace del torneo`() {
        repositorio.equipos = Result.success(listOf(pumas, halcones))
        val vm = crear(enlaceToken = "tok-enlace")

        vm.elegir(pumas)
        vm.inscribir()

        assertEquals(listOf("inscribir:t1:e2:null:tok-enlace"), inscripciones())
        assertEquals("Equipo e2", vm.state.hecha!!.equipo)
        assertFalse(vm.state.inscribiendo)
        assertFalse(vm.state.sePuedeInscribir)
    }

    @Test
    fun `CA-4 cambiar a equipo nuevo e inscribirlo envia el nombre`() {
        repositorio.equipos = Result.success(listOf(halcones))
        val vm = crear()

        vm.elegirNuevo()
        assertNull(vm.state.elegido)
        vm.onNombre("Tiburones FC")
        vm.inscribir()

        assertEquals(listOf("inscribir:t1:null:Tiburones FC:null"), inscripciones())
        assertEquals("Tiburones FC", vm.state.hecha!!.equipo)
    }

    @Test
    fun `CA-5 un nombre repetido no viaja y el motivo sale bajo el campo`() {
        repositorio.equipos = Result.success(listOf(halcones))
        val vm = crear()

        vm.elegirNuevo()
        vm.onNombre("HALCONES FC")
        vm.inscribir()

        assertTrue(inscripciones().isEmpty())
        assertEquals(InscribirEquipoUseCase.mensajeRepetido("HALCONES FC"), vm.state.errorNombre)
        assertNull(vm.state.errorInscribir)

        // Escribir otra vez borra el error.
        vm.onNombre("Halcones B")
        assertNull(vm.state.errorNombre)
    }

    @Test
    fun `CA-5 el rechazo del backend por el nombre tambien sale bajo el campo`() {
        repositorio.resultadoInscribir = Result.failure(AppError.Negocio("EQUIPO_DUPLICADO", "Ya tienes un equipo llamado \"Pumas\"."))
        val vm = crear()

        vm.onNombre("Pumas")
        vm.inscribir()

        assertEquals("Ya tienes un equipo llamado \"Pumas\".", vm.state.errorNombre)
        assertNull(vm.state.hecha)
    }

    @Test
    fun `CA-6 cualquier otro rechazo sale sobre el boton y se puede volver a intentar`() {
        repositorio.equipos = Result.success(listOf(halcones))
        repositorio.resultadoInscribir = Result.failure(AppError.Negocio("CUPOS_AGOTADOS", "Este torneo ya no tiene cupos."))
        val vm = crear()

        vm.inscribir()

        assertEquals("Este torneo ya no tiene cupos.", vm.state.errorInscribir)
        assertNull(vm.state.errorNombre)
        assertNull(vm.state.hecha)
        assertTrue(vm.state.sePuedeInscribir)
    }

    @Test
    fun `CA-6 el doble toque no inscribe dos veces, y mientras viaja no se cambia la eleccion`() {
        repositorio.equipos = Result.success(listOf(halcones, pumas))
        repositorio.bloqueo = CompletableDeferred()
        val vm = crear()

        vm.elegir(halcones)
        vm.inscribir()
        vm.inscribir()
        vm.elegir(pumas)
        assertTrue(vm.state.inscribiendo)
        assertEquals("e1", vm.state.elegido)
        repositorio.bloqueo!!.complete(Unit)

        assertEquals(1, inscripciones().size)
        assertNotNull(vm.state.hecha)
        // Ya inscrito, otro toque no hace nada.
        vm.inscribir()
        assertEquals(1, inscripciones().size)
    }
}
