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
import co.edu.ufps.desafiosports.domain.model.EquipoAInscribir
import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha
import co.edu.ufps.desafiosports.domain.use_case.competicion.InscribirEquipoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisEquiposBaseUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.navigation.InscribirEquipo
import kotlinx.coroutines.launch

data class InscribirEquipoState(
    val torneo: String,
    /** Cuota por equipo, en pesos; 0 si el torneo no cobra. */
    val cuota: Long = 0,
    val exigeDocumento: Boolean = false,
    val cargando: Boolean = true,
    /** No se pudieron pedir sus equipos: ocupa la pantalla, con "Reintentar". */
    val error: ErrorDePantalla? = null,
    val equipos: List<EquipoPropio> = emptyList(),
    /** Id del equipo elegido; null si eligió crear uno nuevo o aún no elige. */
    val elegido: String? = null,
    /** Eligió "Crear un equipo nuevo". */
    val nuevo: Boolean = false,
    val nombreNuevo: String = "",
    /** Va bajo el campo del nombre: vacío, largo o repetido. */
    val errorNombre: String? = null,
    val inscribiendo: Boolean = false,
    /** Va sobre el botón: cupos agotados, ya inscrito, sin conexión… */
    val errorInscribir: String? = null,
    /** Ya se inscribió: la pantalla muestra la confirmación. */
    val hecha: InscripcionHecha? = null,
) {
    val sePuedeInscribir: Boolean
        get() = !cargando && error == null && hecha == null && !inscribiendo &&
            (elegido != null || (nuevo && nombreNuevo.isNotBlank()))
}

/**
 * Inscribir un equipo en un torneo (MOV-0014): elegir uno de sus equipos o crear uno, e inscribirlo.
 * Quien inscribe queda como delegado y en el plantel; la inscripción arranca pendiente.
 */
class InscribirEquipoViewModel(
    private val torneoId: String,
    torneoNombre: String,
    private val enlaceToken: String?,
    cuota: Long,
    exigeDocumento: Boolean,
    private val obtenerMisEquiposBaseUseCase: ObtenerMisEquiposBaseUseCase,
    private val inscribirEquipoUseCase: InscribirEquipoUseCase,
) : ViewModel() {

    var state by mutableStateOf(InscribirEquipoState(torneo = torneoNombre, cuota = cuota, exigeDocumento = exigeDocumento))
        private set

    private var pidiendo = false

    init {
        cargar()
    }

    /** Primera carga y "Reintentar". */
    fun cargar() {
        if (pidiendo) return
        pidiendo = true
        state = state.copy(cargando = true, error = null)
        viewModelScope.launch {
            val resultado = obtenerMisEquiposBaseUseCase()
            pidiendo = false
            resultado
                .onSuccess { equipos ->
                    state = state.copy(
                        cargando = false,
                        equipos = equipos,
                        // Con un solo equipo se elige solo; sin equipos, toca crear uno.
                        elegido = equipos.singleOrNull()?.id,
                        nuevo = equipos.isEmpty(),
                    )
                }
                .onFailure { state = state.copy(cargando = false, error = it.aErrorDePantalla()) }
        }
    }

    fun elegir(equipo: EquipoPropio) {
        if (state.inscribiendo) return
        state = state.copy(elegido = equipo.id, nuevo = false, errorNombre = null, errorInscribir = null)
    }

    fun elegirNuevo() {
        if (state.inscribiendo) return
        state = state.copy(elegido = null, nuevo = true, errorInscribir = null)
    }

    fun onNombre(nombre: String) {
        if (state.inscribiendo) return
        state = state.copy(nombreNuevo = nombre.take(InscribirEquipoUseCase.NOMBRE_MAXIMO), errorNombre = null, errorInscribir = null)
    }

    /** El doble toque no inscribe dos veces. */
    fun inscribir() {
        if (state.inscribiendo || state.hecha != null) return
        val equipo = when {
            state.elegido != null -> EquipoAInscribir.Existente(state.elegido!!)
            state.nuevo -> EquipoAInscribir.Nuevo(state.nombreNuevo)
            else -> return
        }
        state = state.copy(inscribiendo = true, errorNombre = null, errorInscribir = null)
        viewModelScope.launch {
            inscribirEquipoUseCase(torneoId, equipo, enlaceToken, state.equipos)
                .onSuccess { state = state.copy(inscribiendo = false, hecha = it) }
                .onFailure { fallo ->
                    state = if (equipo is EquipoAInscribir.Nuevo && fallo.esDelNombre()) {
                        state.copy(inscribiendo = false, errorNombre = fallo.mensajeParaMostrar())
                    } else {
                        state.copy(inscribiendo = false, errorInscribir = fallo.mensajeParaMostrar())
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<InscribirEquipo>()
                val c = app.container
                InscribirEquipoViewModel(
                    torneoId = ruta.torneoId,
                    torneoNombre = ruta.nombre,
                    enlaceToken = ruta.enlaceToken.ifBlank { null },
                    cuota = ruta.cuota,
                    exigeDocumento = ruta.exigeDocumento,
                    obtenerMisEquiposBaseUseCase = c.obtenerMisEquiposBase,
                    inscribirEquipoUseCase = c.inscribirEquipo,
                )
            }
        }
    }
}

/** El problema es el nombre escrito: la validación local o el rechazo del backend por el nombre. */
private fun Throwable.esDelNombre(): Boolean =
    this is AppError.Validacion ||
        (this as? AppError.Negocio)?.codigo in setOf("EQUIPO_DUPLICADO", "NOMBRE_INVALIDO", "NOMBRE_REQUERIDO")
