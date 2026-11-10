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
)

data class EquipoPlanilla(
    val equipoTorneoId: String,
    val nombre: String,
    val jugadores: List<JugadorPlanilla>,
) {
    val alineados: Int get() = jugadores.count { it.alineado }

    /**
     * A quién se le puede anotar un evento: los aptos, con los alineados primero y después por
     * número de camiseta. El backend no exige alinear antes de registrar.
     */
    fun candidatos(): List<JugadorPlanilla> = jugadores.filter { it.apto }
        .sortedWith(
            compareByDescending<JugadorPlanilla> { it.alineado }
                .thenBy { it.numeroCamiseta ?: Int.MAX_VALUE }
                .thenBy { it.nombre },
        )
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
) {
    val marcador: Marcador get() = partido.marcador ?: Marcador(0, 0)

    fun equipo(lado: Lado): EquipoPlanilla = if (lado == Lado.LOCAL) local else visitante
}
