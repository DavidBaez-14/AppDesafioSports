package co.edu.ufps.desafiosports.presentacion.creador

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.BorradorTorneo
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.TorneoCreado
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.domain.use_case.competicion.CrearTorneoUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerDeportesUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CrearTorneoState(
    val hoy: LocalDate,
    val paso: Int = 1,
    val borrador: BorradorTorneo = BorradorTorneo(),
    /** Catálogo del backend. Null mientras carga o si falló. */
    val deportes: List<Deporte>? = null,
    val cargandoDeportes: Boolean = true,
    val errorDeportes: ErrorDePantalla? = null,
    /** Lo que se ve en el campo del valor: solo dígitos. */
    val valorTexto: String = "",
    val creando: Boolean = false,
    /** Error al crear, con el mensaje del backend. Sale sobre el botón del paso al que corresponde. */
    val error: String? = null,
    val confirmandoSalida: Boolean = false,
    /** Ya se creó: se muestra "Tu torneo está listo". */
    val creado: TorneoCreado? = null,
) {
    val puedeSeguir: Boolean get() = borrador.pasoCompleto(paso, hoy)
    val esUltimoPaso: Boolean get() = paso == BorradorTorneo.PASOS
}

/** Crear torneo en tres pasos: deporte, nombre y formato, y visibilidad (MOV-0007). */
class CrearTorneoViewModel(
    private val obtenerDeportesUseCase: ObtenerDeportesUseCase,
    private val crearTorneoUseCase: CrearTorneoUseCase,
    hoy: LocalDate = LocalDate.now(),
) : ViewModel() {

    var state by mutableStateOf(
        CrearTorneoState(
            hoy = hoy,
            // Lo más común ya viene elegido: empieza en una semana y es público (lo encuentra cualquiera).
            borrador = BorradorTorneo(fechaInicio = hoy.plusDays(7), visibilidad = Visibilidad.PUBLICO),
        ),
    )
        private set

    init {
        cargarDeportes()
    }

    fun cargarDeportes() {
        state = state.copy(cargandoDeportes = true, errorDeportes = null)
        viewModelScope.launch {
            obtenerDeportesUseCase()
                .onSuccess { state = state.copy(cargandoDeportes = false, deportes = it) }
                .onFailure { state = state.copy(cargandoDeportes = false, errorDeportes = it.aErrorDePantalla()) }
        }
    }

    // ── Paso 1 ──────────────────────────────────────────────────────────────

    fun elegirDeporte(deporte: Deporte) = cambiar { copy(deporte = deporte) }

    // ── Paso 2 ──────────────────────────────────────────────────────────────

    fun onNombreChange(valor: String) = cambiar { copy(nombre = valor.take(BorradorTorneo.NOMBRE_MAXIMO)) }

    fun elegirFormato(formato: FormatoTorneo) = cambiar { conFormato(formato) }

    fun cambiarCupos(diferencia: Int) = cambiar { conCupos(cupos + diferencia) }

    fun elegirFecha(fecha: LocalDate) = cambiar { copy(fechaInicio = fecha) }

    // ── Paso 3 ──────────────────────────────────────────────────────────────

    fun elegirVisibilidad(visibilidad: Visibilidad) = cambiar { copy(visibilidad = visibilidad) }

    fun onCobraChange(cobra: Boolean) = cambiar { copy(cobraInscripcion = cobra) }

    fun onValorChange(texto: String) {
        // Solo dígitos, sin ceros a la izquierda y hasta cientos de millones: más no es una inscripción.
        val digitos = texto.filter { it.isDigit() }.trimStart('0').take(VALOR_MAXIMO_DIGITOS)
        state = state.copy(valorTexto = digitos, error = null, borrador = state.borrador.copy(valorInscripcion = digitos.toLongOrNull()))
    }

    // ── Navegación entre pasos ──────────────────────────────────────────────

    /** "Siguiente" o, en el último paso, "Crear torneo". */
    fun siguiente() {
        if (!state.puedeSeguir || state.creando) return
        if (state.esUltimoPaso) crear() else state = state.copy(paso = state.paso + 1, error = null)
    }

    /**
     * Atrás (el botón o el del sistema). Devuelve true si la pantalla debe cerrarse ya; si no, o
     * volvió un paso o quedó abierta la confirmación para no perder lo escrito.
     */
    fun atras(): Boolean {
        if (state.creando) return false
        if (state.paso > 1) {
            state = state.copy(paso = state.paso - 1, error = null)
            return false
        }
        return pedirSalida()
    }

    /** La X de la barra. Devuelve true si se puede salir sin preguntar. */
    fun pedirSalida(): Boolean {
        if (state.creando) return false
        if (!state.borrador.tieneDatos) return true
        state = state.copy(confirmandoSalida = true)
        return false
    }

    fun cancelarSalida() {
        state = state.copy(confirmandoSalida = false)
    }

    private fun crear() {
        state = state.copy(creando = true, error = null)
        viewModelScope.launch {
            crearTorneoUseCase(state.borrador)
                .onSuccess { state = state.copy(creando = false, creado = it) }
                .onFailure { error ->
                    // El backend dice qué campo falló: se vuelve a ese paso sin perder nada (CA-3).
                    val paso = (error as? AppError.Negocio)?.let { PASO_POR_CODIGO[it.codigo] } ?: state.paso
                    state = state.copy(creando = false, paso = paso, error = error.mensajeParaMostrar())
                }
        }
    }

    private fun cambiar(cambio: BorradorTorneo.() -> BorradorTorneo) {
        if (state.creando) return
        state = state.copy(borrador = state.borrador.cambio(), error = null)
    }

    companion object {
        const val VALOR_MAXIMO_DIGITOS = 9

        /** `code` del backend → paso donde se corrige. */
        private val PASO_POR_CODIGO = mapOf(
            "DEPORTE_INEXISTENTE" to 1,
            "NOMBRE_INVALIDO" to 2,
            "FORMATO_INVALIDO" to 2,
            "CUPOS_FUERA_DE_RANGO" to 2,
            "FECHA_INICIO_REQUERIDA" to 2,
            "FECHA_INICIO_PASADA" to 2,
            "VISIBILIDAD_INVALIDA" to 3,
            "VALOR_INSCRIPCION_INVALIDO" to 3,
        )

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                CrearTorneoViewModel(app.container.obtenerDeportes, app.container.crearTorneo)
            }
        }
    }
}
