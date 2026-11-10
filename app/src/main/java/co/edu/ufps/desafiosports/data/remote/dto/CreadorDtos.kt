package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

// DTO de "Crear torneo" y de la sección Creador (MOV-0007 y MOV-0008), tal cual BackendCodeCup.
// Las respuestas son anulables con valor por defecto; los cuerpos que se envían, no.

/** `DeporteDTO` de `GET /api/public/deportes`. */
@Serializable
data class DeporteDto(
    val codigo: String? = null,
    val nombre: String? = null,
    /** EQUIPO o INDIVIDUAL. */
    val tipoCompetencia: String? = null,
)

/** Cuerpo de `POST /api/supercopa/creador/torneos` (`CrearTorneoRapidoRequest`). */
@Serializable
data class CrearTorneoRequestDto(
    val nombre: String,
    val deporte: String,
    val formato: String,
    val cupos: Int,
    /** `2026-11-21`. */
    val fechaInicio: String,
    val visibilidad: String,
    val cobraInscripcion: Boolean,
    /** Null si no cobra: con `explicitNulls = false` no viaja. */
    val valorInscripcion: Long? = null,
)

/** `TorneoCreadoDTO`. */
@Serializable
data class TorneoCreadoDto(
    val id: String? = null,
    val nombre: String? = null,
    val deporte: String? = null,
    val visibilidad: String? = null,
    val enlace: String? = null,
)

@Serializable
data class PendientesDto(
    val inscripciones: Int? = null,
    val comprobantes: Int? = null,
)

/** `CreadorTorneoDTO` de `GET /api/supercopa/creador/torneos`. */
@Serializable
data class CreadorTorneoDto(
    val id: String? = null,
    val nombre: String? = null,
    val estado: String? = null,
    val deporte: String? = null,
    val visibilidad: String? = null,
    val cupos: Int? = null,
    val enlace: String? = null,
    val inscritos: Int? = null,
    val pendientes: PendientesDto? = null,
)

/** `InscripcionDTO` de `GET /api/supercopa/admin/torneos/{id}/inscripciones`. */
@Serializable
data class InscripcionDto(
    val id: String? = null,
    val equipoNombre: String? = null,
    val delegadoNombre: String? = null,
    val estadoInscripcion: String? = null,
    val motivoRechazo: String? = null,
    val fechaInscripcion: String? = null,
    val montoInscripcion: Double? = null,
)

/** Cuerpo de `POST …/inscripciones/{id}/rechazar`. */
@Serializable
data class RechazoRequestDto(val motivo: String)
