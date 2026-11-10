package co.edu.ufps.desafiosports.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

enum class TipoCompetencia { EQUIPO, INDIVIDUAL }

/** Un deporte del catálogo del backend (`GET /api/public/deportes`). */
data class Deporte(val codigo: String, val nombre: String, val tipo: TipoCompetencia)

/** Formatos que se pueden elegir al crear un torneo, con el rango de cupos que acepta el backend. */
enum class FormatoTorneo(val cuposMinimos: Int, val cuposMaximos: Int) {
    LIGA(2, 64),
    ELIMINACION_DIRECTA(2, 16),
    GRUPOS_ELIMINATORIAS(4, 64);

    fun acepta(cupos: Int): Boolean = cupos in cuposMinimos..cuposMaximos
}

/** Quién puede ver un torneo. La elige el organizador. */
enum class Visibilidad {
    /** Aparece en Explorar. */
    PUBLICO,

    /** No aparece en Explorar: entra quien tenga el enlace. */
    NO_LISTADO,

    /** Solo lo ven sus participantes. */
    PRIVADO;

    companion object {
        fun desde(codigo: String?): Visibilidad? = entries.firstOrNull { it.name == codigo }
    }
}

enum class EstadoTorneo {
    BORRADOR, PUBLICADO, EN_CURSO, FINALIZADO, DESCONOCIDO;

    companion object {
        fun desde(codigo: String?): EstadoTorneo = entries.firstOrNull { it.name == codigo } ?: DESCONOCIDO
    }
}

/**
 * Lo que la persona va llenando en "Crear torneo" (MOV-0007). Aquí viven las reglas de cada paso,
 * las mismas que valida el backend, para que "Siguiente" solo se habilite con el paso completo.
 */
data class BorradorTorneo(
    val deporte: Deporte? = null,
    val nombre: String = "",
    val formato: FormatoTorneo? = null,
    val cupos: Int = CUPOS_INICIALES,
    val fechaInicio: LocalDate? = null,
    val visibilidad: Visibilidad? = null,
    val cobraInscripcion: Boolean = false,
    /** En pesos, sin decimales. */
    val valorInscripcion: Long? = null,
) {
    val nombreValido: Boolean get() = nombre.trim().length in NOMBRE_MINIMO..NOMBRE_MAXIMO

    /** Rango de cupos del formato elegido; sin formato, el más amplio. */
    val cuposMinimos: Int get() = formato?.cuposMinimos ?: 2
    val cuposMaximos: Int get() = formato?.cuposMaximos ?: 64

    /** Ya se escribió o eligió algo: salir pide confirmación para no perderlo. */
    val tieneDatos: Boolean get() = deporte != null || nombre.isNotBlank() || formato != null

    fun pasoCompleto(paso: Int, hoy: LocalDate): Boolean = when (paso) {
        1 -> deporte != null
        2 -> nombreValido && formato != null && formato.acepta(cupos) && fechaInicio != null && !fechaInicio.isBefore(hoy)
        3 -> visibilidad != null && (!cobraInscripcion || (valorInscripcion ?: 0) > 0)
        else -> false
    }

    /** Cambiar de formato ajusta los cupos a su rango (eliminación directa admite hasta 16). */
    fun conFormato(nuevo: FormatoTorneo): BorradorTorneo =
        copy(formato = nuevo, cupos = cupos.coerceIn(nuevo.cuposMinimos, nuevo.cuposMaximos))

    fun conCupos(nuevos: Int): BorradorTorneo = copy(cupos = nuevos.coerceIn(cuposMinimos, cuposMaximos))

    /** El torneo listo para enviar, o null si falta algún paso. */
    fun aNuevoTorneo(hoy: LocalDate): NuevoTorneo? {
        if (!(1..PASOS).all { pasoCompleto(it, hoy) }) return null
        return NuevoTorneo(
            nombre = nombre.trim(),
            deporte = deporte!!.codigo,
            formato = formato!!,
            cupos = cupos,
            fechaInicio = fechaInicio!!,
            visibilidad = visibilidad!!,
            cobraInscripcion = cobraInscripcion,
            valorInscripcion = if (cobraInscripcion) valorInscripcion else null,
        )
    }

    companion object {
        const val PASOS = 3
        const val NOMBRE_MINIMO = 3
        const val NOMBRE_MAXIMO = 150
        const val CUPOS_INICIALES = 8
    }
}

