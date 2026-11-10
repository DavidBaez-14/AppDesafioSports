package co.edu.ufps.desafiosports.presentacion.arbitro

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
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.JugadorPlanilla
import co.edu.ufps.desafiosports.domain.model.Lado
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.domain.use_case.competicion.AlinearEquipoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.AlinearJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CerrarPartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlanillaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDeAlineacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarEventoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RegistrarEventoUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.launch
import co.edu.ufps.desafiosports.presentacion.navigation.Planilla as RutaPlanilla

/** Aviso pasajero de la planilla. La pantalla lo convierte en texto (y en "Deshacer" si aplica). */
sealed interface AvisoPlanilla {
    /** Se anotó [evento]: se puede deshacer. */
    data class EventoRegistrado(val evento: EventoPartido) : AvisoPlanilla
    data object EventoQuitado : AvisoPlanilla
    data object PartidoCerrado : AvisoPlanilla

    /** La acción se guardó, pero la planilla no se pudo volver a pedir: lo que se ve puede estar viejo. */
    data object SinActualizar : AvisoPlanilla
    data class Fallo(val mensaje: String) : AvisoPlanilla
}

data class PlanillaState(
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    val planilla: Planilla? = null,
    /** Falló la carga: ocupa la pantalla. */
    val error: ErrorDePantalla? = null,
    /** El error es que no arbitra este partido: se ofrece "Volver" en vez de "Reintentar". */
    val sinPermiso: Boolean = false,
    /** Equipo al que se le anota el próximo evento. */
    val lado: Lado = Lado.LOCAL,
    /** Hay una acción viajando al backend: los controles no aceptan otra. */
    val enAccion: Boolean = false,
    val cerrando: Boolean = false,
    /** Tipo de evento para el que se está eligiendo jugador (hoja abierta). */
    val tipoEnSeleccion: String? = null,
    val alineacionAbierta: Boolean = false,
    val ladoAlineacion: Lado = Lado.LOCAL,
    /** Evento que espera confirmación para quitarse. */
    val eventoPorQuitar: EventoPartido? = null,
    val confirmandoCierre: Boolean = false,
    val aviso: AvisoPlanilla? = null,
) {
    /** Se pueden registrar eventos, cambiar la alineación y cerrar. */
    val editable: Boolean get() = planilla?.editable == true && planilla.partido.cruceDefinido
}

/**
 * Planilla del partido (MOV-0009). Cada acción va al backend y después se vuelve a pedir la
 * planilla: el marcador, la alineación y la lista de eventos siempre son los del servidor.
 */
