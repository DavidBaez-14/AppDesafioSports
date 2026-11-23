package co.edu.ufps.desafiosports.domain.model

// Inscribir un equipo en un torneo (MOV-0014). Quien inscribe queda como delegado del equipo en
// ese torneo y entra a su plantel.

/** Un equipo base de la persona: el que lleva de torneo en torneo (`GET /delegado/equipos`). */
data class EquipoPropio(val id: String, val nombre: String)

/** Qué equipo se inscribe: uno de los suyos o uno nuevo, que se crea en el mismo paso. */
sealed interface EquipoAInscribir {
    data class Existente(val equipoId: String) : EquipoAInscribir
    data class Nuevo(val nombre: String) : EquipoAInscribir
}

/** Cómo quedó la inscripción recién hecha. Arranca pendiente: el organizador la aprueba. */
data class InscripcionHecha(
    val equipoTorneoId: String,
    val equipo: String,
    val estado: EstadoInscripcion,
)

// ── Plantel (MOV-0015) ──────────────────────────────────────────────────────

/** Un miembro del plantel de un equipo en un torneo, visto por su delegado. */
data class MiembroDePlantel(
    val personaId: String,
    val nombre: String,
    /** Solo lo ve el delegado. Null si no se registró. */
    val numeroDocumento: String?,
    val numeroCamiseta: Int?,
    /** False si el delegado lo quitó: sigue en el historial, pero no juega más. */
    val activo: Boolean,
    val esDelegado: Boolean,
    /** False si el delegado lo agregó sin cuenta: aún no puede entrar a la app. */
    val tieneCuenta: Boolean,
    /** El torneo ya terminó: el plantel queda como estaba. */
    val torneoFinalizado: Boolean,
)

/** El plantel como se lee: los activos por número de camiseta y los que ya no están. */
data class Plantel(val miembros: List<MiembroDePlantel>) {
    val activos: List<MiembroDePlantel>
        get() = miembros.filter { it.activo }
            .sortedWith(compareBy<MiembroDePlantel> { it.numeroCamiseta ?: Int.MAX_VALUE }.thenBy { it.nombre.lowercase() })

    val retirados: List<MiembroDePlantel>
        get() = miembros.filterNot { it.activo }.sortedBy { it.nombre.lowercase() }

    /** Con el torneo terminado ya no se cambia nada. */
    val editable: Boolean get() = miembros.none { it.torneoFinalizado }

    /** El mismo plantel con [miembro] puesto en su lugar (o agregado si es nuevo). */
    fun con(miembro: MiembroDePlantel): Plantel =
        if (miembros.any { it.personaId == miembro.personaId }) {
            copy(miembros = miembros.map { if (it.personaId == miembro.personaId) miembro else it })
        } else {
            copy(miembros = miembros + miembro)
        }

    /** Quién tiene ya ese número entre los activos, sin contar a [salvo]. */
    fun duenoDelNumero(numero: Int, salvo: String? = null): MiembroDePlantel? =
        miembros.firstOrNull { it.activo && it.numeroCamiseta == numero && it.personaId != salvo }
}

/** Lo que se escribe para agregar a alguien. El documento identifica a quien ya tiene cuenta. */
data class BorradorJugador(
    val tipoDocumento: TipoDocumento = TipoDocumento.CC,
    val documento: String = "",
    val nombre: String = "",
    val camiseta: String = "",
)
