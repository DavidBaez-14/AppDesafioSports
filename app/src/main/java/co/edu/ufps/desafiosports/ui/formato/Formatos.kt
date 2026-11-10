package co.edu.ufps.desafiosports.ui.formato

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Fechas, horas y dinero como se leen en Colombia. Se arman a mano (sin `DateTimeFormatter` con
 * idioma) para que salgan iguales en cualquier teléfono y en las pruebas de la JVM.
 */
object Formatos {

    private val DIAS = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
    private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    /** "4:00", en reloj de 12 horas. El "a. m." o "p. m." sale de [periodo]. */
    fun horaSinPeriodo(hora: LocalTime): String {
        val h = if (hora.hour % 12 == 0) 12 else hora.hour % 12
        return "$h:${hora.minute.toString().padStart(2, '0')}"
    }

    /** "a. m." o "p. m." */
    fun periodo(hora: LocalTime): String = if (hora.hour < 12) "a. m." else "p. m."

    /** "4:00 p. m." */
    fun hora(hora: LocalTime): String = "${horaSinPeriodo(hora)} ${periodo(hora)}"

    fun hora(fechaHora: LocalDateTime): String = hora(fechaHora.toLocalTime())

    /** "sáb 14" */
    fun diaCorto(fecha: LocalDate): String = "${DIAS[fecha.dayOfWeek.value - 1]} ${fecha.dayOfMonth}"

    /** "sáb 14 nov"; con el año si no es el de [hoy]: "sáb 14 nov 2027". */
    fun fecha(fecha: LocalDate, hoy: LocalDate = LocalDate.now()): String {
        val base = "${diaCorto(fecha)} ${MESES[fecha.monthValue - 1]}"
        return if (fecha.year == hoy.year) base else "$base ${fecha.year}"
    }

    /**
     * El día como se dice al hablar: [textoHoy] o [textoManana] cuando aplica, y si no la fecha
     * con su mes ("sáb 14 nov"): sin el mes, un partido atrasado o lejano no se sabe de cuándo es.
     * Los textos llegan de `strings.xml`.
     */
    fun diaRelativo(fecha: LocalDate, hoy: LocalDate, textoHoy: String, textoManana: String): String = when (fecha) {
        hoy -> textoHoy
        hoy.plusDays(1) -> textoManana
        else -> fecha(fecha, hoy)
    }

    /** "$50.000". Sin decimales: en pesos no se usan. */
    fun pesos(valor: BigDecimal): String {
        val entero = valor.setScale(0, RoundingMode.HALF_UP).toBigInteger()
        val signo = if (entero.signum() < 0) "-" else ""
        val agrupado = entero.abs().toString().reversed().chunked(3).joinToString(".").reversed()
        return "$signo$$agrupado"
    }

    fun pesos(valor: Long): String = pesos(BigDecimal.valueOf(valor))
}
