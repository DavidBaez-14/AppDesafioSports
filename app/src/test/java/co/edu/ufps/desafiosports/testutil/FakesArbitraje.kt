package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.domain.model.EquipoEnPartido
import co.edu.ufps.desafiosports.domain.model.EquipoPlanilla
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EventoPartido
import co.edu.ufps.desafiosports.domain.model.JugadorPlanilla
import co.edu.ufps.desafiosports.domain.model.Marcador
import co.edu.ufps.desafiosports.domain.model.PartidoArbitro
import co.edu.ufps.desafiosports.domain.model.Planilla
import co.edu.ufps.desafiosports.domain.repository.ArbitrajeRepository
import kotlinx.coroutines.CompletableDeferred
import java.time.LocalDateTime

/**
 * Repositorio falso de la sección Árbitro (MOV-0009). Guarda cada llamada en [llamadas] como texto
 * ("evento:persona:equipo:GOL") y responde lo que se le configure.
 */
class FakeArbitrajeRepository : ArbitrajeRepository {
    val llamadas = mutableListOf<String>()

    var partidos: Result<List<PartidoArbitro>> = Result.success(emptyList())

    /** Lo que responde `planilla()`. Se puede cambiar entre llamadas para simular el cambio en el backend. */
    var planilla: Result<Planilla> = Result.success(planillaDePrueba())

    /** Si no es null, se ejecuta justo antes de responder cada acción: sirve para dejar lista la planilla siguiente. */
    var alActuar: (() -> Unit)? = null

    var resultadoEvento: Result<EventoPartido> = Result.success(eventoDePrueba("ev-nuevo"))
    var resultadoAccion: Result<Unit> = Result.success(Unit)

    /** Si no es null, registrarEvento espera a que se complete (doble toque). */
    var bloqueoEvento: CompletableDeferred<Unit>? = null

    val pedidosDePlanilla: Int get() = llamadas.count { it == "planilla" }

    override suspend fun misPartidos(): Result<List<PartidoArbitro>> {
        llamadas += "partidos"
        return partidos
    }

    override suspend fun planilla(partidoId: String): Result<Planilla> {
        llamadas += "planilla"
        return planilla
    }

    override suspend fun registrarEvento(partidoId: String, personaId: String, equipoTorneoId: String, tipo: String): Result<EventoPartido> {
        llamadas += "evento:$personaId:$equipoTorneoId:$tipo"
        bloqueoEvento?.await()
        alActuar?.invoke()
        return resultadoEvento
    }

    override suspend fun quitarEvento(partidoId: String, eventoId: String): Result<Unit> = accion("quitar-evento:$eventoId")

    override suspend fun alinear(partidoId: String, personaId: String, equipoTorneoId: String): Result<Unit> =
        accion("alinear:$personaId:$equipoTorneoId")

    override suspend fun quitarDeAlineacion(partidoId: String, personaId: String): Result<Unit> = accion("desalinear:$personaId")

    override suspend fun alinearEquipo(partidoId: String, equipoTorneoId: String): Result<Unit> = accion("alinear-todos:$equipoTorneoId")

    override suspend fun cerrarPartido(partidoId: String): Result<Unit> = accion("cerrar")

    private fun accion(nombre: String): Result<Unit> {
        llamadas += nombre
        alActuar?.invoke()
        return resultadoAccion
    }
}

fun partidoDePrueba(
    id: String = "p1",
    fechaHora: LocalDateTime? = LocalDateTime.of(2026, 11, 12, 16, 0),
    estado: EstadoPartido = EstadoPartido.PROGRAMADO,
    marcador: Marcador? = null,
    conCruce: Boolean = true,
) = PartidoArbitro(
    id = id,
    torneoId = "t1",
    torneo = "Liga Relámpago (prueba)",
    deporte = "FUTSAL",
    fase = "Fecha 1",
    local = if (conCruce) EquipoEnPartido("eq-local", "Halcones FC") else null,
    visitante = if (conCruce) EquipoEnPartido("eq-visita", "Tiburones FC") else null,
    fechaHora = fechaHora,
    lugar = "Cancha 1",
    estado = estado,
    marcador = marcador,
    asignado = true,
)

fun jugadorDePrueba(
    id: String,
    nombre: String = "Jugador $id",
    numero: Int? = null,
    alineado: Boolean = false,
    apto: Boolean = true,
) = JugadorPlanilla(
    personaId = id,
    nombre = nombre,
    numeroCamiseta = numero,
    numeroDocumento = null,
    esDelegado = false,
    alineado = alineado,
    goles = 0,
    apto = apto,
    motivos = if (apto) emptyList() else listOf("Suspendido por tarjeta roja"),
)

fun eventoDePrueba(id: String, tipo: String = "GOL", orden: Int = 1) = EventoPartido(
    id = id,
    orden = orden,
    personaId = "j1",
    jugadorNombre = "Juan Castro",
    equipoTorneoId = "eq-local",
    equipoNombre = "Halcones FC",
    tipo = tipo,
)

fun planillaDePrueba(
    editable: Boolean = true,
    estado: EstadoPartido = EstadoPartido.PROGRAMADO,
    marcador: Marcador? = null,
    eventos: List<EventoPartido> = emptyList(),
    conCruce: Boolean = true,
) = Planilla(
    partido = partidoDePrueba(estado = estado, marcador = marcador, conCruce = conCruce),
    tiposEvento = listOf("GOL", "AMARILLA", "AZUL", "ROJA"),
    local = EquipoPlanilla(
        "eq-local", "Halcones FC",
        listOf(
            jugadorDePrueba("j1", "Juan Castro", numero = 10, alineado = true),
            jugadorDePrueba("j2", "Mateo Jiménez", numero = 7),
            jugadorDePrueba("j3", "Sebastián Pardo", numero = 4, apto = false),
        ),
    ),
    visitante = EquipoPlanilla("eq-visita", "Tiburones FC", listOf(jugadorDePrueba("j9", "Nicolás Ortiz", numero = 9))),
    eventos = eventos,
    editable = editable,
)