/** Cuerpo de "crear torneo en un paso" (`POST /api/supercopa/creador/torneos`). */
data class NuevoTorneo(
    val nombre: String,
    val deporte: String,
    val formato: FormatoTorneo,
    val cupos: Int,
    val fechaInicio: LocalDate,
    val visibilidad: Visibilidad,
    val cobraInscripcion: Boolean,
    val valorInscripcion: Long?,
)

/** "Tu torneo está listo": lo que responde el backend al crear. */
data class TorneoCreado(
    val id: String,
    val nombre: String,
    val deporte: String,
    val visibilidad: Visibilidad?,
    /** Existe siempre: un torneo público también se comparte con él. */
    val enlace: String,
)

/** Un torneo en "Mis torneos" de la sección Creador (MOV-0008). */
data class TorneoDeCreador(
    val id: String,
    val nombre: String,
    val estado: EstadoTorneo,
    val deporte: String,
    val visibilidad: Visibilidad?,
    /** Máximo de inscripciones aprobadas; null = sin límite. */
    val cupos: Int?,
    val inscritos: Int,
    val enlace: String?,
    val inscripcionesPendientes: Int,
    val comprobantesPendientes: Int,
) {
    val porRevisar: Int get() = inscripcionesPendientes + comprobantesPendientes
}

enum class EstadoInscripcion {
    PENDIENTE_PAGO, APROBADO, RECHAZADO, EXPULSADO, DESCONOCIDO;

    companion object {
        fun desde(codigo: String?): EstadoInscripcion = entries.firstOrNull { it.name == codigo } ?: DESCONOCIDO
    }
}

/** La inscripción de un equipo a un torneo, vista por el organizador (MOV-0008). */
data class Inscripcion(
    /** Id del equipo en el torneo: con él se aprueba o se rechaza. */
    val id: String,
    val equipo: String,
    val delegado: String?,
    val estado: EstadoInscripcion,
    val motivoRechazo: String?,
    val fecha: LocalDateTime?,
    val monto: BigDecimal?,
)

/** Las inscripciones de un torneo repartidas como las lee el organizador. */
data class InscripcionesDeTorneo(
    val porAprobar: List<Inscripcion> = emptyList(),
    val aprobadas: List<Inscripcion> = emptyList(),
    val rechazadas: List<Inscripcion> = emptyList(),
) {
    val vacio: Boolean get() = porAprobar.isEmpty() && aprobadas.isEmpty() && rechazadas.isEmpty()
}

/**
 * Por aprobar (las más antiguas primero: llevan más tiempo esperando), aprobadas y, al final,
 * rechazadas y expulsadas juntas. Un estado desconocido no se pierde: va con las pendientes.
 */
fun List<Inscripcion>.repartir(): InscripcionesDeTorneo = InscripcionesDeTorneo(
    porAprobar = filter { it.estado == EstadoInscripcion.PENDIENTE_PAGO || it.estado == EstadoInscripcion.DESCONOCIDO }
        .sortedWith(compareBy<Inscripcion> { it.fecha == null }.thenBy { it.fecha }),
    aprobadas = filter { it.estado == EstadoInscripcion.APROBADO }.sortedBy { it.equipo.lowercase() },
    rechazadas = filter { it.estado == EstadoInscripcion.RECHAZADO || it.estado == EstadoInscripcion.EXPULSADO }
        .sortedBy { it.equipo.lowercase() },
)
