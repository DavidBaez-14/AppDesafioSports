package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/** Tal cual `MeResponseDTO` de BackendCodeCup (identidad). No sale de la capa data. */
@Serializable
data class MeResponseDto(
    val cuentaId: String? = null,
    val personaId: String? = null,
    val nombres: String? = null,
    val apellidos: String? = null,
    val nombre: String? = null,
    val correo: String? = null,
    val tipoDocumento: String? = null,
    val numeroDocumento: String? = null,
    val estado: String? = null,
    val roles: List<String> = emptyList(),
    val rolesPendientes: List<String> = emptyList(),
)
