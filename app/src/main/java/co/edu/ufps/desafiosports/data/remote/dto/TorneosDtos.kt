package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

// DTO de Explorar y del detalle público del torneo (MOV-0005), tal cual BackendCodeCup (0029).
// Todas las respuestas son anulables con valor por defecto: un campo que falte no rompe la pantalla.

/** `PaginaDTO`. */
@Serializable
data class PaginaDto<T>(
    val contenido: List<T> = emptyList(),
    val pagina: Int = 0,
    val tamano: Int = 0,
    val totalElementos: Long = 0,
    val totalPaginas: Int = 0,
)

/** `TorneoResumenPublicoDTO` de `GET /api/public/torneos`. */
@Serializable
data class TorneoResumenDto(
    val id: String? = null,
    val nombre: String? = null,
    val deporte: String? = null,
    val deporteNombre: String? = null,
    val formato: String? = null,
    val estado: String? = null,
    val cupos: Int? = null,
    val inscritos: Int? = null,
    val cierreInscripciones: String? = null,
    val inicio: String? = null,
    val organizador: String? = null,
    val inscripcionesAbiertas: Boolean? = null,
)

@Serializable
data class EquipoPublicoDto(val equipoTorneoId: String? = null, val nombre: String? = null, val miembros: Int? = null)

@Serializable
data class EquipoEnPartidoPublicoDto(val equipoTorneoId: String? = null, val nombre: String? = null)

@Serializable
data class MarcadorPublicoDto(val local: Int? = null, val visitante: Int? = null)

@Serializable
data class PartidoPublicoDto(
    val partidoId: String? = null,
    val fase: String? = null,
    val jornada: Int? = null,
    val grupo: String? = null,
    val local: EquipoEnPartidoPublicoDto? = null,
    val visitante: EquipoEnPartidoPublicoDto? = null,
    val fechaHora: String? = null,
    val lugar: String? = null,
    val estado: String? = null,
    val marcador: MarcadorPublicoDto? = null,
)

@Serializable
data class PremioPublicoDto(
    val nombre: String? = null,
    val titulo: String? = null,
    val ganadorNombre: String? = null,
    val ganadorEquipo: String? = null,
)

/** `PosicionGrupoDTO`. */
@Serializable
data class PosicionDto(
    val posicion: Int? = null,
    val equipoTorneoId: String? = null,
    val equipoNombre: String? = null,
    val pts: Int? = null,
    val pj: Int? = null,
    val pg: Int? = null,
    val pe: Int? = null,
    val pp: Int? = null,
    val dg: Int? = null,
)

@Serializable
data class RelacionConEquipoDto(
    val equipoTorneoId: String? = null,
    val equipo: String? = null,
    val estadoInscripcion: String? = null,
    val comprobante: String? = null,
)

@Serializable
data class MiRelacionDto(
    val administra: Boolean = false,
    val arbitra: Boolean = false,
    val delegadoDe: RelacionConEquipoDto? = null,
    val juegaEn: RelacionConEquipoDto? = null,
)

/** `TorneoPublicoDTO` de `GET /api/public/torneos/{id}` y `/api/public/t/{enlaceToken}`. */
@Serializable
data class TorneoDetalleDto(
    val id: String? = null,
    val nombre: String? = null,
    val deporte: DeporteDto? = null,
    val formato: String? = null,
    val estado: String? = null,
    val visibilidad: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val cierreInscripciones: String? = null,
    val cupos: Int? = null,
    val inscritos: Int? = null,
    val inscripcionesAbiertas: Boolean? = null,
    val cobraInscripcion: Boolean? = null,
    val valorInscripcion: Double? = null,
    val exigeDocumento: Boolean? = null,
    val reglas: String? = null,
    val organizador: String? = null,
    val enlace: String? = null,
    val equipos: List<EquipoPublicoDto>? = null,
    val tabla: Map<String, List<PosicionDto>>? = null,
    val partidos: List<PartidoPublicoDto>? = null,
    val premios: List<PremioPublicoDto>? = null,
    val miRelacion: MiRelacionDto? = null,
)
