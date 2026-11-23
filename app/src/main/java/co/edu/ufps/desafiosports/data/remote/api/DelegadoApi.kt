package co.edu.ufps.desafiosports.data.remote.api

import retrofit2.http.PATCH
import co.edu.ufps.desafiosports.data.remote.dto.MiembroEquipoDto
import co.edu.ufps.desafiosports.data.remote.dto.CamisetaRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.AgregarMiembroRequestDto
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

    // ── Plantel (MOV-0015). 403 `NO_DELEGADO_DE_LA_INSCRIPCION` si no es su delegado. ──

    /** Activos y retirados, en orden de ingreso. */
    @GET("api/supercopa/delegado/equipo-torneo/{equipoTorneoId}/miembros")
    suspend fun plantel(@Path("equipoTorneoId") equipoTorneoId: String): List<MiembroEquipoDto>

    /**
     * 400 `DOCUMENTO_REQUERIDO`, `NOMBRE_REQUERIDO` o `VALIDACION`; 409 `JUGADOR_BLOQUEADO`; 400
     * `REGLA_NEGOCIO` si ya juega en otro equipo del torneo o el número está ocupado.
     */
    @POST("api/supercopa/delegado/equipo-torneo/{equipoTorneoId}/miembros")
    suspend fun agregarJugador(@Path("equipoTorneoId") equipoTorneoId: String, @Body cuerpo: AgregarMiembroRequestDto): MiembroEquipoDto

    /** Lo deja RETIRADO. 400 `REGLA_NEGOCIO` si es el delegado. */
    @POST("api/supercopa/delegado/equipo-torneo/{equipoTorneoId}/miembros/{personaId}/remover")
    suspend fun quitarJugador(@Path("equipoTorneoId") equipoTorneoId: String, @Path("personaId") personaId: String): MiembroEquipoDto

    /** 400 `REGLA_NEGOCIO` si el número lo tiene otro activo. Sin número (`{}`) se lo quita. */
    @PATCH("api/supercopa/delegado/equipo-torneo/{equipoTorneoId}/miembros/{personaId}/camiseta")
    suspend fun cambiarCamiseta(
        @Path("equipoTorneoId") equipoTorneoId: String,
        @Path("personaId") personaId: String,
        @Body cuerpo: CamisetaRequestDto,
    ): MiembroEquipoDto
}
