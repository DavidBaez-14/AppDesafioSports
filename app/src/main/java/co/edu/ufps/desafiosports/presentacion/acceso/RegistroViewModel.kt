package co.edu.ufps.desafiosports.presentacion.acceso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.use_case.identidad.RegistrarseUseCase
import kotlinx.coroutines.launch

/** Crear cuenta (MOV-0003 · A-3). */
class RegistroViewModel(private val registrarseUseCase: RegistrarseUseCase) : ViewModel() {

    var state by mutableStateOf(RegistroState())
        private set

    fun onCorreoChange(valor: String) {
        state = state.copy(correo = valor, errorCorreo = null, error = null)
    }

    fun onClaveChange(valor: String) {
        state = state.copy(clave = valor, errorClave = null, error = null)
    }

    fun onAlternarClave() {
        state = state.copy(claveVisible = !state.claveVisible)
    }

    fun crearCuenta() {
        if (state.enProceso) return
        state = state.copy(enProceso = true, error = null, errorCorreo = null, errorClave = null)

        viewModelScope.launch {
            registrarseUseCase(state.correo, state.clave)
                .onSuccess { destino ->
                    state = state.copy(enProceso = false, correo = state.correo.trim(), clave = "", claveVisible = false, destino = destino)
                }
                .onFailure { error -> state = conError(error) }
        }
    }

    fun onDestinoAtendido() {
        state = state.copy(destino = null)
    }

    private fun conError(error: Throwable): RegistroState {
        val base = state.copy(enProceso = false)
        return when {
            error is AppError.Validacion && error.campo == CampoValidado.CORREO -> base.copy(errorCorreo = error.mensaje)
            error is AppError.Validacion && error.campo == CampoValidado.CLAVE -> base.copy(errorClave = error.mensaje)
            else -> base.copy(error = error.mensajeParaMostrar())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                RegistroViewModel(app.container.registrarse)
            }
        }
    }
}
