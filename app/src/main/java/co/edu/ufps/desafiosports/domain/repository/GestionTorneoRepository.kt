package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.EstadoDePartidos
import co.edu.ufps.desafiosports.domain.model.InvitacionDeTorneo
import co.edu.ufps.desafiosports.domain.model.PartidoDeTorneo

/** Árbitros y partidos de un torneo que la persona administra (MOV-0011). */
interface GestionTorneoRepository {
    /** Solo los árbitros: los administradores del torneo no entran aquí. */
    suspend fun arbitros(torneoId: String): Result<List<ArbitroDeTorneo>>
    suspend fun quitarArbitro(torneoId: String, rolId: String): Result<Unit>
    suspend fun cambiarTodosLosPartidos(torneoId: String, cuentaId: String, valor: Boolean): Result<Unit>

    /** Solo las invitaciones de árbitro pendientes. */
    suspend fun invitaciones(torneoId: String): Result<List<InvitacionDeTorneo>>

    /** [correo] null = por enlace. */
    suspend fun invitarArbitro(torneoId: String, correo: String?): Result<InvitacionDeTorneo>
    suspend fun cancelarInvitacion(torneoId: String, invitacionId: String): Result<Unit>

    suspend fun estadoDePartidos(torneoId: String): Result<EstadoDePartidos>
    suspend fun partidos(torneoId: String): Result<List<PartidoDeTorneo>>
    suspend fun iniciarTorneo(torneoId: String): Result<Unit>
    suspend fun generarPartidos(torneoId: String): Result<Unit>

    /** Responden los árbitros con que queda el partido. */
    suspend fun asignarArbitro(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>>
    suspend fun quitarArbitroDePartido(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>>
}
