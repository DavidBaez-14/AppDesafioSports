package co.edu.ufps.desafiosports.presentacion.diagnostico

import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.EstadoSesion

/** Estado inmutable de la pantalla. El ViewModel lo reemplaza con copy(). */
data class DiagnosticoState(
    val faltantes: List<String> = emptyList(),
    val apiBaseUrl: String = "",
    val servidor: EstadoComprobacion = EstadoComprobacion.Comprobando,
    val sesion: EstadoSesion = EstadoSesion.Cargando,
    val correo: String = "",
    val clave: String = "",
    val enProceso: Boolean = false,
    val cuenta: Cuenta? = null,
    val error: String? = null,
)

sealed interface EstadoComprobacion {
    data object Comprobando : EstadoComprobacion
    data class EnLinea(val estado: String) : EstadoComprobacion
    data class Fallo(val mensaje: String) : EstadoComprobacion
}
