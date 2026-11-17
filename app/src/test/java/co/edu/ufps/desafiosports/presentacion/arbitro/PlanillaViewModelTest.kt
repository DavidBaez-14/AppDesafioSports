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
import co.edu.ufps.desafiosports.domain.use_case.competicion.ReabrirPartidoUseCase
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
        reabrirPartidoUseCase = ReabrirPartidoUseCase(repositorio),
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

    // ── Regresión del 14-nov: pasar lista con el servidor lento ────────────────────────────────
    // Cada toque esperaba dos viajes al servidor con todas las casillas bloqueadas: parecía que
    // no se podía marcar ni desmarcar a nadie.

    private fun PlanillaViewModel.jugador(personaId: String) =
        (state.planilla!!.local.jugadores + state.planilla!!.visitante.jugadores).first { it.personaId == personaId }

    private fun PlanillaViewModel.alineado(personaId: String): Boolean = jugador(personaId).alineado

    @Test
    fun `CA-7 la casilla cambia al instante, sin esperar al servidor`() {
        val vm = crearViewModel()
        val servidor = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = servidor

        vm.abrirAlineacion()
        vm.cambiarAlineacion(vm.jugador("j2"))

        assertTrue("La casilla debe verse marcada ya", vm.alineado("j2"))
        assertEquals(2, vm.state.planilla!!.local.alineados)
        assertEquals(setOf("j2"), vm.state.alineando)
        // Las demás casillas siguen libres: no hay una acción que bloquee la hoja.
        assertFalse(vm.state.enAccion)

        servidor.complete(Unit)

        assertTrue(vm.state.alineando.isEmpty())
        assertEquals(listOf("alinear:j2:eq-local"), acciones())
    }

    @Test
    fun `CA-7 se marcan varios seguidos sin esperar y se envian en orden`() {
        val vm = crearViewModel()
        val servidor = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = servidor

        vm.abrirAlineacion()
        vm.cambiarAlineacion(vm.jugador("j2"))
        vm.cambiarAlineacion(vm.jugador("j1"))
        vm.elegirLadoAlineacion(Lado.VISITANTE)
        vm.cambiarAlineacion(vm.jugador("j9"))

        assertTrue(vm.alineado("j2"))
        assertFalse(vm.alineado("j1"))
        assertTrue(vm.alineado("j9"))
        assertEquals(listOf("alinear:j2:eq-local"), acciones())

        // El servidor ya trae los tres cambios cuando se le vuelve a pedir la planilla.
        repositorio.planilla = Result.success(vm.state.planilla!!)
        servidor.complete(Unit)

        assertEquals(listOf("alinear:j2:eq-local", "desalinear:j1", "alinear:j9:eq-visita"), acciones())
        assertTrue(vm.state.alineando.isEmpty())
        // Una sola recarga al terminar la tanda, no una por toque.
        assertEquals(2, repositorio.pedidosDePlanilla)
        assertTrue(vm.alineado("j9"))
    }

    @Test
    fun `CA-7 desmarcar a quien se acaba de marcar se envia despues y queda sin marcar`() {
        val vm = crearViewModel()
        val servidor = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = servidor

        vm.cambiarAlineacion(vm.jugador("j2"))
        vm.cambiarAlineacion(vm.jugador("j2"))

        assertFalse(vm.alineado("j2"))
        servidor.complete(Unit)

        assertEquals(listOf("alinear:j2:eq-local", "desalinear:j2"), acciones())
        assertFalse(vm.alineado("j2"))
    }

    @Test
    fun `CA-7 marcar y desmarcar antes de que salga no envia nada`() {
        val vm = crearViewModel()
        val servidor = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = servidor

        vm.cambiarAlineacion(vm.jugador("j2"))
        vm.elegirLadoAlineacion(Lado.VISITANTE)
        vm.cambiarAlineacion(vm.jugador("j9"))
        vm.cambiarAlineacion(vm.jugador("j9"))
        servidor.complete(Unit)

        assertEquals(listOf("alinear:j2:eq-local"), acciones())
        assertFalse(vm.alineado("j9"))
    }

    @Test
    fun `CA-7 si el servidor rechaza el cambio la casilla vuelve y el motivo se ve en la hoja`() {
        val vm = crearViewModel()
        repositorio.resultadoAccion = Result.failure(AppError.Negocio("JUGADOR_NO_APTO", "Jugador no apto: tiene una multa pendiente."))

        vm.abrirAlineacion()
        vm.cambiarAlineacion(vm.jugador("j2"))

        assertFalse(vm.alineado("j2"))
        assertEquals("Jugador no apto: tiene una multa pendiente.", vm.state.errorAlineacion)
        assertTrue(vm.state.alineando.isEmpty())

        // El siguiente toque limpia el aviso.
        repositorio.resultadoAccion = Result.success(Unit)
        vm.cambiarAlineacion(vm.jugador("j2"))
        assertNull(vm.state.errorAlineacion)
    }

    @Test
    fun `CA-7 sin conexion se devuelven todas las casillas pendientes`() {
        val vm = crearViewModel()
        val servidor = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = servidor
        repositorio.resultadoAccion = Result.failure(AppError.SinConexion())

        vm.cambiarAlineacion(vm.jugador("j2"))
        vm.cambiarAlineacion(vm.jugador("j1"))
        servidor.complete(Unit)

        assertFalse(vm.alineado("j2"))
        assertTrue(vm.alineado("j1"))
        assertEquals(listOf("alinear:j2:eq-local"), acciones())
        assertTrue(vm.state.errorAlineacion!!.isNotBlank())
    }

    @Test
    fun `CA-7 un toque mientras se vuelve a pedir la planilla tambien se envia`() {
        val vm = crearViewModel()
        vm.cambiarAlineacion(vm.jugador("j2"))
        assertTrue(vm.state.alineando.isEmpty())

        // El segundo cambio se guarda y la recarga de la planilla se queda esperando.
        val recarga = CompletableDeferred<Unit>()
        repositorio.bloqueoPlanilla = recarga
        vm.cambiarAlineacion(vm.jugador("j1"))
        vm.elegirLadoAlineacion(Lado.VISITANTE)
        vm.cambiarAlineacion(vm.jugador("j9"))

        assertEquals(setOf("j9"), vm.state.alineando)
        repositorio.bloqueoPlanilla = null
        recarga.complete(Unit)

        assertEquals(listOf("alinear:j2:eq-local", "desalinear:j1", "alinear:j9:eq-visita"), acciones())
        assertTrue(vm.state.alineando.isEmpty())
    }

    @Test
    fun `CA-7 alinear a todos espera a que terminen los cambios sueltos`() {
        val vm = crearViewModel()
        val servidor = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = servidor

        vm.cambiarAlineacion(vm.jugador("j2"))
        vm.alinearATodos()

        assertEquals(listOf("alinear:j2:eq-local"), acciones())
        servidor.complete(Unit)
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
        // El motivo va dentro de la hoja de alineación: el aviso de la pantalla queda tapado por ella.
        assertEquals("Jugador no apto: multa pendiente", vm.state.errorAlineacion)
        assertNull(vm.state.aviso)
        assertFalse(vm.state.enAccion)

        vm.cerrarAlineacion()
        assertNull(vm.state.errorAlineacion)
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

    // ── MOV-0012 ────────────────────────────────────────────────────────────

    @Test
    fun `MOV-0012 CA-2 sin nadie alineado la hoja del evento lleva a pasar lista del equipo elegido`() {
        val vm = crearViewModel()

        // El visitante de la planilla de prueba no tiene a nadie alineado.
        vm.elegirLado(Lado.VISITANTE)
        vm.abrirSeleccion("GOL")
        assertTrue(vm.state.planilla!!.visitante.candidatos().isEmpty())

        vm.irAAlineacion()

        assertNull(vm.state.tipoEnSeleccion)
        assertTrue(vm.state.alineacionAbierta)
        assertEquals(Lado.VISITANTE, vm.state.ladoAlineacion)
        assertTrue(acciones().isEmpty())
    }

    @Test
    fun `MOV-0012 CA-4 el rechazo del backend por no estar alineado se ve y la planilla se vuelve a pedir`() {
        repositorio.resultadoEvento = Result.failure(
            AppError.Negocio("JUGADOR_NO_ALINEADO", "Ese jugador no está en la alineación de su equipo. Alinéalo antes de anotarle algo."),
        )
        val vm = crearViewModel()
        val antes = repositorio.pedidosDePlanilla

        vm.abrirSeleccion("GOL")
        vm.registrar(vm.state.planilla!!.local.jugadores.first())

        val aviso = vm.state.aviso as AvisoPlanilla.Fallo
        assertTrue(aviso.mensaje.startsWith("Ese jugador no está en la alineación"))
        assertEquals(antes + 1, repositorio.pedidosDePlanilla)
    }

    @Test
    fun `MOV-0012 CA-3 a un expulsado no se le cambia la alineacion`() {
        val base = planillaDePrueba()
        repositorio.planilla = Result.success(
            base.copy(local = base.local.copy(jugadores = base.local.jugadores.map { if (it.personaId == "j1") it.copy(expulsado = true) else it })),
        )
        val vm = crearViewModel()

        assertFalse(vm.state.planilla!!.local.candidatos().any { it.personaId == "j1" })
    }

    @Test
    fun `MOV-0012 CA-6 quien organiza reabre el partido cerrado, con confirmacion`() {
        repositorio.planilla = Result.success(planillaDePrueba(editable = false, estado = EstadoPartido.FINALIZADO).copy(puedeReabrir = true))
        val vm = crearViewModel()
        assertFalse(vm.state.editable)
        assertTrue(vm.state.puedeReabrir)

        vm.pedirReabrir()
        assertTrue(vm.state.confirmandoReabrir)
        vm.cancelarReabrir()
        assertTrue(acciones().isEmpty())

        repositorio.alActuar = {
            repositorio.planilla = Result.success(planillaDePrueba(editable = true, estado = EstadoPartido.EN_CURSO))
        }
        vm.pedirReabrir()
        vm.confirmarReabrir()

        assertEquals(listOf("reabrir"), acciones())
        assertTrue(vm.state.editable)
        assertFalse(vm.state.puedeReabrir)
        assertFalse(vm.state.reabriendo)
        assertEquals(AvisoPlanilla.PartidoReabierto, vm.state.aviso)
    }

    @Test
    fun `MOV-0012 CA-6 un arbitro no reabre, ni nadie reabre un partido abierto`() {
        // Cerrado, visto por un árbitro: el backend no le da el permiso.
        repositorio.planilla = Result.success(planillaDePrueba(editable = false, estado = EstadoPartido.FINALIZADO))
        val arbitro = crearViewModel()
        arbitro.pedirReabrir()
        arbitro.confirmarReabrir()
        assertFalse(arbitro.state.puedeReabrir)
        assertFalse(arbitro.state.confirmandoReabrir)

        // Abierto: no hay nada que reabrir aunque el dato llegue en true.
        repositorio.planilla = Result.success(planillaDePrueba().copy(puedeReabrir = true))
        val organizador = crearViewModel()
        organizador.pedirReabrir()
        assertFalse(organizador.state.puedeReabrir)
        assertFalse(organizador.state.confirmandoReabrir)

        assertTrue(acciones().isEmpty())
    }

    @Test
    fun `MOV-0012 CA-6 si reabrir falla el partido sigue cerrado y se ve el mensaje`() {
        repositorio.planilla = Result.success(planillaDePrueba(editable = false, estado = EstadoPartido.FINALIZADO).copy(puedeReabrir = true))
        repositorio.resultadoAccion = Result.failure(AppError.AccesoDenegado("No tienes permisos para esta acción."))
        val vm = crearViewModel()

        vm.pedirReabrir()
        vm.confirmarReabrir()

        assertFalse(vm.state.editable)
        assertEquals("No tienes permisos para esta acción.", (vm.state.aviso as AvisoPlanilla.Fallo).mensaje)
    }
}
