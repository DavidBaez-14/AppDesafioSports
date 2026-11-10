package co.edu.ufps.desafiosports.presentacion.arbitro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.PartidosDeArbitro
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SeccionArbitroState(
    /** Primera carga (o reintento): todavía no hay nada que mostrar. */
    val cargando: Boolean = true,
    /** Se está actualizando con datos ya en pantalla. */
    val refrescando: Boolean = false,
    val partidos: PartidosDeArbitro? = null,
    /** Falló la primera carga: ocupa la pantalla con "Reintentar". */
    val error: ErrorDePantalla? = null,
    /** Falló una actualización: los datos anteriores siguen y esto sale en un aviso pasajero. */
    val aviso: String? = null,
)

/** Sección Árbitro: los partidos que arbitra quien inició sesión (MOV-0009). */
class SeccionArbitroViewModel(
    private val obtenerPartidosUseCase: ObtenerPartidosDeArbitroUseCase,
) : ViewModel() {

    var state by mutableStateOf(SeccionArbitroState())
        private set

    private var trabajo: Job? = null

    init {
        cargar()
    }

    /** Primera carga y "Reintentar". */
    fun cargar() {
        if (trabajo?.isActive == true) return
        state = state.copy(cargando = true, error = null)
        pedir()
    }

    /** Deslizar hacia abajo. */
    fun refrescar() {
        if (trabajo?.isActive == true) return
        if (state.partidos == null) {
            cargar()
            return
        }
        state = state.copy(refrescando = true)
        pedir()
    }

    /** La pantalla volvió a verse (por ejemplo, al regresar de la planilla): se actualiza sin indicador. */
    fun alVolver() {
        if (trabajo?.isActive == true || state.partidos == null) return
        pedir()
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    private fun pedir() {
        trabajo = viewModelScope.launch {
            obtenerPartidosUseCase()
                .onSuccess { state = state.copy(cargando = false, refrescando = false, partidos = it, error = null) }
                .onFailure { error ->
                    state = if (state.partidos == null) {
                        state.copy(cargando = false, refrescando = false, error = error.aErrorDePantalla())
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = error.mensajeParaMostrar())
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                SeccionArbitroViewModel(app.container.obtenerPartidosDeArbitro)
            }
        }
    }
}
