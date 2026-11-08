package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Cuerpo de error del backend (application/problem+json).
 * `code` es estable y sirve para decidir; `mensaje` es el texto para mostrar.
 */
@Serializable
data class ProblemDetailDto(
    val status: Int? = null,
    val title: String? = null,
    val detail: String? = null,
    val code: String? = null,
    val mensaje: String? = null,
)
