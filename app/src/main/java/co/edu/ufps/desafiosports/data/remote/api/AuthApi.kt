package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.MeResponseDto
import co.edu.ufps.desafiosports.data.remote.dto.RegistroRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Módulo identidad de BackendCodeCup: `/api/auth/...`. El Bearer lo pone AuthInterceptor. */
interface AuthApi {
    @GET("api/auth/me")
    suspend fun me(): MeResponseDto

    /** Completa el registro tras el primer login: crea persona, cuenta y rol JUGADOR (201). */
    @POST("api/auth/registro")
    suspend fun registro(@Body cuerpo: RegistroRequestDto): MeResponseDto
}
