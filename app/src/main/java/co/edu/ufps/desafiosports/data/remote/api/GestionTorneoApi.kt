package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.ProgramarPartidoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.ArbitroPartidoDto
import co.edu.ufps.desafiosports.data.remote.dto.ArbitroAsignadoDto
import co.edu.ufps.desafiosports.data.remote.dto.AsignarArbitroRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.InvitacionDto
import co.edu.ufps.desafiosports.data.remote.dto.InvitarATorneoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.PartidoAdminDto
import co.edu.ufps.desafiosports.data.remote.dto.RolTorneoDto
import co.edu.ufps.desafiosports.data.remote.dto.TodosLosPartidosRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoConfigDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Árbitros y partidos de un torneo (MOV-0011; backend 0024 y 0025).
 *
 * Todas las rutas las autoriza el rol ADMIN **de ese torneo**: en un torneo ajeno responden 403.
 */
interface GestionTorneoApi {
    /** Administradores y árbitros del torneo. */
    @GET("api/supercopa/admin/torneos/{torneoId}/roles")
    suspend fun roles(@Path("torneoId") torneoId: String): List<RolTorneoDto>

    @DELETE("api/supercopa/admin/torneos/{torneoId}/roles/{rolId}")
    suspend fun quitarRol(@Path("torneoId") torneoId: String, @Path("rolId") rolId: String)

    /** Responde el rol actualizado; no se usa. */
    @PUT("api/supercopa/admin/torneos/{torneoId}/arbitros/{cuentaId}/todos-los-partidos")
    suspend fun todosLosPartidos(
        @Path("torneoId") torneoId: String,
        @Path("cuentaId") cuentaId: String,
        @Body cuerpo: TodosLosPartidosRequestDto,
    )

    /** Las invitaciones pendientes del torneo, de cualquier rol. */
    @GET("api/supercopa/admin/torneos/{torneoId}/invitaciones")
    suspend fun invitaciones(@Path("torneoId") torneoId: String): List<InvitacionDto>

    /** 201 con la invitación y su enlace para compartir. */
    @POST("api/supercopa/admin/torneos/{torneoId}/invitaciones")
    suspend fun invitar(@Path("torneoId") torneoId: String, @Body cuerpo: InvitarATorneoRequestDto): InvitacionDto

    @DELETE("api/supercopa/admin/torneos/{torneoId}/invitaciones/{invitacionId}")
    suspend fun revocarInvitacion(@Path("torneoId") torneoId: String, @Path("invitacionId") invitacionId: String)

    @GET("api/supercopa/admin/torneos/{torneoId}/configuracion")
    suspend fun configuracion(@Path("torneoId") torneoId: String): TorneoConfigDto

    /** PUBLICADO → EN_CURSO. Responde el torneo; no se usa. */
    @POST("api/supercopa/admin/torneos/{torneoId}/iniciar")
    suspend fun iniciar(@Path("torneoId") torneoId: String)

    /** Genera los partidos según el formato. Responde los creados; no se usan: la lista se vuelve a pedir. */
    @POST("api/supercopa/admin/torneos/{torneoId}/fixture")
    suspend fun generarFixture(@Path("torneoId") torneoId: String)

    @GET("api/supercopa/admin/torneos/{torneoId}/partidos")
    suspend fun partidos(@Path("torneoId") torneoId: String): List<PartidoAdminDto>

    /** 409 `NO_ES_ARBITRO_DEL_TORNEO` o `ARBITRO_EN_PLANTILLA`. */
    @POST("api/supercopa/admin/partidos/{partidoId}/arbitros")
    suspend fun asignarArbitro(@Path("partidoId") partidoId: String, @Body cuerpo: AsignarArbitroRequestDto): List<ArbitroAsignadoDto>

    @DELETE("api/supercopa/admin/partidos/{partidoId}/arbitros/{cuentaId}")
    suspend fun quitarArbitro(@Path("partidoId") partidoId: String, @Path("cuentaId") cuentaId: String): List<ArbitroAsignadoDto>

    /** Cuándo y dónde se juega (MOV-0013). 409 `PARTIDO_CERRADO` si ya terminó. */
    @PUT("api/supercopa/admin/partidos/{partidoId}/programacion")
    suspend fun programar(@Path("partidoId") partidoId: String, @Body cuerpo: ProgramarPartidoRequestDto): ArbitroPartidoDto

    /** Borra los partidos (MOV-0013). 400 `REGLA_NEGOCIO` si alguno ya se jugó o está en juego. */
    @DELETE("api/supercopa/admin/torneos/{torneoId}/fixture")
    suspend fun borrarFixture(@Path("torneoId") torneoId: String)
}
