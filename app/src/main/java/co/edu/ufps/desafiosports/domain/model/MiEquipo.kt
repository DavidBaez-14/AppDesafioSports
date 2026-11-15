package co.edu.ufps.desafiosports.domain.model

import java.time.LocalDateTime

/** Qué hace la persona en un equipo. El delegado gestiona la inscripción y también juega. */
enum class RolEnEquipo {
    JUGADOR, DELEGADO;

    companion object {
        /** Un rol que la app no conoce se trata como jugador: es lo que todos son. */
        fun desde(codigo: String?): RolEnEquipo = if (codigo == DELEGADO.name) DELEGADO else JUGADOR
    }
}

/** Cómo va el trámite de un equipo en su torneo, como lo ve quien está en él (MOV-0004 §3.3). */
enum class EstadoTramite {
    /** Inscripción aprobada: no hay nada que avisar. */
    AL_DIA,
    FALTA_COMPROBANTE,
    EN_REVISION,
    COMPROBANTE_RECHAZADO,
    INSCRIPCION_PENDIENTE,
    RECHAZADA,
    EXPULSADO,
}

/** El partido que sigue para un equipo. [rival] y [fechaHora] pueden no estar definidos todavía. */
data class ProximoPartido(
    val partidoId: String,
    val rival: String?,
    /** El equipo propio juega de local. */
    val local: Boolean,
    val fechaHora: LocalDateTime?,
    val lugar: String?,
)

/** Un equipo en el que la persona juega o delega, dentro de un torneo (`GET /yo/equipos`). */
data class MiEquipo(
    val equipoTorneoId: String,
    val equipo: String,
    val torneoId: String,
    val torneo: String,
    /** Código del deporte (FUTSAL…). El icono se resuelve en `ui/deporte`. */
    val deporte: String,
    val rol: RolEnEquipo,
    val estadoInscripcion: EstadoInscripcion,
    /** Estado del comprobante de pago; solo llega para el delegado. Null si no ha subido ninguno. */
    val comprobante: String?,
    val proximoPartido: ProximoPartido?,
) {
    /** El estado que se muestra en el chip de "Mis equipos". Un estado desconocido no avisa nada. */
    val tramite: EstadoTramite
        get() = when (estadoInscripcion) {
            EstadoInscripcion.APROBADO, EstadoInscripcion.DESCONOCIDO -> EstadoTramite.AL_DIA
            EstadoInscripcion.RECHAZADO -> EstadoTramite.RECHAZADA
            EstadoInscripcion.EXPULSADO -> EstadoTramite.EXPULSADO
            EstadoInscripcion.PENDIENTE_PAGO -> when {
                rol != RolEnEquipo.DELEGADO -> EstadoTramite.INSCRIPCION_PENDIENTE
                comprobante == null -> EstadoTramite.FALTA_COMPROBANTE
                comprobante == "RECHAZADO" -> EstadoTramite.COMPROBANTE_RECHAZADO
                // PENDIENTE_REVISION, o aprobado mientras el organizador termina de aprobar al equipo.
                else -> EstadoTramite.EN_REVISION
            }
        }
}

/** El próximo partido de la persona, con el equipo con el que lo juega. */
data class PartidoDeMiEquipo(val equipo: MiEquipo, val partido: ProximoPartido, val fechaHora: LocalDateTime)

/**
 * "Tu próximo partido" (MOV-0004 · CA-4): entre todos los equipos, el partido con la fecha más
 * temprana. Los que no tienen fecha no cuentan.
 */
fun List<MiEquipo>.proximoPartido(): PartidoDeMiEquipo? =
    mapNotNull { equipo ->
        val partido = equipo.proximoPartido ?: return@mapNotNull null
        val fecha = partido.fechaHora ?: return@mapNotNull null
        PartidoDeMiEquipo(equipo, partido, fecha)
    }.minByOrNull { it.fechaHora }
