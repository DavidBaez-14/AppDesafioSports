package co.edu.ufps.desafiosports.domain.model

/** Respuesta de `GET /actuator/health` del backend. */
data class EstadoServidor(
    val url: String,
    /** "UP" cuando el backend y su base de datos responden. */
    val estado: String,
) {
    val enLinea: Boolean get() = estado.equals("UP", ignoreCase = true)
}
