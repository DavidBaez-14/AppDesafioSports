package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.testutil.detalleDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** MOV-0005: el enlace de un torneo y la franja de acciones del detalle. */
class TorneoDetalleTest {

    private val relacion = RelacionConEquipo("et1", "Halcones FC", EstadoInscripcion.PENDIENTE_PAGO, null)

    @Test
    fun `CA-3 el token sale del enlace completo o del token suelto`() {
        assertEquals("0e46a7c3324240e2868e5780fb7a6ba5", tokenDeEnlace("http://localhost:5173/t/0e46a7c3324240e2868e5780fb7a6ba5"))
        assertEquals("0e46a7c3324240e2868e5780fb7a6ba5", tokenDeEnlace("  Inscríbete: https://desafio.sports/t/0e46a7c3324240e2868e5780fb7a6ba5 "))
        assertEquals("0e46a7c3324240e2868e5780fb7a6ba5", tokenDeEnlace("0e46a7c3324240e2868e5780fb7a6ba5"))
    }

    @Test
    fun `CA-3 lo que no parece un enlace de torneo no da token`() {
        assertNull(tokenDeEnlace(""))
        assertNull(tokenDeEnlace("hola"))
        assertNull(tokenDeEnlace("https://google.com/busqueda"))
    }

    @Test
    fun `CA-4 sin sesion invita a entrar solo si las inscripciones estan abiertas`() {
        assertEquals(AccionDelTorneo.EntrarParaInscribirse, detalleDePrueba(abiertas = true).accion(conSesion = false))
        assertEquals(AccionDelTorneo.Cerradas, detalleDePrueba(abiertas = false).accion(conSesion = false))
    }

    @Test
    fun `CA-4 lo que la persona ya es pesa mas que lo que podria hacer`() {
        assertEquals(AccionDelTorneo.Administra, detalleDePrueba(relacion = MiRelacion(true, true, relacion, null)).accion(true))
        assertEquals(AccionDelTorneo.Delegado(relacion), detalleDePrueba(relacion = MiRelacion(false, true, relacion, null)).accion(true))
        assertEquals(AccionDelTorneo.Juega(relacion), detalleDePrueba(relacion = MiRelacion(false, true, null, relacion)).accion(true))
        assertEquals(AccionDelTorneo.Arbitra, detalleDePrueba(relacion = MiRelacion(false, true, null, null)).accion(true))
    }

    @Test
    fun `CA-4 sin relacion depende de las inscripciones y del deporte`() {
        val sinRelacion = MiRelacion(false, false, null, null)
        assertEquals(AccionDelTorneo.Inscribirse, detalleDePrueba(relacion = sinRelacion).accion(true))
        assertEquals(AccionDelTorneo.Cerradas, detalleDePrueba(abiertas = false, relacion = sinRelacion).accion(true))
        assertEquals(
            AccionDelTorneo.IndividualPendiente,
            detalleDePrueba(relacion = sinRelacion, tipo = TipoCompetencia.INDIVIDUAL).accion(true),
        )
    }
}
