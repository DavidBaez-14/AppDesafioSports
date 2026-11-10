package co.edu.ufps.desafiosports.presentacion.creador

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.domain.use_case.competicion.CrearTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerDeportesUseCase
import co.edu.ufps.desafiosports.testutil.FakeCreadorRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.deportesDePrueba
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** MOV-0007: "Crear torneo" vista desde la pantalla. */
class CrearTorneoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val hoy = LocalDate.of(2026, 11, 9)
    private val repositorio = FakeCreadorRepository()

    private fun crearViewModel() =
        CrearTorneoViewModel(ObtenerDeportesUseCase(repositorio), CrearTorneoUseCase(repositorio) { hoy }, hoy)

    /** Lleva el formulario hasta el paso 3 con todo válido. */
    private fun CrearTorneoViewModel.llenarHastaElPaso3() {
        elegirDeporte(state.deportes!!.first())
        siguiente()
        onNombreChange("Relámpago de Fútbol sala")
        elegirFormato(FormatoTorneo.ELIMINACION_DIRECTA)
        siguiente()
    }

    @Test
    fun `CA-1 al abrir carga los deportes y propone fecha y visibilidad`() {
        val vm = crearViewModel()

        assertEquals(deportesDePrueba(), vm.state.deportes)
        assertFalse(vm.state.cargandoDeportes)
        assertEquals(1, vm.state.paso)
        assertEquals(hoy.plusDays(7), vm.state.borrador.fechaInicio)
        assertEquals(Visibilidad.PUBLICO, vm.state.borrador.visibilidad)
        assertFalse("sin deporte no se puede seguir", vm.state.puedeSeguir)
    }

    @Test
    fun `CA-1 siguiente no avanza con el paso incompleto`() {
        val vm = crearViewModel()

        vm.siguiente()
        assertEquals(1, vm.state.paso)

        vm.elegirDeporte(vm.state.deportes!!.first())
        vm.siguiente()
        assertEquals(2, vm.state.paso)

        vm.onNombreChange("ab")
        vm.elegirFormato(FormatoTorneo.LIGA)
        vm.siguiente()
        assertEquals("con el nombre corto se queda en el paso 2", 2, vm.state.paso)
        assertTrue(repositorio.torneosCreados.isEmpty())
    }

    @Test
    fun `CA-1 el nombre se corta en 150 y los cupos respetan el formato`() {
        val vm = crearViewModel()

        vm.onNombreChange("a".repeat(400))
        assertEquals(150, vm.state.borrador.nombre.length)

        vm.elegirFormato(FormatoTorneo.ELIMINACION_DIRECTA)
        repeat(30) { vm.cambiarCupos(1) }
        assertEquals(16, vm.state.borrador.cupos)
        repeat(30) { vm.cambiarCupos(-1) }
        assertEquals(2, vm.state.borrador.cupos)
    }

    @Test
    fun `CA-1 el valor de la inscripcion solo admite digitos`() {
        val vm = crearViewModel()

        vm.onCobraChange(true)
        vm.onValorChange("$ 05.0a00")
        assertEquals("5000", vm.state.valorTexto)
        assertEquals(5_000L, vm.state.borrador.valorInscripcion)

        vm.onValorChange("")
        assertNull(vm.state.borrador.valorInscripcion)

        vm.onValorChange("9".repeat(20))
        assertEquals(CrearTorneoViewModel.VALOR_MAXIMO_DIGITOS, vm.state.valorTexto.length)
    }

    @Test
    fun `CA-2 al crear se envia el torneo completo y se muestra que esta listo`() {
        val vm = crearViewModel()
        vm.llenarHastaElPaso3()
        vm.elegirVisibilidad(Visibilidad.NO_LISTADO)
        vm.onCobraChange(true)
        vm.onValorChange("5000")
        vm.elegirFecha(hoy.plusDays(12))

        vm.siguiente()

        val enviado = repositorio.torneosCreados.single()
        assertEquals("Relámpago de Fútbol sala", enviado.nombre)
        assertEquals("FUTSAL", enviado.deporte)
        assertEquals(FormatoTorneo.ELIMINACION_DIRECTA, enviado.formato)
        assertEquals(8, enviado.cupos)
        assertEquals(hoy.plusDays(12), enviado.fechaInicio)
        assertEquals(Visibilidad.NO_LISTADO, enviado.visibilidad)
        assertTrue(enviado.cobraInscripcion)
        assertEquals(5_000L, enviado.valorInscripcion)
        assertNotNull(vm.state.creado)
        assertFalse(vm.state.creando)
    }

    @Test
    fun `CA-3 un error del backend vuelve al paso que corresponde sin perder lo escrito`() {
        repositorio.resultadoCrear = Result.failure(
            AppError.Negocio("CUPOS_FUERA_DE_RANGO", "Para este formato los cupos van de 2 a 16."),
        )
        val vm = crearViewModel()
        vm.llenarHastaElPaso3()

        vm.siguiente()

        assertEquals(2, vm.state.paso)
        assertEquals("Para este formato los cupos van de 2 a 16.", vm.state.error)
        assertEquals("Relámpago de Fútbol sala", vm.state.borrador.nombre)
        assertNull(vm.state.creado)
        assertFalse(vm.state.creando)

        // Al corregir algo, el aviso se va.
        vm.cambiarCupos(-1)
        assertNull(vm.state.error)
    }

    @Test
    fun `CA-3 un error que no es de un campo se queda en el ultimo paso`() {
        repositorio.resultadoCrear = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()
        vm.llenarHastaElPaso3()

        vm.siguiente()

        assertEquals(3, vm.state.paso)
        assertEquals(AppError.SinConexion().mensaje, vm.state.error)

        // Reintentar con conexión crea el torneo: nada se perdió.
        repositorio.resultadoCrear = FakeCreadorRepository().resultadoCrear
        vm.siguiente()
        assertNotNull(vm.state.creado)
        assertEquals(2, repositorio.torneosCreados.size)
    }

    @Test
    fun `CA-5 atras vuelve un paso y desde el primero pide confirmar si hay algo escrito`() {
        val vm = crearViewModel()
        assertTrue("sin datos se sale sin preguntar", vm.atras())

        vm.elegirDeporte(vm.state.deportes!!.first())
        vm.siguiente()
        assertFalse(vm.atras())
        assertEquals(1, vm.state.paso)

        assertFalse(vm.atras())
        assertTrue(vm.state.confirmandoSalida)
        vm.cancelarSalida()
        assertFalse(vm.state.confirmandoSalida)
        assertNotNull("cancelar conserva lo elegido", vm.state.borrador.deporte)
    }

    @Test
    fun `CA-5 la X pide confirmar en cualquier paso si hay algo escrito`() {
        val vm = crearViewModel()
        assertTrue(vm.pedirSalida())

        vm.llenarHastaElPaso3()
        assertFalse(vm.pedirSalida())
        assertTrue(vm.state.confirmandoSalida)
        assertEquals(3, vm.state.paso)
    }

    @Test
    fun `CA-2 el doble toque en crear no crea dos torneos`() {
        val vm = crearViewModel()
        vm.llenarHastaElPaso3()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueoCrear = bloqueo

        vm.siguiente()
        assertTrue(vm.state.creando)
        vm.siguiente()
        // Mientras se crea no se puede cambiar el formulario ni salir.
        vm.onNombreChange("Otro nombre")
        assertFalse(vm.atras())
        assertFalse(vm.pedirSalida())
        bloqueo.complete(Unit)

        assertEquals(1, repositorio.torneosCreados.size)
        assertEquals("Relámpago de Fútbol sala", vm.state.borrador.nombre)
        assertNotNull(vm.state.creado)
    }

    @Test
    fun `CA-7 si el catalogo de deportes no carga ofrece reintentar`() {
        repositorio.deportes = Result.failure(AppError.SinConexion())
        val vm = crearViewModel()

        assertNull(vm.state.deportes)
        assertTrue(vm.state.errorDeportes!!.sinConexion)

        repositorio.deportes = Result.success(deportesDePrueba())
        vm.cargarDeportes()

        assertNull(vm.state.errorDeportes)
        assertEquals(3, vm.state.deportes!!.size)
    }
}
