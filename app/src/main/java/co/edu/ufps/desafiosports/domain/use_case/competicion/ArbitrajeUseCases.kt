package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.PartidosDeArbitro
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.domain.model.repartir
import co.edu.ufps.desafiosports.domain.repository.ArbitrajeRepository
import java.time.LocalDate

// Casos de uso de la sección Árbitro y la planilla (MOV-0009). Van juntos porque son una sola
// pantalla de trabajo y cada uno es un paso directo al repositorio.

/** Los partidos que arbitra quien inició sesión, repartidos en Hoy, Próximos y Jugados. */
class ObtenerPartidosDeArbitroUseCase(
    private val repositorio: ArbitrajeRepository,
    private val hoy: () -> LocalDate = { LocalDate.now() },
) {
    suspend operator fun invoke(): Result<PartidosDeArbitro> =
        repositorio.misPartidos().map { it.repartir(hoy()) }
}

class ObtenerPlanillaUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String): Result<Planilla> = repositorio.planilla(partidoId)
}

/** Anota un evento al final de la lista. Si el partido estaba programado, el backend lo pone en curso. */
class RegistrarEventoUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String, personaId: String, equipoTorneoId: String, tipo: String): Result<EventoPartido> =
        repositorio.registrarEvento(partidoId, personaId, equipoTorneoId, tipo)
}

class QuitarEventoUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String, eventoId: String): Result<Unit> =
        repositorio.quitarEvento(partidoId, eventoId)
}

/** Marca que un jugador juega el partido. El backend rechaza a quien no es apto. */
class AlinearJugadorUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String, personaId: String, equipoTorneoId: String): Result<Unit> =
        repositorio.alinear(partidoId, personaId, equipoTorneoId)
}

class QuitarDeAlineacionUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String, personaId: String): Result<Unit> =
        repositorio.quitarDeAlineacion(partidoId, personaId)
}

/** Alinea de una vez a todo el plantel de un equipo. */
class AlinearEquipoUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String, equipoTorneoId: String): Result<Unit> =
        repositorio.alinearEquipo(partidoId, equipoTorneoId)
}

/** Guarda el resultado: el backend actualiza la tabla y genera las multas de las tarjetas. */
class CerrarPartidoUseCase(private val repositorio: ArbitrajeRepository) {
    suspend operator fun invoke(partidoId: String): Result<Unit> = repositorio.cerrarPartido(partidoId)
}
