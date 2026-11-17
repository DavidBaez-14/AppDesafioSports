package co.edu.ufps.desafiosports.presentacion.creador

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
import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.EstadoDePartidos
import co.edu.ufps.desafiosports.domain.model.GrupoDePartidos
import co.edu.ufps.desafiosports.domain.model.PartidoDeTorneo
import co.edu.ufps.desafiosports.domain.model.porFaseYJornada
import co.edu.ufps.desafiosports.domain.use_case.competicion.AsignarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.GenerarPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPersonalDeArbitrajeUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarArbitroDePartidoUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.navigation.PartidosTorneo
import kotlinx.coroutines.launch

data class PartidosState(
    val torneo: String,
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    /** Null hasta la primera respuesta. */
    val estado: EstadoDePartidos? = null,
    val partidos: List<PartidoDeTorneo> = emptyList(),
    val error: ErrorDePantalla? = null,
    /** El error es que no administra este torneo: se ofrece "Volver". */
    val sinPermiso: Boolean = false,
    // Generar los partidos
    /** Diálogo abierto: generar inicia el torneo y cierra las inscripciones. */
    val confirmandoGenerar: Boolean = false,
    val generando: Boolean = false,
    // Asignar árbitros (hoja)
    /** Id del partido con la hoja abierta. */
    val asignando: String? = null,
    /** Los árbitros del torneo; null hasta que se abre la hoja por primera vez. */
    val arbitros: List<ArbitroDeTorneo>? = null,
    val cargandoArbitros: Boolean = false,
    /** Cuentas con un cambio viajando en el partido abierto. */
    val enviando: Set<String> = emptySet(),
    /** Error dentro de la hoja: un aviso normal quedaría tapado por ella. */
    val errorAsignacion: String? = null,
    val aviso: String? = null,
) {
    val grupos: List<GrupoDePartidos> get() = partidos.porFaseYJornada()
    val partidoAsignando: PartidoDeTorneo? get() = partidos.firstOrNull { it.id == asignando }

    /** Alguien arbitra todos los partidos: ninguno está "sin árbitro". */
    val hayArbitroGeneral: Boolean get() = arbitros.orEmpty().any { it.todosLosPartidos }

    /** Solo se afirma cuando ya se sabe quién arbitra en el torneo. */
    fun sinArbitro(partido: PartidoDeTorneo): Boolean =
        arbitros != null && partido.admiteArbitros && partido.arbitros.isEmpty() && !hayArbitroGeneral
}

