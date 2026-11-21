package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.model.Programacion
import co.edu.ufps.desafiosports.domain.model.BorradorProgramacion
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.InvitacionDeTorneo
import co.edu.ufps.desafiosports.domain.model.PartidosDeTorneo
import co.edu.ufps.desafiosports.domain.model.PersonalDeArbitraje
import co.edu.ufps.desafiosports.domain.model.correoDeInvitacionValido
import co.edu.ufps.desafiosports.domain.repository.GestionTorneoRepository

// Casos de uso de la gestión de un torneo (MOV-0011): árbitros y partidos.

/** Los árbitros (por nombre) y las invitaciones pendientes (la más reciente arriba). */
class ObtenerPersonalDeArbitrajeUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String): Result<PersonalDeArbitraje> {
        val arbitros = repositorio.arbitros(torneoId).getOrElse { return Result.failure(it) }
        val invitaciones = repositorio.invitaciones(torneoId).getOrElse { return Result.failure(it) }
        return Result.success(
            PersonalDeArbitraje(
                arbitros = arbitros.sortedBy { it.nombre.lowercase() },
                invitaciones = invitaciones.sortedByDescending { it.fecha },
            ),
        )
    }
}

/** Invita a un árbitro. El correo es opcional; uno mal escrito no viaja. */
class InvitarArbitroUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String, correo: String): Result<InvitacionDeTorneo> {
        if (!correoDeInvitacionValido(correo)) {
            return Result.failure(AppError.Validacion(MENSAJE_CORREO))
        }
        return repositorio.invitarArbitro(torneoId, correo.trim().lowercase().ifEmpty { null })
    }

    companion object {
        const val MENSAJE_CORREO = "Revisa el correo, o déjalo vacío para invitar con el enlace."
    }
}

class CancelarInvitacionUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String, invitacionId: String): Result<Unit> =
        repositorio.cancelarInvitacion(torneoId, invitacionId)
}

class QuitarArbitroUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String, rolId: String): Result<Unit> = repositorio.quitarArbitro(torneoId, rolId)
}

class CambiarTodosLosPartidosUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String, cuentaId: String, valor: Boolean): Result<Unit> =
        repositorio.cambiarTodosLosPartidos(torneoId, cuentaId, valor)
}

/** El estado del torneo y sus partidos. Sin partidos generados no se pide la lista. */
class ObtenerPartidosDeTorneoUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String): Result<PartidosDeTorneo> {
        val estado = repositorio.estadoDePartidos(torneoId).getOrElse { return Result.failure(it) }
        if (!estado.generados) return Result.success(PartidosDeTorneo(estado, emptyList()))
        return repositorio.partidos(torneoId).map { PartidosDeTorneo(estado, it) }
    }
}

/**
 * Genera los partidos según el formato del torneo. El backend exige el torneo en curso: con las
 * inscripciones abiertas, primero lo inicia. Vuelve a leer el estado antes de actuar, para no
 * iniciar dos veces un torneo que otra persona ya inició.
 */
class GenerarPartidosUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(torneoId: String): Result<Unit> {
        val estado = repositorio.estadoDePartidos(torneoId).getOrElse { return Result.failure(it) }
        if (estado.generados) return Result.success(Unit)
        if (!estado.puedeGenerar) {
            return Result.failure(AppError.Validacion(MENSAJE_NO_SE_PUEDE))
        }
        if (estado.estadoTorneo == EstadoTorneo.PUBLICADO) {
            repositorio.iniciarTorneo(torneoId).onFailure { return Result.failure(it) }
        }
        return repositorio.generarPartidos(torneoId)
    }

    companion object {
        const val MENSAJE_NO_SE_PUEDE = "Este torneo todavía no puede generar sus partidos."
    }
}

class AsignarArbitroUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>> =
        repositorio.asignarArbitro(partidoId, cuentaId)
}

class QuitarArbitroDePartidoUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>> =
        repositorio.quitarArbitroDePartido(partidoId, cuentaId)
}

/**
 * Programa un partido (MOV-0013): el día y la hora van juntos, y el lugar es opcional. Lo que no
 * cumple no viaja.
 */
class ProgramarPartidoUseCase(private val repositorio: GestionTorneoRepository) {
    suspend operator fun invoke(partidoId: String, borrador: BorradorProgramacion): Result<Programacion> {
        if ((borrador.fecha == null) != (borrador.hora == null)) {
            return Result.failure(AppError.Validacion(MENSAJE_FECHA_INCOMPLETA))
        }
        val lugar = borrador.lugar.trim()
        if (lugar.length > BorradorProgramacion.LUGAR_MAXIMO) {
            return Result.failure(AppError.Validacion(MENSAJE_LUGAR))
        }
        return repositorio.programarPartido(partidoId, borrador.fechaHora, lugar)
    }

    companion object {
        const val MENSAJE_FECHA_INCOMPLETA = "Elige el día y también la hora."
        const val MENSAJE_LUGAR = "El lugar puede tener hasta 120 caracteres."
    }
}

/**
 * Borra los partidos y los vuelve a generar con los equipos aprobados de hoy (MOV-0013). Si se
 * borran pero no se pueden generar, el torneo queda sin partidos y basta tocar "Generar partidos".
 */
class VolverAGenerarPartidosUseCase(
    private val repositorio: GestionTorneoRepository,
    private val generarPartidosUseCase: GenerarPartidosUseCase = GenerarPartidosUseCase(repositorio),
) {
    suspend operator fun invoke(torneoId: String): Result<Unit> {
        repositorio.borrarPartidos(torneoId).onFailure { return Result.failure(it) }
        return generarPartidosUseCase(torneoId)
    }
}
