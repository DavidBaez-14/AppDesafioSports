package co.edu.ufps.desafiosports.data.remote.red

import co.edu.ufps.desafiosports.data.remote.red.AuthInterceptor.Companion.AUTHORIZATION
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Ante un 401 de una petición que iba con token: renueva la sesión UNA vez y reintenta.
 * Si no se puede renovar, o si el reintento vuelve a dar 401, cierra la sesión local.
 * Es lo mismo que hace `src/api/http.js` en la web (spec 0019 del backend).
 */
class TokenAuthenticator(private val proveedor: ProveedorToken) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val tokenEnviado = response.request.header(AUTHORIZATION)?.removePrefix("Bearer ")
            ?: return null // la petición no llevaba sesión: no hay nada que renovar

        // Ya se reintentó una vez y volvió a fallar.
        if (response.priorResponse != null) {
            proveedor.cerrarSesionLocal()
            return null
        }

        synchronized(this) {
            // Otra petición pudo renovar el token mientras esta esperaba.
            val actual = proveedor.tokenActual()
            if (actual != null && actual != tokenEnviado) return conToken(response.request, actual)

            val nuevo = proveedor.renovarToken()
            if (nuevo == null) {
                proveedor.cerrarSesionLocal()
                return null
            }
            return conToken(response.request, nuevo)
        }
    }

    private fun conToken(peticion: Request, token: String): Request =
        peticion.newBuilder().header(AUTHORIZATION, "Bearer $token").build()
}
