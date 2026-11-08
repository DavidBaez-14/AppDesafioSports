package co.edu.ufps.desafiosports.data.remote.red

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Arma OkHttp y Retrofit para BackendCodeCup. Lo usa di/AppContainer y los tests. */
object ClienteHttp {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    fun okHttp(
        proveedor: ProveedorToken,
        errores: ErroresHttp,
        registrarTrafico: Boolean,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(proveedor))
        .addInterceptor(CuentaBloqueadaInterceptor(proveedor, errores))
        .authenticator(TokenAuthenticator(proveedor))
        .apply {
            if (registrarTrafico) {
                // Solo método, URL y status. Nunca el token.
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader(AuthInterceptor.AUTHORIZATION)
                    }
                )
            }
        }
        .build()

    fun retrofit(baseUrl: String, cliente: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(normalizarBaseUrl(baseUrl))
        .client(cliente)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    /** Retrofit exige que la URL base termine en "/". */
    fun normalizarBaseUrl(url: String): String = url.trim().let { if (it.endsWith("/")) it else "$it/" }
}
