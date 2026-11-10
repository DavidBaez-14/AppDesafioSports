package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.Inscripcion
import co.edu.ufps.desafiosports.domain.model.NuevoTorneo
import co.edu.ufps.desafiosports.domain.model.TorneoCreado
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador

/** Crear torneo y sección Creador (MOV-0007 y MOV-0008). Cada persona ve solo lo que administra. */
interface CreadorRepository {
    /** Catálogo de deportes. Es público: no necesita sesión. */
    suspend fun deportes(): Result<List<Deporte>>
    suspend fun crearTorneo(torneo: NuevoTorneo): Result<TorneoCreado>
    suspend fun misTorneos(): Result<List<TorneoDeCreador>>
    suspend fun inscripciones(torneoId: String): Result<List<Inscripcion>>
    suspend fun aprobarInscripcion(torneoId: String, inscripcionId: String): Result<Unit>
    suspend fun rechazarInscripcion(torneoId: String, inscripcionId: String, motivo: String): Result<Unit>
}
