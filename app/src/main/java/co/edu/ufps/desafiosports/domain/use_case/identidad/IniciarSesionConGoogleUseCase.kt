package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.repository.SesionRepository

/** Inicia sesión en Supabase con el ID token de Google (MOV-0006). [nonce] va sin hash. */
class IniciarSesionConGoogleUseCase(private val sesion: SesionRepository) {

    suspend operator fun invoke(idToken: String, nonce: String): Result<Unit> {
        if (idToken.isBlank() || nonce.isBlank()) {
            return Result.failure(AppError.Validacion("No se recibió la cuenta de Google. Inténtalo de nuevo."))
        }
        return sesion.iniciarSesionConGoogle(idToken, nonce)
    }
}
