package co.edu.ufps.desafiosports.presentacion.diagnostico

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
import co.edu.ufps.desafiosports.domain.model.Configuracion
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.IniciarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.domain.use_case.sistema.ComprobarServidorUseCase
import kotlinx.coroutines.launch

/**
 * Diagnóstico de conexión (MOV-0001): comprueba el backend, inicia sesión en Supabase
 * y consulta `GET /api/auth/me`. Pantalla temporal para validar el entorno.
 *
 * Patrón del profesor: estado inmutable en `mutableStateOf` con `private set`, cambios con copy()
 * y la pantalla solo llama funciones. Siempre ViewModel → caso de uso → repositorio.
 */
class DiagnosticoViewModel(
    configuracion: Configuracion,
    private val comprobarServidorUseCase: ComprobarServidorUseCase,
    private val observarSesionUseCase: ObservarSesionUseCase,
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val obtenerCuentaUseCase: ObtenerCuentaUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModel() {

    var state by mutableStateOf(
        DiagnosticoState(faltantes = configuracion.faltantes(), apiBaseUrl = configuracion.apiBaseUrl)
    )
        private set

    init {
        comprobarServidor()
        if (configuracion.completa) {
            observarSesion()
        } else {
            state = state.copy(sesion = EstadoSesion.Inactiva)
        }
    }

    fun onCorreoChange(valor: String) {
        state = state.copy(correo = valor)
    }

    fun onClaveChange(valor: String) {
        state = state.copy(clave = valor)
    }

    fun comprobarServidor() {
        viewModelScope.launch {
            state = state.copy(servidor = EstadoComprobacion.Comprobando)
            comprobarServidorUseCase()
                .onSuccess { state = state.copy(servidor = EstadoComprobacion.EnLinea(it.estado)) }
                .onFailure { state = state.copy(servidor = EstadoComprobacion.Fallo(it.mensajeParaMostrar())) }
        }
    }

    fun iniciarSesion() {
        if (state.enProceso) return
        viewModelScope.launch {
            state = state.copy(enProceso = true, error = null)
            iniciarSesionUseCase(state.correo, state.clave)
                .onSuccess {
                    state = state.copy(enProceso = false, clave = "")
                    consultarCuenta()
                }
                .onFailure { state = state.copy(enProceso = false, error = it.mensajeParaMostrar()) }
        }
    }

    fun consultarCuenta() {
        viewModelScope.launch {
            state = state.copy(enProceso = true, error = null)
            obtenerCuentaUseCase()
                .onSuccess { state = state.copy(enProceso = false, cuenta = it) }
                .onFailure { state = state.copy(enProceso = false, cuenta = null, error = it.mensajeParaMostrar()) }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            state = state.copy(enProceso = true, error = null)
            cerrarSesionUseCase()
                .onFailure { state = state.copy(error = it.mensajeParaMostrar()) }
            state = state.copy(enProceso = false, cuenta = null)
        }
    }

    private fun observarSesion() {
        viewModelScope.launch {
            observarSesionUseCase().collect { sesion ->
                state = state.copy(
                    sesion = sesion,
                    cuenta = if (sesion is EstadoSesion.Activa) state.cuenta else null,
                )
            }
        }
    }

    companion object {
        /** Toma las dependencias del AppContainer de la Application (patrón de CreationExtras). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val c = app.container
                DiagnosticoViewModel(
                    configuracion = c.configuracion,
                    comprobarServidorUseCase = c.comprobarServidor,
                    observarSesionUseCase = c.observarSesion,
                    iniciarSesionUseCase = c.iniciarSesion,
                    obtenerCuentaUseCase = c.obtenerCuenta,
                    cerrarSesionUseCase = c.cerrarSesion,
                )
            }
        }
    }
}

private fun Throwable.mensajeParaMostrar(): String =
    (this as? AppError)?.mensaje ?: AppError.Desconocido(this).mensaje
