package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.model.ResultadoRegistro
import co.edu.ufps.desafiosports.domain.repository.SesionRepository

/**
 * Crea la cuenta en Supabase con correo y contraseña (MOV-0003 · A-3).
 * Mismas reglas que el login: correo válido y contraseña de al menos 8 caracteres.
 */
class RegistrarseUseCase(private val sesion: SesionRepository) {

    suspend operator fun invoke(correo: String, clave: String): Result<ResultadoRegistro> {
        val correoLimpio = correo.trim()
        return when {
            !IniciarSesionUseCase.esCorreoValido(correoLimpio) ->
                Result.failure(AppError.Validacion("Escribe un correo válido.", CampoValidado.CORREO))

            clave.length < IniciarSesionUseCase.LONGITUD_MINIMA_CLAVE ->
                Result.failure(
                    AppError.Validacion(
                        "La contraseña debe tener al menos ${IniciarSesionUseCase.LONGITUD_MINIMA_CLAVE} caracteres.",
                        CampoValidado.CLAVE,
                    )
                )

            else -> sesion.registrarse(correoLimpio, clave)
        }
    }
}
