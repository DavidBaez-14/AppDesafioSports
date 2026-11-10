package co.edu.ufps.desafiosports.ui.formato

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/** MOV-0009: fechas, horas y pesos como se leen en Colombia, iguales en cualquier teléfono. */
class FormatosTest {

    @Test
    fun `la hora va en 12 horas con a m y p m`() {
        assertEquals("4:00 p. m.", Formatos.hora(LocalTime.of(16, 0)))
        assertEquals("8:05 a. m.", Formatos.hora(LocalTime.of(8, 5)))
        assertEquals("12:30 p. m.", Formatos.hora(LocalTime.of(12, 30)))
        assertEquals("12:00 a. m.", Formatos.hora(LocalTime.of(0, 0)))
        assertEquals("11:59 p. m.", Formatos.hora(LocalTime.of(23, 59)))
    }

    @Test
    fun `el dia corto lleva el dia de la semana y el numero`() {
        // 14 de noviembre de 2026 es sábado.
        assertEquals("sáb 14", Formatos.diaCorto(LocalDate.of(2026, 11, 14)))
        assertEquals("lun 9", Formatos.diaCorto(LocalDate.of(2026, 11, 9)))
        assertEquals("dom 15", Formatos.diaCorto(LocalDate.of(2026, 11, 15)))
    }

    @Test
    fun `la fecha lleva el anio solo si no es el actual`() {
        val hoy = LocalDate.of(2026, 11, 9)
        assertEquals("sáb 21 nov", Formatos.fecha(LocalDate.of(2026, 11, 21), hoy))
        assertEquals("vie 1 ene 2027", Formatos.fecha(LocalDate.of(2027, 1, 1), hoy))
    }

    @Test
    fun `hoy y manana se dicen con palabras`() {
        val hoy = LocalDate.of(2026, 11, 12)
        assertEquals("Hoy", Formatos.diaRelativo(hoy, hoy, "Hoy", "Mañana"))
        assertEquals("Mañana", Formatos.diaRelativo(hoy.plusDays(1), hoy, "Hoy", "Mañana"))
        assertEquals("sáb 14", Formatos.diaRelativo(hoy.plusDays(2), hoy, "Hoy", "Mañana"))
        assertEquals("mié 11", Formatos.diaRelativo(hoy.minusDays(1), hoy, "Hoy", "Mañana"))
    }

    @Test
    fun `los pesos van con punto de miles y sin decimales`() {
        assertEquals("$0", Formatos.pesos(0))
        assertEquals("$500", Formatos.pesos(500))
        assertEquals("$5.000", Formatos.pesos(5_000))
        assertEquals("$50.000", Formatos.pesos(BigDecimal("50000.00")))
        assertEquals("$1.250.000", Formatos.pesos(1_250_000))
        assertEquals("$10.001", Formatos.pesos(BigDecimal("10000.5")))
    }
}
