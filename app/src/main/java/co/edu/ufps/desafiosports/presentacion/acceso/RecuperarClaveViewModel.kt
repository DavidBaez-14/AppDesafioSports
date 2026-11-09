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
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.use_case.identidad.RecuperarClaveUseCase
import co.edu.ufps.desafiosports.presentacion.navigation.RecuperarClave
import kotlinx.coroutines.launch

/** Recuperar contraseña (MOV-0002 · N-3). Trae el correo que se escribió en el login. */
class RecuperarClaveViewModel(
    correoInicial: String,
    private val recuperarClaveUseCase: RecuperarClaveUseCase,
) : ViewModel() {

    var state by mutableStateOf(RecuperarClaveState(correo = correoInicial))
        private set

    fun onCorreoChange(valor: String) {
        state = state.copy(correo = valor, errorCorreo = null, error = null)
    }

    fun enviar() {
        if (state.enProceso) return
        state = state.copy(enProceso = true, errorCorreo = null, error = null)

        viewModelScope.launch {
            recuperarClaveUseCase(state.correo)
                .onSuccess { state = state.copy(enProceso = false, correo = state.correo.trim(), enviado = true) }
                .onFailure { error ->
                    state = if (error is AppError.Validacion && error.campo == CampoValidado.CORREO) {
                        state.copy(enProceso = false, errorCorreo = error.mensaje)
                    } else {
                        state.copy(enProceso = false, error = error.mensajeParaMostrar())
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<RecuperarClave>()
                RecuperarClaveViewModel(ruta.correo, app.container.recuperarClave)
            }
        }
    }
}
