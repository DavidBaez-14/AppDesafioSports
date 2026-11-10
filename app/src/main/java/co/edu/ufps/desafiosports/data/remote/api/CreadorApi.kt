package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.CreadorTorneoDto
import co.edu.ufps.desafiosports.data.remote.dto.CrearTorneoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.DeporteDto
import co.edu.ufps.desafiosports.data.remote.dto.InscripcionDto
import co.edu.ufps.desafiosports.data.remote.dto.RechazoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoCreadoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Crear torneo y sección Creador de BackendCodeCup (módulo competición, MOV-0007 y MOV-0008).
 *
 * Las rutas `admin/torneos/{id}/…` las autoriza el rol ADMIN **de ese torneo**: no hay
 * administradores globales. En un torneo ajeno responden 403 `NO_ADMIN_DEL_TORNEO`.
 */
interface CreadorApi {
    /** Catálogo público de deportes: no necesita sesión. */
    @GET("api/public/deportes")
    suspend fun deportes(): List<DeporteDto>

    /** 201 con el torneo y su enlace. Quien lo crea queda como su administrador. */
    @POST("api/supercopa/creador/torneos")
    suspend fun crearTorneo(@Body cuerpo: CrearTorneoRequestDto): TorneoCreadoDto

    @GET("api/supercopa/creador/torneos")
    suspend fun misTorneos(): List<CreadorTorneoDto>

    @GET("api/supercopa/admin/torneos/{torneoId}/inscripciones")
    suspend fun inscripciones(@Path("torneoId") torneoId: String): List<InscripcionDto>

    /** Responde la inscripción; no se usa porque la lista se vuelve a pedir. */
    @POST("api/supercopa/admin/torneos/{torneoId}/inscripciones/{equipoTorneoId}/aprobar")
    suspend fun aprobarInscripcion(@Path("torneoId") torneoId: String, @Path("equipoTorneoId") equipoTorneoId: String)

    @POST("api/supercopa/admin/torneos/{torneoId}/inscripciones/{equipoTorneoId}/rechazar")
    suspend fun rechazarInscripcion(
        @Path("torneoId") torneoId: String,
        @Path("equipoTorneoId") equipoTorneoId: String,
        @Body cuerpo: RechazoRequestDto,
    )
}
