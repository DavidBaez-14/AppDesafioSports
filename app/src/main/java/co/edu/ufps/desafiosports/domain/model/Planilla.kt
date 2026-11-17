package co.edu.ufps.desafiosports.domain.model

/** Un miembro del plantel en la planilla. Si [apto] es false, no se puede alinear. */
data class JugadorPlanilla(
    val personaId: String,
    val nombre: String,
    val numeroCamiseta: Int?,
    /** Para verificar la identidad en la cancha. Null si no se registró. */
    val numeroDocumento: String?,
    val esDelegado: Boolean,
    val alineado: Boolean,
    val goles: Int,
    val apto: Boolean,
    /** Por qué no puede jugar (suspensión, multa pendiente…), en texto del backend. */
    val motivos: List<String>,
    /** Ya tiene una tarjeta azul o roja en este partido: no se le anota nada más (MOV-0012). */
    val expulsado: Boolean = false,
)

data class EquipoPlanilla(
    val equipoTorneoId: String,
    val nombre: String,
    val jugadores: List<JugadorPlanilla>,
) {
    val alineados: Int get() = jugadores.count { it.alineado }

    /**
     * A quién se le puede anotar un evento (MOV-0012): solo a quien está alineado y sigue en la
     * cancha, por número de camiseta. Primero se pasa lista; el backend rechaza lo demás.
     */
    fun candidatos(): List<JugadorPlanilla> = jugadores.filter { it.alineado && !it.expulsado }
        .sortedWith(compareBy<JugadorPlanilla> { it.numeroCamiseta ?: Int.MAX_VALUE }.thenBy { it.nombre })
}

/** Un evento del partido. [tipo] es el código del backend (GOL, AMARILLA…); van en orden, sin minuto. */
data class EventoPartido(
    val id: String,
    val orden: Int,
    val personaId: String,
    val jugadorNombre: String,
    val equipoTorneoId: String,
    val equipoNombre: String,
    val tipo: String,
)

enum class Lado { LOCAL, VISITANTE }

/** Todo lo que muestra la planilla del árbitro (MOV-0009). Siempre es lo que respondió el backend. */
data class Planilla(
    val partido: PartidoArbitro,
    /** Tipos de evento del deporte, en el orden en que se muestran. */
    val tiposEvento: List<String>,
    val local: EquipoPlanilla,
    val visitante: EquipoPlanilla,
    val eventos: List<EventoPartido>,
    /** Se pueden registrar eventos y cambiar la alineación. */
    val editable: Boolean,
    /** Quien la ve organiza el torneo y el partido ya se cerró: puede reabrirlo (MOV-0012). */
    val puedeReabrir: Boolean = false,
) {
    val marcador: Marcador get() = partido.marcador ?: Marcador(0, 0)

    fun equipo(lado: Lado): EquipoPlanilla = if (lado == Lado.LOCAL) local else visitante

    /** El equipo en el que está inscrita la persona, o null si no es de ninguno de los dos. */
    fun equipoDe(personaId: String): EquipoPlanilla? =
        listOf(local, visitante).firstOrNull { equipo -> equipo.jugadores.any { it.personaId == personaId } }

    /** La misma planilla con [personaId] marcado o desmarcado en la alineación. */
    fun conAlineado(personaId: String, alineado: Boolean): Planilla = copy(
        local = local.conAlineado(personaId, alineado),
        visitante = visitante.conAlineado(personaId, alineado),
    )
}

private fun EquipoPlanilla.conAlineado(personaId: String, alineado: Boolean): EquipoPlanilla =
    if (jugadores.none { it.personaId == personaId && it.alineado != alineado }) this
    else copy(jugadores = jugadores.map { if (it.personaId == personaId) it.copy(alineado = alineado) else it })
