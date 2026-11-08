package co.edu.ufps.desafiosports.data.auth

import co.edu.ufps.desafiosports.data.remote.red.ProveedorToken
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.runBlocking

/**
 * Conecta la red con la sesión de supabase-kt.
 * runBlocking es aceptable aquí: OkHttp llama a estos métodos desde sus propios hilos, nunca desde el hilo principal.
 */
class ProveedorTokenSupabase(private val auth: Auth) : ProveedorToken {

    override fun tokenActual(): String? = auth.currentAccessTokenOrNull()

    override fun renovarToken(): String? = runBlocking {
        runCatching {
            auth.refreshCurrentSession()
            auth.currentAccessTokenOrNull()
        }.getOrNull()
    }

    override fun cerrarSesionLocal() {
        runBlocking { runCatching { auth.clearSession() } }
    }
}
