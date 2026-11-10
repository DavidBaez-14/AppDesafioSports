package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.PaginaDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoDetalleDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoResumenDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Lecturas públicas de torneos (backend 0029, MOV-0005). No necesitan sesión; si la hay, el
 * token viaja igual (AuthInterceptor) y el detalle trae `miRelacion`.
 *
 * Un torneo que no existe o que la persona no puede ver responde lo mismo: 404 `NO_ENCONTRADO`.
 */
interface TorneosApi {
    /** Solo los PUBLICO. `q` busca cada palabra en el nombre, sin mayúsculas ni tildes. */
    @GET("api/public/torneos")
    suspend fun explorar(
        @Query("deporte") deporte: String?,
        @Query("q") busqueda: String?,
        @Query("page") pagina: Int,
        @Query("size") tamano: Int,
    ): PaginaDto<TorneoResumenDto>

    @GET("api/public/torneos/{torneoId}")
    suspend fun torneo(@Path("torneoId") torneoId: String): TorneoDetalleDto

    /** El mismo detalle por enlace: abre también los NO_LISTADO. */
    @GET("api/public/t/{enlaceToken}")
    suspend fun torneoPorEnlace(@Path("enlaceToken") enlaceToken: String): TorneoDetalleDto
}
