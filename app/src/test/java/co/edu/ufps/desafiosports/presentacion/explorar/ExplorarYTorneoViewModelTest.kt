package co.edu.ufps.desafiosports.presentacion.explorar

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.AccionDelTorneo
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.model.MiRelacion
import co.edu.ufps.desafiosports.domain.model.Pagina
import co.edu.ufps.desafiosports.domain.use_case.competicion.AbrirTorneoPorEnlaceUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ExplorarTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerDeportesUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import co.edu.ufps.desafiosports.testutil.FakeCreadorRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.FakeTorneosRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.detalleDePrueba
import co.edu.ufps.desafiosports.testutil.resumenDePrueba
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0005: Explorar y el detalle del torneo, vistos desde la pantalla. */
@OptIn(ExperimentalCoroutinesApi::class)
class ExplorarYTorneoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val torneos = FakeTorneosRepository()
    private val creador = FakeCreadorRepository()
    private val sesion = FakeSesionRepository()

    private fun explorar() = ExplorarViewModel(
        ExplorarTorneosUseCase(torneos),
        ObtenerDeportesUseCase(creador),
        AbrirTorneoPorEnlaceUseCase(torneos),
    )

    private fun torneo(id: String = "t1", enlace: String = "") = TorneoViewModel(
        torneoId = id,
        enlaceToken = enlace,
        obtenerTorneoUseCase = ObtenerTorneoUseCase(torneos),
        abrirTorneoPorEnlaceUseCase = AbrirTorneoPorEnlaceUseCase(torneos),
        observarSesionUseCase = ObservarSesionUseCase(sesion),
    )

    // ── Explorar ────────────────────────────────────────────────────────────

    @Test
    fun `CA-1 al abrir lista los torneos y los deportes`() {
        val vm = explorar()

        assertEquals(listOf("t1"), vm.state.torneos!!.map { it.id })
        assertEquals(3, vm.state.deportes.size)
        assertFalse(vm.state.cargando)
        assertEquals("explorar:::0", torneos.llamadas.first())
    }

    @Test
    fun `CA-2 el chip de deporte vuelve a pedir desde la primera pagina`() {
        val vm = explorar()

        vm.elegirDeporte("VOLEIBOL")

        assertEquals("explorar:VOLEIBOL::0", torneos.llamadas.last())
        assertEquals("VOLEIBOL", vm.state.deporte)
        assertTrue(vm.state.conFiltro)
    }

    @Test
    fun `CA-2 la busqueda espera a que se deje de escribir`() = runTest(mainDispatcherRule.dispatcher) {
        val vm = explorar()
        val antes = torneos.llamadas.size

        vm.onBusquedaChange("c")
        vm.onBusquedaChange("co")
        vm.onBusquedaChange("copa")
        advanceTimeBy(ExplorarViewModel.PAUSA_BUSQUEDA_MS + 1)

        // Solo la última búsqueda llega al backend.
        assertEquals(listOf("explorar::copa:0"), torneos.llamadas.drop(antes))
    }

    @Test
    fun `CA-1 ver mas suma la pagina siguiente sin repetir`() {
        torneos.paginas = mapOf(
            0 to Result.success(Pagina(listOf(resumenDePrueba("t1"), resumenDePrueba("t2")), 0, 2)),
            1 to Result.success(Pagina(listOf(resumenDePrueba("t2"), resumenDePrueba("t3")), 1, 2)),
        )
        val vm = explorar()
        assertTrue(vm.state.hayMas)

        vm.cargarMas()

        assertEquals(listOf("t1", "t2", "t3"), vm.state.torneos!!.map { it.id })
        assertFalse(vm.state.hayMas)
    }

    @Test
    fun `CA-1 sin conexion la primera carga muestra el error con reintentar`() {
        torneos.paginas = mapOf(0 to Result.failure(AppError.SinConexion()))

        val vm = explorar()

        assertTrue(vm.state.error!!.sinConexion)
        assertNull(vm.state.torneos)
    }

    @Test
    fun `CA-3 un enlace que no es de torneo se avisa sin llamar al backend`() {
        val vm = explorar()
        vm.pedirEnlace()
        vm.onEnlaceChange("hola")

        vm.abrirEnlace()

        assertEquals(AbrirTorneoPorEnlaceUseCase.MENSAJE_ENLACE_INVALIDO, vm.state.errorEnlace)
        assertTrue(torneos.llamadas.none { it.startsWith("enlace") })
        assertTrue(vm.state.pidiendoEnlace)
    }

    @Test
    fun `CA-3 un enlace valido abre el torneo con su token`() {
        val vm = explorar()
        vm.pedirEnlace()
        vm.onEnlaceChange("https://desafio.test/t/0e46a7c3324240e2868e5780fb7a6ba5")

        vm.abrirEnlace()

        assertEquals(TorneoPorAbrir("t1", "0e46a7c3324240e2868e5780fb7a6ba5"), vm.state.torneoPorAbrir)
        assertFalse(vm.state.pidiendoEnlace)
        vm.onTorneoAbierto()
        assertNull(vm.state.torneoPorAbrir)
    }

    // ── Detalle ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-4 sin sesion la franja invita a entrar`() {
        sesion.estado.value = EstadoSesion.Inactiva

        val vm = torneo()

        assertFalse(vm.state.conSesion)
        assertEquals(AccionDelTorneo.EntrarParaInscribirse, vm.state.accion)
    }

    @Test
    fun `CA-4 con sesion la franja sale de miRelacion`() {
        sesion.estado.value = EstadoSesion.Activa("jugador01@prueba.local")
        torneos.detalle = Result.success(detalleDePrueba(relacion = MiRelacion(administra = true, arbitra = false, delegadoDe = null, juegaEn = null)))

        val vm = torneo()

        assertTrue(vm.state.conSesion)
        assertEquals(AccionDelTorneo.Administra, vm.state.accion)
    }

    @Test
    fun `CA-3 abierto por enlace se vuelve a pedir por enlace`() {
        sesion.estado.value = EstadoSesion.Inactiva

        val vm = torneo(id = "", enlace = "0e46a7c3324240e2868e5780fb7a6ba5")
        vm.refrescar()

        assertEquals(listOf("enlace:0e46a7c3324240e2868e5780fb7a6ba5", "enlace:0e46a7c3324240e2868e5780fb7a6ba5"), torneos.llamadas)
        assertNotNull(vm.state.torneo)
    }

    @Test
    fun `un torneo privado o inexistente muestra el error y no la franja`() {
        sesion.estado.value = EstadoSesion.Inactiva
        torneos.detalle = Result.failure(AppError.NoEncontrado("No encontramos ese torneo."))

        val vm = torneo()

        assertEquals("No encontramos ese torneo.", vm.state.error!!.mensaje)
        assertNull(vm.state.accion)
    }

    @Test
    fun `un fallo al refrescar conserva el detalle y avisa`() {
        sesion.estado.value = EstadoSesion.Inactiva
        val vm = torneo()
        torneos.detalle = Result.failure(AppError.SinConexion())

        vm.refrescar()

        assertNotNull(vm.state.torneo)
        assertNotNull(vm.state.aviso)
    }
}
