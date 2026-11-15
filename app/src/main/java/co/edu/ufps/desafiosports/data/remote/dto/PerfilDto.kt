package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/** `GET /api/supercopa/mi-perfil`. Trae también `partidos`, que la app todavía no usa. */
@Serializable
data class PerfilDto(
    val personaId: String? = null,
    val nombre: String? = null,
    val alturaCm: Int? = null,
    val piernaHabil: String? = null,
    val posicion: String? = null,
    val equipos: List<EquipoDePerfilDto>? = null,
    val resumen: ResumenDePerfilDto? = null,
    val titulos: List<TituloDto>? = null,
)

@Serializable
data class EquipoDePerfilDto(
    val id: String? = null,
    val nombre: String? = null,
    val torneo: String? = null,
)

@Serializable
data class ResumenDePerfilDto(
    val partidosJugados: Int? = null,
    val goles: Int? = null,
    val tarjetas: TarjetasDto? = null,
    val titulos: Int? = null,
)

@Serializable
data class TarjetasDto(
    val amarillas: Int? = null,
    val azules: Int? = null,
    val rojas: Int? = null,
)

@Serializable
data class TituloDto(
    val torneo: String? = null,
    val equipo: String? = null,
    val puesto: String? = null,
)
