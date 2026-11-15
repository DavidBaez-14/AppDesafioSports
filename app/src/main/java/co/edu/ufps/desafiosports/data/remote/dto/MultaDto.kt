package co.edu.ufps.desafiosports.data.remote.dto

import kotlinx.serialization.Serializable

/** Un elemento de `GET /api/finanzas/multas/mias`. La respuesta trae más campos; se usan estos. */
@Serializable
data class MultaDto(
    val id: String? = null,
    val tipoSancion: String? = null,
    val monto: Double? = null,
    val estado: String? = null,
    val fechaGeneracion: String? = null,
)
