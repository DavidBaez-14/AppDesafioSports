package co.edu.ufps.desafiosports.presentacion.arbitro

import co.edu.ufps.desafiosports.domain.model.Lado
import co.edu.ufps.desafiosports.testutil.eventoDePrueba
import co.edu.ufps.desafiosports.testutil.planillaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Test

/** MOV-0010 · CA-7: cada evento va del lado de su equipo en la línea de la planilla. */
class LineaDeEventosTest {

    private val planilla = planillaDePrueba()

    @Test
    fun `CA-7 cada evento queda del lado de su equipo`() {
        val delLocal = eventoDePrueba("ev1").copy(equipoTorneoId = "eq-local", equipoNombre = "Halcones FC")
        val delVisitante = eventoDePrueba("ev2").copy(equipoTorneoId = "eq-visita", equipoNombre = "Tiburones FC")

        assertEquals(Lado.LOCAL, ladoDelEvento(delLocal, planilla))
        assertEquals(Lado.VISITANTE, ladoDelEvento(delVisitante, planilla))
    }

    @Test
    fun `CA-7 si el id no coincide se reconoce por el nombre del equipo`() {
        val evento = eventoDePrueba("ev3").copy(equipoTorneoId = "otro-id", equipoNombre = "Tiburones FC")

        assertEquals(Lado.VISITANTE, ladoDelEvento(evento, planilla))
    }

    @Test
    fun `CA-7 un evento de un equipo desconocido no se pierde y va al local`() {
        val evento = eventoDePrueba("ev4").copy(equipoTorneoId = "otro-id", equipoNombre = "Otro")

        assertEquals(Lado.LOCAL, ladoDelEvento(evento, planilla))
    }
}
