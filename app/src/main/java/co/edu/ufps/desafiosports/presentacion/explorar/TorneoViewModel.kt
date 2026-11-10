package co.edu.ufps.desafiosports.presentacion.explorar

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
import co.edu.ufps.desafiosports.domain.model.AccionDelTorneo
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.model.TorneoDetalle
import co.edu.ufps.desafiosports.domain.model.accion
import co.edu.ufps.desafiosports.domain.use_case.competicion.AbrirTorneoPorEnlaceUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.navigation.Torneo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TorneoState(
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    val torneo: TorneoDetalle? = null,
    val conSesion: Boolean = false,
    /** El token con el que se abrió, si fue por enlace: inscribirse en un NO_LISTADO lo necesita. */
    val enlaceToken: String? = null,
    val error: ErrorDePantalla? = null,
    val aviso: String? = null,
) {
    val accion: AccionDelTorneo? get() = torneo?.accion(conSesion)
}

/** Detalle del torneo (MOV-0005): lo público es igual para todos; la franja depende de la relación. */
class TorneoViewModel(
    private val torneoId: String,
    enlaceToken: String,
    private val obtenerTorneoUseCase: ObtenerTorneoUseCase,
    private val abrirTorneoPorEnlaceUseCase: AbrirTorneoPorEnlaceUseCase,
    private val observarSesionUseCase: ObservarSesionUseCase,
) : ViewModel() {

    var state by mutableStateOf(TorneoState(enlaceToken = enlaceToken.ifBlank { null }))
        private set

    private var trabajo: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        if (trabajo?.isActive == true) return
        state = state.copy(cargando = true, error = null)
        pedir()
    }

    fun refrescar() {
        if (trabajo?.isActive == true) return
        if (state.torneo == null) return cargar()
        state = state.copy(refrescando = true)
        pedir()
    }

    /** Al volver a la pantalla (por ejemplo, tras iniciar sesión), la franja puede cambiar. */
    fun alVolver() {
        if (trabajo?.isActive == true || state.torneo == null) return
        pedir()
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    private fun pedir() {
        trabajo = viewModelScope.launch {
            val sesion = observarSesionUseCase().first { it != EstadoSesion.Cargando }
            val conSesion = sesion is EstadoSesion.Activa || sesion is EstadoSesion.SinRenovar
            val token = state.enlaceToken
            val resultado = if (token != null) {
                abrirTorneoPorEnlaceUseCase(token).map { it.second }
            } else {
                obtenerTorneoUseCase(torneoId)
            }
            resultado
                .onSuccess { torneo ->
                    state = state.copy(cargando = false, refrescando = false, torneo = torneo, conSesion = conSesion, error = null)
                }
                .onFailure { e ->
                    state = if (state.torneo == null) {
                        state.copy(cargando = false, refrescando = false, error = e.aErrorDePantalla(), conSesion = conSesion)
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = e.mensajeParaMostrar())
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<Torneo>()
                val c = app.container
                TorneoViewModel(
                    torneoId = ruta.torneoId,
                    enlaceToken = ruta.enlaceToken,
                    obtenerTorneoUseCase = c.obtenerTorneo,
                    abrirTorneoPorEnlaceUseCase = c.abrirTorneoPorEnlace,
                    observarSesionUseCase = c.observarSesion,
                )
            }
        }
    }
}
