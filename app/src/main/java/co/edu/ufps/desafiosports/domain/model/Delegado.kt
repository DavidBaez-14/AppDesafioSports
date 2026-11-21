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
