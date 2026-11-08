package co.edu.ufps.desafiosports.data.remote.red

import co.edu.ufps.desafiosports.domain.error.AppError
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Ejecuta una llamada de Retrofit y la convierte en [Result] con un [AppError] tipado.
 * Todos los repositorios que hablan con el backend pasan por aquí.
 */
suspend fun <T> llamarApi(errores: ErroresHttp, llamada: suspend () -> T): Result<T> =
    try {
        Result.success(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(errores.desde(e.code(), e.response()?.errorBody()?.string()))
    } catch (e: IOException) {
        Result.failure(AppError.SinConexion(e))
    } catch (e: SerializationException) {
        Result.failure(AppError.Desconocido(e))
    }