class PlanillaViewModel(
    private val partidoId: String,
    private val obtenerPlanillaUseCase: ObtenerPlanillaUseCase,
    private val registrarEventoUseCase: RegistrarEventoUseCase,
    private val quitarEventoUseCase: QuitarEventoUseCase,
    private val alinearJugadorUseCase: AlinearJugadorUseCase,
    private val quitarDeAlineacionUseCase: QuitarDeAlineacionUseCase,
    private val alinearEquipoUseCase: AlinearEquipoUseCase,
    private val cerrarPartidoUseCase: CerrarPartidoUseCase,
) : ViewModel() {

    var state by mutableStateOf(PlanillaState())
        private set

    private var pidiendo = false

    init {
        cargar()
    }

    // ── Carga ───────────────────────────────────────────────────────────────

    /** Primera carga y "Reintentar". */
    fun cargar() {
        if (pidiendo) return
        state = state.copy(cargando = true, error = null, sinPermiso = false)
        pedir()
    }

    /** Deslizar hacia abajo. */
    fun refrescar() {
        if (pidiendo || state.enAccion) return
        if (state.planilla == null) {
            cargar()
            return
        }
        state = state.copy(refrescando = true)
        pedir()
    }

    private fun pedir() {
        pidiendo = true
        viewModelScope.launch {
            val resultado = obtenerPlanillaUseCase(partidoId)
            pidiendo = false
            resultado
                .onSuccess { state = state.copy(cargando = false, refrescando = false, planilla = it, error = null) }
                .onFailure { error ->
                    state = if (state.planilla == null) {
                        state.copy(cargando = false, refrescando = false, error = error.aErrorDePantalla(), sinPermiso = error.esSinPermiso())
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = AvisoPlanilla.Fallo(error.mensajeParaMostrar()))
                    }
                }
        }
    }

    // ── Eventos ─────────────────────────────────────────────────────────────

    fun elegirLado(lado: Lado) {
        state = state.copy(lado = lado)
    }

    /** Tocó un tipo de evento: se abre la hoja para elegir a quién. */
    fun abrirSeleccion(tipo: String) {
        if (!state.editable || state.enAccion) return
        state = state.copy(tipoEnSeleccion = tipo)
    }

    fun cerrarSeleccion() {
        state = state.copy(tipoEnSeleccion = null)
    }

    fun registrar(jugador: JugadorPlanilla) {
        val tipo = state.tipoEnSeleccion ?: return
        val equipo = state.planilla?.equipo(state.lado) ?: return
        state = state.copy(tipoEnSeleccion = null)
        accion(
            operacion = { registrarEventoUseCase(partidoId, jugador.personaId, equipo.equipoTorneoId, tipo) },
            alTerminar = { AvisoPlanilla.EventoRegistrado(it) },
        )
    }

    /** "Deshacer" del aviso: borra el evento que se acaba de anotar. */
    fun deshacer(eventoId: String) {
        accion(operacion = { quitarEventoUseCase(partidoId, eventoId) }, alTerminar = { AvisoPlanilla.EventoQuitado })
    }

    fun pedirQuitar(evento: EventoPartido) {
        if (!state.editable || state.enAccion) return
        state = state.copy(eventoPorQuitar = evento)
    }

    fun cancelarQuitar() {
        state = state.copy(eventoPorQuitar = null)
    }

    fun confirmarQuitar() {
        val evento = state.eventoPorQuitar ?: return
        state = state.copy(eventoPorQuitar = null)
        accion(operacion = { quitarEventoUseCase(partidoId, evento.id) }, alTerminar = { AvisoPlanilla.EventoQuitado })
    }

    // ── Alineación ──────────────────────────────────────────────────────────

    fun abrirAlineacion() {
        state = state.copy(alineacionAbierta = true, ladoAlineacion = state.lado)
    }

    fun cerrarAlineacion() {
        state = state.copy(alineacionAbierta = false)
    }

    fun elegirLadoAlineacion(lado: Lado) {
        state = state.copy(ladoAlineacion = lado)
    }

    /** Marca o desmarca a un jugador. A quien no es apto no se le puede marcar. */
    fun cambiarAlineacion(jugador: JugadorPlanilla) {
        val equipo = state.planilla?.equipo(state.ladoAlineacion) ?: return
        when {
            jugador.alineado ->
                accion(operacion = { quitarDeAlineacionUseCase(partidoId, jugador.personaId) }, alTerminar = { null })
            jugador.apto ->
                accion(operacion = { alinearJugadorUseCase(partidoId, jugador.personaId, equipo.equipoTorneoId) }, alTerminar = { null })
        }
    }

    fun alinearATodos() {
        val equipo = state.planilla?.equipo(state.ladoAlineacion) ?: return
        accion(operacion = { alinearEquipoUseCase(partidoId, equipo.equipoTorneoId) }, alTerminar = { null })
    }

    // ── Cierre ──────────────────────────────────────────────────────────────

    fun pedirCierre() {
        if (!state.editable || state.enAccion) return
        state = state.copy(confirmandoCierre = true)
    }

    fun cancelarCierre() {
        state = state.copy(confirmandoCierre = false)
    }

    fun confirmarCierre() {
        if (!state.confirmandoCierre) return
        state = state.copy(confirmandoCierre = false)
        accion(
            operacion = { cerrarPartidoUseCase(partidoId) },
            alTerminar = { AvisoPlanilla.PartidoCerrado },
            esCierre = true,
        )
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    /**
     * Envía una acción y vuelve a pedir la planilla. Mientras viaja no se acepta otra (el doble
     * toque no duplica eventos). Si falla, la planilla no cambia y el mensaje sale como aviso.
     */
    private fun <T> accion(
        operacion: suspend () -> Result<T>,
        alTerminar: (T) -> AvisoPlanilla?,
        esCierre: Boolean = false,
    ) {
        if (state.enAccion || !state.editable) return
        state = state.copy(enAccion = true, cerrando = esCierre)
        viewModelScope.launch {
            val resultado = operacion()
            val fallo = resultado.exceptionOrNull()
            // Sin conexión no tiene caso pedirla. En los demás casos sí: si la acción falló porque
            // el partido cambió (otro árbitro lo cerró, por ejemplo), la planilla vieja engañaría.
            val recarga = if (fallo is AppError.SinConexion) null else obtenerPlanillaUseCase(partidoId)
            val aviso = when {
                fallo != null -> AvisoPlanilla.Fallo(fallo.mensajeParaMostrar())
                recarga?.isSuccess != true -> AvisoPlanilla.SinActualizar
                else -> alTerminar(resultado.getOrThrow())
            }
            state = state.copy(
                enAccion = false,
                cerrando = false,
                planilla = recarga?.getOrNull() ?: state.planilla,
                aviso = aviso,
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<RutaPlanilla>()
                val c = app.container
                PlanillaViewModel(
                    partidoId = ruta.partidoId,
                    obtenerPlanillaUseCase = c.obtenerPlanilla,
                    registrarEventoUseCase = c.registrarEvento,
                    quitarEventoUseCase = c.quitarEvento,
                    alinearJugadorUseCase = c.alinearJugador,
                    quitarDeAlineacionUseCase = c.quitarDeAlineacion,
                    alinearEquipoUseCase = c.alinearEquipo,
                    cerrarPartidoUseCase = c.cerrarPartido,
                )
            }
        }
    }
}

/** 403: no es árbitro de este partido (o perdió el permiso). */
private fun Throwable.esSinPermiso(): Boolean =
    this is AppError.AccesoDenegado || (this as? AppError.Negocio)?.codigo == "NO_ARBITRO_DEL_PARTIDO"
