package co.edu.ufps.desafiosports.data.repository

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

// Conversiones que comparten los repositorios. Una fecha que no se puede leer queda en null:
// la pantalla la muestra como "sin fecha" en vez de cerrarse.

/** `2026-11-12T16:00:00` (sin zona) → fecha y hora. */
internal fun String?.aFechaHora(): LocalDateTime? =
    if (isNullOrBlank()) null
    else try {
        LocalDateTime.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }

/** `2026-11-12` → fecha. */
internal fun String?.aFecha(): LocalDate? =
    if (isNullOrBlank()) null
    else try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
