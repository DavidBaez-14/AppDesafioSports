package co.edu.ufps.desafiosports.presentacion.delegado

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarCamisetaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDelPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.mensajeNumeroOcupado
import co.edu.ufps.desafiosports.testutil.FakeDelegadoRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.miembro
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0015: el plantel del equipo, visto desde la pantalla de su delegado. */
class PlantelViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeDelegadoRepository()

    private val delegado = miembro("p1", "Andrés Gómez", 1, delegado = true)
    private val juan = miembro("p2", "Juan Castro", 10)
    private val sofia = miembro("p3", "Sofía Moreno", null, cuenta = false)
    private val mateo = miembro("p4", "Mateo Jiménez", 7, activo = false)

    init {
        repositorio.plantel = Result.success(listOf(sofia, juan, mateo, delegado))
    }

    private fun crear() = PlantelViewModel(
        equipoTorneoId = "et1",
        equipo = "Halcones FC",
        torneo = "Copa Prueba 2026",
        exigeDocumento = false,
        obtenerPlantelUseCase = ObtenerPlantelUseCase(repositorio),
        cambiarCamisetaUseCase = CambiarCamisetaUseCase(repositorio),
        quitarDelPlantelUseCase = QuitarDelPlantelUseCase(repositorio),
    )

    private fun pedidas() = repositorio.llamadas.count { it.startsWith("plantel") }
    private fun cambios() = repositorio.llamadas.filterNot { it.startsWith("plantel") }

    @Test
    fun `CA-2 al abrir se pide el plantel y se ven los activos en orden y los retirados aparte`() {
        val vm = crear()

        assertEquals(listOf("plantel:et1"), repositorio.llamadas)
        assertFalse(vm.state.cargando)
        assertNull(vm.state.error)
        assertEquals(listOf("p1", "p2", "p3"), vm.state.plantel!!.activos.map { it.personaId })
        assertEquals(listOf("p4"), vm.state.plantel!!.retirados.map { it.personaId })
        assertTrue(vm.state.editable)
    }

    @Test
    fun `CA-3 si no carga se ofrece reintentar y se recupera`() {
        repositorio.plantel = Result.failure(AppError.SinConexion())
        val vm = crear()

        assertNotNull(vm.state.error)
        assertTrue(vm.state.error!!.sinConexion)
        assertFalse(vm.state.sinPermiso)

        repositorio.plantel = Result.success(listOf(delegado, juan))
        vm.cargar()

        assertNull(vm.state.error)
        assertEquals(2, vm.state.plantel!!.activos.size)
    }

    @Test
    fun `CA-3 si no es su delegado se ofrece volver en vez de reintentar`() {
        repositorio.plantel = Result.failure(AppError.Negocio("NO_DELEGADO_DE_LA_INSCRIPCION", "No eres el delegado de esta inscripción."))
        val conCodigo = crear()
        repositorio.plantel = Result.failure(AppError.AccesoDenegado("No tienes permisos para esta acción."))
        val sinCodigo = crear()

        assertTrue(conCodigo.state.sinPermiso)
        assertEquals("No eres el delegado de esta inscripción.", conCodigo.state.error!!.mensaje)
        assertTrue(sinCodigo.state.sinPermiso)
    }

    @Test
    fun `CA-3 si falla al refrescar se conserva lo que se ve y se avisa`() {
        val vm = crear()
        repositorio.plantel = Result.failure(AppError.SinConexion())

        vm.refrescar()

        assertFalse(vm.state.refrescando)
        assertNull(vm.state.error)
        assertEquals(3, vm.state.plantel!!.activos.size)
        assertTrue(vm.state.aviso is AvisoPlantel.Fallo)
        vm.onAvisoMostrado()
        assertNull(vm.state.aviso)
    }

    @Test
    fun `CA-4 al volver de agregar se pide de nuevo sin indicador y se ve al agregado`() {
        val vm = crear()
        repositorio.plantel = Result.success(listOf(sofia, juan, mateo, delegado, miembro("p5", "Ana Pérez", 5)))

        vm.alVolver()

        assertEquals(2, pedidas())
        assertFalse(vm.state.cargando)
        assertFalse(vm.state.refrescando)
        assertEquals(listOf("p1", "p5", "p2", "p3"), vm.state.plantel!!.activos.map { it.personaId })
    }

    @Test
    fun `CA-6 guardar un numero nuevo actualiza la fila y cierra el dialogo sin volver a pedir el plantel`() {
        val vm = crear()

        vm.abrirJugador(sofia)
        assertEquals("p3", vm.state.editando)
        assertEquals("", vm.state.camisetaTexto)
        vm.onCamiseta("8a9")
        assertEquals("89", vm.state.camisetaTexto)
        vm.onCamiseta("8")
        vm.guardarCamiseta()

        assertEquals(listOf("camiseta:et1:p3:8"), cambios())
        assertEquals(1, pedidas())
        assertNull(vm.state.editando)
        assertEquals(8, vm.state.plantel!!.miembros.first { it.personaId == "p3" }.numeroCamiseta)
    }

    @Test
    fun `CA-6 un numero de otro activo no viaja y el motivo queda en el dialogo`() {
        val vm = crear()

        vm.abrirJugador(sofia)
        vm.onCamiseta("10")
        vm.guardarCamiseta()

        assertTrue(cambios().isEmpty())
        assertEquals("p3", vm.state.editando)
        assertEquals(mensajeNumeroOcupado(10, "Juan Castro"), vm.state.errorEdicion)

        vm.onCamiseta("11")
        assertNull(vm.state.errorEdicion)
    }

    @Test
    fun `CA-6 el rechazo del backend queda en el dialogo`() {
        repositorio.resultadoCamiseta = Result.failure(AppError.Negocio("REGLA_NEGOCIO", "El número 9 ya está asignado a otro jugador activo."))
        val vm = crear()

        vm.abrirJugador(juan)
        assertEquals("10", vm.state.camisetaTexto)
        vm.onCamiseta("9")
        vm.guardarCamiseta()

        assertFalse(vm.state.guardando)
        assertEquals("p2", vm.state.editando)
        assertEquals("El número 9 ya está asignado a otro jugador activo.", vm.state.errorEdicion)
        assertEquals(10, vm.state.plantel!!.miembros.first { it.personaId == "p2" }.numeroCamiseta)
    }

    @Test
    fun `CA-6 el doble toque en guardar envia una sola vez`() {
        val espera = CompletableDeferred<Unit>()
        repositorio.bloqueo = espera
        val vm = crear()

        vm.abrirJugador(sofia)
        vm.onCamiseta("8")
        vm.guardarCamiseta()
        vm.guardarCamiseta()
        assertTrue(vm.state.guardando)
        espera.complete(Unit)

        assertEquals(listOf("camiseta:et1:p3:8"), cambios())
        assertFalse(vm.state.guardando)
    }

    @Test
    fun `CA-7 quitar pide confirmacion, pasa a retirados y avisa`() {
        val vm = crear()

        vm.abrirJugador(juan)
        vm.pedirQuitar()
        assertTrue(vm.state.confirmandoQuitar)
        assertTrue(cambios().isEmpty())
        vm.confirmarQuitar()

        assertEquals(listOf("quitar:et1:p2"), cambios())
        assertNull(vm.state.editando)
        assertFalse(vm.state.confirmandoQuitar)
        assertEquals(listOf("p1", "p3"), vm.state.plantel!!.activos.map { it.personaId })
        assertEquals(listOf("p2", "p4"), vm.state.plantel!!.retirados.map { it.personaId })
        assertEquals(AvisoPlantel.Quitado("Juan Castro"), vm.state.aviso)
    }

    @Test
    fun `CA-7 al delegado no se le pide confirmar y no viaja nada`() {
        val vm = crear()

        vm.abrirJugador(delegado)
        vm.pedirQuitar()
        vm.confirmarQuitar()

        assertFalse(vm.state.confirmandoQuitar)
        assertTrue(cambios().isEmpty())
        assertEquals("p1", vm.state.editando)
    }

    @Test
    fun `CA-7 si quitar falla vuelve al dialogo del jugador con el motivo`() {
        repositorio.resultadoQuitar = Result.failure(AppError.SinConexion())
        val vm = crear()

        vm.abrirJugador(juan)
        vm.pedirQuitar()
        vm.confirmarQuitar()

        assertFalse(vm.state.confirmandoQuitar)
        assertEquals("p2", vm.state.editando)
        assertEquals(AppError.SinConexion().mensaje, vm.state.errorEdicion)
        assertTrue(vm.state.plantel!!.activos.any { it.personaId == "p2" })
    }

    @Test
    fun `CA-8 con el torneo terminado tocar a un jugador no abre nada`() {
        repositorio.plantel = Result.success(
            listOf(miembro("p1", "Andrés Gómez", 1, delegado = true, torneoFinalizado = true), miembro("p2", "Juan Castro", 10, torneoFinalizado = true)),
        )
        val vm = crear()

        assertFalse(vm.state.editable)
        vm.abrirJugador(vm.state.plantel!!.activos.last())

        assertNull(vm.state.editando)
    }
}
