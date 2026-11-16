package co.edu.ufps.desafiosports.presentacion.perfil

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.use_case.identidad.AceptarInvitacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.VerInvitacionUseCase
import co.edu.ufps.desafiosports.testutil.FakeInvitacionesRepository
import co.edu.ufps.desafiosports.testutil.MainDispatcherRule
import co.edu.ufps.desafiosports.testutil.TOKEN_DE_PRUEBA
import co.edu.ufps.desafiosports.testutil.invitacionRecibidaDePrueba
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** MOV-0011: aceptar una invitación pegando su enlace. */
class InvitacionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repositorio = FakeInvitacionesRepository()

    private fun crear() = InvitacionViewModel(VerInvitacionUseCase(repositorio), AceptarInvitacionUseCase(repositorio))

    @Test
    fun `CA-11 pegar el enlace y buscar muestra la invitacion`() {
        val vm = crear()

        vm.onTextoChange("https://desafio.test/invitacion/$TOKEN_DE_PRUEBA")
        vm.buscar()

        assertEquals(listOf("ver:$TOKEN_DE_PRUEBA"), repositorio.llamadas)
        assertEquals("Copa Prueba 2026", vm.state.invitacion!!.torneo)
        assertEquals("Carlos Rojas", vm.state.invitacion!!.invitadoPor)
        assertFalse(vm.state.buscando)
        assertNull(vm.state.error)
    }

    @Test
    fun `CA-11 un texto que no parece una invitacion se avisa sin llamar al backend`() {
        val vm = crear()

        vm.onTextoChange("hola, quiero arbitrar")
        vm.buscar()

        assertTrue(repositorio.llamadas.isEmpty())
        assertEquals(VerInvitacionUseCase.MENSAJE_ENLACE, vm.state.error)
        assertNull(vm.state.invitacion)

        // Corregir el texto borra el error.
        vm.onTextoChange(TOKEN_DE_PRUEBA)
        assertNull(vm.state.error)
    }

    @Test
    fun `CA-11 una invitacion que no existe muestra el mensaje del backend`() {
        repositorio.resultadoVer = Result.failure(AppError.NoEncontrado("Invitación no encontrada."))
        val vm = crear()

        vm.onTextoChange(TOKEN_DE_PRUEBA)
        vm.buscar()

        assertEquals("Invitación no encontrada.", vm.state.error)
        assertNull(vm.state.invitacion)
    }

    @Test
    fun `CA-11 una invitacion ya usada se muestra pero no se puede aceptar`() {
        repositorio.resultadoVer = Result.success(invitacionRecibidaDePrueba(pendiente = false))
        val vm = crear()

        vm.onTextoChange(TOKEN_DE_PRUEBA)
        vm.buscar()
        vm.aceptar()

        assertFalse(vm.state.invitacion!!.pendiente)
        assertEquals(listOf("ver:$TOKEN_DE_PRUEBA"), repositorio.llamadas)
        assertFalse(vm.state.aceptada)
    }

    @Test
    fun `CA-11 cambiar el texto descarta la invitacion que se estaba viendo`() {
        val vm = crear()
        vm.onTextoChange(TOKEN_DE_PRUEBA)
        vm.buscar()

        vm.onTextoChange("otro")

        assertNull(vm.state.invitacion)
        vm.aceptar()
        assertEquals(1, repositorio.llamadas.size)
    }

    @Test
    fun `CA-12 aceptar envia el token y muestra la confirmacion`() {
        val vm = crear()
        vm.onTextoChange(TOKEN_DE_PRUEBA)
        vm.buscar()

        vm.aceptar()

        assertEquals(listOf("ver:$TOKEN_DE_PRUEBA", "aceptar:$TOKEN_DE_PRUEBA"), repositorio.llamadas)
        assertTrue(vm.state.aceptada)
        assertFalse(vm.state.aceptando)
    }

    @Test
    fun `CA-12 el doble toque no acepta dos veces`() {
        val vm = crear()
        vm.onTextoChange(TOKEN_DE_PRUEBA)
        vm.buscar()
        val bloqueo = CompletableDeferred<Unit>()
        repositorio.bloqueo = bloqueo

        vm.aceptar()
        vm.aceptar()
        vm.onTextoChange("otro")

        assertTrue(vm.state.aceptando)
        bloqueo.complete(Unit)

        assertEquals(1, repositorio.llamadas.count { it.startsWith("aceptar") })
        assertTrue(vm.state.aceptada)
    }

    @Test
    fun `CA-12 una invitacion para otro correo muestra el mensaje y deja reintentar`() {
        repositorio.resultadoAceptar = Result.failure(
            AppError.Negocio("INVITACION_OTRO_CORREO", "Esta invitación es para otro correo. Inicia sesión con el correo invitado."),
        )
        val vm = crear()
        vm.onTextoChange(TOKEN_DE_PRUEBA)
        vm.buscar()

        vm.aceptar()

        assertEquals("Esta invitación es para otro correo. Inicia sesión con el correo invitado.", vm.state.errorAlAceptar)
        assertFalse(vm.state.aceptada)
        // La invitación sigue a la vista.
        assertEquals(TOKEN_DE_PRUEBA, vm.state.invitacion!!.token)
    }
}
