package co.edu.ufps.desafiosports.presentacion.creador

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarTodosLosPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CancelarInvitacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.InvitarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPersonalDeArbitrajeUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarArbitroUseCase
import co.edu.ufps.desafiosports.testutil.FakeGestionTorneoRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.arbitroDePrueba
import co.edu.ufps.desafiosports.testutil.invitacionDePrueba
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0011: los árbitros de un torneo, vistos desde la pantalla. */
class ArbitrosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeGestionTorneoRepository()

    private fun crear() = ArbitrosTorneoViewModel(
        torneoId = "t1",
        torneoNombre = "Copa Prueba 2026",
        obtenerPersonalUseCase = ObtenerPersonalDeArbitrajeUseCase(repositorio),
        invitarArbitroUseCase = InvitarArbitroUseCase(repositorio),
        cancelarInvitacionUseCase = CancelarInvitacionUseCase(repositorio),
        quitarArbitroUseCase = QuitarArbitroUseCase(repositorio),
        cambiarTodosLosPartidosUseCase = CambiarTodosLosPartidosUseCase(repositorio),
    )

    private fun acciones() = repositorio.llamadas.filterNot { it.startsWith("arbitros:") || it.startsWith("invitaciones:") }

    @Test
    fun `CA-2 al abrir muestra los arbitros y las invitaciones pendientes`() {
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1), arbitroDePrueba(2)))
        repositorio.invitaciones = Result.success(listOf(invitacionDePrueba("i1")))

        val vm = crear()

        assertEquals(listOf("r1", "r2"), vm.state.personal!!.arbitros.map { it.rolId })
        assertEquals(listOf("i1"), vm.state.personal!!.invitaciones.map { it.id })
        assertFalse(vm.state.cargando)
        assertEquals("Copa Prueba 2026", vm.state.torneo)
    }

    @Test
    fun `CA-2 sin arbitros ni invitaciones queda vacio y sin error`() {
        val vm = crear()

        assertTrue(vm.state.personal!!.vacio)
        assertNull(vm.state.error)
    }

    // ── Invitar ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-3 invitar sin correo crea la invitacion, la suma a pendientes y ofrece compartirla`() {
        repositorio.invitaciones = Result.success(listOf(invitacionDePrueba("vieja")))
        val vm = crear()

        vm.pedirInvitacion()
        vm.confirmarInvitar()

        assertEquals(listOf("invitar:t1:null"), acciones())
        assertFalse(vm.state.invitando)
        assertEquals(listOf("i-nueva", "vieja"), vm.state.personal!!.invitaciones.map { it.id })
        assertEquals("i-nueva", vm.state.porCompartir!!.id)

        vm.onCompartido()
        assertNull(vm.state.porCompartir)
    }

    @Test
    fun `CA-3 invitar con correo lo envia limpio`() {
        val vm = crear()

        vm.pedirInvitacion()
        vm.onCorreoChange("  Ana@Correo.com ")
        vm.confirmarInvitar()

        assertEquals(listOf("invitar:t1:ana@correo.com"), acciones())
    }

    @Test
    fun `CA-3 un correo mal escrito no viaja y el error sale bajo el campo`() {
        val vm = crear()

        vm.pedirInvitacion()
        vm.onCorreoChange("ana@correo")
        vm.confirmarInvitar()

        assertTrue(acciones().isEmpty())
        assertTrue(vm.state.invitando)
        assertEquals(InvitarArbitroUseCase.MENSAJE_CORREO, vm.state.errorCorreo)

        vm.onCorreoChange("ana@correo.com")
        assertNull(vm.state.errorCorreo)
    }

    @Test
    fun `CA-3 una invitacion duplicada deja el dialogo abierto con el mensaje del backend`() {
        repositorio.resultadoInvitar = Result.failure(AppError.Negocio("INVITACION_DUPLICADA", "Ya hay una invitación pendiente para ese correo y rol."))
        val vm = crear()

        vm.pedirInvitacion()
        vm.onCorreoChange("ana@correo.com")
        vm.confirmarInvitar()

        assertTrue(vm.state.invitando)
        assertEquals("ana@correo.com", vm.state.correo)
        assertEquals("Ya hay una invitación pendiente para ese correo y rol.", vm.state.errorCorreo)
        assertNull(vm.state.porCompartir)
        assertFalse(vm.state.enviandoInvitacion)
    }

    @Test
    fun `CA-3 el doble toque no crea dos invitaciones`() {
        val vm = crear()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueo = bloqueo

        vm.pedirInvitacion()
        vm.confirmarInvitar()
        vm.confirmarInvitar()
        vm.cancelarInvitar()

        assertTrue(vm.state.enviandoInvitacion)
        assertTrue(vm.state.invitando)
        bloqueo.complete(Unit)

        assertEquals(1, acciones().size)
        assertEquals(1, vm.state.personal!!.invitaciones.size)
    }

    // ── Todos los partidos ──────────────────────────────────────────────────

    @Test
    fun `CA-4 el interruptor cambia al instante y envia el valor`() {
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        val vm = crear()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueo = bloqueo

        vm.cambiarTodosLosPartidos(vm.state.personal!!.arbitros.single(), true)

        // Antes de que responda el backend ya se ve marcado.
        assertTrue(vm.state.personal!!.arbitros.single().todosLosPartidos)
        assertEquals("r1", vm.state.ocupado)
        bloqueo.complete(Unit)

        assertEquals(listOf("todos:t1:c1:true"), acciones())
        assertTrue(vm.state.personal!!.arbitros.single().todosLosPartidos)
        assertNull(vm.state.ocupado)
    }

    @Test
    fun `CA-4 si el backend lo rechaza el interruptor vuelve a como estaba`() {
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1, todosLosPartidos = true)))
        repositorio.resultadoAccion = Result.failure(AppError.Negocio("NO_ES_ARBITRO_DEL_TORNEO", "Esa cuenta no es árbitro de este torneo."))
        val vm = crear()

        vm.cambiarTodosLosPartidos(vm.state.personal!!.arbitros.single(), false)

        assertTrue(vm.state.personal!!.arbitros.single().todosLosPartidos)
        assertEquals(AvisoArbitros.Fallo("Esa cuenta no es árbitro de este torneo."), vm.state.aviso)
    }

    // ── Quitar y cancelar ───────────────────────────────────────────────────

    @Test
    fun `CA-5 quitar pide confirmar, y al confirmar lo saca de la lista`() {
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1, "Ana Ruiz"), arbitroDePrueba(2, "Beto Díaz")))
        val vm = crear()
        val ana = vm.state.personal!!.arbitros.first()

        vm.pedirQuitar(ana)
        assertEquals(ana, vm.state.quitando)
        assertTrue(acciones().isEmpty())

        vm.confirmarQuitar()

        assertEquals(listOf("quitar:t1:r1"), acciones())
        assertEquals(listOf("r2"), vm.state.personal!!.arbitros.map { it.rolId })
        assertEquals(AvisoArbitros.Quitado("Ana Ruiz"), vm.state.aviso)
        assertNull(vm.state.quitando)
    }

    @Test
    fun `CA-5 cancelar el dialogo no quita a nadie, y si quitar falla la lista no cambia`() {
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        val vm = crear()
        val arbitro = vm.state.personal!!.arbitros.single()

        vm.pedirQuitar(arbitro)
        vm.cancelarQuitar()
        vm.confirmarQuitar()
        assertTrue(acciones().isEmpty())

        repositorio.resultadoAccion = Result.failure(AppError.SinConexion())
        vm.pedirQuitar(arbitro)
        vm.confirmarQuitar()

        assertEquals(1, vm.state.personal!!.arbitros.size)
        assertTrue(vm.state.aviso is AvisoArbitros.Fallo)
    }

    @Test
    fun `CA-5 cancelar una invitacion la saca de pendientes`() {
        repositorio.invitaciones = Result.success(listOf(invitacionDePrueba("i1"), invitacionDePrueba("i2")))
        val vm = crear()

        vm.cancelarInvitacion(vm.state.personal!!.invitaciones.first { it.id == "i1" })

        assertEquals(listOf("cancelar:t1:i1"), acciones())
        assertEquals(listOf("i2"), vm.state.personal!!.invitaciones.map { it.id })
        assertEquals(AvisoArbitros.InvitacionCancelada, vm.state.aviso)

        vm.onAvisoMostrado()
        assertNull(vm.state.aviso)
    }

    // ── Errores ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-13 un torneo que no administra muestra el aviso con volver`() {
        repositorio.arbitros = Result.failure(AppError.Negocio("NO_ADMIN_DEL_TORNEO", "No administras este torneo."))

        val vm = crear()

        assertTrue(vm.state.sinPermiso)
        assertEquals("No administras este torneo.", vm.state.error!!.mensaje)
        assertNull(vm.state.personal)
    }

    @Test
    fun `CA-13 sin conexion ofrece reintentar, y si falla al actualizar conserva la lista`() {
        repositorio.arbitros = Result.failure(AppError.SinConexion())
        val vm = crear()

        assertTrue(vm.state.error!!.sinConexion)
        assertFalse(vm.state.sinPermiso)

        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        vm.cargar()
        assertNull(vm.state.error)
        assertNotNull(vm.state.personal)

        repositorio.invitaciones = Result.failure(AppError.SinConexion())
        vm.refrescar()

        assertEquals(1, vm.state.personal!!.arbitros.size)
        assertNull(vm.state.error)
        assertTrue(vm.state.aviso is AvisoArbitros.Fallo)
        assertFalse(vm.state.refrescando)
    }
}
