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
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.MiEquipo
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.PartidoDeMiEquipo
import co.edu.ufps.desafiosports.domain.model.PartidosDeArbitro
import co.edu.ufps.desafiosports.domain.model.Secciones
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.model.primerNombre
import co.edu.ufps.desafiosports.domain.model.proximoPartido
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisEquiposUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMisTorneosUseCase
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerPartidosDeArbitroUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Lo que una sección de Inicio tiene para mostrar: cargando, su resumen o su error. */
sealed interface Seccion<out T> {
    data object Cargando : Seccion<Nothing>
    data class Lista<T>(val valor: T) : Seccion<T>
    data class Fallo(val error: ErrorDePantalla) : Seccion<Nothing>
}

/** Resumen de Árbitro en Inicio: el partido que sigue y cuántos faltan por jugar. */
data class ResumenArbitro(val proximo: PartidoArbitro?, val porJugar: Int, val total: Int)

/** Resumen de Creador en Inicio: cuántos torneos administra y cuánto espera su revisión. */
data class ResumenCreador(val torneos: List<TorneoDeCreador>) {
    val porRevisar: Int get() = torneos.sumOf { it.porRevisar }

    /** El torneo que más necesita al organizador; si nada espera, el primero de la lista. */
    val destacado: TorneoDeCreador? get() = torneos.maxByOrNull { it.porRevisar }
}

/** En qué va la primera carga de una pantalla. Después de [LISTO] los datos ya no se borran. */
enum class Carga { CARGANDO, LISTO, ERROR }

data class InicioState(
    val carga: Carga = Carga.CARGANDO,
    /** Falló la primera carga: ocupa el lugar de la lista, con "Reintentar". */
    val error: ErrorDePantalla? = null,
    /** Primer nombre para el saludo. */
    val nombre: String? = null,
    /** Qué secciones tiene la persona: decide qué bloques de "Tus secciones" aparecen (MOV-0004 · CA-2). */
    val secciones: Secciones = Secciones(),
    val arbitro: Seccion<ResumenArbitro> = Seccion.Cargando,
    val creador: Seccion<ResumenCreador> = Seccion.Cargando,
    val equipos: List<MiEquipo> = emptyList(),
    val refrescando: Boolean = false,
    /** Falló una actualización y lo que se ve se conserva: sale como aviso pasajero. */
    val aviso: String? = null,
    /** Evento: la sesión es de una cuenta sin registro en el backend (MOV-0004 · CA-10). */
    val completarRegistro: Boolean = false,
) {
    /** "Tu próximo partido": el más cercano entre todos los equipos (CA-4). */
    val proximoPartido: PartidoDeMiEquipo? get() = equipos.proximoPartido()
}

/**
 * Inicio (MOV-0004): el saludo, "Tus secciones", "Tu próximo partido" y "Mis equipos".
 *
 * La cuenta y los equipos son lo que hace falta para pintar la pantalla. Los bloques de sección
 * (MOV-0010) cargan cada uno por su lado y solo si la persona tiene esa sección: si uno falla, el
 * resto se ve igual.
 */
class InicioViewModel(
    private val obtenerCuentaUseCase: ObtenerCuentaUseCase,
    private val obtenerMisEquiposUseCase: ObtenerMisEquiposUseCase,
    private val obtenerPartidosUseCase: ObtenerPartidosDeArbitroUseCase,
    private val obtenerMisTorneosUseCase: ObtenerMisTorneosUseCase,
) : ViewModel() {

    var state by mutableStateOf(InicioState())
        private set

    private var trabajo: Job? = null

    init {
        cargar()
    }

    /** Primera carga y "Reintentar". */
    fun cargar() {
        if (trabajo?.isActive == true) return
        if (state.carga != Carga.LISTO) state = state.copy(carga = Carga.CARGANDO, error = null)
        pedir()
    }

    /** Deslizar hacia abajo: actualiza todo sin borrar lo que se ve. */
    fun refrescar() {
        if (state.refrescando || trabajo?.isActive == true) return
        state = state.copy(refrescando = true)
        pedir()
    }

    /** Al volver de otra pantalla o de otra pestaña, los datos pueden haber cambiado (CA-13). */
    fun alVolver() {
        if (trabajo?.isActive == true) return
        if (state.carga == Carga.ERROR) return
        pedir()
    }

    fun reintentarArbitro() {
        state = state.copy(arbitro = Seccion.Cargando)
        viewModelScope.launch { pedirArbitro() }
    }

    fun reintentarCreador() {
        state = state.copy(creador = Seccion.Cargando)
        viewModelScope.launch { pedirCreador() }
    }

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    fun onCompletarRegistroAtendido() {
        state = state.copy(completarRegistro = false)
    }

    private fun pedir() {
        trabajo = viewModelScope.launch {
            coroutineScope {
                val cuentaPedida = async { obtenerCuentaUseCase() }
                val equiposPedidos = async { obtenerMisEquiposUseCase() }
                val cuenta = cuentaPedida.await()
                // Los bloques de sección no esperan a los equipos: arrancan en cuanto se sabe cuáles hay.
                cuenta.getOrNull()?.secciones?.let { secciones ->
                    if (secciones.arbitro != null) launch { pedirArbitro() }
                    if (secciones.creador != null) launch { pedirCreador() }
                }
                aplicar(cuenta, equiposPedidos.await())
            }
            state = state.copy(refrescando = false)
        }
    }

    private fun aplicar(cuenta: Result<Cuenta>, equipos: Result<List<MiEquipo>>) {
        val fallo = cuenta.exceptionOrNull() ?: equipos.exceptionOrNull()
        if (cuenta.exceptionOrNull() is AppError.CuentaNoRegistrada) {
            state = state.copy(completarRegistro = true)
            return
        }
        val conDatos = state.carga == Carga.LISTO
        state = when {
            fallo == null || conDatos -> state.copy(
                carga = Carga.LISTO,
                error = null,
                nombre = cuenta.getOrNull()?.primerNombre() ?: state.nombre,
                secciones = cuenta.getOrNull()?.secciones ?: state.secciones,
                equipos = equipos.getOrNull() ?: state.equipos,
                // Lo que ya se ve se conserva; el fallo sale como aviso.
                aviso = fallo?.mensajeParaMostrar(),
            )
            else -> state.copy(carga = Carga.ERROR, error = fallo.aErrorDePantalla())
        }
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
                InicioViewModel(c.obtenerCuenta, c.obtenerMisEquipos, c.obtenerPartidosDeArbitro, c.obtenerMisTorneos)
            }
        }
    }
}

/**
 * El partido que sigue para el árbitro (MOV-0010 · CA-2): el de hoy que falta por cerrar y, si no
 * hay, el primero de Próximos (los atrasados van primero: aún necesitan su planilla). Se prefiere
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
