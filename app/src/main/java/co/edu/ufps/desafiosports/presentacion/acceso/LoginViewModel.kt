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
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionConGoogleUseCase
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
    private val iniciarSesionConGoogleUseCase: IniciarSesionConGoogleUseCase,
    private val obtenerCuentaUseCase: ObtenerCuentaUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModel() {

    /**
     * Antes de abrir el selector de Google: deja el login en proceso para que un segundo toque no
     * abra otro. Devuelve false si ya había algo en proceso (MOV-0006 · CA-2).
     */
    fun iniciarGoogle(): Boolean {
        if (state.enProceso) return false
        state = state.copy(enProceso = true, error = null, errorCorreo = null, errorClave = null)
        return true
    }

    fun onResultadoGoogle(resultado: ResultadoGoogle) {
        when (resultado) {
            ResultadoGoogle.Cancelado -> state = state.copy(enProceso = false)
            is ResultadoGoogle.Fallo -> state = state.copy(enProceso = false, error = resultado.mensaje)
            is ResultadoGoogle.Token -> viewModelScope.launch {
                iniciarSesionConGoogleUseCase(resultado.idToken, resultado.nonce)
                    .onSuccess { comprobarCuenta() }
                    .onFailure { error -> state = state.copy(enProceso = false, error = error.mensajeParaMostrar()) }
            }
        }
    }

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
            comprobarCuenta()
        }
    }

    /** Con la sesión de Supabase abierta (correo o Google), `/api/auth/me` decide a dónde ir. */
    private suspend fun comprobarCuenta() {
        val errorCuenta = obtenerCuentaUseCase().exceptionOrNull()
        // Correo confirmado pero sin registro completo: la sesión sigue y toca completar registro (MOV-0003 · CA-7).
        if (errorCuenta is AppError.CuentaNoRegistrada) {
            state = state.copy(enProceso = false, clave = "", claveVisible = false, debeCompletarRegistro = true)
            return
        }
        if (errorCuenta is AppError.CuentaBloqueada) {
            cerrarSesionUseCase()
            state = state.copy(enProceso = false, clave = "", error = errorCuenta.mensajeParaMostrar())
            return
        }
        // Cualquier otro error de /me (servidor caído, por ejemplo) no impide entrar: la sesión
        // de Supabase es válida y el inicio muestra el estado del servidor.
        state = state.copy(enProceso = false, clave = "", claveVisible = false, ingresado = true)
    }

    fun onIngresoAtendido() {
        state = state.copy(ingresado = false)
    }

    fun onCompletarRegistroAtendido() {
        state = state.copy(debeCompletarRegistro = false)
    }

    private fun conError(error: Throwable): LoginState {
        val base = state.copy(enProceso = false)
        return if (error is AppError.Validacion) {
            when (error.campo) {
                CampoValidado.CORREO -> base.copy(errorCorreo = error.mensaje)
                CampoValidado.CLAVE -> base.copy(errorClave = error.mensaje)
                else -> base.copy(error = error.mensaje)
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
                LoginViewModel(c.iniciarSesion, c.iniciarSesionConGoogle, c.obtenerCuenta, c.cerrarSesion)
            }
        }
    }
}
