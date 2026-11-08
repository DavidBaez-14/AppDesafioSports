package co.edu.ufps.desafiosports.data.remote.red

import okhttp3.Interceptor
import okhttp3.Response

/** Pone `Authorization: Bearer <JWT de Supabase>` en cada petición al backend, si hay sesión. */
class AuthInterceptor(private val proveedor: ProveedorToken) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticion = chain.request()
        if (peticion.header(AUTHORIZATION) != null) return chain.proceed(peticion)

        val token = proveedor.tokenActual() ?: return chain.proceed(peticion)
        return chain.proceed(
            peticion.newBuilder().header(AUTHORIZATION, "Bearer $token").build()
        )
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
    }
}
