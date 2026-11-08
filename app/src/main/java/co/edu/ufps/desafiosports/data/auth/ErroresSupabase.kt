package co.edu.ufps.desafiosports.data.auth

import co.edu.ufps.desafiosports.domain.error.AppError
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Traduce las excepciones de supabase-kt a [AppError]. */
internal fun Throwable.aAppError(): AppError = when (this) {
    is AppError -> this
    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> AppError.CredencialesInvalidas()
        AuthErrorCode.EmailNotConfirmed -> AppError.CorreoNoConfirmado()
        else -> AppError.Negocio(errorCode?.value ?: error, errorDescription)
    }
    is RestException -> AppError.Negocio(error, description ?: error)
    is HttpRequestException, is IOException -> AppError.SinConexion(this)
    else -> AppError.Desconocido(this)
}

/** Ejecuta una operación de supabase-kt y devuelve [Result] con [AppError]. */
internal suspend fun <T> llamarSupabase(operacion: suspend () -> T): Result<T> =
    try {
        Result.success(operacion())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.aAppError())
    }
