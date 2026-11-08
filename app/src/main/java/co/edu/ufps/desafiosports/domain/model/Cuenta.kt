package co.edu.ufps.desafiosports.domain.model

/** Roles globales que hoy devuelve el backend. Con los roles por torneo (backend M3) cambia. */
enum class Rol { JUGADOR, DELEGADO, ARBITRO, ADMINISTRADOR }

/** La cuenta de quien inició sesión, según `GET /api/auth/me`. */
data class Cuenta(
    val id: String,
    val nombres: String,
    val apellidos: String,
    /** Nombres + apellidos, listo para mostrar. */
    val nombre: String,
    val correo: String,
    val estado: String,
    val roles: Set<Rol>,
    val rolesPendientes: Set<Rol>,
)
