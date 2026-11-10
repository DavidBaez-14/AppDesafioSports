package co.edu.ufps.desafiosports.presentacion.arbitro

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.testutil.FakeArbitrajeRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.partidoDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** MOV-0009 · CA-1, CA-2 y CA-15: la sección Árbitro vista desde la pantalla. */
class SeccionArbitroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val hoy = LocalDate.of(2026, 11, 12)
    private val repositorio = FakeArbitrajeRepository()

    private fun crearViewModel() = SeccionArbitroViewModel(ObtenerPartidosDeArbitroUseCase(repositorio) { hoy })

    @Test
    fun `CA-1 al abrir carga los partidos repartidos en sus bloques`() {
        repositorio.partidos = Result.success(
            listOf(
                partidoDePrueba("hoy", hoy.atTime(16, 0)),
                partidoDePrueba("manana", hoy.plusDays(1).atTime(16, 0)),
                partidoDePrueba("jugado", hoy.minusDays(2).atTime(16, 0), EstadoPartido.FINALIZADO, Marcador(2, 2)),
            ),
        )

        val vm = crearViewModel()

        assertFalse(vm.state.cargando)
        assertNull(vm.state.error)
        val partidos = vm.state.partidos!!
        assertEquals(listOf("hoy"), partidos.hoy.map { it.id })
        assertEquals(listOf("manana"), partidos.proximos.map { it.id })
        assertEquals(listOf("jugado"), partidos.jugados.map { it.id })
    }

    @Test
    fun `CA-2 sin partidos asignados la lista queda vacia y sin error`() {
        val vm = crearViewModel()

        assertTrue(vm.state.partidos!!.vacio)
        assertNull(vm.state.error)
        assertFalse(vm.state.cargando)
    }

    @Test
    fun `CA-15 sin conexion muestra el error con reintentar y al reintentar se recupera`() {
        repositorio.partidos = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()

        assertNull(vm.state.partidos)
        assertTrue(vm.state.error!!.sinConexion)
        assertFalse(vm.state.cargando)

        repositorio.partidos = Result.success(listOf(partidoDePrueba("hoy", hoy.atTime(16, 0))))
        vm.cargar()

        assertNull(vm.state.error)
        assertEquals(1, vm.state.partidos!!.hoy.size)
    }

    @Test
    fun `CA-15 si falla una actualizacion conserva lo que habia y avisa`() {
        repositorio.partidos = Result.success(listOf(partidoDePrueba("hoy", hoy.atTime(16, 0))))
        val vm = crearViewModel()

        repositorio.partidos = Result.failure(AppError.SinConexion())
        vm.refrescar()

        assertEquals(1, vm.state.partidos!!.hoy.size)
        assertNull(vm.state.error)
        assertFalse(vm.state.refrescando)
        assertEquals(AppError.SinConexion().mensaje, vm.state.aviso)

        vm.onAvisoMostrado()
        assertNull(vm.state.aviso)
    }

    @Test
    fun `CA-1 al volver de la planilla se vuelve a pedir la lista`() {
        repositorio.partidos = Result.success(listOf(partidoDePrueba("p1", hoy.atTime(16, 0))))
        val vm = crearViewModel()

        repositorio.partidos = Result.success(
            listOf(partidoDePrueba("p1", hoy.atTime(16, 0), EstadoPartido.FINALIZADO, Marcador(3, 2))),
        )
        vm.alVolver()

        assertEquals(2, repositorio.llamadas.count { it == "partidos" })
        assertEquals(EstadoPartido.FINALIZADO, vm.state.partidos!!.hoy.single().estado)
        assertNotNull(vm.state.partidos!!.hoy.single().marcador)
    }

    @Test
    fun `CA-1 volver antes de que haya datos no dispara otra carga`() {
        repositorio.partidos = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()

        vm.alVolver()

        assertEquals(1, repositorio.llamadas.count { it == "partidos" })
    }
}
