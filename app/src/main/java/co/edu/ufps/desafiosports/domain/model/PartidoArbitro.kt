package co.edu.ufps.desafiosports.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** Estado de un partido según el backend. Lo que la app no conoce cae en [DESCONOCIDO]. */
enum class EstadoPartido {
    PROGRAMADO, EN_CURSO, FINALIZADO, APLAZADO, WO, DESCONOCIDO;

    /** Ya tiene resultado: no admite más eventos. */
    val cerrado: Boolean get() = this == FINALIZADO || this == WO

    companion object {
        fun desde(codigo: String?): EstadoPartido = entries.firstOrNull { it.name == codigo } ?: DESCONOCIDO
    }
}

data class EquipoEnPartido(val equipoTorneoId: String, val nombre: String)

data class Marcador(val local: Int, val visitante: Int)

/** Un partido que la persona arbitra (MOV-0009). */
data class PartidoArbitro(
    val id: String,
    val torneoId: String,
    val torneo: String,
    /** Código del deporte del torneo (FUTSAL, VOLEIBOL…). */
    val deporte: String,
    /** Listo para mostrar: "Grupo A · Fecha 3", "Semifinal"… */
    val fase: String,
    /** Null mientras el cruce no está definido. */
    val local: EquipoEnPartido?,
    val visitante: EquipoEnPartido?,
    val fechaHora: LocalDateTime?,
    val lugar: String?,
    val estado: EstadoPartido,
    /** Null mientras está programado. */
    val marcador: Marcador?,
    /** False si lo ve por el permiso "todos los partidos" del torneo. */
    val asignado: Boolean,
) {
    val cruceDefinido: Boolean get() = local != null && visitante != null
}

/** Los partidos de la sección Árbitro, ya repartidos en sus tres bloques. */
data class PartidosDeArbitro(
    val hoy: List<PartidoArbitro> = emptyList(),
    val proximos: List<PartidoArbitro> = emptyList(),
    val jugados: List<PartidoArbitro> = emptyList(),
) {
    val vacio: Boolean get() = hoy.isEmpty() && proximos.isEmpty() && jugados.isEmpty()
}

/**
 * Reparte los partidos (MOV-0009 · CA-1):
 *  - **Hoy:** los de la fecha de hoy, cerrados o no, en orden de hora;
 *  - **Próximos:** los que faltan por jugar. Incluye los atrasados (fecha pasada y sin cerrar),
 *    que van primero porque aún necesitan su planilla, y al final los que no tienen fecha;
 *  - **Jugados:** los cerrados de otros días, el más reciente arriba.
 */
fun List<PartidoArbitro>.repartir(hoy: LocalDate): PartidosDeArbitro {
    val deHoy = filter { it.fechaHora?.toLocalDate() == hoy }.sortedBy { it.fechaHora }
    val resto = filter { it.fechaHora?.toLocalDate() != hoy }
    val proximos = resto.filter { !it.estado.cerrado }
        .sortedWith(compareBy<PartidoArbitro> { it.fechaHora == null }.thenBy { it.fechaHora })
    val jugados = resto.filter { it.estado.cerrado }.sortedByDescending { it.fechaHora }
    return PartidosDeArbitro(hoy = deHoy, proximos = proximos, jugados = jugados)
}
