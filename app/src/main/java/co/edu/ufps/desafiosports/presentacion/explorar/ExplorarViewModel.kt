package co.edu.ufps.desafiosports.presentacion.explorar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.TorneoResumen
import co.edu.ufps.desafiosports.domain.use_case.competicion.AbrirTorneoPorEnlaceUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ExplorarTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerDeportesUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Un torneo que se abrió con su enlace: la pantalla navega a él y avisa con [ExplorarViewModel.onTorneoAbierto]. */
data class TorneoPorAbrir(val torneoId: String, val enlaceToken: String)

data class ExplorarState(
    val deportes: List<Deporte> = emptyList(),
    /** Código del deporte elegido; null = todos. */
    val deporte: String? = null,
    val busqueda: String = "",
    val torneos: List<TorneoResumen>? = null,
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    val cargandoMas: Boolean = false,
    val pagina: Int = 0,
    val hayMas: Boolean = false,
    val error: ErrorDePantalla? = null,
    val aviso: String? = null,
    // Abrir con enlace (MOV-0005 · CA-3)
    val pidiendoEnlace: Boolean = false,
    val enlace: String = "",
    val errorEnlace: String? = null,
    val abriendoEnlace: Boolean = false,
    val torneoPorAbrir: TorneoPorAbrir? = null,
) {
    /** Sin resultados con un filtro puesto: el vacío explica que los torneos con enlace no salen. */
    val conFiltro: Boolean get() = deporte != null || busqueda.isNotBlank()
}

/** Explorar torneos públicos (MOV-0005): por deporte, por nombre o con un enlace. */
class ExplorarViewModel(
    private val explorarTorneosUseCase: ExplorarTorneosUseCase,
    private val obtenerDeportesUseCase: ObtenerDeportesUseCase,
    private val abrirTorneoPorEnlaceUseCase: AbrirTorneoPorEnlaceUseCase,
) : ViewModel() {

    var state by mutableStateOf(ExplorarState())
        private set

    private var trabajo: Job? = null

    init {
        cargarDeportes()
        cargar()
    }

    /** Los chips de deporte. Si fallan (por ejemplo, con el servidor despertando), se piden otra vez con la lista. */
    private fun cargarDeportes() {
        viewModelScope.launch {
            obtenerDeportesUseCase().onSuccess { state = state.copy(deportes = it) }
        }
    }

    /** Primera carga, "Reintentar" y cada cambio de filtro. */
    fun cargar() {
        state = state.copy(cargando = true, error = null)
        pedir(pagina = 0, retraso = 0)
    }

    fun refrescar() {
        if (state.torneos == null) return cargar()
        state = state.copy(refrescando = true)
        pedir(pagina = 0, retraso = 0)
    }

    fun elegirDeporte(codigo: String?) {
        if (codigo == state.deporte) return
        state = state.copy(deporte = codigo, cargando = true, error = null)
        pedir(pagina = 0, retraso = 0)
    }

    /** Busca mientras se escribe, con una pausa corta para no pedir por cada letra. */
    fun onBusquedaChange(texto: String) {
        state = state.copy(busqueda = texto)
        state = state.copy(cargando = state.torneos == null, error = null)
        pedir(pagina = 0, retraso = PAUSA_BUSQUEDA_MS)
    }

    fun cargarMas() {
        if (!state.hayMas || state.cargandoMas || trabajo?.isActive == true) return
        state = state.copy(cargandoMas = true)
        pedir(pagina = state.pagina + 1, retraso = 0)
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    // ── Abrir con enlace ────────────────────────────────────────────────────

    fun pedirEnlace() {
        state = state.copy(pidiendoEnlace = true, enlace = "", errorEnlace = null)
    }

    fun cancelarEnlace() {
        if (state.abriendoEnlace) return
        state = state.copy(pidiendoEnlace = false, errorEnlace = null)
    }

    fun onEnlaceChange(texto: String) {
        state = state.copy(enlace = texto, errorEnlace = null)
    }

    fun abrirEnlace() {
        if (state.abriendoEnlace) return
        state = state.copy(abriendoEnlace = true, errorEnlace = null)
        viewModelScope.launch {
            abrirTorneoPorEnlaceUseCase(state.enlace)
                .onSuccess { (token, torneo) ->
                    state = state.copy(
                        abriendoEnlace = false,
                        pidiendoEnlace = false,
                        torneoPorAbrir = TorneoPorAbrir(torneo.id, token),
                    )
                }
                .onFailure { state = state.copy(abriendoEnlace = false, errorEnlace = it.mensajeParaMostrar()) }
        }
    }

    fun onTorneoAbierto() {
        state = state.copy(torneoPorAbrir = null)
    }

    private fun pedir(pagina: Int, retraso: Long) {
        trabajo?.cancel()
        val deporte = state.deporte
        val busqueda = state.busqueda
        trabajo = viewModelScope.launch {
            if (retraso > 0) delay(retraso)
            explorarTorneosUseCase(deporte, busqueda, pagina)
                .onSuccess { p ->
                    if (state.deportes.isEmpty()) cargarDeportes()
                    val previos = if (pagina == 0) emptyList() else state.torneos.orEmpty()
                    state = state.copy(
                        torneos = (previos + p.contenido).distinctBy { it.id },
                        pagina = p.pagina,
                        hayMas = p.hayMas,
                        cargando = false,
                        refrescando = false,
                        cargandoMas = false,
                        error = null,
                    )
                }
                .onFailure { e ->
                    state = if (state.torneos == null || (pagina == 0 && !state.refrescando)) {
                        state.copy(cargando = false, refrescando = false, cargandoMas = false, error = e.aErrorDePantalla(), torneos = null)
                    } else {
                        state.copy(cargando = false, refrescando = false, cargandoMas = false, aviso = e.mensajeParaMostrar())
                    }
                }
        }
    }

    companion object {
        const val PAUSA_BUSQUEDA_MS = 350L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val c = app.container
                ExplorarViewModel(c.explorarTorneos, c.obtenerDeportes, c.abrirTorneoPorEnlace)
            }
        }
    }
}
