package co.edu.ufps.desafiosports.presentacion.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.Configuracion
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObservarSesionUseCase
import kotlinx.coroutines.launch

/** A dónde va la app al abrir (MOV-0002 · CA-1). */
enum class DestinoRaiz {
    LOGIN,

    /** Con sesión. Hoy es el diagnóstico (inicio provisional); Inicio llega con su propia spec. */
    INICIO,

    /** Falta configurar local.properties: el diagnóstico dice qué. */
    DIAGNOSTICO,
}

/**
 * Vive a nivel de la Activity: decide la primera pantalla y avisa cuando se cierra la sesión
 * para volver al login desde donde sea (MOV-0002 · CA-1 y CA-10).
 *
 * Iniciar sesión NO dispara navegación aquí: la pantalla de login navega sola después de
 * comprobar la cuenta con /api/auth/me.
 */
class RaizViewModel(
    configuracion: Configuracion,
    private val observarSesionUseCase: ObservarSesionUseCase,
) : ViewModel() {

    var destinoInicial by mutableStateOf<DestinoRaiz?>(null)
        private set

    /** Evento: había sesión y ya no. La navegación vuelve al login y llama a onSesionCerradaAtendida(). */
    var sesionCerrada by mutableStateOf(false)
        private set

    private var anterior: EstadoSesion = EstadoSesion.Cargando

    init {
        if (configuracion.completa) {
            observarSesion()
        } else {
            destinoInicial = DestinoRaiz.DIAGNOSTICO
        }
    }

    fun onSesionCerradaAtendida() {
        sesionCerrada = false
    }

    private fun observarSesion() {
        viewModelScope.launch {
            observarSesionUseCase().collect { actual ->
                if (destinoInicial == null && actual != EstadoSesion.Cargando) {
                    destinoInicial = if (actual.tieneSesion()) DestinoRaiz.INICIO else DestinoRaiz.LOGIN
                }
                if (anterior.tieneSesion() && actual == EstadoSesion.Inactiva) {
                    sesionCerrada = true
                }
                anterior = actual
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                RaizViewModel(app.container.configuracion, app.container.observarSesion)
            }
        }
    }
}

private fun EstadoSesion.tieneSesion() = this is EstadoSesion.Activa || this is EstadoSesion.SinRenovar
