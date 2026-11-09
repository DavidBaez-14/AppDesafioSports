package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.repository.SesionRepository

/**
 * Inicia sesión con correo y contraseña.
 *
 * Valida antes de llamar a Supabase, como el LoginUseCase del profesor, pero sin
 * android.util.Patterns: el dominio es Kotlin puro y se prueba en la JVM.
 */
class IniciarSesionUseCase(private val sesion: SesionRepository) {

    suspend operator fun invoke(correo: String, clave: String): Result<Unit> {
        val correoLimpio = correo.trim()
        return when {
            !esCorreoValido(correoLimpio) ->
                Result.failure(AppError.Validacion("Escribe un correo válido.", CampoValidado.CORREO))

            clave.length < LONGITUD_MINIMA_CLAVE ->
                Result.failure(
                    AppError.Validacion("La contraseña debe tener al menos $LONGITUD_MINIMA_CLAVE caracteres.", CampoValidado.CLAVE)
                )

            else -> sesion.iniciarSesion(correoLimpio, clave)
        }
    }

    companion object {
        const val LONGITUD_MINIMA_CLAVE = 8
        private val PATRON_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun esCorreoValido(correo: String): Boolean = PATRON_CORREO.matches(correo)
    }
}
