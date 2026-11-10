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
import co.edu.ufps.desafiosports.domain.model.Inscripcion
import co.edu.ufps.desafiosports.domain.model.InscripcionesDeTorneo
import co.edu.ufps.desafiosports.domain.use_case.competicion.AprobarInscripcionUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerInscripcionesUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.RechazarInscripcionUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.navigation.InscripcionesTorneo
import kotlinx.coroutines.launch

/** Aviso pasajero de la pantalla de inscripciones. */
sealed interface AvisoInscripciones {
    data class Aprobada(val equipo: String) : AvisoInscripciones
    data class Rechazada(val equipo: String) : AvisoInscripciones

    /** Se guardó, pero la lista no se pudo volver a pedir. */
    data object SinActualizar : AvisoInscripciones
    data class Fallo(val mensaje: String) : AvisoInscripciones
}

data class InscripcionesState(
    val torneo: String,
    val cargando: Boolean = true,
    val refrescando: Boolean = false,
    val inscripciones: InscripcionesDeTorneo? = null,
    val error: ErrorDePantalla? = null,
    /** El error es que no administra este torneo: se ofrece "Volver". */
    val sinPermiso: Boolean = false,
    /** Id de la inscripción con una acción viajando: sus botones muestran que está ocupada. */
    val enAccion: String? = null,
    /** Inscripción que se está rechazando (diálogo del motivo abierto). */
    val rechazando: Inscripcion? = null,
    val motivo: String = "",
    val errorMotivo: String? = null,
    val aviso: AvisoInscripciones? = null,
)

/** Inscripciones de un torneo que la persona administra: aprobar y rechazar (MOV-0008). */
class InscripcionesViewModel(
    private val torneoId: String,
    torneoNombre: String,
    private val obtenerInscripcionesUseCase: ObtenerInscripcionesUseCase,
    private val aprobarInscripcionUseCase: AprobarInscripcionUseCase,
    private val rechazarInscripcionUseCase: RechazarInscripcionUseCase,
) : ViewModel() {

    var state by mutableStateOf(InscripcionesState(torneo = torneoNombre))
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
        if (pidiendo || state.enAccion != null) return
        if (state.inscripciones == null) {
            cargar()
            return
        }
        state = state.copy(refrescando = true)
        pedir()
    }

    private fun pedir() {
        pidiendo = true
        viewModelScope.launch {
            val resultado = obtenerInscripcionesUseCase(torneoId)
            pidiendo = false
            resultado
                .onSuccess { state = state.copy(cargando = false, refrescando = false, inscripciones = it, error = null) }
                .onFailure { error ->
                    state = if (state.inscripciones == null) {
                        state.copy(cargando = false, refrescando = false, error = error.aErrorDePantalla(), sinPermiso = error.esSinPermiso())
                    } else {
                        state.copy(cargando = false, refrescando = false, aviso = AvisoInscripciones.Fallo(error.mensajeParaMostrar()))
                    }
                }
        }
    }

    fun aprobar(inscripcion: Inscripcion) {
        accion(inscripcion, { aprobarInscripcionUseCase(torneoId, inscripcion.id) }) { AvisoInscripciones.Aprobada(inscripcion.equipo) }
    }

    // ── Rechazo: pide el motivo ─────────────────────────────────────────────

    fun pedirRechazo(inscripcion: Inscripcion) {
        if (state.enAccion != null) return
        state = state.copy(rechazando = inscripcion, motivo = "", errorMotivo = null)
    }

    fun onMotivoChange(valor: String) {
        state = state.copy(motivo = valor.take(MOTIVO_MAXIMO), errorMotivo = null)
    }

    fun cancelarRechazo() {
        state = state.copy(rechazando = null, motivo = "", errorMotivo = null)
    }

    fun confirmarRechazo() {
        val inscripcion = state.rechazando ?: return
        val motivo = state.motivo
        if (motivo.trim().length < RechazarInscripcionUseCase.MOTIVO_MINIMO) {
            // Se valida aquí para no cerrar el diálogo: el caso de uso lo vuelve a comprobar.
            state = state.copy(errorMotivo = MENSAJE_MOTIVO)
            return
        }
        state = state.copy(rechazando = null, motivo = "", errorMotivo = null)
        accion(inscripcion, { rechazarInscripcionUseCase(torneoId, inscripcion.id, motivo) }) { AvisoInscripciones.Rechazada(inscripcion.equipo) }
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    /** Envía la acción y vuelve a pedir la lista. Una a la vez: el doble toque no aprueba dos veces. */
    private fun accion(inscripcion: Inscripcion, operacion: suspend () -> Result<Unit>, alTerminar: () -> AvisoInscripciones) {
        if (state.enAccion != null) return
        state = state.copy(enAccion = inscripcion.id)
        viewModelScope.launch {
            val fallo = operacion().exceptionOrNull()
            val recarga = if (fallo is AppError.SinConexion) null else obtenerInscripcionesUseCase(torneoId)
            val aviso = when {
                fallo != null -> AvisoInscripciones.Fallo(fallo.mensajeParaMostrar())
                recarga?.isSuccess != true -> AvisoInscripciones.SinActualizar
                else -> alTerminar()
            }
            state = state.copy(enAccion = null, inscripciones = recarga?.getOrNull() ?: state.inscripciones, aviso = aviso)
        }
    }

    companion object {
        const val MOTIVO_MAXIMO = 300
        const val MENSAJE_MOTIVO = "Escribe el motivo del rechazo: lo verá quien se inscribió."

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<InscripcionesTorneo>()
                InscripcionesViewModel(
                    torneoId = ruta.torneoId,
                    torneoNombre = ruta.nombre,
                    obtenerInscripcionesUseCase = app.container.obtenerInscripciones,
                    aprobarInscripcionUseCase = app.container.aprobarInscripcion,
                    rechazarInscripcionUseCase = app.container.rechazarInscripcion,
                )
            }
        }
    }
}

/** 403: no administra este torneo. */
private fun Throwable.esSinPermiso(): Boolean =
    this is AppError.AccesoDenegado || (this as? AppError.Negocio)?.codigo == "NO_ADMIN_DEL_TORNEO"
