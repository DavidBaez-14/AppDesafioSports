package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha
import co.edu.ufps.desafiosports.domain.repository.DelegadoRepository
import kotlinx.coroutines.CompletableDeferred

/** Repositorio falso de quien inscribe un equipo (MOV-0014). */
class FakeDelegadoRepository : DelegadoRepository {
    val llamadas = mutableListOf<String>()

    var equipos: Result<List<EquipoPropio>> = Result.success(emptyList())

    /** Null = responde la inscripción pendiente del equipo enviado. */
    var resultadoInscribir: Result<InscripcionHecha>? = null

    /** Si no es null, inscribir espera a que se complete (doble toque). */
    var bloqueo: CompletableDeferred<Unit>? = null

    override suspend fun misEquipos(): Result<List<EquipoPropio>> {
        llamadas += "equipos"
        return equipos
    }

    override suspend fun inscribir(
        torneoId: String,
        equipoId: String?,
        nombreEquipo: String?,
        enlaceToken: String?,
    ): Result<InscripcionHecha> {
        llamadas += "inscribir:$torneoId:$equipoId:$nombreEquipo:$enlaceToken"
        bloqueo?.await()
        return resultadoInscribir
            ?: Result.success(InscripcionHecha("et-nuevo", nombreEquipo ?: "Equipo $equipoId", EstadoInscripcion.PENDIENTE_PAGO))
    }
}
