package co.edu.ufps.desafiosports.domain.model

/** Tipos de documento que acepta el backend (Colombia). TI = tarjeta de identidad (menores de edad). */
enum class TipoDocumento { CC, TI, CE, PPT, PASAPORTE }

/**
 * Lo que pide `POST /api/auth/registro` para completar el registro (MOV-0003).
 * El documento es opcional, pero va completo (tipo y número) o no va (backend 0023).
 */
data class DatosRegistro(
    val nombres: String,
    val apellidos: String,
    val tipoDocumento: TipoDocumento? = null,
    val numeroDocumento: String? = null,
)
