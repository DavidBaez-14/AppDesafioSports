package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.ArbitrajeApi
import co.edu.ufps.desafiosports.data.remote.dto.AlineacionRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.AlinearEquipoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.ArbitroPartidoDto
import co.edu.ufps.desafiosports.data.remote.dto.EquipoEnPartidoDto
import co.edu.ufps.desafiosports.data.remote.dto.EquipoPlanillaDto
import co.edu.ufps.desafiosports.data.remote.dto.EventoDto
import co.edu.ufps.desafiosports.data.remote.dto.EventoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.PlanillaDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.EquipoEnPartido
import co.edu.ufps.desafiosports.domain.model.EquipoPlanilla
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.JugadorPlanilla
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.domain.repository.ArbitrajeRepository

class ArbitrajeRepositoryImpl(
    private val api: ArbitrajeApi,
    private val errores: ErroresHttp,
) : ArbitrajeRepository {

    override suspend fun misPartidos(): Result<List<PartidoArbitro>> =
        llamarApi(errores) { api.misPartidos() }.map { lista -> lista.map { it.aDominio() } }

    override suspend fun planilla(partidoId: String): Result<Planilla> =
        llamarApi(errores) { api.planilla(partidoId) }.map { it.aDominio() }

    override suspend fun registrarEvento(partidoId: String, personaId: String, equipoTorneoId: String, tipo: String): Result<EventoPartido> =
        llamarApi(errores) { api.registrarEvento(partidoId, EventoRequestDto(personaId, equipoTorneoId, tipo)) }
            .map { it.aDominio() }

    override suspend fun quitarEvento(partidoId: String, eventoId: String): Result<Unit> =
        llamarApi(errores) { api.quitarEvento(partidoId, eventoId) }

    override suspend fun alinear(partidoId: String, personaId: String, equipoTorneoId: String): Result<Unit> =
        llamarApi(errores) { api.alinear(partidoId, AlineacionRequestDto(personaId, equipoTorneoId)) }

    override suspend fun quitarDeAlineacion(partidoId: String, personaId: String): Result<Unit> =
        llamarApi(errores) { api.quitarDeAlineacion(partidoId, personaId) }

    override suspend fun alinearEquipo(partidoId: String, equipoTorneoId: String): Result<Unit> =
        llamarApi(errores) { api.alinearEquipo(partidoId, AlinearEquipoRequestDto(equipoTorneoId)) }

    override suspend fun cerrarPartido(partidoId: String): Result<Unit> =
        llamarApi(errores) { api.cerrarPartido(partidoId) }
}

// ── DTO → modelo ────────────────────────────────────────────────────────────

internal fun ArbitroPartidoDto.aDominio(): PartidoArbitro = PartidoArbitro(
    id = partidoId.orEmpty(),
    torneoId = torneoId.orEmpty(),
    torneo = torneo.orEmpty(),
    deporte = deporte.orEmpty(),
    fase = fase.orEmpty(),
    local = local.aDominio(),
    visitante = visitante.aDominio(),
    fechaHora = fechaHora.aFechaHora(),
    lugar = lugar?.takeIf { it.isNotBlank() },
    estado = EstadoPartido.desde(estado),
    marcador = marcador?.let { Marcador(it.local ?: 0, it.visitante ?: 0) },
    asignado = asignado ?: false,
)

/** Un equipo sin id es un cruce aún sin definir. */
private fun EquipoEnPartidoDto?.aDominio(): EquipoEnPartido? {
    val id = this?.equipoTorneoId?.takeIf { it.isNotBlank() } ?: return null
    return EquipoEnPartido(id, this.nombre.orEmpty())
}

internal fun PlanillaDto.aDominio(): Planilla = Planilla(
    partido = (partido ?: ArbitroPartidoDto()).aDominio(),
    tiposEvento = tiposEvento.orEmpty().filter { it.isNotBlank() },
    local = local.aDominio(),
    visitante = visitante.aDominio(),
    eventos = eventos.orEmpty().map { it.aDominio() }.sortedBy { it.orden },
    editable = editable ?: false,
)

private fun EquipoPlanillaDto?.aDominio(): EquipoPlanilla = EquipoPlanilla(
    equipoTorneoId = this?.equipoTorneoId.orEmpty(),
    nombre = this?.nombre.orEmpty(),
    jugadores = this?.jugadores.orEmpty().map { j ->
        JugadorPlanilla(
            personaId = j.personaId.orEmpty(),
            nombre = j.nombre.orEmpty(),
            numeroCamiseta = j.numeroCamiseta,
            numeroDocumento = j.numeroDocumento?.takeIf { it.isNotBlank() },
            esDelegado = j.esDelegado ?: false,
            alineado = j.alineado ?: false,
            goles = j.goles ?: 0,
            // Sin dato, se le deja jugar: el backend vuelve a comprobarlo al alinear.
            apto = j.apto ?: true,
            motivos = j.motivos.orEmpty(),
        )
    },
)

internal fun EventoDto.aDominio(): EventoPartido = EventoPartido(
    id = id.orEmpty(),
    orden = orden ?: 0,
    personaId = personaId.orEmpty(),
    jugadorNombre = jugadorNombre.orEmpty(),
    equipoTorneoId = equipoTorneoId.orEmpty(),
    equipoNombre = equipoNombre.orEmpty(),
    tipo = tipoEvento.orEmpty(),
)
