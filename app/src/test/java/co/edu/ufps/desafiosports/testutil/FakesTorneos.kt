package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.domain.model.Deporte
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FormatoTorneo
import co.edu.ufps.desafiosports.domain.model.MiRelacion
import co.edu.ufps.desafiosports.domain.model.Pagina
import co.edu.ufps.desafiosports.domain.model.TipoCompetencia
import co.edu.ufps.desafiosports.domain.model.TorneoDetalle
import co.edu.ufps.desafiosports.domain.model.TorneoResumen
import co.edu.ufps.desafiosports.domain.model.Visibilidad
import co.edu.ufps.desafiosports.domain.repository.TorneosRepository
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Repositorio falso de Explorar y del detalle (MOV-0005). Guarda cada llamada como texto
 * ("explorar:FUTSAL:copa:0", "torneo:t1", "enlace:abc") y responde lo que se le configure.
 */
class FakeTorneosRepository : TorneosRepository {
    val llamadas = mutableListOf<String>()

    /** Lo que responde explorar, por número de página. */
    var paginas: Map<Int, Result<Pagina<TorneoResumen>>> = mapOf(0 to Result.success(Pagina(listOf(resumenDePrueba("t1")), 0, 1)))
    var detalle: Result<TorneoDetalle> = Result.success(detalleDePrueba())

    override suspend fun explorar(deporte: String?, busqueda: String?, pagina: Int): Result<Pagina<TorneoResumen>> {
        llamadas += "explorar:${deporte.orEmpty()}:${busqueda.orEmpty()}:$pagina"
        return paginas[pagina] ?: Result.success(Pagina(emptyList(), pagina, pagina + 1))
    }

    override suspend fun torneo(id: String): Result<TorneoDetalle> {
        llamadas += "torneo:$id"
        return detalle
    }

    override suspend fun torneoPorEnlace(enlaceToken: String): Result<TorneoDetalle> {
        llamadas += "enlace:$enlaceToken"
        return detalle
    }
}

fun resumenDePrueba(id: String, nombre: String = "Torneo $id") = TorneoResumen(
    id = id,
    nombre = nombre,
    deporte = "FUTSAL",
    deporteNombre = "Fútbol sala",
    formato = FormatoTorneo.LIGA,
    estado = EstadoTorneo.PUBLICADO,
    cupos = 16,
    inscritos = 2,
    cierreInscripciones = null,
    inicio = LocalDate.of(2026, 11, 17),
    organizador = null,
    inscripcionesAbiertas = true,
)

fun detalleDePrueba(
    abiertas: Boolean = true,
    relacion: MiRelacion? = null,
    tipo: TipoCompetencia = TipoCompetencia.EQUIPO,
    estado: EstadoTorneo = EstadoTorneo.PUBLICADO,
) = TorneoDetalle(
    id = "t1",
    nombre = "Copa Prueba 2026",
    deporte = Deporte(if (tipo == TipoCompetencia.EQUIPO) "FUTSAL" else "TENIS_MESA", "Deporte", tipo),
    formato = FormatoTorneo.LIGA,
    estado = estado,
    visibilidad = Visibilidad.PUBLICO,
    fechaInicio = LocalDate.of(2026, 11, 17),
    fechaFin = null,
    cierreInscripciones = null,
    cupos = 16,
    inscritos = 2,
    inscripcionesAbiertas = abiertas,
    cobraInscripcion = true,
    valorInscripcion = BigDecimal("50000"),
    exigeDocumento = false,
    reglas = null,
    organizador = null,
    enlace = "https://desafio.test/t/0e46a7c3324240e2868e5780fb7a6ba5",
    equipos = emptyList(),
    tabla = emptyMap(),
    partidos = emptyList(),
    premios = emptyList(),
    miRelacion = relacion,
)
