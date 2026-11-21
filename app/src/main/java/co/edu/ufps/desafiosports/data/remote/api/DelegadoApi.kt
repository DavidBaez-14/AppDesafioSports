package co.edu.ufps.desafiosports.data.remote.api

import co.edu.ufps.desafiosports.data.remote.dto.EquipoBaseDto
import co.edu.ufps.desafiosports.data.remote.dto.InscribirEquipoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.InscripcionDelegadoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Lo que hace quien inscribe un equipo (MOV-0014; backend 0026 y 0029). Basta con estar
 * registrado: no hace falta un rol global de delegado.
 */
interface DelegadoApi {
    /** Sus equipos base, el más reciente primero. */
    @GET("api/supercopa/delegado/equipos")
    suspend fun misEquipos(): List<EquipoBaseDto>

    /**
     * Queda `PENDIENTE_PAGO`, con quien inscribe como delegado y en el plantel. 409 `YA_INSCRITO`,
     * `EQUIPO_DUPLICADO`, `CUPOS_AGOTADOS`, `INSCRIPCIONES_CERRADAS`, `TORNEO_INDIVIDUAL`,
     * `YA_EN_UN_EQUIPO_DEL_TORNEO` o `DOCUMENTO_REQUERIDO`; 400 `NOMBRE_INVALIDO`.
     */
    @POST("api/supercopa/delegado/torneos/{torneoId}/inscribir")
    suspend fun inscribir(@Path("torneoId") torneoId: String, @Body cuerpo: InscribirEquipoRequestDto): InscripcionDelegadoDto
}
