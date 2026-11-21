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
