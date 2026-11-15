package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/** Un elemento de `GET /api/supercopa/yo/equipos`. */
@Serializable
data class MiEquipoDto(
    val equipoTorneoId: String? = null,
    val equipoId: String? = null,
    val equipo: String? = null,
    val torneoId: String? = null,
    val torneo: String? = null,
    val deporte: String? = null,
    val estadoTorneo: String? = null,
    val rol: String? = null,
    val estadoInscripcion: String? = null,
    val motivoRechazo: String? = null,
    val comprobante: String? = null,
    val numeroCamiseta: Int? = null,
    val proximoPartido: ProximoPartidoDto? = null,
)

@Serializable
data class ProximoPartidoDto(
    val partidoId: String? = null,
    val rival: String? = null,
    val local: Boolean? = null,
    val fechaHora: String? = null,
    val lugar: String? = null,
    val estado: String? = null,
)
