package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

// DTO de la sección Árbitro y la planilla (MOV-0009), tal cual los responde BackendCodeCup.
// Todo es anulable con valor por defecto: el backend manda los campos vacíos como null y puede
// agregar campos nuevos sin que la app falle. No salen de la capa data.

@Serializable
data class EquipoEnPartidoDto(
    val equipoTorneoId: String? = null,
    val nombre: String? = null,
)

@Serializable
data class MarcadorDto(
    val local: Int? = null,
    val visitante: Int? = null,
)

/** `ArbitroPartidoDTO`. */
@Serializable
data class ArbitroPartidoDto(
    val partidoId: String? = null,
    val torneoId: String? = null,
    val torneo: String? = null,
    val deporte: String? = null,
    val fase: String? = null,
    val local: EquipoEnPartidoDto? = null,
    val visitante: EquipoEnPartidoDto? = null,
    /** `2026-11-12T16:00:00`, sin zona. */
    val fechaHora: String? = null,
    val lugar: String? = null,
    val estado: String? = null,
    val marcador: MarcadorDto? = null,
    val asignado: Boolean? = null,
)

@Serializable
data class JugadorPlanillaDto(
    val personaId: String? = null,
    val nombre: String? = null,
    val numeroCamiseta: Int? = null,
    val numeroDocumento: String? = null,
    val esDelegado: Boolean? = null,
    val alineado: Boolean? = null,
    val goles: Int? = null,
    val apto: Boolean? = null,
    val motivos: List<String>? = null,
)

@Serializable
data class EquipoPlanillaDto(
    val equipoTorneoId: String? = null,
    val nombre: String? = null,
    val jugadores: List<JugadorPlanillaDto>? = null,
)

/** `EventoDTO`. */
@Serializable
data class EventoDto(
    val id: String? = null,
    val orden: Int? = null,
    val personaId: String? = null,
    val jugadorNombre: String? = null,
    val equipoTorneoId: String? = null,
    val equipoNombre: String? = null,
    val tipoEvento: String? = null,
)

/** `PlanillaDTO`. */
@Serializable
data class PlanillaDto(
    val partido: ArbitroPartidoDto? = null,
    val tiposEvento: List<String>? = null,
    val local: EquipoPlanillaDto? = null,
    val visitante: EquipoPlanillaDto? = null,
    val eventos: List<EventoDto>? = null,
    val editable: Boolean? = null,
)

/** Cuerpo de `POST …/eventos`. */
@Serializable
data class EventoRequestDto(
    val personaId: String,
    val equipoTorneoId: String,
    val tipoEvento: String,
)

/** Cuerpo de `POST …/alineacion`. */
@Serializable
data class AlineacionRequestDto(
    val personaId: String,
    val equipoTorneoId: String,
)

/** Cuerpo de `POST …/alineacion/add-all`. */
@Serializable
data class AlinearEquipoRequestDto(val equipoTorneoId: String)
