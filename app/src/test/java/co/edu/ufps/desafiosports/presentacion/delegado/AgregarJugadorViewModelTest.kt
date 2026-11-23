package co.edu.ufps.desafiosports.presentacion.delegado

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.use_case.competicion.AgregarJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.mensajeNumeroOcupado
import co.edu.ufps.desafiosports.testutil.FakeDelegadoRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.miembro
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0015: agregar jugadores al plantel, visto desde la pantalla. */
class AgregarJugadorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeDelegadoRepository()

    init {
        repositorio.plantel = Result.success(listOf(miembro("p1", "Andrés Gómez", 1, delegado = true), miembro("p2", "Juan Castro", 10)))
    }

    private fun crear(exigeDocumento: Boolean = false) = AgregarJugadorViewModel(
        equipoTorneoId = "et1",
        equipo = "Halcones FC",
        exigeDocumento = exigeDocumento,
        obtenerPlantelUseCase = ObtenerPlantelUseCase(repositorio),
        agregarJugadorUseCase = AgregarJugadorUseCase(repositorio),
    )

    private fun agregados() = repositorio.llamadas.filter { it.startsWith("agregar") }

    @Test
    fun `CA-4 sin documento ni nombre no se puede agregar`() {
        val vm = crear()

        assertFalse(vm.state.sePuedeAgregar)
        vm.onCamiseta("5")
        assertFalse(vm.state.sePuedeAgregar)
        vm.onNombre("Sofía Moreno")
        assertTrue(vm.state.sePuedeAgregar)
    }

    @Test
    fun `CA-4 agregar avisa, vacia el formulario conservando el tipo y acumula los agregados`() {
        val vm = crear()

        vm.onTipo(TipoDocumento.TI)
        vm.onDocumento("1090123")
        vm.onCamiseta("7")
        vm.agregar()

        assertEquals(listOf("agregar:et1:TI:1090123:null:7"), agregados())
        assertFalse(vm.state.agregando)
        assertEquals("Cuenta 1090123", vm.state.aviso)
        assertEquals(TipoDocumento.TI, vm.state.borrador.tipoDocumento)
        assertEquals("", vm.state.borrador.documento)
        assertEquals("", vm.state.borrador.camiseta)

        vm.onAvisoMostrado()
        vm.onNombre("Sofía Moreno")
        vm.agregar()

        assertEquals("Sofía Moreno", vm.state.aviso)
        assertEquals(listOf("Cuenta 1090123", "Sofía Moreno"), vm.state.agregados)
    }

    @Test
    fun `CA-5 el numero de otro activo sale bajo la camiseta y no viaja`() {
        val vm = crear()

        vm.onNombre("Sofía Moreno")
        vm.onCamiseta("10")
        vm.agregar()

        assertTrue(agregados().isEmpty())
        assertEquals(mensajeNumeroOcupado(10, "Juan Castro"), vm.state.errorCamiseta)
        assertNull(vm.state.errorGeneral)

        vm.onCamiseta("11")
        assertNull(vm.state.errorCamiseta)
    }

    @Test
    fun `CA-5 sin el documento que el torneo exige el motivo sale bajo el documento`() {
        val vm = crear(exigeDocumento = true)

        vm.onNombre("Sofía Moreno")
        vm.agregar()

        assertTrue(agregados().isEmpty())
        assertEquals(AgregarJugadorUseCase.MENSAJE_DOCUMENTO_EXIGIDO, vm.state.errorDocumento)
        vm.onDocumento("1")
        assertNull(vm.state.errorDocumento)
    }

    @Test
    fun `CA-5 los codigos del backend van bajo su campo y el resto sobre el boton`() {
        val vm = crear()
        vm.onNombre("Sofía Moreno")

        repositorio.resultadoAgregar = Result.failure(AppError.Negocio("DOCUMENTO_REQUERIDO", "Este torneo exige documento."))
        vm.agregar()
        assertEquals("Este torneo exige documento.", vm.state.errorDocumento)

        repositorio.resultadoAgregar = Result.failure(AppError.Negocio("VALIDACION", "El documento no es válido."))
        vm.agregar()
        assertEquals("El documento no es válido.", vm.state.errorDocumento)

        repositorio.resultadoAgregar = Result.failure(AppError.Negocio("NOMBRE_REQUERIDO", "Escribe su nombre completo."))
        vm.agregar()
        assertEquals("Escribe su nombre completo.", vm.state.errorNombre)
        assertNull(vm.state.errorDocumento)

        repositorio.resultadoAgregar = Result.failure(AppError.Negocio("REGLA_NEGOCIO", "El jugador ya pertenece a un equipo en este torneo."))
        vm.agregar()
        assertEquals("El jugador ya pertenece a un equipo en este torneo.", vm.state.errorGeneral)
        assertNull(vm.state.errorNombre)

        // Lo escrito se conserva para corregirlo.
        assertEquals("Sofía Moreno", vm.state.borrador.nombre)
        assertTrue(vm.state.agregados.isEmpty())
    }

    @Test
    fun `CA-5 el doble toque no agrega dos veces y mientras viaja no se cambia lo escrito`() {
        val espera = CompletableDeferred<Unit>()
        repositorio.bloqueo = espera
        val vm = crear()

        vm.onNombre("Sofía Moreno")
        vm.agregar()
        vm.agregar()
        vm.onNombre("Otro nombre")
        assertTrue(vm.state.agregando)
        assertEquals("Sofía Moreno", vm.state.borrador.nombre)
        espera.complete(Unit)

        assertEquals(listOf("agregar:et1:null:null:Sofía Moreno:null"), agregados())
        assertFalse(vm.state.agregando)
        assertEquals(listOf("Sofía Moreno"), vm.state.agregados)
    }
}
