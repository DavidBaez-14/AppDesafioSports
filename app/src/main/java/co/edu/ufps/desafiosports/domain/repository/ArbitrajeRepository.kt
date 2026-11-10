package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.Planilla

/** Sección Árbitro y planilla del partido (MOV-0009). Todo pasa por el backend. */
interface ArbitrajeRepository {
    suspend fun misPartidos(): Result<List<PartidoArbitro>>
    suspend fun planilla(partidoId: String): Result<Planilla>
    suspend fun registrarEvento(partidoId: String, personaId: String, equipoTorneoId: String, tipo: String): Result<EventoPartido>
    suspend fun quitarEvento(partidoId: String, eventoId: String): Result<Unit>
    suspend fun alinear(partidoId: String, personaId: String, equipoTorneoId: String): Result<Unit>
    suspend fun quitarDeAlineacion(partidoId: String, personaId: String): Result<Unit>
    suspend fun alinearEquipo(partidoId: String, equipoTorneoId: String): Result<Unit>
    suspend fun cerrarPartido(partidoId: String): Result<Unit>
}
