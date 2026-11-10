package co.edu.ufps.desafiosports.presentacion.inicio

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.testutil.FakeArbitrajeRepository
import co.edu.ufps.desafiosports.testutil.FakeCreadorRepository
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.cuentaDePrueba
import co.edu.ufps.desafiosports.testutil.partidoDePrueba
import co.edu.ufps.desafiosports.testutil.torneoDeCreadorDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** MOV-0010: Inicio provisional con "Tus secciones". */
class InicioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val hoy = LocalDate.of(2026, 11, 12)
    private val sesion = FakeSesionRepository()
    private val cuentas = FakeCuentaRepository()
    private val arbitraje = FakeArbitrajeRepository()
    private val creador = FakeCreadorRepository()

    private fun crear() = InicioViewModel(
        obtenerCuentaUseCase = ObtenerCuentaUseCase(cuentas),
        obtenerPartidosUseCase = ObtenerPartidosDeArbitroUseCase(arbitraje) { hoy },
        obtenerMisTorneosUseCase = ObtenerMisTorneosUseCase(creador),
        cerrarSesionUseCase = CerrarSesionUseCase(sesion),
    )

    private fun InicioViewModel.resumenArbitro() = (state.arbitro as Seccion.Lista).valor
    private fun InicioViewModel.resumenCreador() = (state.creador as Seccion.Lista).valor

    @Test
    fun `CA-1 carga el nombre de la cuenta`() {
        cuentas.resultado = Result.success(cuentaDePrueba().copy(nombres = "Carlos Andrés", apellidos = "Ramírez"))

        val vm = crear()

        assertEquals("Carlos", vm.state.nombre)
        assertEquals("CR", vm.state.iniciales)
    }

    @Test
    fun `CA-1 si falla la cuenta el saludo queda generico y las secciones cargan`() {
        cuentas.resultado = Result.failure(AppError.SinConexion())

        val vm = crear()

        assertNull(vm.state.nombre)
        assertTrue(vm.state.creador is Seccion.Lista)
    }

    @Test
    fun `CA-2 el proximo partido es el de hoy sin cerrar`() {
        arbitraje.partidos = Result.success(
            listOf(
                partidoDePrueba("cerrado-hoy", hoy.atTime(10, 0), EstadoPartido.FINALIZADO, Marcador(1, 0)),
                partidoDePrueba("hoy", hoy.atTime(16, 0)),
                partidoDePrueba("manana", hoy.plusDays(1).atTime(9, 0)),
            ),
        )

        val resumen = crear().resumenArbitro()

        assertEquals("hoy", resumen.proximo?.id)
        assertEquals(2, resumen.porJugar)
        assertTrue(resumen.visible)
    }

    @Test
    fun `CA-2 sin partidos de hoy toma el proximo con cruce definido`() {
        arbitraje.partidos = Result.success(
            listOf(
                partidoDePrueba("sin-cruce", hoy.plusDays(1).atTime(9, 0), conCruce = false),
                partidoDePrueba("con-cruce", hoy.plusDays(2).atTime(9, 0)),
            ),
        )

        assertEquals("con-cruce", crear().resumenArbitro().proximo?.id)
    }

    @Test
    fun `CA-2 con solo partidos jugados no hay proximo pero la seccion aparece`() {
        arbitraje.partidos = Result.success(
            listOf(partidoDePrueba("jugado", hoy.minusDays(3).atTime(9, 0), EstadoPartido.FINALIZADO, Marcador(2, 1))),
        )

        val resumen = crear().resumenArbitro()

        assertNull(resumen.proximo)
        assertEquals(0, resumen.porJugar)
        assertTrue(resumen.visible)
    }

    @Test
    fun `CA-3 sin partidos la seccion arbitro no aparece`() {
        arbitraje.partidos = Result.success(emptyList())

        assertFalse(crear().resumenArbitro().visible)
    }

    @Test
    fun `CA-4 suma lo que hay por revisar y destaca el torneo que mas espera`() {
        creador.torneos = Result.success(
            listOf(
                torneoDeCreadorDePrueba("a", inscripcionesPendientes = 1),
                torneoDeCreadorDePrueba("b", inscripcionesPendientes = 2, comprobantesPendientes = 1),
                torneoDeCreadorDePrueba("c"),
            ),
        )

        val resumen = crear().resumenCreador()

        assertEquals(4, resumen.porRevisar)
        assertEquals("b", resumen.destacado?.id)
        assertEquals(3, resumen.torneos.size)
    }

    @Test
    fun `CA-5 si falla una seccion la otra carga`() {
        arbitraje.partidos = Result.failure(AppError.SinConexion())
        creador.torneos = Result.success(listOf(torneoDeCreadorDePrueba("a")))

        val vm = crear()

        val fallo = vm.state.arbitro as Seccion.Fallo
        assertTrue(fallo.error.sinConexion)
        assertTrue(vm.state.creador is Seccion.Lista)
    }

    @Test
    fun `CA-5 reintentar vuelve a pedir solo esa seccion`() {
        creador.torneos = Result.failure(AppError.SinConexion())
        val vm = crear()
        val pedidosArbitro = arbitraje.llamadas.count { it == "partidos" }
        creador.torneos = Result.success(listOf(torneoDeCreadorDePrueba("a")))

        vm.reintentarCreador()

        assertEquals(1, vm.resumenCreador().torneos.size)
        assertEquals(pedidosArbitro, arbitraje.llamadas.count { it == "partidos" })
    }

    @Test
    fun `CA-5 un fallo al refrescar conserva el resumen que ya se ve`() {
        creador.torneos = Result.success(listOf(torneoDeCreadorDePrueba("a")))
        val vm = crear()
        creador.torneos = Result.failure(AppError.SinConexion())

        vm.refrescar()

        assertEquals(1, vm.resumenCreador().torneos.size)
        assertFalse(vm.state.refrescando)
    }

    @Test
    fun `CA-6 cerrar sesion llama al caso de uso`() {
        val vm = crear()

        vm.cerrarSesion()

        assertEquals(1, sesion.cierres)
        assertFalse(vm.state.cerrandoSesion)
    }
}