/** Partidos de un torneo que la persona administra: generarlos y ponerles árbitros (MOV-0011). */
class PartidosTorneoViewModel(
    private val torneoId: String,
    torneoNombre: String,
    private val obtenerPartidosUseCase: ObtenerPartidosDeTorneoUseCase,
    private val generarPartidosUseCase: GenerarPartidosUseCase,
    private val obtenerPersonalUseCase: ObtenerPersonalDeArbitrajeUseCase,
    private val asignarArbitroUseCase: AsignarArbitroUseCase,
    private val quitarArbitroDePartidoUseCase: QuitarArbitroDePartidoUseCase,
) : ViewModel() {

    var state by mutableStateOf(PartidosState(torneo = torneoNombre))
        private set

    private var pidiendo = false

    init {
        cargar()
    }

    /** Primera carga y "Reintentar". */
    fun cargar() {
        if (pidiendo) return
        state = state.copy(cargando = true, error = null, sinPermiso = false)
        pedir()
    }

    /** Deslizar hacia abajo. */
    fun refrescar() {
        if (pidiendo || state.generando) return
        if (state.estado == null) {
            cargar()
            return
        }
        state = state.copy(refrescando = true)
        pedir()
    }

    /**
     * Al volver de invitar árbitros o de una planilla: se vuelven a pedir los partidos (el marcador
     * y el estado pudieron cambiar, MOV-0012) y quién arbitra.
     */
    fun alVolver() {
        if (state.estado != null && state.asignando == null && !pidiendo && !state.generando) pedir()
        if (state.asignando != null || state.arbitros == null) return
        state = state.copy(arbitros = null)
        cargarArbitros()
    }

    private fun pedir() {
        pidiendo = true
        viewModelScope.launch {
            val resultado = obtenerPartidosUseCase(torneoId)
            pidiendo = false
            resultado
                .onSuccess {
                    state = state.copy(cargando = false, refrescando = false, estado = it.estado, partidos = it.partidos, error = null)
                    // Con partidos hace falta saber quién arbitra para decir cuáles están sin árbitro.
                    if (it.partidos.isNotEmpty() && state.arbitros == null) cargarArbitros()
                }
                .onFailure { error ->
                    state = if (state.estado == null) {
                        state.copy(
                            cargando = false, refrescando = false,
                            error = error.aErrorDePantalla(), sinPermiso = error.noAdministraElTorneo(),
                        )
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = error.mensajeParaMostrar())
                    }
                }
        }
    }

    // ── Generar los partidos ────────────────────────────────────────────────

    /** Con las inscripciones abiertas pide confirmar: generar los partidos inicia el torneo. */
    fun pedirGenerar() {
        val estado = state.estado ?: return
        if (state.generando || !estado.puedeGenerar) return
        if (estado.generarIniciaElTorneo) {
            state = state.copy(confirmandoGenerar = true)
        } else {
            generar()
        }
    }

    fun cancelarGenerar() {
        state = state.copy(confirmandoGenerar = false)
    }

    fun confirmarGenerar() {
        if (!state.confirmandoGenerar) return
        state = state.copy(confirmandoGenerar = false)
        generar()
    }

    /** Genera y vuelve a pedir todo: aunque falle, la pantalla muestra el estado real del torneo. */
    private fun generar() {
        if (state.generando) return
        state = state.copy(generando = true)
        viewModelScope.launch {
            val fallo = generarPartidosUseCase(torneoId).exceptionOrNull()
            val recarga = obtenerPartidosUseCase(torneoId).getOrNull()
            state = state.copy(
                generando = false,
                estado = recarga?.estado ?: state.estado,
                partidos = recarga?.partidos ?: state.partidos,
                aviso = fallo?.mensajeParaMostrar(),
            )
            if (state.partidos.isNotEmpty() && state.arbitros == null) cargarArbitros()
        }
    }

    // ── Asignar árbitros ────────────────────────────────────────────────────

    fun abrirAsignacion(partido: PartidoDeTorneo) {
        if (!partido.admiteArbitros) return
        state = state.copy(asignando = partido.id, errorAsignacion = null, enviando = emptySet())
        if (state.arbitros == null) cargarArbitros()
    }

    fun cerrarAsignacion() {
        state = state.copy(asignando = null, errorAsignacion = null)
    }

    /** "Reintentar" dentro de la hoja. */
    fun cargarArbitros() {
        if (state.cargandoArbitros) return
        state = state.copy(cargandoArbitros = true, errorAsignacion = null)
        viewModelScope.launch {
            obtenerPersonalUseCase(torneoId)
                .onSuccess { state = state.copy(cargandoArbitros = false, arbitros = it.arbitros) }
                .onFailure { state = state.copy(cargandoArbitros = false, errorAsignacion = it.mensajeParaMostrar()) }
        }
    }

    /**
     * Marca o desmarca a un árbitro en el partido abierto. La casilla cambia al tocarla; el
     * partido queda con lo que responde el backend y, si lo rechaza, vuelve a como estaba.
     */
    fun alternarArbitro(arbitro: ArbitroDeTorneo) {
        val partido = state.partidoAsignando ?: return
        // Quien arbitra todos los partidos no necesita asignación.
        if (arbitro.todosLosPartidos || arbitro.cuentaId in state.enviando) return
        val antes = partido.arbitros
        val estaba = antes.any { it.cuentaId == arbitro.cuentaId }
        val despues = if (estaba) {
            antes.filterNot { it.cuentaId == arbitro.cuentaId }
        } else {
            antes + ArbitroAsignado(arbitro.cuentaId, arbitro.nombre)
        }
        cambiarArbitros(partido.id) { despues }
        state = state.copy(enviando = state.enviando + arbitro.cuentaId, errorAsignacion = null)
        viewModelScope.launch {
            val resultado = if (estaba) {
                quitarArbitroDePartidoUseCase(partido.id, arbitro.cuentaId)
            } else {
                asignarArbitroUseCase(partido.id, arbitro.cuentaId)
            }
            resultado
                .onSuccess { confirmados -> cambiarArbitros(partido.id) { confirmados } }
                .onFailure { error ->
                    // Solo se deshace este cambio: otro que siga viajando conserva el suyo.
                    cambiarArbitros(partido.id) { actuales ->
                        if (estaba) {
                            if (actuales.any { it.cuentaId == arbitro.cuentaId }) actuales else actuales + ArbitroAsignado(arbitro.cuentaId, arbitro.nombre)
                        } else {
                            actuales.filterNot { it.cuentaId == arbitro.cuentaId }
                        }
                    }
                    state = state.copy(errorAsignacion = error.mensajeParaMostrar())
                }
            state = state.copy(enviando = state.enviando - arbitro.cuentaId)
        }
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    private fun cambiarArbitros(partidoId: String, cambio: (List<ArbitroAsignado>) -> List<ArbitroAsignado>) {
        state = state.copy(partidos = state.partidos.map { if (it.id == partidoId) it.copy(arbitros = cambio(it.arbitros)) else it })
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<PartidosTorneo>()
                val c = app.container
                PartidosTorneoViewModel(
                    torneoId = ruta.torneoId,
                    torneoNombre = ruta.nombre,
                    obtenerPartidosUseCase = c.obtenerPartidosDeTorneo,
                    generarPartidosUseCase = c.generarPartidos,
                    obtenerPersonalUseCase = c.obtenerPersonalDeArbitraje,
                    asignarArbitroUseCase = c.asignarArbitro,
                    quitarArbitroDePartidoUseCase = c.quitarArbitroDePartido,
                )
            }
        }
    }
}
