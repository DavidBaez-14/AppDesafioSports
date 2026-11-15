package co.edu.ufps.desafiosports.domain.model

/**
 * Las secciones de la persona según `GET /api/auth/me` (backend 0026, MOV-0004). Todos entran
 * como jugador; Creador y Árbitro son secciones que solo aparecen si la persona las tiene.
 * Una sección en null es una que no tiene.
 */
data class Secciones(
    val creador: SeccionDeCreador? = null,
    val arbitro: SeccionDeArbitro? = null,
) {
    val ninguna: Boolean get() = creador == null && arbitro == null
}

/** Administra al menos un torneo. [porRevisar] suma inscripciones y comprobantes pendientes. */
data class SeccionDeCreador(val torneos: Int, val porRevisar: Int)

/** Es árbitro de al menos un torneo, aunque todavía no tenga partidos. */
data class SeccionDeArbitro(val partidosAsignados: Int)
