package co.edu.ufps.desafiosports.presentacion.inicio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.PartidosDeArbitro
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Lo que una sección de Inicio tiene para mostrar: cargando, su resumen o su error. */
sealed interface Seccion<out T> {
    data object Cargando : Seccion<Nothing>
    data class Lista<T>(val valor: T) : Seccion<T>
    data class Fallo(val error: ErrorDePantalla) : Seccion<Nothing>
}

/** Resumen de Árbitro en Inicio: el partido que sigue y cuántos faltan por jugar. */
data class ResumenArbitro(val proximo: PartidoArbitro?, val porJugar: Int, val total: Int) {
    /** Árbitro aparece solo si te asignaron partidos (PRODUCT.md: "solo si te invitaron"). */
    val visible: Boolean get() = total > 0
}

/** Resumen de Creador en Inicio: cuántos torneos administra y cuánto espera su revisión. */
data class ResumenCreador(val torneos: List<TorneoDeCreador>) {
    val porRevisar: Int get() = torneos.sumOf { it.porRevisar }

    /** El torneo que más necesita al organizador; si nada espera, el primero de la lista. */
    val destacado: TorneoDeCreador? get() = torneos.maxByOrNull { it.porRevisar }
}

data class InicioState(
    /** Primer nombre para el saludo. Null mientras carga o si /me falló: el saludo queda genérico. */
    val nombre: String? = null,
    val iniciales: String = "",
    val correo: String? = null,
    val arbitro: Seccion<ResumenArbitro> = Seccion.Cargando,
    val creador: Seccion<ResumenCreador> = Seccion.Cargando,
    val refrescando: Boolean = false,
    val cerrandoSesion: Boolean = false,
)

/**
 * Inicio provisional (MOV-0010): el saludo y "Tus secciones" con lo que ya da el backend.
 * Cada sección carga por su lado: si una falla, la otra se muestra igual (CA-5).
 * El Inicio completo, con Explorar y Perfil, llega con MOV-0004.
 */
class InicioViewModel(
    private val obtenerCuentaUseCase: ObtenerCuentaUseCase,
    private val obtenerPartidosUseCase: ObtenerPartidosDeArbitroUseCase,
    private val obtenerMisTorneosUseCase: ObtenerMisTorneosUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModel() {

    var state by mutableStateOf(InicioState())
        private set

    private var trabajoArbitro: Job? = null
    private var trabajoCreador: Job? = null

    init {
        cargarCuenta()
        cargarArbitro()
        cargarCreador()
    }

    fun reintentarArbitro() {
        state = state.copy(arbitro = Seccion.Cargando)
        cargarArbitro()
    }

    fun reintentarCreador() {
        state = state.copy(creador = Seccion.Cargando)
        cargarCreador()
    }

    /** Deslizar hacia abajo: actualiza las dos secciones sin borrar lo que se ve. */
    fun refrescar() {
        if (state.refrescando) return
        state = state.copy(refrescando = true)
        viewModelScope.launch {
            val a = launch { pedirArbitro() }
            val c = launch { pedirCreador() }
            a.join()
            c.join()
            state = state.copy(refrescando = false)
        }
    }

    /** Al volver de una sección o de la planilla, los conteos pueden haber cambiado. */
    fun alVolver() {
        if (trabajoArbitro?.isActive != true) trabajoArbitro = viewModelScope.launch { pedirArbitro() }
        if (trabajoCreador?.isActive != true) trabajoCreador = viewModelScope.launch { pedirCreador() }
    }

    /** La navegación vuelve al login sola cuando la sesión se cierra (RaizViewModel). */
    fun cerrarSesion() {
        if (state.cerrandoSesion) return
        state = state.copy(cerrandoSesion = true)
        viewModelScope.launch {
            cerrarSesionUseCase()
            state = state.copy(cerrandoSesion = false)
        }
    }

    private fun cargarCuenta() {
        viewModelScope.launch {
            obtenerCuentaUseCase().onSuccess { cuenta ->
                state = state.copy(nombre = cuenta.primerNombre(), iniciales = cuenta.iniciales(), correo = cuenta.correo)
            }
        }
    }

    private fun cargarArbitro() {
        if (trabajoArbitro?.isActive == true) return
        trabajoArbitro = viewModelScope.launch { pedirArbitro() }
    }

    private fun cargarCreador() {
        if (trabajoCreador?.isActive == true) return
        trabajoCreador = viewModelScope.launch { pedirCreador() }
    }

    private suspend fun pedirArbitro() {
        obtenerPartidosUseCase()
            .onSuccess { state = state.copy(arbitro = Seccion.Lista(it.resumen())) }
            // Un fallo al actualizar no borra el resumen que ya se ve.
            .onFailure { if (state.arbitro !is Seccion.Lista) state = state.copy(arbitro = Seccion.Fallo(it.aErrorDePantalla())) }
    }

    private suspend fun pedirCreador() {
        obtenerMisTorneosUseCase()
            .onSuccess { state = state.copy(creador = Seccion.Lista(ResumenCreador(it))) }
            .onFailure { if (state.creador !is Seccion.Lista) state = state.copy(creador = Seccion.Fallo(it.aErrorDePantalla())) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val c = app.container
                InicioViewModel(c.obtenerCuenta, c.obtenerPartidosDeArbitro, c.obtenerMisTorneos, c.cerrarSesion)
            }
        }
    }
}

/**
 * El partido que sigue para el árbitro (CA-2): el de hoy que falta por cerrar y, si no hay,
 * el primero de Próximos (los atrasados van primero: aún necesitan su planilla). Se prefiere
 * uno con el cruce definido, porque solo ese abre la planilla.
 */
fun PartidosDeArbitro.proximo(): PartidoArbitro? {
    val candidatos = hoy.filter { !it.estado.cerrado } + proximos
    return candidatos.firstOrNull { it.cruceDefinido } ?: candidatos.firstOrNull()
}

fun PartidosDeArbitro.resumen(): ResumenArbitro = ResumenArbitro(
    proximo = proximo(),
    porJugar = hoy.count { !it.estado.cerrado } + proximos.size,
    total = hoy.size + proximos.size + jugados.size,
)

private fun Cuenta.primerNombre(): String? =
    nombres.trim().split(' ').firstOrNull { it.isNotBlank() } ?: nombre.trim().split(' ').firstOrNull { it.isNotBlank() }

private fun Cuenta.iniciales(): String {
    val partes = listOf(nombres, apellidos).mapNotNull { parte -> parte.trim().firstOrNull()?.uppercaseChar() }
    return if (partes.isNotEmpty()) partes.joinToString("") else nombre.trim().take(1).uppercase()
}
