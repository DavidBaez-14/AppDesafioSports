package co.edu.ufps.desafiosports.data.remote.red

/**
 * Lo que la red necesita de la sesión. Lo implementa la capa de auth (Supabase) y,
 * en los tests, un fake. Los métodos son bloqueantes porque OkHttp los llama desde sus hilos.
 */
interface ProveedorToken {
    /** Access token vigente (JWT de Supabase) o null si no hay sesión. */
    fun tokenActual(): String?

    /** Renueva la sesión y devuelve el token nuevo, o null si no se pudo. */
    fun renovarToken(): String?

    /** Borra la sesión guardada en el teléfono, sin llamar a la red. */
    fun cerrarSesionLocal()
}
