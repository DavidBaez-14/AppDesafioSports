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
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.model.BorradorJugador
import co.edu.ufps.desafiosports.domain.model.Plantel
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.use_case.competicion.AgregarJugadorUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.CAMISETA_MAXIMA
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPlantelUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.navigation.AgregarJugador
import kotlinx.coroutines.launch

data class AgregarJugadorState(
    val equipo: String,
    val exigeDocumento: Boolean = false,
    val borrador: BorradorJugador = BorradorJugador(),
    val errorDocumento: String? = null,
    val errorNombre: String? = null,
    val errorCamiseta: String? = null,
    /** Va sobre el botón: ya juega en otro equipo, cuenta bloqueada, número ocupado, sin conexión… */
    val errorGeneral: String? = null,
    val agregando: Boolean = false,
    /** Los nombres de quienes se agregaron desde que se abrió la pantalla. */
    val agregados: List<String> = emptyList(),
    /** Evento: el nombre del recién agregado, para el aviso. */
    val aviso: String? = null,
) {
    val sePuedeAgregar: Boolean
        get() = !agregando && (borrador.documento.isNotBlank() || borrador.nombre.isNotBlank())
}

/**
 * Agregar jugadores al plantel (MOV-0015). Después de agregar a uno el formulario queda vacío para
 * el siguiente: así se carga un plantel entero sin salir de la pantalla.
 */
class AgregarJugadorViewModel(
    private val equipoTorneoId: String,
    equipo: String,
    exigeDocumento: Boolean,
    private val obtenerPlantelUseCase: ObtenerPlantelUseCase,
    private val agregarJugadorUseCase: AgregarJugadorUseCase,
) : ViewModel() {

    var state by mutableStateOf(AgregarJugadorState(equipo = equipo, exigeDocumento = exigeDocumento))
        private set

    /** Para avisar antes de enviar que un número ya está ocupado. Si no carga, decide el backend. */
    private var plantel: Plantel? = null

    init {
        viewModelScope.launch { obtenerPlantelUseCase(equipoTorneoId).onSuccess { plantel = it } }
    }

    fun onTipo(tipo: TipoDocumento) = cambiar { it.copy(tipoDocumento = tipo) }

    fun onDocumento(texto: String) {
        cambiar { it.copy(documento = texto.take(AgregarJugadorUseCase.DOCUMENTO_MAXIMO)) }
        state = state.copy(errorDocumento = null)
    }

    fun onNombre(texto: String) {
        cambiar { it.copy(nombre = texto.take(AgregarJugadorUseCase.NOMBRE_MAXIMO)) }
        state = state.copy(errorNombre = null)
    }

    /** Solo dígitos, como mucho dos. */
    fun onCamiseta(texto: String) {
        cambiar { it.copy(camiseta = texto.filter { c -> c.isDigit() }.take(CAMISETA_MAXIMA.toString().length)) }
        state = state.copy(errorCamiseta = null)
    }

    private fun cambiar(cambio: (BorradorJugador) -> BorradorJugador) {
        if (state.agregando) return
        state = state.copy(borrador = cambio(state.borrador), errorGeneral = null)
    }

    /** El doble toque no agrega dos veces. */
    fun agregar() {
        if (state.agregando) return
        state = state.copy(agregando = true, errorDocumento = null, errorNombre = null, errorCamiseta = null, errorGeneral = null)
        viewModelScope.launch {
            agregarJugadorUseCase(equipoTorneoId, state.borrador, state.exigeDocumento, plantel)
                .onSuccess { miembro ->
                    plantel = plantel?.con(miembro)
                    state = state.copy(
                        agregando = false,
                        // El tipo se conserva: casi siempre es el mismo para todo el equipo.
                        borrador = BorradorJugador(tipoDocumento = state.borrador.tipoDocumento),
                        agregados = state.agregados + miembro.nombre,
                        aviso = miembro.nombre,
                    )
                }
                .onFailure { state = conError(it) }
        }
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    /** Cada error va junto al campo que lo causó; el resto, sobre el botón. */
    private fun conError(error: Throwable): AgregarJugadorState {
        val base = state.copy(agregando = false)
        val mensaje = error.mensajeParaMostrar()
        val campo = (error as? AppError.Validacion)?.campo
        val codigo = (error as? AppError.Negocio)?.codigo
        return when {
            campo == CampoValidado.DOCUMENTO || codigo == "DOCUMENTO_REQUERIDO" || codigo == "VALIDACION" ->
                base.copy(errorDocumento = mensaje)
            campo == CampoValidado.NOMBRES || codigo == "NOMBRE_REQUERIDO" -> base.copy(errorNombre = mensaje)
            campo == CampoValidado.CAMISETA -> base.copy(errorCamiseta = mensaje)
            else -> base.copy(errorGeneral = mensaje)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val ruta = createSavedStateHandle().toRoute<AgregarJugador>()
                val c = app.container
                AgregarJugadorViewModel(
                    equipoTorneoId = ruta.equipoTorneoId,
                    equipo = ruta.equipo,
                    exigeDocumento = ruta.exigeDocumento,
                    obtenerPlantelUseCase = c.obtenerPlantel,
                    agregarJugadorUseCase = c.agregarJugador,
                )
            }
        }
    }
}
