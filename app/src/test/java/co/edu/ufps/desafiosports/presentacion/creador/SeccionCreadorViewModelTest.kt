package co.edu.ufps.desafiosports.presentacion.creador

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.use_case.competicion.AprobarInscripcionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerInscripcionesUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RechazarInscripcionUseCase
import co.edu.ufps.desafiosports.testutil.FakeCreadorRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.inscripcionDePrueba
import co.edu.ufps.desafiosports.testutil.torneoDeCreadorDePrueba
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0008: la sección Creador y la revisión de inscripciones, vistas desde la pantalla. */
class SeccionCreadorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeCreadorRepository()

    private fun crearSeccion() = SeccionCreadorViewModel(ObtenerMisTorneosUseCase(repositorio))

    private fun crearInscripciones() = InscripcionesViewModel(
        torneoId = "t1",
        torneoNombre = "Copa Prueba 2026",
        obtenerInscripcionesUseCase = ObtenerInscripcionesUseCase(repositorio),
        aprobarInscripcionUseCase = AprobarInscripcionUseCase(repositorio),
        rechazarInscripcionUseCase = RechazarInscripcionUseCase(repositorio),
    )

    private fun acciones() = repositorio.llamadas.filter { it.startsWith("aprobar") || it.startsWith("rechazar") }

    // ── Sección ─────────────────────────────────────────────────────────────

    @Test
    fun `CA-1 lista mis torneos con los que tienen algo por revisar primero`() {
        repositorio.torneos = Result.success(
            listOf(
                torneoDeCreadorDePrueba("tranquilo"),
                torneoDeCreadorDePrueba("con-comprobante", comprobantesPendientes = 1),
                torneoDeCreadorDePrueba("con-inscripciones", inscripcionesPendientes = 3),
            ),
        )

        val vm = crearSeccion()

        assertEquals(listOf("con-comprobante", "con-inscripciones", "tranquilo"), vm.state.torneos!!.map { it.id })
        assertEquals(listOf("con-comprobante", "con-inscripciones"), vm.state.porRevisar.map { it.id })
        assertEquals(3, vm.state.porRevisar.last().porRevisar)
        assertFalse(vm.state.cargando)
    }

    @Test
    fun `CA-5 sin torneos la lista queda vacia y sin error`() {
        val vm = crearSeccion()

        assertTrue(vm.state.torneos!!.isEmpty())
        assertTrue(vm.state.porRevisar.isEmpty())
        assertNull(vm.state.error)
    }

    @Test
    fun `CA-1 al volver de crear un torneo la lista se vuelve a pedir`() {
        val vm = crearSeccion()

        repositorio.torneos = Result.success(listOf(torneoDeCreadorDePrueba("nuevo")))
        vm.alVolver()

        assertEquals(listOf("nuevo"), vm.state.torneos!!.map { it.id })
        assertEquals(2, repositorio.llamadas.count { it == "torneos" })
    }

    @Test
    fun `CA-6 sin conexion ofrece reintentar, y si falla al actualizar conserva la lista`() {
        repositorio.torneos = Result.failure(AppError.SinConexion())
        val vm = crearSeccion()
        assertTrue(vm.state.error!!.sinConexion)

        repositorio.torneos = Result.success(listOf(torneoDeCreadorDePrueba()))
        vm.cargar()
        assertNull(vm.state.error)

        repositorio.torneos = Result.failure(AppError.SinConexion())
        vm.refrescar()
        assertEquals(1, vm.state.torneos!!.size)
        assertEquals(AppError.SinConexion().mensaje, vm.state.aviso)
        assertFalse(vm.state.refrescando)
    }

    // ── Inscripciones ───────────────────────────────────────────────────────

    @Test
    fun `CA-2 revisar abre las inscripciones del torneo repartidas`() {
        repositorio.inscripciones = Result.success(
            listOf(
                inscripcionDePrueba("a"),
                inscripcionDePrueba("b", estado = EstadoInscripcion.APROBADO),
                inscripcionDePrueba("c", estado = EstadoInscripcion.RECHAZADO, motivoRechazo = "Pago incompleto"),
            ),
        )

        val vm = crearInscripciones()

        assertEquals("Copa Prueba 2026", vm.state.torneo)
        assertEquals(listOf("inscripciones:t1"), repositorio.llamadas)
        val lista = vm.state.inscripciones!!
        assertEquals(listOf("a"), lista.porAprobar.map { it.id })
        assertEquals(listOf("b"), lista.aprobadas.map { it.id })
        assertEquals("Pago incompleto", lista.rechazadas.single().motivoRechazo)
    }

    @Test
    fun `CA-3 aprobar envia la accion, recarga y la mueve a aprobadas`() {
        val pendiente = inscripcionDePrueba("a", "Halcones FC")
        repositorio.inscripciones = Result.success(listOf(pendiente))
        val vm = crearInscripciones()
        repositorio.alActuar = {
            repositorio.inscripciones = Result.success(listOf(pendiente.copy(estado = EstadoInscripcion.APROBADO)))
        }

        vm.aprobar(pendiente)

        assertEquals(listOf("aprobar:t1:a"), acciones())
        assertTrue(vm.state.inscripciones!!.porAprobar.isEmpty())
        assertEquals(listOf("a"), vm.state.inscripciones!!.aprobadas.map { it.id })
        assertEquals(AvisoInscripciones.Aprobada("Halcones FC"), vm.state.aviso)
        assertNull(vm.state.enAccion)
    }

    @Test
    fun `CA-3 rechazar exige un motivo y no cierra el dialogo sin el`() {
        val pendiente = inscripcionDePrueba("a", "Halcones FC")
        repositorio.inscripciones = Result.success(listOf(pendiente))
        val vm = crearInscripciones()

        vm.pedirRechazo(pendiente)
        assertEquals(pendiente, vm.state.rechazando)
        vm.confirmarRechazo()

        assertNotNull(vm.state.errorMotivo)
        assertEquals(pendiente, vm.state.rechazando)
        assertTrue(acciones().isEmpty())

        vm.onMotivoChange("  ")
        vm.confirmarRechazo()
        assertTrue("solo espacios no es un motivo", acciones().isEmpty())

        vm.onMotivoChange("  El comprobante no corresponde.  ")
        assertNull(vm.state.errorMotivo)
        vm.confirmarRechazo()

        assertEquals(listOf("rechazar:t1:a:El comprobante no corresponde."), acciones())
        assertNull(vm.state.rechazando)
        assertEquals("", vm.state.motivo)
        assertEquals(AvisoInscripciones.Rechazada("Halcones FC"), vm.state.aviso)
    }

    @Test
    fun `CA-3 cancelar el rechazo no envia nada y limpia el motivo`() {
        val pendiente = inscripcionDePrueba("a")
        repositorio.inscripciones = Result.success(listOf(pendiente))
        val vm = crearInscripciones()

        vm.pedirRechazo(pendiente)
        vm.onMotivoChange("Motivo a medias")
        vm.cancelarRechazo()

        assertNull(vm.state.rechazando)
        assertEquals("", vm.state.motivo)
        assertTrue(acciones().isEmpty())

        vm.onMotivoChange("x".repeat(1000))
        assertEquals(InscripcionesViewModel.MOTIVO_MAXIMO, vm.state.motivo.length)
    }

    @Test
    fun `CA-3 si aprobar falla la lista no cambia y se ve el mensaje del backend`() {
        val pendiente = inscripcionDePrueba("a")
        repositorio.inscripciones = Result.success(listOf(pendiente))
        val vm = crearInscripciones()
        repositorio.resultadoAccion = Result.failure(
            AppError.Negocio("REGLA_NEGOCIO", "El equipo fue descalificado del torneo y no puede aprobarse."),
        )

        vm.aprobar(pendiente)

        assertEquals(listOf("a"), vm.state.inscripciones!!.porAprobar.map { it.id })
        assertEquals(AvisoInscripciones.Fallo("El equipo fue descalificado del torneo y no puede aprobarse."), vm.state.aviso)
        assertNull(vm.state.enAccion)

        vm.onAvisoMostrado()
        assertNull(vm.state.aviso)
    }

    @Test
    fun `CA-3 si se guarda pero la lista no se puede volver a pedir, lo avisa`() {
        val pendiente = inscripcionDePrueba("a")
        repositorio.inscripciones = Result.success(listOf(pendiente))
        val vm = crearInscripciones()
        repositorio.alActuar = { repositorio.inscripciones = Result.failure(AppError.SinConexion()) }

        vm.aprobar(pendiente)

        assertEquals(AvisoInscripciones.SinActualizar, vm.state.aviso)
        assertEquals(listOf("a"), vm.state.inscripciones!!.porAprobar.map { it.id })
    }

    @Test
    fun `CA-3 el doble toque no aprueba dos veces ni deja abrir otro rechazo`() {
        val uno = inscripcionDePrueba("a")
        val dos = inscripcionDePrueba("b")
        repositorio.inscripciones = Result.success(listOf(uno, dos))
        val vm = crearInscripciones()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueoAccion = bloqueo

        vm.aprobar(uno)
        assertEquals("a", vm.state.enAccion)
        vm.aprobar(uno)
        vm.aprobar(dos)
        vm.pedirRechazo(dos)
        vm.refrescar()
        assertNull(vm.state.rechazando)
        bloqueo.complete(Unit)

        assertEquals(listOf("aprobar:t1:a"), acciones())
        assertNull(vm.state.enAccion)
    }

    @Test
    fun `CA-4 un torneo que no administra muestra el aviso con volver`() {
        repositorio.inscripciones = Result.failure(
            AppError.Negocio("NO_ADMIN_DEL_TORNEO", "No administras este torneo."),
        )

        val vm = crearInscripciones()

        assertNull(vm.state.inscripciones)
        assertTrue(vm.state.sinPermiso)
        assertEquals("No administras este torneo.", vm.state.error!!.mensaje)
    }

    @Test
    fun `CA-6 sin conexion al abrir las inscripciones ofrece reintentar`() {
        repositorio.inscripciones = Result.failure(AppError.SinConexion())
        val vm = crearInscripciones()

        assertTrue(vm.state.error!!.sinConexion)
        assertFalse(vm.state.sinPermiso)

        repositorio.inscripciones = Result.success(emptyList())
        vm.cargar()

        assertNull(vm.state.error)
        assertTrue(vm.state.inscripciones!!.vacio)
    }
}
