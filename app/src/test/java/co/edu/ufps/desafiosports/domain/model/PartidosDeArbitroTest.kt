package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.testutil.jugadorDePrueba
import co.edu.ufps.desafiosports.testutil.partidoDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** MOV-0009 · CA-1: cómo se reparten los partidos del árbitro, y a quién se le anota un evento. */
class PartidosDeArbitroTest {

    private val hoy = LocalDate.of(2026, 11, 12)

    @Test
    fun `CA-1 reparte en hoy proximos y jugados`() {
        val partidos = listOf(
            partidoDePrueba("ayer-cerrado", hoy.minusDays(1).atTime(16, 0), EstadoPartido.FINALIZADO, Marcador(1, 0)),
            partidoDePrueba("hoy-tarde", hoy.atTime(18, 0)),
            partidoDePrueba("hoy-cerrado", hoy.atTime(8, 0), EstadoPartido.FINALIZADO, Marcador(3, 2)),
            partidoDePrueba("manana", hoy.plusDays(1).atTime(16, 0)),
            partidoDePrueba("wo-viejo", hoy.minusDays(5).atTime(16, 0), EstadoPartido.WO),
        )

        val repartidos = partidos.repartir(hoy)

        // Los de hoy van juntos, cerrados o no, en orden de hora.
        assertEquals(listOf("hoy-cerrado", "hoy-tarde"), repartidos.hoy.map { it.id })
        assertEquals(listOf("manana"), repartidos.proximos.map { it.id })
        // El más reciente arriba.
        assertEquals(listOf("ayer-cerrado", "wo-viejo"), repartidos.jugados.map { it.id })
        assertFalse(repartidos.vacio)
    }

    @Test
    fun `CA-1 un partido atrasado sin cerrar sigue pendiente y va primero`() {
        val partidos = listOf(
            partidoDePrueba("futuro", hoy.plusDays(3).atTime(16, 0)),
            partidoDePrueba("sin-fecha", fechaHora = null),
            partidoDePrueba("atrasado", hoy.minusDays(2).atTime(16, 0), EstadoPartido.EN_CURSO),
            partidoDePrueba("aplazado", hoy.minusDays(1).atTime(16, 0), EstadoPartido.APLAZADO),
        )

        val repartidos = partidos.repartir(hoy)

        assertEquals(listOf("atrasado", "aplazado", "futuro", "sin-fecha"), repartidos.proximos.map { it.id })
        assertTrue(repartidos.hoy.isEmpty())
        assertTrue(repartidos.jugados.isEmpty())
    }

    @Test
    fun `CA-2 sin partidos queda vacio`() {
        assertTrue(emptyList<PartidoArbitro>().repartir(hoy).vacio)
    }

    @Test
    fun `CA-3 un partido sin los dos equipos no tiene el cruce definido`() {
        assertTrue(partidoDePrueba().cruceDefinido)
        assertFalse(partidoDePrueba(conCruce = false).cruceDefinido)
        assertFalse(partidoDePrueba().copy(visitante = null).cruceDefinido)
    }

    @Test
    fun `CA-13 un estado que la app no conoce no falla`() {
        assertEquals(EstadoPartido.DESCONOCIDO, EstadoPartido.desde("SUSPENDIDO_POR_LLUVIA"))
        assertEquals(EstadoPartido.DESCONOCIDO, EstadoPartido.desde(null))
        assertEquals(EstadoPartido.WO, EstadoPartido.desde("WO"))
        assertFalse(EstadoPartido.DESCONOCIDO.cerrado)
        assertTrue(EstadoPartido.WO.cerrado)
    }

    @Test
    fun `CA-4 se ofrecen los aptos con los alineados primero y luego por camiseta`() {
        val equipo = EquipoPlanilla(
            "e1", "Halcones FC",
            listOf(
                jugadorDePrueba("sin-numero", "Zoe", numero = null),
                jugadorDePrueba("nueve", "Ana", numero = 9),
                jugadorDePrueba("suspendido", "Beto", numero = 1, apto = false),
                jugadorDePrueba("alineado-10", "Caro", numero = 10, alineado = true),
                jugadorDePrueba("alineado-2", "Dani", numero = 2, alineado = true),
            ),
        )

        assertEquals(listOf("alineado-2", "alineado-10", "nueve", "sin-numero"), equipo.candidatos().map { it.personaId })
        assertEquals(2, equipo.alineados)
    }
}
