package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/** Respuesta de Spring Boot Actuator en `/actuator/health`. */
@Serializable
data class HealthDto(val status: String = "DESCONOCIDO")
