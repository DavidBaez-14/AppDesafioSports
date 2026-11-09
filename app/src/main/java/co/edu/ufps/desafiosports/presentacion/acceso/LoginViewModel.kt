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
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import kotlinx.coroutines.launch

/**
 * Login con correo (MOV-0002 · A-2).
 *
 * Entra en dos pasos: Supabase acepta las credenciales y luego `GET /api/auth/me` confirma
 * que la cuenta puede usar la app. Una cuenta sin registro completo o bloqueada cierra la
 * sesión y se queda aquí con el mensaje del backend (CA-5).
 */
class LoginViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val obtenerCuentaUseCase: ObtenerCuentaUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModel() {

    var state by mutableStateOf(LoginState())
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

    fun entrar() {
        // El estado cambia antes de lanzar la corrutina: un segundo toque ya lo ve (CA-7).
        if (state.enProceso) return
        state = state.copy(enProceso = true, error = null, errorCorreo = null, errorClave = null)

        viewModelScope.launch {
            val inicio = iniciarSesionUseCase(state.correo, state.clave)
            inicio.exceptionOrNull()?.let { error ->
                state = conError(error)
                return@launch
            }

            val cuenta = obtenerCuentaUseCase()
            val errorCuenta = cuenta.exceptionOrNull()
            if (errorCuenta is AppError.CuentaNoRegistrada || errorCuenta is AppError.CuentaBloqueada) {
                cerrarSesionUseCase()
                state = state.copy(enProceso = false, clave = "", error = errorCuenta.mensajeParaMostrar())
                return@launch
            }
            // Cualquier otro error de /me (servidor caído, por ejemplo) no impide entrar: la sesión
            // de Supabase es válida y el inicio muestra el estado del servidor.
            state = state.copy(enProceso = false, clave = "", claveVisible = false, ingresado = true)
        }
    }

    fun onIngresoAtendido() {
        state = state.copy(ingresado = false)
    }

    private fun conError(error: Throwable): LoginState {
        val base = state.copy(enProceso = false)
        return if (error is AppError.Validacion) {
            when (error.campo) {
                CampoValidado.CORREO -> base.copy(errorCorreo = error.mensaje)
                CampoValidado.CLAVE -> base.copy(errorClave = error.mensaje)
                null -> base.copy(error = error.mensaje)
            }
        } else {
            base.copy(error = error.mensajeParaMostrar())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val c = app.container
                LoginViewModel(c.iniciarSesion, c.obtenerCuenta, c.cerrarSesion)
            }
        }
    }
}
