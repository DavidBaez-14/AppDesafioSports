package co.edu.ufps.desafiosports.presentacion.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.InvitacionRecibida
import co.edu.ufps.desafiosports.domain.use_case.identidad.AceptarInvitacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerMisInvitacionesUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.VerInvitacionUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import kotlinx.coroutines.launch

data class InvitacionState(
    /** Las invitaciones que le hicieron por su correo y aún no acepta (MOV-0012). */
    val pendientes: List<InvitacionRecibida> = emptyList(),
    /** El enlace o el código que pega la persona. */
    val texto: String = "",
    val buscando: Boolean = false,
    /** Va bajo el campo: enlace que no sirve, invitación que no existe, sin conexión. */
    val error: String? = null,
    /** La invitación encontrada; null mientras no se busca. */
    val invitacion: InvitacionRecibida? = null,
    val aceptando: Boolean = false,
    val errorAlAceptar: String? = null,
    /** Ya se aceptó: la pantalla muestra la confirmación con "Listo". */
    val aceptada: Boolean = false,
)

/**
 * Aceptar una invitación (MOV-0011): las que le hicieron por su correo salen solas (MOV-0012) y
 * las de enlace se pegan.
 */
class InvitacionViewModel(
    private val verInvitacionUseCase: VerInvitacionUseCase,
    private val aceptarInvitacionUseCase: AceptarInvitacionUseCase,
    private val obtenerMisInvitacionesUseCase: ObtenerMisInvitacionesUseCase,
) : ViewModel() {

    var state by mutableStateOf(InvitacionState())
        private set

    init {
        // Si falla (sin conexión, backend viejo) la pantalla sigue sirviendo para pegar un enlace.
        viewModelScope.launch {
            obtenerMisInvitacionesUseCase().onSuccess { state = state.copy(pendientes = it) }
        }
    }

    /** "Aceptar" en una de las invitaciones que llegaron por correo. */
    fun aceptarPendiente(invitacion: InvitacionRecibida) {
        if (state.buscando || state.aceptando || state.aceptada) return
        state = state.copy(invitacion = invitacion, texto = "", error = null, errorAlAceptar = null)
        aceptar()
    }

    /** Cambiar el texto descarta la invitación que se estaba viendo. */
    fun onTextoChange(valor: String) {
        if (state.aceptando || state.aceptada) return
        state = state.copy(texto = valor.take(TEXTO_MAXIMO), error = null, invitacion = null, errorAlAceptar = null)
    }

    fun buscar() {
        if (state.buscando || state.aceptando || state.aceptada) return
        state = state.copy(buscando = true, error = null, invitacion = null, errorAlAceptar = null)
        viewModelScope.launch {
            verInvitacionUseCase(state.texto)
                .onSuccess { state = state.copy(buscando = false, invitacion = it) }
                .onFailure { state = state.copy(buscando = false, error = it.mensajeParaMostrar()) }
        }
    }

    fun aceptar() {
        val invitacion = state.invitacion ?: return
        if (state.aceptando || state.aceptada || !invitacion.pendiente) return
        state = state.copy(aceptando = true, errorAlAceptar = null)
        viewModelScope.launch {
            aceptarInvitacionUseCase(invitacion.token)
                .onSuccess { state = state.copy(aceptando = false, aceptada = true) }
                .onFailure { state = state.copy(aceptando = false, errorAlAceptar = it.mensajeParaMostrar()) }
        }
    }

    companion object {
        const val TEXTO_MAXIMO = 500

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val c = app.container
                InvitacionViewModel(c.verInvitacion, c.aceptarInvitacion, c.obtenerMisInvitaciones)
            }
        }
    }
}
