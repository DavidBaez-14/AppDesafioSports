package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.MiEquipoDto
import co.edu.ufps.desafiosports.data.remote.dto.PerfilDto
import retrofit2.http.GET

/** Lo que el módulo de competición le dice a la persona sobre sí misma (backend 0026, MOV-0004). */
interface CompeticionApi {
    @GET("api/supercopa/yo/equipos")
    suspend fun misEquipos(): List<MiEquipoDto>

    @GET("api/supercopa/mi-perfil")
    suspend fun miPerfil(): PerfilDto
}
