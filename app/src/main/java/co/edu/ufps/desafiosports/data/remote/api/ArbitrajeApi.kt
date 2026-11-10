package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.AlineacionRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.AlinearEquipoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.ArbitroPartidoDto
import co.edu.ufps.desafiosports.data.remote.dto.EventoDto
import co.edu.ufps.desafiosports.data.remote.dto.EventoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.PlanillaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Sección Árbitro de BackendCodeCup (módulo competición, MOV-0009).
 *
 * Las lecturas están en `/api/supercopa/arbitro/…`. Las acciones de la planilla viven en
 * `/api/supercopa/admin/partidos/{id}/…`, pero las autoriza el mismo permiso de árbitro del partido.
 */
interface ArbitrajeApi {
    @GET("api/supercopa/arbitro/partidos")
    suspend fun misPartidos(): List<ArbitroPartidoDto>

    @GET("api/supercopa/arbitro/partidos/{partidoId}/planilla")
    suspend fun planilla(@Path("partidoId") partidoId: String): PlanillaDto

    @POST("api/supercopa/admin/partidos/{partidoId}/eventos")
    suspend fun registrarEvento(@Path("partidoId") partidoId: String, @Body cuerpo: EventoRequestDto): EventoDto

    @DELETE("api/supercopa/admin/partidos/{partidoId}/eventos/{eventoId}")
    suspend fun quitarEvento(@Path("partidoId") partidoId: String, @Path("eventoId") eventoId: String)

    @POST("api/supercopa/admin/partidos/{partidoId}/alineacion")
    suspend fun alinear(@Path("partidoId") partidoId: String, @Body cuerpo: AlineacionRequestDto)

    @DELETE("api/supercopa/admin/partidos/{partidoId}/alineacion/{personaId}")
    suspend fun quitarDeAlineacion(@Path("partidoId") partidoId: String, @Path("personaId") personaId: String)

    @POST("api/supercopa/admin/partidos/{partidoId}/alineacion/add-all")
    suspend fun alinearEquipo(@Path("partidoId") partidoId: String, @Body cuerpo: AlinearEquipoRequestDto)

    /** Responde el partido cerrado; no se usa porque la planilla se vuelve a pedir. */
    @POST("api/supercopa/admin/partidos/{partidoId}/cerrar")
    suspend fun cerrarPartido(@Path("partidoId") partidoId: String)
}
