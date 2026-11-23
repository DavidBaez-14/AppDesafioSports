package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.testutil.miembro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0015: cómo se lee el plantel. */
class PlantelTest {

    private val plantel = Plantel(
        listOf(
            miembro("p1", "Zoe Ruiz", null),
            miembro("p2", "andrés Gómez", 10, delegado = true),
            miembro("p3", "Mateo Jiménez", 7, activo = false),
            miembro("p4", "Juan Castro", 1),
            miembro("p5", "Ana Pérez", null),
            miembro("p6", "Bruno Díaz", 3, activo = false),
        ),
    )

    @Test
    fun `CA-2 los activos van por numero, los que no tienen al final y por nombre, y los retirados aparte`() {
        assertEquals(listOf("p4", "p2", "p5", "p1"), plantel.activos.map { it.personaId })
        assertEquals(listOf("p6", "p3"), plantel.retirados.map { it.personaId })
    }

    @Test
    fun `CA-4 un miembro nuevo se suma y uno que ya estaba se reemplaza en su lugar`() {
        val conNuevo = plantel.con(miembro("p7", "Sofía Moreno", 5))
        val reactivado = plantel.con(miembro("p3", "Mateo Jiménez", 7))

        assertEquals(7, conNuevo.miembros.size)
        assertEquals("p7", conNuevo.miembros.last().personaId)
        assertEquals(6, reactivado.miembros.size)
        assertTrue(reactivado.miembros.first { it.personaId == "p3" }.activo)
        assertEquals(listOf("p4", "p3", "p2", "p5", "p1"), reactivado.activos.map { it.personaId })
    }

    @Test
    fun `CA-6 el dueno de un numero es un activo distinto del que se cambia`() {
        assertEquals("p2", plantel.duenoDelNumero(10)?.personaId)
        assertNull(plantel.duenoDelNumero(10, salvo = "p2"))
        // El 7 lo tenía un retirado: queda libre.
        assertNull(plantel.duenoDelNumero(7))
    }

    @Test
    fun `CA-8 con el torneo terminado el plantel no se puede cambiar`() {
        assertTrue(plantel.editable)
        assertFalse(Plantel(listOf(miembro("p1", "Zoe Ruiz", 1, torneoFinalizado = true))).editable)
    }
}
