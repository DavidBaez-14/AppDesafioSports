package co.edu.ufps.desafiosports.presentacion.acceso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.use_case.identidad.ReenviarConfirmacionUseCase
import co.edu.ufps.desafiosports.presentacion.navigation.RevisaTuCorreo
import kotlinx.coroutines.launch

/** Estado de Revisa tu correo (MOV-0003 · N-1). */
data class RevisaTuCorreoState(
    val correo: String = "",
    val enProceso: Boolean = false,
    /** Ya se pidió otro envío: la pantalla lo confirma. */
    val reenviado: Boolean = false,
    val error: String? = null,
)

/** Revisa tu correo (MOV-0003 · N-1). */
class RevisaTuCorreoViewModel(
    correo: String,
    private val reenviarConfirmacionUseCase: ReenviarConfirmacionUseCase,
) : ViewModel() {

    var state by mutableStateOf(RevisaTuCorreoState(correo = correo))
        private set

    fun reenviar() {
        if (state.enProceso) return
        state = state.copy(enProceso = true, error = null, reenviado = false)

        viewModelScope.launch {
            reenviarConfirmacionUseCase(state.correo)
                .onSuccess { state = state.copy(enProceso = false, reenviado = true) }
                .onFailure { error -> state = state.copy(enProceso = false, error = error.mensajeParaMostrar()) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<RevisaTuCorreo>()
                RevisaTuCorreoViewModel(ruta.correo, app.container.reenviarConfirmacion)
            }
        }
    }
}
