package co.edu.ufps.desafiosports.presentacion.perfil

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.ufps.desafiosports.DesafioSportsApp
import co.edu.ufps.desafiosports.domain.model.Multa
import co.edu.ufps.desafiosports.domain.model.Perfil
import co.edu.ufps.desafiosports.domain.model.iniciales
import co.edu.ufps.desafiosports.domain.model.nombrePublico
import co.edu.ufps.desafiosports.domain.use_case.competicion.ObtenerMiPerfilUseCase
import co.edu.ufps.desafiosports.domain.use_case.finanzas.ObtenerMisMultasUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.CerrarSesionUseCase
import co.edu.ufps.desafiosports.domain.use_case.identidad.ObtenerCuentaUseCase
import co.edu.ufps.desafiosports.presentacion.acceso.mensajeParaMostrar
import co.edu.ufps.desafiosports.presentacion.comun.ErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.comun.aErrorDePantalla
import co.edu.ufps.desafiosports.presentacion.inicio.Carga
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class PerfilState(
    // El encabezado sale de la cuenta y se pinta en cuanto llega, sin esperar al resto.
    val nombre: String = "",
    /** "Carlos R.": así se ve la persona en lo público. */
    val nombrePublico: String = "",
    val iniciales: String = "",
    val correo: String = "",
    val carga: Carga = Carga.CARGANDO,
    /** Falló la primera carga. "Cerrar sesión" sigue disponible (MOV-0004 · CA-11). */
    val error: ErrorDePantalla? = null,
    val perfil: Perfil? = null,
    /** Ya ordenadas: primero las que siguen abiertas. */
    val multas: List<Multa> = emptyList(),
    val refrescando: Boolean = false,
    val aviso: String? = null,
    val cerrandoSesion: Boolean = false,
) {
    /** Hay algo que mostrar en "Sanciones": multas o tarjetas (CA-8). */
    val conSanciones: Boolean get() = multas.isNotEmpty() || (perfil?.tarjetas?.total ?: 0) > 0
}

/** Perfil (MOV-0004): quién es la persona, sus cifras, su trayectoria, sus sanciones y su cuenta. */
class PerfilViewModel(
    private val obtenerCuentaUseCase: ObtenerCuentaUseCase,
    private val obtenerMiPerfilUseCase: ObtenerMiPerfilUseCase,
    private val obtenerMisMultasUseCase: ObtenerMisMultasUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
) : ViewModel() {

    var state by mutableStateOf(PerfilState())
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

    /** Deslizar hacia abajo. */
    fun refrescar() {
        if (state.refrescando || trabajo?.isActive == true) return
        state = state.copy(refrescando = true)
        pedir()
    }

    /** Al volver a la pestaña: una multa o un partido nuevo se ven sin tener que deslizar. */
    fun alVolver() {
        if (trabajo?.isActive == true || state.carga == Carga.ERROR) return
        pedir()
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

    fun onAvisoMostrado() {
        state = state.copy(aviso = null)
    }

    private fun pedir() {
        trabajo = viewModelScope.launch {
            coroutineScope {
                val perfilPedido = async { obtenerMiPerfilUseCase() }
                val multasPedidas = async { obtenerMisMultasUseCase() }
                // Si la cuenta falla, el encabezado se arma con el nombre que trae el perfil.
                obtenerCuentaUseCase().onSuccess { cuenta ->
                    state = state.copy(
                        nombre = cuenta.nombre,
                        nombrePublico = cuenta.nombrePublico(),
                        iniciales = cuenta.iniciales(),
                        correo = cuenta.correo,
                    )
                }
                aplicar(perfilPedido.await(), multasPedidas.await())
            }
            state = state.copy(refrescando = false)
        }
    }

    private fun aplicar(perfil: Result<Perfil>, multas: Result<List<Multa>>) {
        val fallo = perfil.exceptionOrNull() ?: multas.exceptionOrNull()
        val conDatos = state.carga == Carga.LISTO
        state = when {
            fallo == null || conDatos -> state.copy(
                carga = Carga.LISTO,
                error = null,
                nombre = state.nombre.ifBlank { perfil.getOrNull()?.nombre.orEmpty() },
                perfil = perfil.getOrNull() ?: state.perfil,
                multas = multas.getOrNull() ?: state.multas,
                aviso = fallo?.mensajeParaMostrar(),
            )
            else -> state.copy(carga = Carga.ERROR, error = fallo.aErrorDePantalla())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DesafioSportsApp
                val c = app.container
                PerfilViewModel(c.obtenerCuenta, c.obtenerMiPerfil, c.obtenerMisMultas, c.cerrarSesion)
            }
        }
    }
}
