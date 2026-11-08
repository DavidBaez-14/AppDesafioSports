package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.MeResponseDto
import retrofit2.http.GET

/** Módulo identidad de BackendCodeCup: `/api/auth/...`. El Bearer lo pone AuthInterceptor. */
interface AuthApi {
    @GET("api/auth/me")
    suspend fun me(): MeResponseDto
}
