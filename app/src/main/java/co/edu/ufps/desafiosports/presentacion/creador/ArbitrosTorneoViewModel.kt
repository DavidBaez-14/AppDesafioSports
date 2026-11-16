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
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.InvitacionDeTorneo
import co.edu.ufps.desafiosports.domain.model.PersonalDeArbitraje
import co.edu.ufps.desafiosports.domain.model.correoDeInvitacionValido
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarTodosLosPartidosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CancelarInvitacionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.InvitarArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPersonalDeArbitrajeUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarArbitroUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.navigation.ArbitrosTorneo
import kotlinx.coroutines.launch

/** Aviso pasajero de la pantalla de árbitros. */
sealed interface AvisoArbitros {
    data class Quitado(val nombre: String) : AvisoArbitros
    data object InvitacionCancelada : AvisoArbitros
    data class Fallo(val mensaje: String) : AvisoArbitros
}

data class ArbitrosState(
    val torneo: String,
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    val personal: PersonalDeArbitraje? = null,
    val error: ErrorDePantalla? = null,
    /** El error es que no administra este torneo: se ofrece "Volver". */
    val sinPermiso: Boolean = false,
    // Invitar (diálogo)
    val invitando: Boolean = false,
    val correo: String = "",
    val errorCorreo: String? = null,
    val enviandoInvitacion: Boolean = false,
    /** Invitación recién creada: la pantalla abre la hoja de compartir y avisa con `onCompartido`. */
    val porCompartir: InvitacionDeTorneo? = null,
    /** Árbitro que se está quitando (diálogo de confirmación abierto). */
    val quitando: ArbitroDeTorneo? = null,
    /** Id de la fila (rol o invitación) con una acción viajando. */
    val ocupado: String? = null,
    val aviso: AvisoArbitros? = null,
)

