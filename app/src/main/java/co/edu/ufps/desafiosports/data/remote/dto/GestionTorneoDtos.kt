package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

// DTO de la gestión de un torneo (MOV-0011), tal cual BackendCodeCup (specs 0024 y 0025).
// Las respuestas son anulables con valor por defecto; los cuerpos que se envían, no.

/** `RolTorneoDTO` de `GET /api/supercopa/admin/torneos/{id}/roles`. Trae admins y árbitros. */
@Serializable
data class RolTorneoDto(
    val id: String? = null,
    val cuentaId: String? = null,
    val nombre: String? = null,
    val correo: String? = null,
    /** ADMIN o ARBITRO. */
    val rol: String? = null,
    val todosLosPartidos: Boolean? = null,
)

/** Cuerpo de `PUT …/arbitros/{cuentaId}/todos-los-partidos`. */
@Serializable
data class TodosLosPartidosRequestDto(val todosLosPartidos: Boolean)

/** `InvitacionDTO`. La lista del torneo trae también coadministradores y cupos de equipo. */
@Serializable
data class InvitacionDto(
    val id: String? = null,
    val correo: String? = null,
    val rol: String? = null,
    val estado: String? = null,
    val enlace: String? = null,
    val createdAt: String? = null,
)

/** Cuerpo de `POST …/invitaciones`. Sin correo es "por enlace": con `explicitNulls = false` no viaja. */
@Serializable
data class InvitarATorneoRequestDto(val rol: String, val correo: String? = null)

/** `TorneoConfigDTO` de `GET …/configuracion`: solo lo que hace falta para generar los partidos. */
@Serializable
data class TorneoConfigDto(
    val estado: String? = null,
    val equiposAprobados: Int? = null,
    val fixtureGenerado: Boolean? = null,
)

@Serializable
data class ArbitroAsignadoDto(
    val cuentaId: String? = null,
    val nombre: String? = null,
)

/** `PartidoAdminDTO` de `GET …/partidos`. */
@Serializable
data class PartidoAdminDto(
    val id: String? = null,
    val fecha: String? = null,
    val estado: String? = null,
    val localNombre: String? = null,
    val visitanteNombre: String? = null,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val fase: String? = null,
    val jornada: Int? = null,
    val grupo: String? = null,
    val lugar: String? = null,
    val arbitros: List<ArbitroAsignadoDto>? = null,
)

/** Cuerpo de `POST /api/supercopa/admin/partidos/{id}/arbitros`. */
@Serializable
data class AsignarArbitroRequestDto(val cuentaId: String)

/** `InvitacionVistaDTO` de `GET /api/auth/invitaciones/{token}`. */
@Serializable
data class InvitacionVistaDto(
    val rol: String? = null,
    val ambito: String? = null,
    val ambitoDescripcion: String? = null,
    val estado: String? = null,
    val invitadoPor: String? = null,
    val paraCorreo: String? = null,
)
