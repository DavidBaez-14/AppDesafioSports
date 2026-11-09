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
import co.edu.ufps.desafiosports.domain.model.DatosRegistro
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CompletarRegistroUseCase
import kotlinx.coroutines.launch

/** Estado de Completar registro (MOV-0003 · N-2). */
data class CompletarRegistroState(
    val nombres: String = "",
    val apellidos: String = "",
    val tipoDocumento: TipoDocumento? = null,
    val numeroDocumento: String = "",
    val enProceso: Boolean = false,
    val errorNombres: String? = null,
    val errorApellidos: String? = null,
    val errorDocumento: String? = null,
    val error: String? = null,
    /** Evento: el registro quedó hecho y la pantalla navega al inicio. */
    val registrado: Boolean = false,
)

/**
 * Completar registro (MOV-0003 · N-2). Se llega con sesión abierta en Supabase y sin cuenta
 * en el backend. "Salir" cierra la sesión; la navegación raíz vuelve al login sola.
 */
class CompletarRegistroViewModel(
    private val completarRegistroUseCase: CompletarRegistroUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModel() {

    var state by mutableStateOf(CompletarRegistroState())
        private set

    fun onNombresChange(valor: String) {
        state = state.copy(nombres = valor, errorNombres = null, error = null)
    }

    fun onApellidosChange(valor: String) {
        state = state.copy(apellidos = valor, errorApellidos = null, error = null)
    }

    fun onTipoDocumentoChange(valor: TipoDocumento?) {
        // "Sin documento" borra el número: el campo queda deshabilitado y no podría corregirse.
        state = state.copy(
            tipoDocumento = valor,
            numeroDocumento = if (valor == null) "" else state.numeroDocumento,
            errorDocumento = null,
            error = null,
        )
    }

    fun onNumeroDocumentoChange(valor: String) {
        state = state.copy(numeroDocumento = valor, errorDocumento = null, error = null)
    }

    fun terminar() {
        if (state.enProceso) return
        state = state.copy(enProceso = true, error = null, errorNombres = null, errorApellidos = null, errorDocumento = null)

        viewModelScope.launch {
            val datos = DatosRegistro(state.nombres, state.apellidos, state.tipoDocumento, state.numeroDocumento)
            completarRegistroUseCase(datos)
                .onSuccess { state = state.copy(enProceso = false, registrado = true) }
                .onFailure { error -> state = conError(error) }
        }
    }

    fun onRegistroAtendido() {
        state = state.copy(registrado = false)
    }

    fun salir() {
        viewModelScope.launch { cerrarSesionUseCase() }
    }

    private fun conError(error: Throwable): CompletarRegistroState {
        val base = state.copy(enProceso = false)
        if (error !is AppError.Validacion) return base.copy(error = error.mensajeParaMostrar())
        return when (error.campo) {
            CampoValidado.NOMBRES -> base.copy(errorNombres = error.mensaje)
            CampoValidado.APELLIDOS -> base.copy(errorApellidos = error.mensaje)
            CampoValidado.DOCUMENTO -> base.copy(errorDocumento = error.mensaje)
            else -> base.copy(error = error.mensaje)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                CompletarRegistroViewModel(app.container.completarRegistro, app.container.cerrarSesion)
            }
        }
    }
}
