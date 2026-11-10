package co.edu.ufps.desafiosports.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// Explorar y detalle del torneo (MOV-0005), según las lecturas públicas del backend (0029).

/** Un torneo en la lista de Explorar (`GET /api/public/torneos`). */
data class TorneoResumen(
    val id: String,
    val nombre: String,
    /** Código del deporte (FUTSAL, TENIS_MESA…). */
    val deporte: String,
    val deporteNombre: String,
    val formato: FormatoTorneo?,
    val estado: EstadoTorneo,
    /** Null = sin límite. */
    val cupos: Int?,
    val inscritos: Int,
    val cierreInscripciones: LocalDate?,
    val inicio: LocalDate?,
    val organizador: String?,
    val inscripcionesAbiertas: Boolean,
)

/** Una página de resultados. [pagina] empieza en 0. */
data class Pagina<T>(val contenido: List<T>, val pagina: Int, val totalPaginas: Int) {
    val hayMas: Boolean get() = pagina + 1 < totalPaginas
}

data class EquipoInscrito(val equipoTorneoId: String, val nombre: String, val miembros: Int)

/** Una fila de la tabla de posiciones. Los nombres de columna los decide la pantalla. */
data class FilaTabla(
    val posicion: Int,
    val equipoTorneoId: String,
    val equipo: String,
    val puntos: Int,
    val jugados: Int,
    val ganados: Int,
    val empatados: Int,
    val perdidos: Int,
    val diferencia: Int,
)

data class PartidoDelTorneo(
    val id: String,
    /** Listo para mostrar: "Fecha 3", "Semifinal". */
    val fase: String,
    val local: String?,
    val visitante: String?,
    val fechaHora: LocalDateTime?,
    val lugar: String?,
    val estado: EstadoPartido,
    val marcador: Marcador?,
)

data class Premio(val nombre: String, val ganador: String?)

/** Lo que la persona es en este torneo (solo con sesión). Una sola respuesta pinta la franja de acciones. */
data class MiRelacion(
    val administra: Boolean,
    val arbitra: Boolean,
    val delegadoDe: RelacionConEquipo?,
    val juegaEn: RelacionConEquipo?,
)

data class RelacionConEquipo(
    val equipoTorneoId: String,
    val equipo: String,
    val estadoInscripcion: EstadoInscripcion,
    /** Estado del comprobante de inscripción; solo llega para el delegado. */
    val comprobante: String?,
)

/** El detalle de un torneo (`GET /api/public/torneos/{id}` o `/t/{enlaceToken}`). */
data class TorneoDetalle(
    val id: String,
    val nombre: String,
    val deporte: Deporte?,
    val formato: FormatoTorneo?,
    val estado: EstadoTorneo,
    val visibilidad: Visibilidad?,
    val fechaInicio: LocalDate?,
    val fechaFin: LocalDate?,
    val cierreInscripciones: LocalDate?,
    val cupos: Int?,
    val inscritos: Int,
    val inscripcionesAbiertas: Boolean,
    val cobraInscripcion: Boolean,
    val valorInscripcion: BigDecimal?,
    val exigeDocumento: Boolean,
    val reglas: String?,
    val organizador: String?,
    /** Null en un privado, salvo para sus administradores. */
    val enlace: String?,
    val equipos: List<EquipoInscrito>,
    /** Por grupo; en una liga hay un solo grupo. */
    val tabla: Map<String, List<FilaTabla>>,
    val partidos: List<PartidoDelTorneo>,
    val premios: List<Premio>,
    /** Null sin sesión. */
    val miRelacion: MiRelacion?,
) {
    val individual: Boolean get() = deporte?.tipo == TipoCompetencia.INDIVIDUAL
}

/**
 * El token de un enlace de torneo (`https://…/t/0e46a7c3…`), o el token suelto si se pegó solo
 * ese. Null si el texto no parece un enlace de torneo.
 */
fun tokenDeEnlace(texto: String): String? {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return null
    val despuesDeT = Regex("""/t/([A-Za-z0-9_-]{8,})""").find(limpio)?.groupValues?.get(1)
    if (despuesDeT != null) return despuesDeT
    return limpio.takeIf { Regex("""[A-Za-z0-9_-]{16,}""").matches(it) }
}

/**
 * La acción principal del detalle del torneo (MOV-0005 · CA-4), por la relación de la persona.
 * Lo que la persona ya es pesa más que lo que podría hacer: si organiza, eso se muestra aunque
 * las inscripciones estén abiertas.
 */
sealed interface AccionDelTorneo {
    /** Sin sesión y con inscripciones abiertas: entrar para inscribirse. */
    data object EntrarParaInscribirse : AccionDelTorneo
    data object Administra : AccionDelTorneo
    data class Delegado(val relacion: RelacionConEquipo) : AccionDelTorneo
    data class Juega(val relacion: RelacionConEquipo) : AccionDelTorneo
    data object Arbitra : AccionDelTorneo
    /** Inscripciones abiertas en un deporte de equipo. Inscribir y unirse llegan con MOV-0011 y MOV-0012. */
    data object Inscribirse : AccionDelTorneo
    /** Deporte individual: la inscripción individual todavía no existe. */
    data object IndividualPendiente : AccionDelTorneo
    data object Cerradas : AccionDelTorneo
}

fun TorneoDetalle.accion(conSesion: Boolean): AccionDelTorneo {
    val relacion = miRelacion
    return when {
        !conSesion -> if (inscripcionesAbiertas) AccionDelTorneo.EntrarParaInscribirse else AccionDelTorneo.Cerradas
        relacion?.administra == true -> AccionDelTorneo.Administra
        relacion?.delegadoDe != null -> AccionDelTorneo.Delegado(relacion.delegadoDe)
        relacion?.juegaEn != null -> AccionDelTorneo.Juega(relacion.juegaEn)
        relacion?.arbitra == true -> AccionDelTorneo.Arbitra
        !inscripcionesAbiertas -> AccionDelTorneo.Cerradas
        individual -> AccionDelTorneo.IndividualPendiente
        else -> AccionDelTorneo.Inscribirse
    }
}
