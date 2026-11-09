package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.model.ResultadoRegistro

/** Estado inmutable de Crear cuenta (MOV-0003 · A-3). */
data class RegistroState(
    val correo: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val enProceso: Boolean = false,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val error: String? = null,
    /** Evento: a dónde navegar tras crear la cuenta. Se apaga con onDestinoAtendido(). */
    val destino: ResultadoRegistro? = null,
)
