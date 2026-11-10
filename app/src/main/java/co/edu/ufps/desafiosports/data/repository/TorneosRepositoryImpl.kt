package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.TorneosApi
import co.edu.ufps.desafiosports.data.remote.dto.MiRelacionDto
import co.edu.ufps.desafiosports.data.remote.dto.PartidoPublicoDto
import co.edu.ufps.desafiosports.data.remote.dto.PosicionDto
import co.edu.ufps.desafiosports.data.remote.dto.RelacionConEquipoDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoDetalleDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoResumenDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.EquipoInscrito
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FilaTabla
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.MiRelacion
import co.edu.ufps.desafiosports.domain.model.Pagina
import co.edu.ufps.desafiosports.domain.model.PartidoDelTorneo
import co.edu.ufps.desafiosports.domain.model.Premio
import co.edu.ufps.desafiosports.domain.model.RelacionConEquipo
import co.edu.ufps.desafiosports.domain.model.TorneoDetalle
import co.edu.ufps.desafiosports.domain.model.TorneoResumen
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.domain.repository.TorneosRepository
import java.math.BigDecimal

class TorneosRepositoryImpl(
    private val api: TorneosApi,
    private val errores: ErroresHttp,
) : TorneosRepository {

    override suspend fun explorar(deporte: String?, busqueda: String?, pagina: Int): Result<Pagina<TorneoResumen>> =
        llamarApi(errores) {
            api.explorar(
                deporte = deporte?.takeIf { it.isNotBlank() },
                busqueda = busqueda?.trim()?.takeIf { it.isNotEmpty() },
                pagina = pagina,
                tamano = TAMANO_PAGINA,
            )
        }.map { p -> Pagina(p.contenido.mapNotNull { it.aDominio() }, p.pagina, p.totalPaginas) }

    override suspend fun torneo(id: String): Result<TorneoDetalle> =
        llamarApi(errores) { api.torneo(id) }.map { it.aDominio() }

    override suspend fun torneoPorEnlace(enlaceToken: String): Result<TorneoDetalle> =
        llamarApi(errores) { api.torneoPorEnlace(enlaceToken) }.map { it.aDominio() }

    companion object {
        const val TAMANO_PAGINA = 20
    }
}

// ── DTO → modelo ────────────────────────────────────────────────────────────

private fun formatoDe(codigo: String?): FormatoTorneo? = FormatoTorneo.entries.firstOrNull { it.name == codigo }

/** Un torneo sin id no se puede abrir: se descarta. */
internal fun TorneoResumenDto.aDominio(): TorneoResumen? {
    val id = id?.takeIf { it.isNotBlank() } ?: return null
    return TorneoResumen(
        id = id,
        nombre = nombre.orEmpty(),
        deporte = deporte.orEmpty(),
        deporteNombre = deporteNombre ?: deporte.orEmpty(),
        formato = formatoDe(formato),
        estado = EstadoTorneo.desde(estado),
        cupos = cupos,
        inscritos = inscritos ?: 0,
        cierreInscripciones = cierreInscripciones.aFecha(),
        inicio = inicio.aFecha(),
        organizador = organizador?.takeIf { it.isNotBlank() },
        inscripcionesAbiertas = inscripcionesAbiertas ?: false,
    )
}

internal fun TorneoDetalleDto.aDominio(): TorneoDetalle = TorneoDetalle(
    id = id.orEmpty(),
    nombre = nombre.orEmpty(),
    deporte = deporte?.aDominio(),
    formato = formatoDe(formato),
    estado = EstadoTorneo.desde(estado),
    visibilidad = Visibilidad.desde(visibilidad),
    fechaInicio = fechaInicio.aFecha(),
    fechaFin = fechaFin.aFecha(),
    cierreInscripciones = cierreInscripciones.aFecha(),
    cupos = cupos,
    inscritos = inscritos ?: 0,
    inscripcionesAbiertas = inscripcionesAbiertas ?: false,
    cobraInscripcion = cobraInscripcion ?: false,
    valorInscripcion = valorInscripcion?.let { BigDecimal.valueOf(it) },
    exigeDocumento = exigeDocumento ?: false,
    reglas = reglas?.takeIf { it.isNotBlank() },
    organizador = organizador?.takeIf { it.isNotBlank() },
    enlace = enlace?.takeIf { it.isNotBlank() },
    equipos = equipos.orEmpty().mapNotNull { e ->
        val id = e.equipoTorneoId ?: return@mapNotNull null
        EquipoInscrito(id, e.nombre.orEmpty(), e.miembros ?: 0)
    },
    tabla = tabla.orEmpty()
        .mapValues { (_, filas) -> filas.mapNotNull { it.aDominio() }.sortedBy { it.posicion } }
        .filterValues { it.isNotEmpty() },
    partidos = partidos.orEmpty().mapNotNull { it.aDominio() },
    premios = premios.orEmpty().map { p ->
        Premio(
            nombre = p.titulo?.takeIf { it.isNotBlank() } ?: p.nombre.orEmpty(),
            ganador = p.ganadorNombre?.takeIf { it.isNotBlank() } ?: p.ganadorEquipo?.takeIf { it.isNotBlank() },
        )
    },
    miRelacion = miRelacion?.aDominio(),
)

private fun PosicionDto.aDominio(): FilaTabla? {
    val id = equipoTorneoId ?: return null
    return FilaTabla(
        posicion = posicion ?: 0,
        equipoTorneoId = id,
        equipo = equipoNombre.orEmpty(),
        puntos = pts ?: 0,
        jugados = pj ?: 0,
        ganados = pg ?: 0,
        empatados = pe ?: 0,
        perdidos = pp ?: 0,
        diferencia = dg ?: 0,
    )
}

private fun PartidoPublicoDto.aDominio(): PartidoDelTorneo? {
    val id = partidoId ?: return null
    val m = marcador
    return PartidoDelTorneo(
        id = id,
        fase = fase?.takeIf { it.isNotBlank() } ?: jornada?.let { "Fecha $it" }.orEmpty(),
        local = local?.nombre?.takeIf { it.isNotBlank() },
        visitante = visitante?.nombre?.takeIf { it.isNotBlank() },
        fechaHora = fechaHora.aFechaHora(),
        lugar = lugar?.takeIf { it.isNotBlank() },
        estado = EstadoPartido.desde(estado),
        marcador = if (m?.local != null && m.visitante != null) Marcador(m.local, m.visitante) else null,
    )
}

private fun MiRelacionDto.aDominio(): MiRelacion = MiRelacion(
    administra = administra,
    arbitra = arbitra,
    delegadoDe = delegadoDe?.aDominio(),
    juegaEn = juegaEn?.aDominio(),
)

private fun RelacionConEquipoDto.aDominio(): RelacionConEquipo? {
    val id = equipoTorneoId ?: return null
    return RelacionConEquipo(id, equipo.orEmpty(), EstadoInscripcion.desde(estadoInscripcion), comprobante)
}
