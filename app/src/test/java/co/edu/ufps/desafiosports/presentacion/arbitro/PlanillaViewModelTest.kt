package co.edu.ufps.desafiosports.presentacion.arbitro

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.Lado
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.use_case.competicion.AlinearEquipoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.AlinearJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CerrarPartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlanillaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDeAlineacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarEventoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RegistrarEventoUseCase
import co.edu.ufps.desafiosports.testutil.FakeArbitrajeRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.eventoDePrueba
import co.edu.ufps.desafiosports.testutil.planillaDePrueba
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0009 · CA-4 a CA-14: la planilla vista desde la pantalla. */
class PlanillaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeArbitrajeRepository()

    private fun crearViewModel() = PlanillaViewModel(
        partidoId = "p1",
        obtenerPlanillaUseCase = ObtenerPlanillaUseCase(repositorio),
        registrarEventoUseCase = RegistrarEventoUseCase(repositorio),
        quitarEventoUseCase = QuitarEventoUseCase(repositorio),
        alinearJugadorUseCase = AlinearJugadorUseCase(repositorio),
        quitarDeAlineacionUseCase = QuitarDeAlineacionUseCase(repositorio),
        alinearEquipoUseCase = AlinearEquipoUseCase(repositorio),
        cerrarPartidoUseCase = CerrarPartidoUseCase(repositorio),
    )

    private fun acciones() = repositorio.llamadas.filter { it != "planilla" }

    @Test
    fun `CA-4 registrar un evento lo envia al equipo elegido y recarga la planilla`() {
        val vm = crearViewModel()
        val gol = eventoDePrueba("ev-1")
        repositorio.resultadoEvento = Result.success(gol)
        repositorio.alActuar = {
            repositorio.planilla = Result.success(
                planillaDePrueba(estado = EstadoPartido.EN_CURSO, marcador = Marcador(0, 1), eventos = listOf(gol)),
            )
        }

        vm.elegirLado(Lado.VISITANTE)
        vm.abrirSeleccion("GOL")
        assertEquals("GOL", vm.state.tipoEnSeleccion)
        vm.registrar(vm.state.planilla!!.visitante.jugadores.first())

        assertEquals(listOf("evento:j9:eq-visita:GOL"), acciones())
        assertEquals(2, repositorio.pedidosDePlanilla)
        assertNull(vm.state.tipoEnSeleccion)
        assertFalse(vm.state.enAccion)
        // El marcador y la lista son los que respondió el backend, no un cálculo de la app.
        assertEquals(Marcador(0, 1), vm.state.planilla!!.marcador)
        assertEquals(listOf("ev-1"), vm.state.planilla!!.eventos.map { it.id })
        assertEquals(AvisoPlanilla.EventoRegistrado(gol), vm.state.aviso)
    }

    @Test
    fun `CA-5 deshacer borra el evento recien creado`() {
        val vm = crearViewModel()

        vm.deshacer("ev-1")

        assertEquals(listOf("quitar-evento:ev-1"), acciones())
        assertEquals(AvisoPlanilla.EventoQuitado, vm.state.aviso)
        assertEquals(2, repositorio.pedidosDePlanilla)
    }

    @Test
    fun `CA-6 quitar pide confirmacion y cancelar no borra nada`() {
        val evento = eventoDePrueba("ev-7")
        repositorio.planilla = Result.success(planillaDePrueba(eventos = listOf(evento)))
        val vm = crearViewModel()

        vm.pedirQuitar(evento)
        assertEquals(evento, vm.state.eventoPorQuitar)
        vm.cancelarQuitar()

        assertNull(vm.state.eventoPorQuitar)
        assertTrue(acciones().isEmpty())

        vm.pedirQuitar(evento)
        vm.confirmarQuitar()

        assertEquals(listOf("quitar-evento:ev-7"), acciones())
        assertNull(vm.state.eventoPorQuitar)
        assertEquals(AvisoPlanilla.EventoQuitado, vm.state.aviso)
    }

    @Test
    fun `CA-7 marcar alinea, desmarcar quita y a quien no es apto no se le alinea`() {
        val vm = crearViewModel()
        val (alineado, sinAlinear, noApto) = vm.state.planilla!!.local.jugadores

        vm.abrirAlineacion()
        assertTrue(vm.state.alineacionAbierta)
        vm.cambiarAlineacion(sinAlinear)
        vm.cambiarAlineacion(alineado)
        vm.cambiarAlineacion(noApto)

        assertEquals(listOf("alinear:j2:eq-local", "desalinear:j1"), acciones())
        // Cambiar la alineación no muestra aviso: el cambio se ve en la propia lista.
        assertNull(vm.state.aviso)
    }

    @Test
    fun `CA-7 la alineacion se abre en el equipo que estaba elegido y se puede cambiar`() {
        val vm = crearViewModel()

        vm.elegirLado(Lado.VISITANTE)
        vm.abrirAlineacion()
        assertEquals(Lado.VISITANTE, vm.state.ladoAlineacion)
        vm.cambiarAlineacion(vm.state.planilla!!.visitante.jugadores.first())
        vm.elegirLadoAlineacion(Lado.LOCAL)
        vm.cerrarAlineacion()

        assertEquals(listOf("alinear:j9:eq-visita"), acciones())
        assertEquals(Lado.LOCAL, vm.state.ladoAlineacion)
        assertFalse(vm.state.alineacionAbierta)
    }

    @Test
    fun `CA-8 alinear a todos lo pide para el equipo de la hoja`() {
        val vm = crearViewModel()

        vm.abrirAlineacion()
        vm.elegirLadoAlineacion(Lado.VISITANTE)
        vm.alinearATodos()

        assertEquals(listOf("alinear-todos:eq-visita"), acciones())
        assertEquals(2, repositorio.pedidosDePlanilla)
    }

    @Test
    fun `CA-9 cerrar pide confirmacion y deja la planilla de solo lectura`() {
        val vm = crearViewModel()
        repositorio.alActuar = {
            repositorio.planilla = Result.success(
                planillaDePrueba(editable = false, estado = EstadoPartido.FINALIZADO, marcador = Marcador(3, 2)),
            )
        }

        vm.pedirCierre()
        assertTrue(vm.state.confirmandoCierre)
        assertTrue(acciones().isEmpty())
        vm.cancelarCierre()
        assertFalse(vm.state.confirmandoCierre)
        vm.confirmarCierre()
        assertTrue("sin la confirmación abierta no se cierra", acciones().isEmpty())

        vm.pedirCierre()
        vm.confirmarCierre()

        assertEquals(listOf("cerrar"), acciones())
        assertFalse(vm.state.confirmandoCierre)
        assertFalse(vm.state.cerrando)
        assertFalse(vm.state.editable)
        assertEquals(EstadoPartido.FINALIZADO, vm.state.planilla!!.partido.estado)
        assertEquals(AvisoPlanilla.PartidoCerrado, vm.state.aviso)
    }

    @Test
    fun `CA-10 con la planilla de solo lectura ninguna accion llega al backend`() {
        repositorio.planilla = Result.success(planillaDePrueba(editable = false, estado = EstadoPartido.FINALIZADO))
        val vm = crearViewModel()
        val jugador = vm.state.planilla!!.local.jugadores[1]

        vm.abrirSeleccion("GOL")
        vm.pedirQuitar(eventoDePrueba("ev-1"))
        vm.pedirCierre()
        vm.deshacer("ev-1")
        vm.abrirAlineacion()
        vm.cambiarAlineacion(jugador)
        vm.alinearATodos()

        assertFalse(vm.state.editable)
        assertNull(vm.state.tipoEnSeleccion)
        assertNull(vm.state.eventoPorQuitar)
        assertFalse(vm.state.confirmandoCierre)
        assertTrue(acciones().isEmpty())
        // La alineación sí se puede abrir para consultarla.
        assertTrue(vm.state.alineacionAbierta)
    }

    @Test
    fun `CA-10 un partido sin el cruce definido tampoco se edita`() {
        repositorio.planilla = Result.success(planillaDePrueba(editable = true, conCruce = false))
        val vm = crearViewModel()

        vm.abrirSeleccion("GOL")

        assertFalse(vm.state.editable)
        assertNull(vm.state.tipoEnSeleccion)
    }

    @Test
    fun `CA-11 si la accion falla la planilla no cambia y se ve el mensaje del backend`() {
        val vm = crearViewModel()
        val antes = vm.state.planilla
        repositorio.resultadoAccion = Result.failure(AppError.Negocio("REGLA_NEGOCIO", "Jugador no apto: multa pendiente"))

        vm.abrirAlineacion()
        vm.cambiarAlineacion(vm.state.planilla!!.local.jugadores[1])

        assertEquals(antes, vm.state.planilla)
        assertEquals(AvisoPlanilla.Fallo("Jugador no apto: multa pendiente"), vm.state.aviso)
        assertFalse(vm.state.enAccion)

        vm.onAvisoMostrado()
        assertNull(vm.state.aviso)
    }

    @Test
    fun `CA-11 sin conexion al registrar no se vuelve a pedir la planilla`() {
        val vm = crearViewModel()
        repositorio.resultadoEvento = Result.failure(AppError.SinConexion())

        vm.abrirSeleccion("GOL")
        vm.registrar(vm.state.planilla!!.local.jugadores.first())

        assertEquals(1, repositorio.pedidosDePlanilla)
        assertEquals(AvisoPlanilla.Fallo(AppError.SinConexion().mensaje), vm.state.aviso)
        assertTrue(vm.state.editable)
    }

    @Test
    fun `CA-11 si la accion se guarda pero la planilla no se puede volver a pedir, lo avisa`() {
        val vm = crearViewModel()
        val antes = vm.state.planilla
        repositorio.alActuar = { repositorio.planilla = Result.failure(AppError.SinConexion()) }

        vm.abrirSeleccion("AMARILLA")
        vm.registrar(vm.state.planilla!!.local.jugadores.first())

        assertEquals(listOf("evento:j1:eq-local:AMARILLA"), acciones())
        assertEquals(AvisoPlanilla.SinActualizar, vm.state.aviso)
        assertEquals(antes, vm.state.planilla)
    }

    @Test
    fun `CA-12 si no arbitra el partido muestra el aviso con volver`() {
        repositorio.planilla = Result.failure(
            AppError.Negocio("NO_ARBITRO_DEL_PARTIDO", "No tienes permisos para arbitrar este partido."),
        )

        val vm = crearViewModel()

        assertNull(vm.state.planilla)
        assertTrue(vm.state.sinPermiso)
        assertEquals("No tienes permisos para arbitrar este partido.", vm.state.error!!.mensaje)
        assertFalse(vm.state.cargando)
    }

    @Test
    fun `CA-15 sin conexion al abrir ofrece reintentar y se recupera`() {
        repositorio.planilla = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()

        assertTrue(vm.state.error!!.sinConexion)
        assertFalse(vm.state.sinPermiso)

        repositorio.planilla = Result.success(planillaDePrueba())
        vm.cargar()

        assertNull(vm.state.error)
        assertTrue(vm.state.editable)
    }

    @Test
    fun `CA-15 si falla al deslizar conserva la planilla y avisa`() {
        val vm = crearViewModel()
        repositorio.planilla = Result.failure(AppError.SinConexion())

        vm.refrescar()

        assertTrue(vm.state.planilla != null)
        assertNull(vm.state.error)
        assertFalse(vm.state.refrescando)
        assertEquals(AvisoPlanilla.Fallo(AppError.SinConexion().mensaje), vm.state.aviso)
    }

    @Test
    fun `CA-14 el doble toque no envia dos veces`() {
        val vm = crearViewModel()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueoEvento = bloqueo
        val jugador = vm.state.planilla!!.local.jugadores.first()

        vm.abrirSeleccion("GOL")
        vm.registrar(jugador)
        assertTrue(vm.state.enAccion)

        // Mientras el primero viaja: ni otro evento, ni quitar, ni cerrar, ni alinear.
        vm.abrirSeleccion("ROJA")
        vm.registrar(jugador)
        vm.pedirQuitar(eventoDePrueba("ev-1"))
        vm.pedirCierre()
        vm.alinearATodos()
        vm.refrescar()

        assertNull(vm.state.tipoEnSeleccion)
        assertNull(vm.state.eventoPorQuitar)
        assertFalse(vm.state.confirmandoCierre)

        bloqueo.complete(Unit)

        assertEquals(listOf("evento:j1:eq-local:GOL"), acciones())
        assertFalse(vm.state.enAccion)
    }
}
