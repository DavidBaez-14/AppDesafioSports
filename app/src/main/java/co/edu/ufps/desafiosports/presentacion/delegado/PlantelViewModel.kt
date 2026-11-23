package co.edu.ufps.desafiosports.presentacion.delegado

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
import co.edu.ufps.desafiosports.domain.model.MiembroDePlantel
import co.edu.ufps.desafiosports.domain.model.Plantel
import co.edu.ufps.desafiosports.domain.use_case.competicion.CAMISETA_MAXIMA
import co.edu.ufps.desafiosports.domain.use_case.competicion.CambiarCamisetaUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlantelUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.QuitarDelPlantelUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.navigation.PlantelEquipo
import kotlinx.coroutines.launch

/** Aviso pasajero del plantel. La pantalla lo convierte en texto. */
sealed interface AvisoPlantel {
    data class Quitado(val nombre: String) : AvisoPlantel
    data class Fallo(val mensaje: String) : AvisoPlantel
}

data class PlantelState(
    val equipo: String,
    val torneo: String,
    val exigeDocumento: Boolean = false,
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    val plantel: Plantel? = null,
    /** Falló la primera carga: ocupa la pantalla. */
    val error: ErrorDePantalla? = null,
    /** No es el delegado de este equipo: se ofrece "Volver" en vez de "Reintentar". */
    val sinPermiso: Boolean = false,
    // Editar a un jugador (diálogo)
    /** Persona con el diálogo abierto. */
    val editando: String? = null,
    val camisetaTexto: String = "",
    /** Va dentro del diálogo: un aviso normal quedaría tapado. */
    val errorEdicion: String? = null,
    val guardando: Boolean = false,
    /** Segundo diálogo: confirmar que se quita del plantel. */
    val confirmandoQuitar: Boolean = false,
    val aviso: AvisoPlantel? = null,
) {
    val miembroEditando: MiembroDePlantel? get() = plantel?.miembros?.firstOrNull { it.personaId == editando }

    /** Con el torneo terminado el plantel queda como estaba. */
    val editable: Boolean get() = plantel?.editable == true
}

/**
 * El plantel de un equipo en un torneo, para su delegado (MOV-0015): quién está, el número de
 * cada uno, y quitar o cambiar números. Agregar va en su propia pantalla; al volver, se pide de nuevo.
 */
class PlantelViewModel(
    private val equipoTorneoId: String,
    equipo: String,
    torneo: String,
    exigeDocumento: Boolean,
    private val obtenerPlantelUseCase: ObtenerPlantelUseCase,
    private val cambiarCamisetaUseCase: CambiarCamisetaUseCase,
    private val quitarDelPlantelUseCase: QuitarDelPlantelUseCase,
) : ViewModel() {

    var state by mutableStateOf(PlantelState(equipo = equipo, torneo = torneo, exigeDocumento = exigeDocumento))
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
        if (pidiendo) return
        if (state.plantel == null) return cargar()
        state = state.copy(refrescando = true)
        pedir()
    }

    /** Al volver de agregar jugadores: el plantel cambió. Sin indicador, conservando lo que se ve. */
    fun alVolver() {
        if (pidiendo || state.plantel == null || state.editando != null) return
        pedir()
    }

    private fun pedir() {
        pidiendo = true
        viewModelScope.launch {
            val resultado = obtenerPlantelUseCase(equipoTorneoId)
            pidiendo = false
            resultado
                .onSuccess { state = state.copy(cargando = false, refrescando = false, plantel = it, error = null) }
                .onFailure { e ->
                    state = if (state.plantel == null) {
                        state.copy(cargando = false, refrescando = false, error = e.aErrorDePantalla(), sinPermiso = e.noEsSuDelegado())
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = AvisoPlantel.Fallo(e.mensajeParaMostrar()))
                    }
                }
        }
    }

    // ── Editar a un jugador ─────────────────────────────────────────────────

    /** Tocar a un jugador activo abre su diálogo con el número que tiene. */
    fun abrirJugador(miembro: MiembroDePlantel) {
        if (!state.editable || !miembro.activo || state.guardando) return
        state = state.copy(
            editando = miembro.personaId,
            camisetaTexto = miembro.numeroCamiseta?.toString().orEmpty(),
            errorEdicion = null,
            confirmandoQuitar = false,
        )
    }

    /** Solo dígitos, como mucho dos: el número va del 0 al 99. */
    fun onCamiseta(texto: String) {
        if (state.guardando) return
        val limpio = texto.filter { it.isDigit() }.take(CAMISETA_MAXIMA.toString().length)
        state = state.copy(camisetaTexto = limpio, errorEdicion = null)
    }

    fun cerrarEdicion() {
        if (state.guardando) return
        state = state.copy(editando = null, errorEdicion = null, confirmandoQuitar = false)
    }

    fun guardarCamiseta() {
        val miembro = state.miembroEditando ?: return
        val plantel = state.plantel ?: return
        if (state.guardando) return
        state = state.copy(guardando = true, errorEdicion = null)
        viewModelScope.launch {
            cambiarCamisetaUseCase(equipoTorneoId, miembro, state.camisetaTexto, plantel)
                .onSuccess { actualizado ->
                    state = state.copy(guardando = false, editando = null, plantel = state.plantel?.con(actualizado))
                }
                .onFailure { state = state.copy(guardando = false, errorEdicion = it.mensajeParaMostrar()) }
        }
    }

    // ── Quitar del plantel ──────────────────────────────────────────────────

    fun pedirQuitar() {
        val miembro = state.miembroEditando ?: return
        if (miembro.esDelegado || state.guardando) return
        state = state.copy(confirmandoQuitar = true, errorEdicion = null)
    }

    fun cancelarQuitar() {
        if (state.guardando) return
        state = state.copy(confirmandoQuitar = false)
    }

    /** Si sale bien se cierra todo y se avisa; si no, vuelve al diálogo del jugador con el motivo. */
    fun confirmarQuitar() {
        val miembro = state.miembroEditando ?: return
        if (!state.confirmandoQuitar || state.guardando) return
        state = state.copy(guardando = true)
        viewModelScope.launch {
            quitarDelPlantelUseCase(equipoTorneoId, miembro)
                .onSuccess { retirado ->
                    state = state.copy(
                        guardando = false,
                        confirmandoQuitar = false,
                        editando = null,
                        plantel = state.plantel?.con(retirado),
                        aviso = AvisoPlantel.Quitado(miembro.nombre),
                    )
                }
                .onFailure {
                    state = state.copy(guardando = false, confirmandoQuitar = false, errorEdicion = it.mensajeParaMostrar())
                }
        }
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<PlantelEquipo>()
                val c = app.container
                PlantelViewModel(
                    equipoTorneoId = ruta.equipoTorneoId,
                    equipo = ruta.equipo,
                    torneo = ruta.torneo,
                    exigeDocumento = ruta.exigeDocumento,
                    obtenerPlantelUseCase = c.obtenerPlantel,
                    cambiarCamisetaUseCase = c.cambiarCamiseta,
                    quitarDelPlantelUseCase = c.quitarDelPlantel,
                )
            }
        }
    }
}

/** 403: no es (o dejó de ser) el delegado de este equipo. */
private fun Throwable.noEsSuDelegado(): Boolean =
    this is AppError.AccesoDenegado || (this as? AppError.Negocio)?.codigo == "NO_DELEGADO_DE_LA_INSCRIPCION"
