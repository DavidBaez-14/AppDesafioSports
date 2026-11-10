package co.edu.ufps.desafiosports.presentacion.creador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SeccionCreadorState(
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    /** Los torneos que administra, con los que tienen algo por revisar primero. */
    val torneos: List<TorneoDeCreador>? = null,
    val error: ErrorDePantalla? = null,
    val aviso: String? = null,
) {
    val porRevisar: List<TorneoDeCreador> get() = torneos.orEmpty().filter { it.porRevisar > 0 }
}

/** Sección Creador: los torneos que administra quien inició sesión (MOV-0008). */
class SeccionCreadorViewModel(
    private val obtenerMisTorneosUseCase: ObtenerMisTorneosUseCase,
) : ViewModel() {

    var state by mutableStateOf(SeccionCreadorState())
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
        if (state.torneos == null) {
            cargar()
            return
        }
        state = state.copy(refrescando = true)
        pedir()
    }

    /** Al volver de crear un torneo o de revisar inscripciones: los conteos cambian. */
    fun alVolver() {
        if (trabajo?.isActive == true || state.torneos == null) return
        pedir()
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    private fun pedir() {
        trabajo = viewModelScope.launch {
            obtenerMisTorneosUseCase()
                .onSuccess { state = state.copy(cargando = false, refrescando = false, torneos = it, error = null) }
                .onFailure { error ->
                    state = if (state.torneos == null) {
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
                SeccionCreadorViewModel(app.container.obtenerMisTorneos)
            }
        }
    }
}
