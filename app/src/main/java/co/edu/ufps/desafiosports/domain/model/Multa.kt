package co.edu.ufps.desafiosports.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

enum class EstadoMulta {
    PENDIENTE, EN_REVISION, PAGADA, CONDONADA, DESCONOCIDO;

    /** Todavía hay algo por hacer con la multa. */
    val abierta: Boolean get() = this == PENDIENTE || this == EN_REVISION

    companion object {
        fun desde(codigo: String?): EstadoMulta = entries.firstOrNull { it.name == codigo } ?: DESCONOCIDO
    }
}

/** Una multa de quien inició sesión (`GET /api/finanzas/multas/mias`, MOV-0004). */
data class Multa(
    val id: String,
    /** Código del backend (AMARILLA, ROJA_DIRECTA…). El texto se resuelve en la pantalla. */
    val tipoSancion: String,
    val monto: BigDecimal,
    val estado: EstadoMulta,
    val fechaGeneracion: LocalDateTime?,
)

/** Primero las que siguen abiertas; dentro de cada grupo, la más reciente arriba (MOV-0004 · CA-7). */
fun List<Multa>.ordenadas(): List<Multa> =
    sortedWith(
        compareByDescending<Multa> { it.estado.abierta }
            .thenByDescending { it.fechaGeneracion ?: LocalDateTime.MIN },
    )
