package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.BorradorTorneo
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.InscripcionesDeTorneo
import co.edu.ufps.desafiosports.domain.model.TorneoCreado
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.model.repartir
import co.edu.ufps.desafiosports.domain.repository.CreadorRepository
import java.time.LocalDate

// Casos de uso de "Crear torneo" (MOV-0007) y de la sección Creador (MOV-0008).

class ObtenerDeportesUseCase(private val repositorio: CreadorRepository) {
    suspend operator fun invoke(): Result<List<Deporte>> = repositorio.deportes()
}

/**
 * Crea el torneo. Valida antes de llamar al backend: un borrador incompleto no viaja.
 * El torneo nace con inscripciones abiertas y quien lo crea queda como su administrador.
 */
class CrearTorneoUseCase(
    private val repositorio: CreadorRepository,
    private val hoy: () -> LocalDate = { LocalDate.now() },
) {
    suspend operator fun invoke(borrador: BorradorTorneo): Result<TorneoCreado> {
        val torneo = borrador.aNuevoTorneo(hoy())
            ?: return Result.failure(AppError.Validacion("Completa los tres pasos antes de crear el torneo."))
        return repositorio.crearTorneo(torneo)
    }
}

/** Los torneos que administra la persona: primero los que tienen algo por revisar. */
class ObtenerMisTorneosUseCase(private val repositorio: CreadorRepository) {
    suspend operator fun invoke(): Result<List<TorneoDeCreador>> =
        repositorio.misTorneos().map { torneos -> torneos.sortedByDescending { it.porRevisar > 0 } }
}

class ObtenerInscripcionesUseCase(private val repositorio: CreadorRepository) {
    suspend operator fun invoke(torneoId: String): Result<InscripcionesDeTorneo> =
        repositorio.inscripciones(torneoId).map { it.repartir() }
}

class AprobarInscripcionUseCase(private val repositorio: CreadorRepository) {
    suspend operator fun invoke(torneoId: String, inscripcionId: String): Result<Unit> =
        repositorio.aprobarInscripcion(torneoId, inscripcionId)
}

/** Rechazar exige un motivo: es lo que ve quien se inscribió. */
class RechazarInscripcionUseCase(private val repositorio: CreadorRepository) {
    suspend operator fun invoke(torneoId: String, inscripcionId: String, motivo: String): Result<Unit> {
        val limpio = motivo.trim()
        if (limpio.length < MOTIVO_MINIMO) {
            return Result.failure(AppError.Validacion("Escribe el motivo del rechazo: lo verá quien se inscribió."))
        }
        return repositorio.rechazarInscripcion(torneoId, inscripcionId, limpio)
    }

    companion object {
        const val MOTIVO_MINIMO = 3
    }
}
