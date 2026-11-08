package co.edu.ufps.desafiosports.data.remote.red

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Si el backend responde 403 `CUENTA_BLOQUEADA` (en cualquier endpoint), cierra la sesión local.
 * El error sigue su camino hasta la pantalla, que muestra el aviso de bloqueo.
 */
class CuentaBloqueadaInterceptor(
    private val proveedor: ProveedorToken,
    private val errores: ErroresHttp,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val respuesta = chain.proceed(chain.request())
        if (respuesta.code == 403) {
            val cuerpo = respuesta.peekBody(MAX_BYTES_CUERPO).string()
            if (errores.codigo(cuerpo) == CUENTA_BLOQUEADA) proveedor.cerrarSesionLocal()
        }
        return respuesta
    }

    private companion object {
        const val CUENTA_BLOQUEADA = "CUENTA_BLOQUEADA"
        const val MAX_BYTES_CUERPO = 64L * 1024
    }
}
