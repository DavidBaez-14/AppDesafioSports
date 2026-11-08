package co.edu.ufps.desafiosports.data.remote.red

import co.edu.ufps.desafiosports.data.remote.dto.ProblemDetailDto
import co.edu.ufps.desafiosports.domain.error.AppError
import kotlinx.serialization.json.Json

/**
 * Traduce una respuesta de error del backend a [AppError].
 *
 * Decide por el `code` del ProblemDetail (nunca por el texto) y muestra el `mensaje`
 * que manda el backend, igual que `src/api/http.js` de la web.
 */
class ErroresHttp(private val json: Json) {

    fun desde(status: Int, cuerpo: String?): AppError {
        val problema = leer(cuerpo)
        val mensaje = problema?.mensaje?.takeIf { it.isNotBlank() }
            ?: problema?.detail?.takeIf { it.isNotBlank() }
            ?: mensajePorStatus(status)

        return when (problema?.code) {
            "CUENTA_NO_REGISTRADA" -> AppError.CuentaNoRegistrada(mensaje)
            "CUENTA_BLOQUEADA" -> AppError.CuentaBloqueada(mensaje)
            "NO_AUTENTICADO" -> AppError.NoAutenticado(mensaje)
            "ACCESO_DENEGADO" -> AppError.AccesoDenegado(mensaje)
            "NO_ENCONTRADO" -> AppError.NoEncontrado(mensaje)
            null -> porStatus(status, mensaje)
            else -> AppError.Negocio(problema.code, mensaje)
        }
    }

    /** Solo el `code`, sin construir el error. Lo usa [CuentaBloqueadaInterceptor]. */
    fun codigo(cuerpo: String?): String? = leer(cuerpo)?.code

    private fun leer(cuerpo: String?): ProblemDetailDto? =
        if (cuerpo.isNullOrBlank()) null
        else runCatching { json.decodeFromString<ProblemDetailDto>(cuerpo) }.getOrNull()

    private fun porStatus(status: Int, mensaje: String): AppError = when (status) {
        401 -> AppError.NoAutenticado()
        403 -> AppError.AccesoDenegado(mensaje)
        404 -> AppError.NoEncontrado(mensaje)
        in 500..599 -> AppError.Desconocido()
        else -> AppError.Negocio("HTTP_$status", mensaje)
    }

    private fun mensajePorStatus(status: Int): String = when (status) {
        401 -> "Inicia sesión para continuar."
        403 -> "No tienes permisos para esta acción."
        404 -> "No se encontró lo que buscabas."
        in 500..599 -> "Ocurrió un error inesperado."
        else -> "La solicitud no se pudo completar."
    }
}
