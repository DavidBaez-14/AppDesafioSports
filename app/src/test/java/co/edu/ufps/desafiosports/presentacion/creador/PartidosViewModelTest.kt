package co.edu.ufps.desafiosports.presentacion.creador

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.EstadoDePartidos
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.use_case.competicion.AsignarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.GenerarPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPersonalDeArbitrajeUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarArbitroDePartidoUseCase
import co.edu.ufps.desafiosports.testutil.FakeGestionTorneoRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.arbitroDePrueba
import co.edu.ufps.desafiosports.testutil.partidoDeTorneoDePrueba
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0011: los partidos de un torneo, vistos desde la pantalla. */
class PartidosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeGestionTorneoRepository()

    private fun crear() = PartidosTorneoViewModel(
        torneoId = "t1",
        torneoNombre = "Copa Prueba 2026",
        obtenerPartidosUseCase = ObtenerPartidosDeTorneoUseCase(repositorio),
        generarPartidosUseCase = GenerarPartidosUseCase(repositorio),
        obtenerPersonalUseCase = ObtenerPersonalDeArbitrajeUseCase(repositorio),
        asignarArbitroUseCase = AsignarArbitroUseCase(repositorio),
        quitarArbitroDePartidoUseCase = QuitarArbitroDePartidoUseCase(repositorio),
    )

    private fun sinPartidos(estado: EstadoTorneo = EstadoTorneo.PUBLICADO, aprobados: Int = 4) {
        repositorio.estado = Result.success(EstadoDePartidos(estado, aprobados, generados = false))
        repositorio.partidos = Result.success(emptyList())
    }

    private fun conPartidos(vararg ids: String) {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
        repositorio.partidos = Result.success(ids.map { partidoDeTorneoDePrueba(it) })
    }

    private fun llamadas(prefijo: String) = repositorio.llamadas.filter { it.startsWith(prefijo) }

    // ── Generar ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-6 sin partidos muestra el estado del torneo y no pide la lista ni los arbitros`() {
        sinPartidos(aprobados = 1)

        val vm = crear()

        assertEquals(1, vm.state.estado!!.equiposAprobados)
        assertFalse(vm.state.estado!!.puedeGenerar)
        assertTrue(vm.state.partidos.isEmpty())
        assertEquals(listOf("estado:t1"), repositorio.llamadas)

        // El botón está deshabilitado; aunque llegue el toque, no hace nada.
        vm.pedirGenerar()
        assertFalse(vm.state.confirmandoGenerar)
        assertEquals(listOf("estado:t1"), repositorio.llamadas)
    }

    @Test
    fun `CA-7 con las inscripciones abiertas pide confirmar, inicia el torneo y genera`() {
        sinPartidos(EstadoTorneo.PUBLICADO)
        val vm = crear()
        repositorio.alGenerar = { conPartidos("p1", "p2") }

        vm.pedirGenerar()
        assertTrue(vm.state.confirmandoGenerar)
        assertTrue(llamadas("iniciar").isEmpty())

        vm.confirmarGenerar()

        assertEquals(listOf("iniciar:t1"), llamadas("iniciar"))
        assertEquals(listOf("generar:t1"), llamadas("generar"))
        assertEquals(listOf("p1", "p2"), vm.state.partidos.map { it.id })
        assertTrue(vm.state.estado!!.generados)
        assertFalse(vm.state.generando)
        assertNull(vm.state.aviso)
    }

    @Test
    fun `CA-7 cancelar el dialogo no inicia el torneo`() {
        sinPartidos(EstadoTorneo.PUBLICADO)
        val vm = crear()

        vm.pedirGenerar()
        vm.cancelarGenerar()
        vm.confirmarGenerar()

        assertFalse(vm.state.confirmandoGenerar)
        assertTrue(llamadas("iniciar").isEmpty())
        assertTrue(llamadas("generar").isEmpty())
    }

    @Test
    fun `CA-7 con el torneo en curso genera sin dialogo y sin iniciarlo`() {
        sinPartidos(EstadoTorneo.EN_CURSO)
        val vm = crear()
        repositorio.alGenerar = { conPartidos("p1") }

        vm.pedirGenerar()

        assertFalse(vm.state.confirmandoGenerar)
        assertTrue(llamadas("iniciar").isEmpty())
        assertEquals(listOf("generar:t1"), llamadas("generar"))
        assertEquals(1, vm.state.partidos.size)
    }

    @Test
    fun `CA-7 el doble toque no genera dos veces`() {
        sinPartidos(EstadoTorneo.EN_CURSO)
        val vm = crear()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueo = bloqueo

        vm.pedirGenerar()
        vm.pedirGenerar()
        vm.refrescar()

        assertTrue(vm.state.generando)
        bloqueo.complete(Unit)

        assertEquals(1, llamadas("generar").size)
        assertFalse(vm.state.generando)
    }

    @Test
    fun `CA-7 si generar falla se ve el mensaje y la pantalla refleja el estado real del torneo`() {
        sinPartidos(EstadoTorneo.PUBLICADO)
        val vm = crear()
        repositorio.resultadoGenerar = Result.failure(
            AppError.Negocio("REGLA_NEGOCIO", "Hay 9 equipos aprobados pero la primera ronda (CUARTOS) solo tiene 8 cupos."),
        )

        // El torneo sí se inicia: la recarga lo trae en curso y sin partidos.
        repositorio.alIniciar = { repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 9, generados = false)) }

        vm.pedirGenerar()
        vm.confirmarGenerar()

        assertEquals("Hay 9 equipos aprobados pero la primera ronda (CUARTOS) solo tiene 8 cupos.", vm.state.aviso)
        assertTrue(vm.state.partidos.isEmpty())
        assertEquals(EstadoTorneo.EN_CURSO, vm.state.estado!!.estadoTorneo)
        assertFalse(vm.state.generando)

        // El segundo intento ya no pide confirmar ni vuelve a iniciar.
        vm.onAvisoMostrado()
        vm.pedirGenerar()
        assertFalse(vm.state.confirmandoGenerar)
        assertEquals(1, llamadas("iniciar").size)
    }

    // ── Lista ───────────────────────────────────────────────────────────────

    @Test
    fun `CA-8 con partidos los agrupa y pide los arbitros del torneo para saber cuales no tienen`() {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
        repositorio.partidos = Result.success(
            listOf(
                partidoDeTorneoDePrueba("j2", jornada = 2),
                partidoDeTorneoDePrueba("j1", jornada = 1, arbitros = listOf(ArbitroAsignado("c1", "Árbitro 1"))),
                partidoDeTorneoDePrueba("cerrado", jornada = 1, estado = EstadoPartido.FINALIZADO),
            ),
        )
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))

        val vm = crear()

        assertEquals(listOf(1, 2), vm.state.grupos.map { it.jornada })
        assertEquals(1, llamadas("arbitros").size)
        val porId = vm.state.partidos.associateBy { it.id }
        assertTrue(vm.state.sinArbitro(porId.getValue("j2")))
        assertFalse(vm.state.sinArbitro(porId.getValue("j1")))
        // Un partido cerrado ya no necesita árbitro.
        assertFalse(vm.state.sinArbitro(porId.getValue("cerrado")))
    }

    @Test
    fun `CA-8 si alguien arbitra todos los partidos ninguno esta sin arbitro`() {
        conPartidos("p1")
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1, todosLosPartidos = true)))

        val vm = crear()

        assertTrue(vm.state.hayArbitroGeneral)
        assertFalse(vm.state.sinArbitro(vm.state.partidos.single()))
    }

    // ── Asignar ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-9 marcar asigna al instante y el partido queda con lo que responde el backend`() {
        conPartidos("p1", "p2")
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1), arbitroDePrueba(2)))
        val vm = crear()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueo = bloqueo

        vm.abrirAsignacion(vm.state.partidos.first())
        assertEquals("p1", vm.state.partidoAsignando!!.id)
        vm.alternarArbitro(vm.state.arbitros!!.first())

        // Antes de que responda ya se ve marcado, con el nombre que había en la lista.
        assertEquals(listOf("Árbitro 1"), vm.state.partidoAsignando!!.arbitros.map { it.nombre })
        assertEquals(setOf("c1"), vm.state.enviando)
        bloqueo.complete(Unit)

        assertEquals(listOf("asignar:p1:c1"), llamadas("asignar"))
        assertEquals(listOf("Confirmado c1"), vm.state.partidoAsignando!!.arbitros.map { it.nombre })
        assertTrue(vm.state.enviando.isEmpty())
        // El otro partido no cambia.
        assertTrue(vm.state.partidos.last().arbitros.isEmpty())
    }

    @Test
    fun `CA-9 desmarcar quita al arbitro del partido`() {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
        repositorio.partidos = Result.success(listOf(partidoDeTorneoDePrueba("p1", arbitros = listOf(ArbitroAsignado("c1", "Árbitro 1")))))
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        val vm = crear()

        vm.abrirAsignacion(vm.state.partidos.single())
        vm.alternarArbitro(vm.state.arbitros!!.single())

        assertEquals(listOf("desasignar:p1:c1"), llamadas("desasignar"))
        assertTrue(vm.state.partidoAsignando!!.arbitros.isEmpty())
    }

    @Test
    fun `CA-9 si el arbitro juega en el partido no queda marcado y el mensaje sale en la hoja`() {
        conPartidos("p1")
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        repositorio.resultadoAsignacion = Result.failure(
            AppError.Negocio("ARBITRO_EN_PLANTILLA", "Esa persona juega en uno de los equipos del partido: no puede arbitrarlo."),
        )
        val vm = crear()

        vm.abrirAsignacion(vm.state.partidos.single())
        vm.alternarArbitro(vm.state.arbitros!!.single())

        assertTrue(vm.state.partidoAsignando!!.arbitros.isEmpty())
        assertEquals("Esa persona juega en uno de los equipos del partido: no puede arbitrarlo.", vm.state.errorAsignacion)
        // No va al aviso de la pantalla: la hoja lo taparía.
        assertNull(vm.state.aviso)
        assertTrue(vm.state.enviando.isEmpty())
    }

    @Test
    fun `CA-9 el doble toque sobre el mismo arbitro no envia dos cambios`() {
        conPartidos("p1")
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1), arbitroDePrueba(2)))
        val vm = crear()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueo = bloqueo

        vm.abrirAsignacion(vm.state.partidos.single())
        val (uno, dos) = vm.state.arbitros!!
        vm.alternarArbitro(uno)
        vm.alternarArbitro(uno)
        // Otro árbitro sí se puede marcar mientras tanto.
        vm.alternarArbitro(dos)

        assertEquals(listOf("asignar:p1:c1", "asignar:p1:c2"), llamadas("asignar"))
        bloqueo.complete(Unit)
        assertTrue(vm.state.enviando.isEmpty())
    }

    @Test
    fun `CA-9 quien arbitra todos los partidos no se asigna, y un partido cerrado no abre la hoja`() {
        repositorio.estado = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
        repositorio.partidos = Result.success(
            listOf(partidoDeTorneoDePrueba("abierto"), partidoDeTorneoDePrueba("cerrado", estado = EstadoPartido.FINALIZADO)),
        )
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1, todosLosPartidos = true)))
        val vm = crear()

        vm.abrirAsignacion(vm.state.partidos.first { it.id == "cerrado" })
        assertNull(vm.state.asignando)

        vm.abrirAsignacion(vm.state.partidos.first { it.id == "abierto" })
        vm.alternarArbitro(vm.state.arbitros!!.single())

        assertTrue(llamadas("asignar").isEmpty())
        vm.cerrarAsignacion()
        assertNull(vm.state.asignando)
    }

    @Test
    fun `CA-10 sin arbitros en el torneo la hoja queda vacia, y al volver de invitar se piden otra vez`() {
        conPartidos("p1")
        val vm = crear()

        vm.abrirAsignacion(vm.state.partidos.single())
        assertTrue(vm.state.arbitros!!.isEmpty())
        assertEquals(1, llamadas("arbitros").size)

        // "Invitar árbitros" cierra la hoja y abre la otra pantalla; al volver ya hay uno.
        vm.cerrarAsignacion()
        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        vm.alVolver()

        assertEquals(2, llamadas("arbitros").size)
        assertEquals(listOf("c1"), vm.state.arbitros!!.map { it.cuentaId })
    }

    @Test
    fun `CA-10 si los arbitros no cargan la hoja muestra el error y deja reintentar`() {
        conPartidos("p1")
        repositorio.arbitros = Result.failure(AppError.SinConexion())
        val vm = crear()

        vm.abrirAsignacion(vm.state.partidos.single())
        assertNull(vm.state.arbitros)
        assertEquals(AppError.SinConexion().mensaje, vm.state.errorAsignacion)
        // Sin saber quién arbitra no se afirma que el partido esté sin árbitro.
        assertFalse(vm.state.sinArbitro(vm.state.partidos.single()))

        repositorio.arbitros = Result.success(listOf(arbitroDePrueba(1)))
        vm.cargarArbitros()

        assertEquals(1, vm.state.arbitros!!.size)
        assertNull(vm.state.errorAsignacion)
    }

    // ── Errores ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-13 un torneo que no administra muestra el aviso con volver`() {
        repositorio.estado = Result.failure(AppError.AccesoDenegado("No tienes permisos para esta acción."))

        val vm = crear()

        assertTrue(vm.state.sinPermiso)
        assertNull(vm.state.estado)
    }

    @Test
    fun `CA-13 sin conexion ofrece reintentar, y si falla al actualizar conserva los partidos`() {
        repositorio.estado = Result.failure(AppError.SinConexion())
        val vm = crear()
        assertTrue(vm.state.error!!.sinConexion)

        conPartidos("p1")
        vm.cargar()
        assertNull(vm.state.error)
        assertEquals(1, vm.state.partidos.size)

        repositorio.partidos = Result.failure(AppError.SinConexion())
        vm.refrescar()

        assertEquals(1, vm.state.partidos.size)
        assertEquals(AppError.SinConexion().mensaje, vm.state.aviso)
        assertFalse(vm.state.refrescando)
    }
}
