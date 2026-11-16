package co.edu.ufps.desafiosports.domain.model

import java.time.LocalDateTime

// Gestión de un torneo que la persona administra (MOV-0011): sus árbitros, las invitaciones
// pendientes y los partidos.

/** Un árbitro del torneo. [rolId] sirve para quitarlo y [cuentaId] para asignarlo a un partido. */
data class ArbitroDeTorneo(
    val rolId: String,
    val cuentaId: String,
    val nombre: String,
    val correo: String?,
    /** Puede operar la planilla de cualquier partido del torneo, sin asignación. */
    val todosLosPartidos: Boolean,
)

/** Una invitación de árbitro que nadie ha aceptado todavía. */
data class InvitacionDeTorneo(
    val id: String,
    /** Null si es "por enlace": la acepta la primera cuenta que la abra. */
    val correo: String?,
    val enlace: String,
    val fecha: LocalDateTime?,
)

data class PersonalDeArbitraje(
    val arbitros: List<ArbitroDeTorneo> = emptyList(),
    val invitaciones: List<InvitacionDeTorneo> = emptyList(),
) {
    val vacio: Boolean get() = arbitros.isEmpty() && invitaciones.isEmpty()
}

data class ArbitroAsignado(val cuentaId: String, val nombre: String)

/** Las fases en el orden en que se juegan. */
enum class FasePartido {
    GRUPOS, REPECHAJE, OCTAVOS, CUARTOS, SEMIS, TERCER_PUESTO, FINAL, DESCONOCIDA;

    companion object {
        fun desde(codigo: String?): FasePartido = entries.firstOrNull { it.name == codigo } ?: DESCONOCIDA
    }
}

/** Un partido del torneo, visto por quien lo organiza. */
data class PartidoDeTorneo(
    val id: String,
    val fecha: LocalDateTime?,
    val estado: EstadoPartido,
    /** El código que mandó el backend: `DESCANSO` no está en [EstadoPartido]. */
    val estadoCodigo: String,
    /** Null en una eliminatoria cuyo cruce aún no se conoce. */
    val local: String?,
    val visitante: String?,
    val golesLocal: Int,
    val golesVisitante: Int,
    val fase: FasePartido,
    /** El código de la fase, para mostrar legible una que la app no conoce. */
    val faseCodigo: String,
    val jornada: Int?,
    val grupo: String?,
    val lugar: String?,
    val arbitros: List<ArbitroAsignado>,
) {
    val descansa: Boolean get() = estadoCodigo == "DESCANSO"

    /** Todavía se le pueden cambiar los árbitros. */
    val admiteArbitros: Boolean get() = !estado.cerrado && !descansa
}

/** Los partidos que comparten encabezado: una jornada, un grupo en una jornada o una ronda. */
data class GrupoDePartidos(
    val fase: FasePartido,
    val faseCodigo: String,
    val jornada: Int?,
    val grupo: String?,
    val partidos: List<PartidoDeTorneo>,
) {
    /** Estable entre recargas: sirve de llave en la lista. */
    val clave: String get() = "$faseCodigo|${jornada ?: ""}|${grupo.orEmpty()}"
}

/**
 * Agrupa y ordena los partidos como se juegan: la fase de grupos por jornada y luego por grupo,
 * y después las rondas hasta la final. Dentro de cada grupo, por fecha (sin fecha, al final).
 */
fun List<PartidoDeTorneo>.porFaseYJornada(): List<GrupoDePartidos> =
    groupBy { p ->
        // En las rondas la jornada no separa: todos los cuartos van juntos.
        if (p.fase == FasePartido.GRUPOS) Triple(p.faseCodigo, p.jornada, p.grupo) else Triple(p.faseCodigo, null, null)
    }.map { (clave, partidos) ->
        GrupoDePartidos(
            fase = partidos.first().fase,
            faseCodigo = clave.first,
            jornada = clave.second,
            grupo = clave.third,
            partidos = partidos.sortedWith(compareBy<PartidoDeTorneo> { it.fecha == null }.thenBy { it.fecha }),
        )
    }.sortedWith(
        compareBy<GrupoDePartidos> { it.fase.ordinal }
            .thenBy { it.jornada ?: Int.MAX_VALUE }
            .thenBy { it.grupo.orEmpty() }
            .thenBy { it.faseCodigo },
    )

/** Por qué no se pueden generar los partidos todavía. */
sealed interface BloqueoDePartidos {
    data class FaltanEquipos(val aprobados: Int) : BloqueoDePartidos
    data object TorneoEnBorrador : BloqueoDePartidos
    data object TorneoFinalizado : BloqueoDePartidos
}

/** Lo que hace falta saber del torneo para generar sus partidos (`GET …/configuracion`). */
data class EstadoDePartidos(
    val estadoTorneo: EstadoTorneo,
    val equiposAprobados: Int,
    val generados: Boolean,
) {
    val bloqueo: BloqueoDePartidos?
        get() = when {
            estadoTorneo == EstadoTorneo.BORRADOR -> BloqueoDePartidos.TorneoEnBorrador
            estadoTorneo == EstadoTorneo.FINALIZADO -> BloqueoDePartidos.TorneoFinalizado
            equiposAprobados < EQUIPOS_MINIMOS -> BloqueoDePartidos.FaltanEquipos(equiposAprobados)
            else -> null
        }

    val puedeGenerar: Boolean get() = !generados && bloqueo == null

    /** Con las inscripciones abiertas, generar los partidos inicia el torneo: se avisa antes. */
    val generarIniciaElTorneo: Boolean get() = estadoTorneo == EstadoTorneo.PUBLICADO

    companion object {
        const val EQUIPOS_MINIMOS = 2
    }
}

/** Los partidos de un torneo con lo necesario para generarlos si aún no existen. */
data class PartidosDeTorneo(
    val estado: EstadoDePartidos,
    val partidos: List<PartidoDeTorneo>,
)

// ── Invitación recibida ─────────────────────────────────────────────────────

enum class RolInvitado {
    ARBITRO, ADMINISTRADOR, DELEGADO, DESCONOCIDO;

    companion object {
        fun desde(codigo: String?): RolInvitado = entries.firstOrNull { it.name == codigo } ?: DESCONOCIDO
    }
}

/** Lo que ve quien abre una invitación, antes de aceptarla. */
data class InvitacionRecibida(
    val token: String,
    val rol: RolInvitado,
    /** El nombre del torneo; null si es una invitación a la plataforma. */
    val torneo: String?,
    val invitadoPor: String?,
    /** Correo al que va dirigida, enmascarado; null si es por enlace. */
    val paraCorreo: String?,
    val pendiente: Boolean,
)

private val TOKEN_EN_ENLACE = Regex("""/invitacion/([A-Za-z0-9_-]{16,})""")
private val TOKEN_SUELTO = Regex("""[A-Za-z0-9_-]{16,}""")
private val CORREO = Regex("""[^@\s]+@[^@\s]+\.[^@\s]+""")

/**
 * El token de una invitación, del enlace completo (`…/invitacion/{token}`) o suelto.
 * Null si el texto no parece una invitación: así no se llama al backend por nada.
 */
fun tokenDeInvitacion(texto: String): String? {
    val limpio = texto.trim()
    TOKEN_EN_ENLACE.find(limpio)?.let { return it.groupValues[1] }
    return limpio.takeIf { TOKEN_SUELTO.matches(it) }
}

/** Un correo opcional: vacío vale; escrito, tiene que parecer un correo. */
fun correoDeInvitacionValido(correo: String): Boolean {
    val limpio = correo.trim()
    return limpio.isEmpty() || CORREO.matches(limpio)
}
