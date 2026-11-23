package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

// DTO de la inscripción de un equipo (MOV-0014), tal cual los responde BackendCodeCup. Todo es
// anulable con valor por defecto: el backend puede agregar campos sin que la app falle.

/** `EquipoDTO`: un equipo base de la persona. */
@Serializable
data class EquipoBaseDto(
    val id: String? = null,
    val nombre: String? = null,
)

/**
 * Cuerpo de `POST …/inscribir`. Va [equipoId] o [nombreEquipo] (crea el equipo); los null no
 * viajan. [enlaceToken] solo en un torneo que se abrió con su enlace.
 */
@Serializable
data class InscribirEquipoRequestDto(
    val equipoId: String? = null,
    val nombreEquipo: String? = null,
    val enlaceToken: String? = null,
)

/** `InscripcionDTO` (solo los campos que se usan). */
@Serializable
data class InscripcionDelegadoDto(
    val id: String? = null,
    val equipoNombre: String? = null,
    val estadoInscripcion: String? = null,
)

// ── Plantel (MOV-0015) ──

/** `MiembroEquipoDTO` (solo los campos que se usan). */
@Serializable
data class MiembroEquipoDto(
    val personaId: String? = null,
    val numeroDocumento: String? = null,
    val nombre: String? = null,
    val numeroCamiseta: Int? = null,
    /** ACTIVO o RETIRADO. */
    val estado: String? = null,
    val esDelegado: Boolean? = null,
    val torneoEstado: String? = null,
    val tieneCuenta: Boolean? = null,
)

/** Cuerpo de `POST …/miembros`. Los null no viajan. */
@Serializable
data class AgregarMiembroRequestDto(
    val numeroDocumento: String? = null,
    val tipoDocumento: String? = null,
    val nombre: String? = null,
    val numeroCamiseta: Int? = null,
)

/** Cuerpo de `PATCH …/camiseta`. Sin número viaja `{}` y el backend se lo quita. */
@Serializable
data class CamisetaRequestDto(val numeroCamiseta: Int? = null)
