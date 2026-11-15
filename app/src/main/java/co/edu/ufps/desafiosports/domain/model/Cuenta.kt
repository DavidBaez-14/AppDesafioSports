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
    /** Qué secciones tiene además de la de jugador (MOV-0004). */
    val secciones: Secciones = Secciones(),
)

/** "Carlos", para el saludo. Null si la cuenta no trae nombre. */
fun Cuenta.primerNombre(): String? =
    nombres.trim().split(' ').firstOrNull { it.isNotBlank() } ?: nombre.trim().split(' ').firstOrNull { it.isNotBlank() }

/** "CR": las iniciales para el círculo de la persona mientras no haya fotos. */
fun Cuenta.iniciales(): String {
    val partes = listOf(nombres, apellidos).mapNotNull { parte -> parte.trim().firstOrNull()?.uppercaseChar() }
    return if (partes.isNotEmpty()) partes.joinToString("") else nombre.trim().take(1).uppercase()
}

/**
 * "Carlos R.": así se ve la persona en lo público. Nunca el apellido completo ni el documento,
 * porque hay menores (arquitectura §5.2, regla 9).
 */
fun Cuenta.nombrePublico(): String {
    val inicial = apellidos.trim().firstOrNull()?.uppercaseChar()
    val base = nombres.trim().ifBlank { nombre.trim() }
    return if (inicial != null && nombres.isNotBlank()) "$base $inicial." else base
}
