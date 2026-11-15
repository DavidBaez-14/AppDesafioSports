package co.edu.ufps.desafiosports.domain.model

/** El puesto que ganó un equipo en un torneo. */
enum class Puesto {
    CAMPEON, SUBCAMPEON, TERCERO;

    companion object {
        fun desde(codigo: String?): Puesto? = entries.firstOrNull { it.name == codigo }
    }
}

data class Tarjetas(val amarillas: Int = 0, val azules: Int = 0, val rojas: Int = 0) {
    val total: Int get() = amarillas + azules + rojas
}

/** Un renglón de la trayectoria: un equipo en un torneo y, si lo hubo, el puesto que ganó. */
data class PasoPorEquipo(val equipo: String, val torneo: String, val puesto: Puesto?)

/** El perfil de jugador de quien inició sesión (`GET /api/supercopa/mi-perfil`, MOV-0004). */
data class Perfil(
    val nombre: String,
    val alturaCm: Int?,
    /** Código del backend (DERECHA, IZQUIERDA, AMBIDIESTRO). Null si no lo ha indicado. */
    val piernaHabil: String?,
    /** Código del backend (PORTERO, DEFENSA…). Null si no lo ha indicado. */
    val posicion: String?,
    val trayectoria: List<PasoPorEquipo>,
    val partidosJugados: Int,
    val goles: Int,
    val titulos: Int,
    val tarjetas: Tarjetas,
) {
    /** Las cifras no se muestran si las tres están en cero (MOV-0004 · CA-8). */
    val conCifras: Boolean get() = partidosJugados > 0 || goles > 0 || titulos > 0

    val conPerfilDeportivo: Boolean get() = alturaCm != null || piernaHabil != null || posicion != null
}
