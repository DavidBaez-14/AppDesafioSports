package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.CreadorApi
import co.edu.ufps.desafiosports.data.remote.dto.CreadorTorneoDto
import co.edu.ufps.desafiosports.data.remote.dto.CrearTorneoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.DeporteDto
import co.edu.ufps.desafiosports.data.remote.dto.InscripcionDto
import co.edu.ufps.desafiosports.data.remote.dto.RechazoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoCreadoDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.Inscripcion
import co.edu.ufps.desafiosports.domain.model.NuevoTorneo
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.TorneoCreado
import co.edu.ufps.desafiosports.domain.model.TorneoDeCreador
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.domain.repository.CreadorRepository
import java.math.BigDecimal

class CreadorRepositoryImpl(
    private val api: CreadorApi,
    private val errores: ErroresHttp,
) : CreadorRepository {

    override suspend fun deportes(): Result<List<Deporte>> =
        llamarApi(errores) { api.deportes() }.map { lista -> lista.mapNotNull { it.aDominio() } }

    override suspend fun crearTorneo(torneo: NuevoTorneo): Result<TorneoCreado> =
        llamarApi(errores) {
            api.crearTorneo(
                CrearTorneoRequestDto(
                    nombre = torneo.nombre,
                    deporte = torneo.deporte,
                    formato = torneo.formato.name,
                    cupos = torneo.cupos,
                    fechaInicio = torneo.fechaInicio.toString(),
                    visibilidad = torneo.visibilidad.name,
                    cobraInscripcion = torneo.cobraInscripcion,
                    valorInscripcion = torneo.valorInscripcion,
                ),
            )
        }.map { it.aDominio() }

    override suspend fun misTorneos(): Result<List<TorneoDeCreador>> =
        llamarApi(errores) { api.misTorneos() }.map { lista -> lista.map { it.aDominio() } }

    override suspend fun inscripciones(torneoId: String): Result<List<Inscripcion>> =
        llamarApi(errores) { api.inscripciones(torneoId) }.map { lista -> lista.map { it.aDominio() } }

    override suspend fun aprobarInscripcion(torneoId: String, inscripcionId: String): Result<Unit> =
        llamarApi(errores) { api.aprobarInscripcion(torneoId, inscripcionId) }

    override suspend fun rechazarInscripcion(torneoId: String, inscripcionId: String, motivo: String): Result<Unit> =
        llamarApi(errores) { api.rechazarInscripcion(torneoId, inscripcionId, RechazoRequestDto(motivo)) }
}

// ── DTO → modelo ────────────────────────────────────────────────────────────

/** Un deporte sin código no sirve para crear un torneo: se descarta. */
internal fun DeporteDto.aDominio(): Deporte? {
    val codigo = codigo?.takeIf { it.isNotBlank() } ?: return null
    return Deporte(
        codigo = codigo,
        nombre = nombre?.takeIf { it.isNotBlank() } ?: codigo,
        tipo = if (tipoCompetencia == "INDIVIDUAL") TipoCompetencia.INDIVIDUAL else TipoCompetencia.EQUIPO,
    )
}

internal fun TorneoCreadoDto.aDominio(): TorneoCreado = TorneoCreado(
    id = id.orEmpty(),
    nombre = nombre.orEmpty(),
    deporte = deporte.orEmpty(),
    visibilidad = Visibilidad.desde(visibilidad),
    enlace = enlace.orEmpty(),
)

internal fun CreadorTorneoDto.aDominio(): TorneoDeCreador = TorneoDeCreador(
    id = id.orEmpty(),
    nombre = nombre.orEmpty(),
    estado = EstadoTorneo.desde(estado),
    deporte = deporte.orEmpty(),
    visibilidad = Visibilidad.desde(visibilidad),
    cupos = cupos,
    inscritos = inscritos ?: 0,
    enlace = enlace?.takeIf { it.isNotBlank() },
    inscripcionesPendientes = pendientes?.inscripciones ?: 0,
    comprobantesPendientes = pendientes?.comprobantes ?: 0,
)

internal fun InscripcionDto.aDominio(): Inscripcion = Inscripcion(
    id = id.orEmpty(),
    equipo = equipoNombre.orEmpty(),
    delegado = delegadoNombre?.takeIf { it.isNotBlank() },
    estado = EstadoInscripcion.desde(estadoInscripcion),
    motivoRechazo = motivoRechazo?.takeIf { it.isNotBlank() },
    fecha = fechaInscripcion.aFechaHora(),
    monto = montoInscripcion?.let { BigDecimal.valueOf(it) },
)
