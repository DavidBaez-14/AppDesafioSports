package co.edu.ufps.desafiosports.presentacion.inicio

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.SeccionDeArbitro
import co.edu.ufps.desafiosports.domain.model.SeccionDeCreador
import co.edu.ufps.desafiosports.domain.model.Secciones
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisEquiposUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.testutil.FakeArbitrajeRepository
import co.edu.ufps.desafiosports.testutil.FakeCreadorRepository
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeMisEquiposRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.cuentaDePrueba
import co.edu.ufps.desafiosports.testutil.equipoDePrueba
import co.edu.ufps.desafiosports.testutil.partidoDePrueba
import co.edu.ufps.desafiosports.testutil.torneoDeCreadorDePrueba
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerMisInvitacionesUseCase
import co.edu.ufps.desafiosports.testutil.FakeInvitacionesRepository
import co.edu.ufps.desafiosports.testutil.invitacionRecibidaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Inicio. Los casos sin prefijo son de MOV-0010 (los bloques de "Tus secciones"), que se conservan;
 * los que empiezan por MOV-0004 son del Inicio completo.
 */
class InicioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val hoy = LocalDate.of(2026, 11, 12)
    private val conLasDos = Secciones(SeccionDeCreador(torneos = 1, porRevisar = 0), SeccionDeArbitro(partidosAsignados = 1))

    /** Por defecto la cuenta tiene las dos secciones, que es lo que ejercitan los casos de MOV-0010. */
    private val cuentas = FakeCuentaRepository(Result.success(cuentaDePrueba().copy(secciones = conLasDos)))
    private val equipos = FakeMisEquiposRepository()
    private val arbitraje = FakeArbitrajeRepository()
    private val creador = FakeCreadorRepository()
    private val invitaciones = FakeInvitacionesRepository()

    private fun crear() = InicioViewModel(
        obtenerCuentaUseCase = ObtenerCuentaUseCase(cuentas),
        obtenerMisEquiposUseCase = ObtenerMisEquiposUseCase(equipos),
        obtenerPartidosUseCase = ObtenerPartidosDeArbitroUseCase(arbitraje) { hoy },
        obtenerMisTorneosUseCase = ObtenerMisTorneosUseCase(creador),
        obtenerMisInvitacionesUseCase = ObtenerMisInvitacionesUseCase(invitaciones),
    )

    private fun InicioViewModel.resumenArbitro() = (state.arbitro as Seccion.Lista).valor
    private fun InicioViewModel.resumenCreador() = (state.creador as Seccion.Lista).valor
    private fun pedidosArbitro() = arbitraje.llamadas.count { it == "partidos" }

    // ── MOV-0010: los bloques de sección ────────────────────────────────────

    @Test
    fun `CA-1 carga el nombre de la cuenta`() {
        cuentas.resultado = Result.success(cuentaDePrueba().copy(nombres = "Carlos Andrés", apellidos = "Ramírez"))

        val vm = crear()

        assertEquals("Carlos", vm.state.nombre)
        assertEquals(Carga.LISTO, vm.state.carga)
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
    fun `CA-2 con solo partidos jugados no hay proximo`() {
        arbitraje.partidos = Result.success(
            listOf(partidoDePrueba("jugado", hoy.minusDays(3).atTime(9, 0), EstadoPartido.FINALIZADO, Marcador(2, 1))),
        )

        val resumen = crear().resumenArbitro()

        assertNull(resumen.proximo)
        assertEquals(0, resumen.porJugar)
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
    fun `CA-5 si falla una seccion la otra carga y el resto de Inicio se ve`() {
        arbitraje.partidos = Result.failure(AppError.SinConexion())
        creador.torneos = Result.success(listOf(torneoDeCreadorDePrueba("a")))

        val vm = crear()

        val fallo = vm.state.arbitro as Seccion.Fallo
        assertTrue(fallo.error.sinConexion)
        assertTrue(vm.state.creador is Seccion.Lista)
        assertEquals(Carga.LISTO, vm.state.carga)
    }

    @Test
    fun `CA-5 reintentar vuelve a pedir solo esa seccion`() {
        creador.torneos = Result.failure(AppError.SinConexion())
        val vm = crear()
        val antes = pedidosArbitro()
        creador.torneos = Result.success(listOf(torneoDeCreadorDePrueba("a")))

        vm.reintentarCreador()

        assertEquals(1, vm.resumenCreador().torneos.size)
        assertEquals(antes, pedidosArbitro())
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

    // ── MOV-0004: el Inicio completo ────────────────────────────────────────

    @Test
    fun `MOV-0004 CA-2 con las dos secciones se muestran y se piden las dos`() {
        val vm = crear()

        assertEquals(conLasDos, vm.state.secciones)
        assertEquals(1, pedidosArbitro())
        assertTrue(vm.state.arbitro is Seccion.Lista)
        assertTrue(vm.state.creador is Seccion.Lista)
    }

    @Test
    fun `MOV-0004 CA-2 con una sola seccion solo se pide esa`() {
        cuentas.resultado = Result.success(cuentaDePrueba().copy(secciones = Secciones(arbitro = SeccionDeArbitro(0))))

        val vm = crear()

        assertNull(vm.state.secciones.creador)
        assertEquals(1, pedidosArbitro())
        assertEquals(0, creador.llamadas.size)
    }

    @Test
    fun `MOV-0004 CA-2 sin secciones no se pide ningun detalle`() {
        cuentas.resultado = Result.success(cuentaDePrueba())

        val vm = crear()

        assertTrue(vm.state.secciones.ninguna)
        assertEquals(0, arbitraje.llamadas.size)
        assertEquals(0, creador.llamadas.size)
        assertEquals(Carga.LISTO, vm.state.carga)
    }

    @Test
    fun `MOV-0004 CA-4 muestra el proximo partido mas cercano entre los equipos`() {
        equipos.equipos = Result.success(
            listOf(
                equipoDePrueba("lejos", partido = hoy.plusDays(6).atTime(18, 0)),
                equipoDePrueba("cerca", partido = hoy.plusDays(2).atTime(16, 0)),
            ),
        )

        val vm = crear()

        assertEquals(2, vm.state.equipos.size)
        assertEquals("cerca", vm.state.proximoPartido?.equipo?.equipoTorneoId)
    }

    @Test
    fun `MOV-0004 CA-6 cuenta nueva ve el estado vacio`() {
        cuentas.resultado = Result.success(cuentaDePrueba())

        val vm = crear()

        assertEquals(Carga.LISTO, vm.state.carga)
        assertTrue(vm.state.equipos.isEmpty())
        assertTrue(vm.state.secciones.ninguna)
        assertNull(vm.state.proximoPartido)
        assertNull(vm.state.error)
    }

    @Test
    fun `MOV-0004 CA-10 cuenta sin registro pide completar registro`() {
        cuentas.resultado = Result.failure(AppError.CuentaNoRegistrada("Completa tu registro para continuar."))

        val vm = crear()

        assertTrue(vm.state.completarRegistro)
        assertNull(vm.state.error)

        vm.onCompletarRegistroAtendido()
        assertFalse(vm.state.completarRegistro)
    }

    @Test
    fun `MOV-0004 CA-11 sin conexion muestra reintentar y se recupera`() {
        cuentas.resultado = Result.failure(AppError.SinConexion())
        equipos.equipos = Result.failure(AppError.SinConexion())

        val vm = crear()

        assertEquals(Carga.ERROR, vm.state.carga)
        assertTrue(vm.state.error!!.sinConexion)
        // Sin saber qué secciones tiene, no se pide ninguna.
        assertEquals(0, arbitraje.llamadas.size)

        cuentas.resultado = Result.success(cuentaDePrueba().copy(nombres = "Carlos", secciones = conLasDos))
        equipos.equipos = Result.success(listOf(equipoDePrueba()))
        vm.cargar()

        assertEquals(Carga.LISTO, vm.state.carga)
        assertNull(vm.state.error)
        assertEquals("Carlos", vm.state.nombre)
        assertEquals(1, vm.state.equipos.size)
        assertEquals(1, pedidosArbitro())
    }

    @Test
    fun `MOV-0004 CA-11 si fallan los equipos sin datos previos tambien se ofrece reintentar`() {
        equipos.equipos = Result.failure(AppError.Desconocido())

        val vm = crear()

        assertEquals(Carga.ERROR, vm.state.carga)
        assertFalse(vm.state.error!!.sinConexion)
    }

    @Test
    fun `MOV-0004 CA-11 un fallo al actualizar conserva lo que se ve y avisa`() {
        equipos.equipos = Result.success(listOf(equipoDePrueba()))
        val vm = crear()
        equipos.equipos = Result.failure(AppError.SinConexion())

        vm.refrescar()

        assertEquals(Carga.LISTO, vm.state.carga)
        assertEquals(1, vm.state.equipos.size)
        assertTrue(vm.state.aviso!!.isNotBlank())
        assertFalse(vm.state.refrescando)

        vm.onAvisoMostrado()
        assertNull(vm.state.aviso)
    }

    @Test
    fun `MOV-0004 CA-13 recargar vuelve a pedir los datos`() {
        val vm = crear()
        assertEquals(1, cuentas.llamadas)
        assertEquals(1, equipos.llamadas)
        equipos.equipos = Result.success(listOf(equipoDePrueba("nuevo")))

        vm.alVolver()

        assertEquals(2, cuentas.llamadas)
        assertEquals(2, equipos.llamadas)
        assertEquals(2, pedidosArbitro())
        assertEquals("nuevo", vm.state.equipos.single().equipoTorneoId)

        vm.refrescar()

        assertEquals(3, equipos.llamadas)
    }

    @Test
    fun `MOV-0012 CA-1 Inicio muestra las invitaciones pendientes y las actualiza al volver`() {
        invitaciones.resultadoMias = Result.success(listOf(invitacionRecibidaDePrueba()))
        val vm = crear()

        assertEquals("Copa Prueba 2026", vm.state.invitaciones.single().torneo)

        // La aceptó en la otra pantalla: al volver ya no está.
        invitaciones.resultadoMias = Result.success(emptyList())
        vm.alVolver()

        assertTrue(vm.state.invitaciones.isEmpty())
        assertEquals(2, invitaciones.pedidosDeMias)
    }

    @Test
    fun `MOV-0012 CA-1 si las invitaciones fallan Inicio se ve igual y sin aviso`() {
        invitaciones.resultadoMias = Result.failure(AppError.SinConexion())
        val vm = crear()

        assertEquals(Carga.LISTO, vm.state.carga)
        assertTrue(vm.state.invitaciones.isEmpty())
        assertNull(vm.state.aviso)
    }
}
