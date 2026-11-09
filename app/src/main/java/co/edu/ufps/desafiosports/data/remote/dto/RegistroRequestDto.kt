package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Cuerpo de `POST /api/auth/registro` (MOV-0003). Los campos nulos no se envían
 * (ClienteHttp.json tiene explicitNulls = false), igual que si el documento no se escribe.
 */
@Serializable
data class RegistroRequestDto(
    val nombres: String,
    val apellidos: String,
    val tipoDocumento: String? = null,
    val numeroDocumento: String? = null,
)
