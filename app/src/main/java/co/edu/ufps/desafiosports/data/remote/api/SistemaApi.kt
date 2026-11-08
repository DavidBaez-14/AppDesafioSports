package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.HealthDto
import retrofit2.http.GET

/** Endpoints técnicos del backend. `/actuator/health` es público. */
interface SistemaApi {
    @GET("actuator/health")
    suspend fun health(): HealthDto
}
