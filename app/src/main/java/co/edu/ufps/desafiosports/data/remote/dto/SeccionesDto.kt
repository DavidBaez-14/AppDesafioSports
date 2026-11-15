package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/** `secciones` de `GET /api/auth/me` (backend 0026). Una sección en null es una que la cuenta no tiene. */
@Serializable
data class SeccionesDto(
    val creador: SeccionCreadorDto? = null,
    val arbitro: SeccionArbitroDto? = null,
)

@Serializable
data class SeccionCreadorDto(
    val torneos: Int? = null,
    val porRevisar: Int? = null,
)

/** Trae también `hoy` (su partido del día); la app lo toma de la lista de partidos (MOV-0004 §4.1). */
@Serializable
data class SeccionArbitroDto(
    val partidosAsignados: Int? = null,
)
