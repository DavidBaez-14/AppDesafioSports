package co.edu.ufps.desafiosports.presentacion.perfil

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoMulta
import co.edu.ufps.desafiosports.domain.model.PasoPorEquipo
import co.edu.ufps.desafiosports.domain.model.Puesto
import co.edu.ufps.desafiosports.domain.model.Tarjetas
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMiPerfilUseCase
import co.edu.ufps.desafiosports.domain.use_case.finanzas.ObtenerMisMultasUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.presentacion.inicio.Carga
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.FakeMultasRepository
import co.edu.ufps.desafiosports.testutil.FakePerfilRepository
import co.edu.ufps.desafiosports.testutil.FakeSesionRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.cuentaDePrueba
import co.edu.ufps.desafiosports.testutil.multaDePrueba
import co.edu.ufps.desafiosports.testutil.perfilDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/** MOV-0004 · CA-7 a CA-9 y CA-11: Perfil visto desde la pantalla. */
class PerfilViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sesion = FakeSesionRepository()
    private val cuentas = FakeCuentaRepository(Result.success(cuentaDePrueba().copy(nombres = "Juan", apellidos = "Castro", nombre = "Juan Castro")))
    private val perfiles = FakePerfilRepository()
    private val multas = FakeMultasRepository()
    private val base = LocalDateTime.of(2026, 11, 14, 16, 0)

    private fun crear() = PerfilViewModel(
        obtenerCuentaUseCase = ObtenerCuentaUseCase(cuentas),
        obtenerMiPerfilUseCase = ObtenerMiPerfilUseCase(perfiles),
        obtenerMisMultasUseCase = ObtenerMisMultasUseCase(multas),
        cerrarSesionUseCase = CerrarSesionUseCase(sesion),
    )

    @Test
    fun `CA-7 arma trayectoria titulos y multas ordenadas`() {
        perfiles.perfil = Result.success(
            perfilDePrueba(partidos = 5, goles = 4, titulos = 1, tarjetas = Tarjetas(3, 0, 1)).copy(
                trayectoria = listOf(
                    PasoPorEquipo("Halcones FC", "Copa Sistemas 2025", Puesto.SUBCAMPEON),
                    PasoPorEquipo("Halcones FC", "Liga Relámpago (prueba)", null),
                ),
            ),
        )
        multas.multas = Result.success(
            listOf(
                multaDePrueba("pagada", EstadoMulta.PAGADA, base.minusDays(1)),
                multaDePrueba("pendiente", EstadoMulta.PENDIENTE, base.minusDays(9)),
            ),
        )

        val state = crear().state

        assertEquals(Carga.LISTO, state.carga)
        assertEquals("Juan Castro", state.nombre)
        assertEquals("Juan C.", state.nombrePublico)
        assertEquals("JC", state.iniciales)
        assertEquals(5, state.perfil!!.partidosJugados)
        assertTrue(state.perfil!!.conCifras)
        assertEquals(Puesto.SUBCAMPEON, state.perfil!!.trayectoria.first().puesto)
        assertEquals(listOf("pendiente", "pagada"), state.multas.map { it.id })
        assertTrue(state.conSanciones)
    }

    @Test
    fun `CA-8 cuenta nueva oculta cifras y sanciones`() {
        val state = crear().state

        assertEquals(Carga.LISTO, state.carga)
        assertFalse(state.perfil!!.conCifras)
        assertTrue(state.perfil!!.trayectoria.isEmpty())
        assertFalse(state.conSanciones)
    }

    @Test
    fun `CA-8 con tarjetas y sin multas el bloque de sanciones aparece`() {
        perfiles.perfil = Result.success(perfilDePrueba(tarjetas = Tarjetas(amarillas = 1)))

        assertTrue(crear().state.conSanciones)
    }

    @Test
    fun `CA-9 cerrar sesion llama al caso de uso`() {
        val vm = crear()

        vm.cerrarSesion()

        assertEquals(1, sesion.cierres)
        assertFalse(vm.state.cerrandoSesion)
    }

    @Test
    fun `CA-11 sin conexion muestra reintentar y se recupera`() {
        perfiles.perfil = Result.failure(AppError.SinConexion())
        multas.multas = Result.failure(AppError.SinConexion())

        val vm = crear()

        assertEquals(Carga.ERROR, vm.state.carga)
        assertTrue(vm.state.error!!.sinConexion)
        assertNull(vm.state.perfil)
        // Con el error en pantalla se puede cerrar sesión igual.
        vm.cerrarSesion()
        assertEquals(1, sesion.cierres)

        perfiles.perfil = Result.success(perfilDePrueba(partidos = 2))
        multas.multas = Result.success(emptyList())
        vm.cargar()

        assertEquals(Carga.LISTO, vm.state.carga)
        assertNull(vm.state.error)
        assertEquals(2, vm.state.perfil!!.partidosJugados)
    }

    @Test
    fun `CA-11 si falla la cuenta el encabezado usa el nombre del perfil`() {
        cuentas.resultado = Result.failure(AppError.Desconocido())

        val state = crear().state

        assertEquals(Carga.LISTO, state.carga)
        assertEquals("Juan Castro", state.nombre)
        assertEquals("", state.nombrePublico)
    }

    @Test
    fun `CA-11 un fallo al actualizar conserva lo que se ve y avisa`() {
        perfiles.perfil = Result.success(perfilDePrueba(partidos = 3))
        val vm = crear()
        perfiles.perfil = Result.failure(AppError.SinConexion())

        vm.refrescar()

        assertEquals(Carga.LISTO, vm.state.carga)
        assertEquals(3, vm.state.perfil!!.partidosJugados)
        assertTrue(vm.state.aviso!!.isNotBlank())
        assertFalse(vm.state.refrescando)
    }
}
