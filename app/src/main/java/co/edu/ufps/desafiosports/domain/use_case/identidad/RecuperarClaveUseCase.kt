package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.repository.SesionRepository

/**
 * Pide el correo para crear una contraseña nueva (MOV-0002 · N-3).
 *
 * Supabase responde igual exista o no la cuenta (no revela qué correos están registrados),
 * así que la pantalla siempre dice "si el correo tiene una cuenta, te llegará un enlace".
 */
class RecuperarClaveUseCase(private val sesion: SesionRepository) {

    suspend operator fun invoke(correo: String): Result<Unit> {
        val correoLimpio = correo.trim()
        if (!IniciarSesionUseCase.esCorreoValido(correoLimpio)) {
            return Result.failure(AppError.Validacion("Escribe un correo válido.", CampoValidado.CORREO))
        }
        return sesion.recuperarClave(correoLimpio)
    }
}
