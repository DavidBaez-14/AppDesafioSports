package co.edu.ufps.desafiosports.presentacion.comun

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** MOV-0010 · CA-8: la barra de cupos de "Mis torneos". */
class LlenadoDeCuposTest {

    @Test
    fun `CA-8 la barra es proporcional a los inscritos`() {
        assertEquals(0.75f, llenadoDeCupos(inscritos = 12, cupos = 16)!!, 0.0001f)
        assertEquals(0f, llenadoDeCupos(inscritos = 0, cupos = 8)!!, 0.0001f)
    }

    @Test
    fun `CA-8 la barra no pasa de lleno`() {
        assertEquals(1f, llenadoDeCupos(inscritos = 20, cupos = 16)!!, 0.0001f)
    }

    @Test
    fun `CA-8 sin cupos no hay barra`() {
        assertNull(llenadoDeCupos(inscritos = 5, cupos = null))
        assertNull(llenadoDeCupos(inscritos = 5, cupos = 0))
    }

    @Test
    fun `la inicial de un equipo ignora espacios y simbolos`() {
        assertEquals("H", inicialDe("  halcones FC"))
        assertEquals("R", inicialDe("¡Real Código!"))
        assertEquals("?", inicialDe("   "))
    }
}
