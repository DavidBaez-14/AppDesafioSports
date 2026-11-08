package co.edu.ufps.desafiosports.domain.model

/** Estado de la sesión de Supabase Auth, sin tipos de Supabase. */
sealed interface EstadoSesion {
    /** Se está leyendo la sesión guardada en el teléfono. */
    data object Cargando : EstadoSesion

    data object Inactiva : EstadoSesion

    data class Activa(val correo: String) : EstadoSesion

    /** Hay sesión guardada pero no se pudo renovar el token (por ejemplo, sin red). */
    data object SinRenovar : EstadoSesion
}
