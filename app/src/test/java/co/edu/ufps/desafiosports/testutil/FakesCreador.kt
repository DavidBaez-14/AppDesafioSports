package co.edu.ufps.desafiosports.testutil

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
import kotlinx.coroutines.CompletableDeferred
import java.math.BigDecimal
import java.time.LocalDateTime

/** Repositorio falso de "Crear torneo" y la sección Creador (MOV-0007 y MOV-0008). */
class FakeCreadorRepository : CreadorRepository {
    val llamadas = mutableListOf<String>()

    var deportes: Result<List<Deporte>> = Result.success(deportesDePrueba())
    var resultadoCrear: Result<TorneoCreado> =
        Result.success(TorneoCreado("t-nuevo", "Relámpago de Fútbol sala", "FUTSAL", Visibilidad.PUBLICO, "https://desafio.test/t/abc"))
    val torneosCreados = mutableListOf<NuevoTorneo>()

    /** Si no es null, crearTorneo espera a que se complete (doble toque). */
    var bloqueoCrear: CompletableDeferred<Unit>? = null

    var torneos: Result<List<TorneoDeCreador>> = Result.success(emptyList())
    var inscripciones: Result<List<Inscripcion>> = Result.success(emptyList())
    var resultadoAccion: Result<Unit> = Result.success(Unit)

    /** Se ejecuta antes de responder aprobar o rechazar: sirve para dejar lista la lista siguiente. */
    var alActuar: (() -> Unit)? = null

    /** Si no es null, aprobar espera a que se complete (doble toque). */
    var bloqueoAccion: CompletableDeferred<Unit>? = null

    override suspend fun deportes(): Result<List<Deporte>> {
        llamadas += "deportes"
        return deportes
    }

    override suspend fun crearTorneo(torneo: NuevoTorneo): Result<TorneoCreado> {
        llamadas += "crear"
        torneosCreados += torneo
        bloqueoCrear?.await()
        return resultadoCrear
    }

    override suspend fun misTorneos(): Result<List<TorneoDeCreador>> {
        llamadas += "torneos"
        return torneos
    }

    override suspend fun inscripciones(torneoId: String): Result<List<Inscripcion>> {
        llamadas += "inscripciones:$torneoId"
        return inscripciones
    }

    override suspend fun aprobarInscripcion(torneoId: String, inscripcionId: String): Result<Unit> {
        llamadas += "aprobar:$torneoId:$inscripcionId"
        bloqueoAccion?.await()
        alActuar?.invoke()
        return resultadoAccion
    }

    override suspend fun rechazarInscripcion(torneoId: String, inscripcionId: String, motivo: String): Result<Unit> {
        llamadas += "rechazar:$torneoId:$inscripcionId:$motivo"
        alActuar?.invoke()
        return resultadoAccion
    }
}

fun deportesDePrueba() = listOf(
    Deporte("FUTSAL", "Fútbol sala", TipoCompetencia.EQUIPO),
    Deporte("VOLEIBOL", "Voleibol", TipoCompetencia.EQUIPO),
    Deporte("TENIS_MESA", "Tenis de mesa", TipoCompetencia.INDIVIDUAL),
)

fun torneoDeCreadorDePrueba(
    id: String = "t1",
    nombre: String = "Copa Prueba 2026",
    inscripcionesPendientes: Int = 0,
    comprobantesPendientes: Int = 0,
) = TorneoDeCreador(
    id = id,
    nombre = nombre,
    estado = EstadoTorneo.PUBLICADO,
    deporte = "FUTSAL",
    visibilidad = Visibilidad.PUBLICO,
    cupos = 16,
    inscritos = 2,
    enlace = "https://desafio.test/t/$id",
    inscripcionesPendientes = inscripcionesPendientes,
    comprobantesPendientes = comprobantesPendientes,
)

fun inscripcionDePrueba(
    id: String,
    equipo: String = "Equipo $id",
    estado: EstadoInscripcion = EstadoInscripcion.PENDIENTE_PAGO,
    fecha: LocalDateTime? = LocalDateTime.of(2026, 11, 7, 10, 0),
    motivoRechazo: String? = null,
) = Inscripcion(
    id = id,
    equipo = equipo,
    delegado = "Andrés Gómez",
    estado = estado,
    motivoRechazo = motivoRechazo,
    fecha = fecha,
    monto = BigDecimal("50000"),
)