/** Árbitros de un torneo que la persona administra: invitar, dar permiso total y quitar (MOV-0011). */
class ArbitrosTorneoViewModel(
    private val torneoId: String,
    torneoNombre: String,
    private val obtenerPersonalUseCase: ObtenerPersonalDeArbitrajeUseCase,
    private val invitarArbitroUseCase: InvitarArbitroUseCase,
    private val cancelarInvitacionUseCase: CancelarInvitacionUseCase,
    private val quitarArbitroUseCase: QuitarArbitroUseCase,
    private val cambiarTodosLosPartidosUseCase: CambiarTodosLosPartidosUseCase,
) : ViewModel() {

    var state by mutableStateOf(ArbitrosState(torneo = torneoNombre))
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
        if (pidiendo || state.ocupado != null) return
        if (state.personal == null) {
            cargar()
            return
        }
        state = state.copy(refrescando = true)
        pedir()
    }

    private fun pedir() {
        pidiendo = true
        viewModelScope.launch {
            val resultado = obtenerPersonalUseCase(torneoId)
            pidiendo = false
            resultado
                .onSuccess { state = state.copy(cargando = false, refrescando = false, personal = it, error = null) }
                .onFailure { error ->
                    state = if (state.personal == null) {
                        state.copy(
                            cargando = false, refrescando = false,
                            error = error.aErrorDePantalla(), sinPermiso = error.noAdministraElTorneo(),
                        )
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = AvisoArbitros.Fallo(error.mensajeParaMostrar()))
                    }
                }
        }
    }

    // ── Invitar ─────────────────────────────────────────────────────────────

    fun pedirInvitacion() {
        if (state.personal == null) return
        state = state.copy(invitando = true, correo = "", errorCorreo = null)
    }

    fun onCorreoChange(valor: String) {
        state = state.copy(correo = valor.take(CORREO_MAXIMO), errorCorreo = null)
    }

    fun cancelarInvitar() {
        if (state.enviandoInvitacion) return
        state = state.copy(invitando = false, correo = "", errorCorreo = null)
    }

    /** Un error (correo mal escrito, invitación duplicada) deja el diálogo abierto con lo escrito. */
    fun confirmarInvitar() {
        if (state.enviandoInvitacion) return
        if (!correoDeInvitacionValido(state.correo)) {
            state = state.copy(errorCorreo = InvitarArbitroUseCase.MENSAJE_CORREO)
            return
        }
        state = state.copy(enviandoInvitacion = true, errorCorreo = null)
        viewModelScope.launch {
            invitarArbitroUseCase(torneoId, state.correo)
                .onSuccess { invitacion ->
                    val personal = state.personal ?: PersonalDeArbitraje()
                    state = state.copy(
                        enviandoInvitacion = false,
                        invitando = false,
                        correo = "",
                        personal = personal.copy(invitaciones = listOf(invitacion) + personal.invitaciones),
                        porCompartir = invitacion.takeIf { it.enlace.isNotBlank() },
                    )
                }
                .onFailure { state = state.copy(enviandoInvitacion = false, errorCorreo = it.mensajeParaMostrar()) }
        }
    }

    fun onCompartido() {
        state = state.copy(porCompartir = null)
    }

    fun cancelarInvitacion(invitacion: InvitacionDeTorneo) {
        if (state.ocupado != null) return
        state = state.copy(ocupado = invitacion.id)
        viewModelScope.launch {
            cancelarInvitacionUseCase(torneoId, invitacion.id)
                .onSuccess {
                    cambiarPersonal { it.copy(invitaciones = it.invitaciones.filterNot { i -> i.id == invitacion.id }) }
                    state = state.copy(ocupado = null, aviso = AvisoArbitros.InvitacionCancelada)
                }
                .onFailure { state = state.copy(ocupado = null, aviso = AvisoArbitros.Fallo(it.mensajeParaMostrar())) }
        }
    }

    // ── Árbitros ────────────────────────────────────────────────────────────

    /** El interruptor cambia al tocarlo; si el backend lo rechaza, vuelve a como estaba. */
    fun cambiarTodosLosPartidos(arbitro: ArbitroDeTorneo, valor: Boolean) {
        if (state.ocupado != null || arbitro.todosLosPartidos == valor) return
        marcarTodosLosPartidos(arbitro.rolId, valor)
        state = state.copy(ocupado = arbitro.rolId)
        viewModelScope.launch {
            cambiarTodosLosPartidosUseCase(torneoId, arbitro.cuentaId, valor)
                .onSuccess { state = state.copy(ocupado = null) }
                .onFailure {
                    marcarTodosLosPartidos(arbitro.rolId, !valor)
                    state = state.copy(ocupado = null, aviso = AvisoArbitros.Fallo(it.mensajeParaMostrar()))
                }
        }
    }

    fun pedirQuitar(arbitro: ArbitroDeTorneo) {
        if (state.ocupado != null) return
        state = state.copy(quitando = arbitro)
    }

    fun cancelarQuitar() {
        state = state.copy(quitando = null)
    }

    fun confirmarQuitar() {
        val arbitro = state.quitando ?: return
        state = state.copy(quitando = null, ocupado = arbitro.rolId)
        viewModelScope.launch {
            quitarArbitroUseCase(torneoId, arbitro.rolId)
                .onSuccess {
                    cambiarPersonal { it.copy(arbitros = it.arbitros.filterNot { a -> a.rolId == arbitro.rolId }) }
                    state = state.copy(ocupado = null, aviso = AvisoArbitros.Quitado(arbitro.nombre))
                }
                .onFailure { state = state.copy(ocupado = null, aviso = AvisoArbitros.Fallo(it.mensajeParaMostrar())) }
        }
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    private fun marcarTodosLosPartidos(rolId: String, valor: Boolean) {
        cambiarPersonal { p -> p.copy(arbitros = p.arbitros.map { if (it.rolId == rolId) it.copy(todosLosPartidos = valor) else it }) }
    }

    private fun cambiarPersonal(cambio: (PersonalDeArbitraje) -> PersonalDeArbitraje) {
        state = state.copy(personal = state.personal?.let(cambio))
    }

    companion object {
        const val CORREO_MAXIMO = 150

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<ArbitrosTorneo>()
                val c = app.container
                ArbitrosTorneoViewModel(
                    torneoId = ruta.torneoId,
                    torneoNombre = ruta.nombre,
                    obtenerPersonalUseCase = c.obtenerPersonalDeArbitraje,
                    invitarArbitroUseCase = c.invitarArbitro,
                    cancelarInvitacionUseCase = c.cancelarInvitacion,
                    quitarArbitroUseCase = c.quitarArbitro,
                    cambiarTodosLosPartidosUseCase = c.cambiarTodosLosPartidos,
                )
            }
        }
    }
}

/** 403: la persona no administra este torneo. */
internal fun Throwable.noAdministraElTorneo(): Boolean =
    this is AppError.AccesoDenegado || (this as? AppError.Negocio)?.codigo == "NO_ADMIN_DEL_TORNEO"
