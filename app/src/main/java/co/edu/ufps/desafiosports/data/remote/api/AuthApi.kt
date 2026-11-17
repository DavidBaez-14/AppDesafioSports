package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.InvitacionVistaDto
import co.edu.ufps.desafiosports.data.remote.dto.MeResponseDto
import co.edu.ufps.desafiosports.data.remote.dto.MiInvitacionDto
import co.edu.ufps.desafiosports.data.remote.dto.RegistroRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Módulo identidad de BackendCodeCup: `/api/auth/...`. El Bearer lo pone AuthInterceptor. */
interface AuthApi {
    @GET("api/auth/me")
    suspend fun me(): MeResponseDto

    /** Completa el registro tras el primer login: crea persona, cuenta y rol JUGADOR (201). */
    @POST("api/auth/registro")
    suspend fun registro(@Body cuerpo: RegistroRequestDto): MeResponseDto

    /** Qué es una invitación antes de aceptarla (MOV-0011). No necesita sesión. */
    @GET("api/auth/invitaciones/{token}")
    suspend fun verInvitacion(@Path("token") token: String): InvitacionVistaDto

    /** Las invitaciones pendientes dirigidas al correo de la sesión, con su token (MOV-0012). */
    @GET("api/auth/mis-invitaciones")
    suspend fun misInvitaciones(): List<MiInvitacionDto>

    /** Responde la cuenta con sus roles; no se usa: Inicio vuelve a pedir `/me`. */
    @POST("api/auth/invitaciones/{token}/aceptar")
    suspend fun aceptarInvitacion(@Path("token") token: String)
}
