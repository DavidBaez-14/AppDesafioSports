package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.Pagina
import co.edu.ufps.desafiosports.domain.model.TorneoDetalle
import co.edu.ufps.desafiosports.domain.model.TorneoResumen
import co.edu.ufps.desafiosports.domain.model.tokenDeEnlace
import co.edu.ufps.desafiosports.domain.repository.TorneosRepository

// Casos de uso de Explorar y del detalle del torneo (MOV-0005).

class ExplorarTorneosUseCase(private val repositorio: TorneosRepository) {
    suspend operator fun invoke(deporte: String?, busqueda: String?, pagina: Int = 0): Result<Pagina<TorneoResumen>> =
        repositorio.explorar(deporte, busqueda, pagina)
}

class ObtenerTorneoUseCase(private val repositorio: TorneosRepository) {
    suspend operator fun invoke(torneoId: String): Result<TorneoDetalle> = repositorio.torneo(torneoId)
}

/**
 * Abre un torneo con su enlace (MOV-0005 · CA-3): acepta el enlace completo o el token suelto.
 * Lo que no parece un enlace de torneo no viaja al backend.
 */
class AbrirTorneoPorEnlaceUseCase(private val repositorio: TorneosRepository) {
    suspend operator fun invoke(texto: String): Result<Pair<String, TorneoDetalle>> {
        val token = tokenDeEnlace(texto)
            ?: return Result.failure(AppError.Validacion(MENSAJE_ENLACE_INVALIDO))
        return repositorio.torneoPorEnlace(token).map { token to it }
    }

    companion object {
        const val MENSAJE_ENLACE_INVALIDO = "Ese enlace no es de un torneo. Pega el enlace completo que te compartieron."
    }
}
