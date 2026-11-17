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
import co.edu.ufps.desafiosports.domain.use_case.competicion.ReabrirPartidoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RegistrarEventoUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import co.edu.ufps.desafiosports.presentacion.navigation.Planilla as RutaPlanilla

/** Aviso pasajero de la planilla. La pantalla lo convierte en texto (y en "Deshacer" si aplica). */
sealed interface AvisoPlanilla {
    /** Se anotó [evento]: se puede deshacer. */
    data class EventoRegistrado(val evento: EventoPartido) : AvisoPlanilla
    data object EventoQuitado : AvisoPlanilla
    data object PartidoCerrado : AvisoPlanilla
    data object PartidoReabierto : AvisoPlanilla

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
    /** Diálogo abierto: reabrir el partido cerrado (solo quien organiza el torneo). */
    val confirmandoReabrir: Boolean = false,
    val reabriendo: Boolean = false,
    val aviso: AvisoPlanilla? = null,
    /** Jugadores con un cambio de alineación que el servidor aún no confirma. */
    val alineando: Set<String> = emptySet(),
    /** Por qué no se guardó el último cambio de alineación. Se muestra dentro de la hoja. */
    val errorAlineacion: String? = null,
) {
    /** Se pueden registrar eventos, cambiar la alineación y cerrar. */
    val editable: Boolean get() = planilla?.editable == true && planilla.partido.cruceDefinido

    /** El partido está cerrado y quien lo ve organiza el torneo: puede reabrirlo (MOV-0012). */
    val puedeReabrir: Boolean get() = planilla?.puedeReabrir == true && !editable
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
    private val reabrirPartidoUseCase: ReabrirPartidoUseCase,
) : ViewModel() {

    var state by mutableStateOf(PlanillaState())
        private set

    private var pidiendo = false

    // Alineación: cada casilla cambia al instante y los cambios viajan de uno en uno, en orden.
    /** Lo que la persona dejó marcado y aún no sale al servidor: personaId → alineado. */
    private val porEnviar = LinkedHashMap<String, Boolean>()

    /** El cambio que está viajando ahora. */
    private var enVuelo: Pair<String, Boolean>? = null

    /** La alineación que el servidor tiene confirmada. */
    private val enServidor = HashMap<String, Boolean>()
    private var trabajoAlineacion: Job? = null

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
                .onSuccess { state = state.copy(cargando = false, refrescando = false, planilla = recibir(it), error = null) }
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

    /** Desde la hoja de un evento sin nadie alineado: se pasa lista del equipo elegido (MOV-0012). */
    fun irAAlineacion() {
        state = state.copy(tipoEnSeleccion = null, alineacionAbierta = true, ladoAlineacion = state.lado)
    }

    fun cerrarAlineacion() {
        state = state.copy(alineacionAbierta = false, errorAlineacion = null)
    }

    fun elegirLadoAlineacion(lado: Lado) {
        state = state.copy(ladoAlineacion = lado)
    }

    /**
     * Marca o desmarca a un jugador. A quien no es apto no se le puede marcar.
     *
     * La casilla cambia ya, sin esperar al servidor, y el cambio se pone en cola: se puede pasar
     * lista de corrido aunque el servidor tarde. Si un cambio no se guarda, esa casilla vuelve a
     * como estaba y el motivo queda en [PlanillaState.errorAlineacion].
     */
    fun cambiarAlineacion(jugador: JugadorPlanilla) {
        if (!state.editable || state.enAccion) return
        val planilla = state.planilla ?: return
        // Se mira la casilla como está en pantalla, no la copia que traía la fila al dibujarse.
        val actual = planilla.equipoDe(jugador.personaId)?.jugadores?.firstOrNull { it.personaId == jugador.personaId } ?: return
        val quiere = !actual.alineado
        if (quiere && !actual.apto) return

        porEnviar[actual.personaId] = quiere
        state = state.copy(
            planilla = planilla.conAlineado(actual.personaId, quiere),
            alineando = pendientes(),
            errorAlineacion = null,
        )
        if (trabajoAlineacion?.isActive != true) trabajoAlineacion = viewModelScope.launch { enviarAlineacion() }
    }

    fun alinearATodos() {
        // Con cambios sueltos en camino se espera: mezclarlos dejaría casillas a medias.
        if (pendientes().isNotEmpty()) return
        val equipo = state.planilla?.equipo(state.ladoAlineacion) ?: return
        state = state.copy(errorAlineacion = null)
        accion(operacion = { alinearEquipoUseCase(partidoId, equipo.equipoTorneoId) }, alTerminar = { null })
    }

    /** Envía la cola de cambios, uno a la vez, y al terminar vuelve a pedir la planilla. */
    private suspend fun enviarAlineacion() {
        // Mientras se vuelve a pedir la planilla pueden llegar más toques: se repite hasta vaciar la cola.
        do {
            val guardados = enviarPendientes()
            // Los aptos, los goles y los conteos son siempre los del servidor.
            if (guardados > 0) {
                obtenerPlanillaUseCase(partidoId).onSuccess { state = state.copy(planilla = recibir(it)) }
            }
        } while (porEnviar.isNotEmpty())
    }

    /** Envía lo que hay en la cola y dice cuántos cambios quedaron guardados. */
    private suspend fun enviarPendientes(): Int {
        var guardados = 0
        while (true) {
            val (personaId, quiere) = porEnviar.entries.firstOrNull()?.toPair() ?: break
            porEnviar.remove(personaId)
            // Marcó y desmarcó antes de que saliera: no hay nada que enviar.
            if ((enServidor[personaId] ?: false) == quiere) {
                state = state.copy(alineando = pendientes())
                continue
            }
            enVuelo = personaId to quiere
            val resultado = if (quiere) {
                val equipo = state.planilla?.equipoDe(personaId)?.equipoTorneoId.orEmpty()
                alinearJugadorUseCase(partidoId, personaId, equipo)
            } else {
                quitarDeAlineacionUseCase(partidoId, personaId)
            }
            enVuelo = null
            val fallo = resultado.exceptionOrNull()
            // Quitar a quien ya no estaba es lo que se quería: no es un error.
            if (fallo == null || (!quiere && fallo is AppError.NoEncontrado)) {
                enServidor[personaId] = quiere
                guardados++
            } else {
                // Sin conexión no tiene caso seguir: todo lo pendiente vuelve a como estaba.
                if (fallo is AppError.SinConexion) porEnviar.clear()
                state = state.copy(
                    planilla = state.planilla?.let { conPendientes(conLoDelServidor(it)) },
                    errorAlineacion = fallo.mensajeParaMostrar(),
                )
            }
            state = state.copy(alineando = pendientes())
        }
        return guardados
    }

    private fun pendientes(): Set<String> = porEnviar.keys + listOfNotNull(enVuelo?.first)

    /** Llega una planilla del servidor: se anota su alineación y se conserva lo que aún no se ha enviado. */
    private fun recibir(planilla: Planilla): Planilla {
        enServidor.clear()
        (planilla.local.jugadores + planilla.visitante.jugadores).forEach { enServidor[it.personaId] = it.alineado }
        return conPendientes(planilla)
    }

    private fun conPendientes(planilla: Planilla): Planilla {
        var resultado = planilla
        enVuelo?.let { (id, quiere) -> resultado = resultado.conAlineado(id, quiere) }
        porEnviar.forEach { (id, quiere) -> resultado = resultado.conAlineado(id, quiere) }
        return resultado
    }

    private fun conLoDelServidor(planilla: Planilla): Planilla {
        var resultado = planilla
        enServidor.forEach { (id, alineado) -> resultado = resultado.conAlineado(id, alineado) }
        return resultado
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

    // ── Reabrir (MOV-0012) ──────────────────────────────────────────────────

    fun pedirReabrir() {
        if (!state.puedeReabrir || state.enAccion) return
        state = state.copy(confirmandoReabrir = true)
    }

    fun cancelarReabrir() {
        state = state.copy(confirmandoReabrir = false)
    }

    fun confirmarReabrir() {
        if (!state.confirmandoReabrir) return
        state = state.copy(confirmandoReabrir = false)
        accion(
            operacion = { reabrirPartidoUseCase(partidoId) },
            alTerminar = { AvisoPlanilla.PartidoReabierto },
            esReapertura = true,
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
        esReapertura: Boolean = false,
    ) {
        // Reabrir es lo único que se hace con el partido cerrado.
        val permitido = if (esReapertura) state.puedeReabrir else state.editable
        if (state.enAccion || !permitido) return
        state = state.copy(enAccion = true, cerrando = esCierre, reabriendo = esReapertura)
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
                reabriendo = false,
                planilla = recarga?.getOrNull()?.let { recibir(it) } ?: state.planilla,
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
                    reabrirPartidoUseCase = c.reabrirPartido,
                )
            }
        }
    }
}

/** 403: no es árbitro de este partido (o perdió el permiso). */
private fun Throwable.esSinPermiso(): Boolean =
    this is AppError.AccesoDenegado || (this as? AppError.Negocio)?.codigo == "NO_ARBITRO_DEL_PARTIDO"
