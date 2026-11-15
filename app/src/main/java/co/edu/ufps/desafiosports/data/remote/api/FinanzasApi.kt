package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.MultaDto
import retrofit2.http.GET

/** Módulo de finanzas de BackendCodeCup (MOV-0004). */
interface FinanzasApi {
    @GET("api/finanzas/multas/mias")
    suspend fun misMultas(): List<MultaDto>
}
