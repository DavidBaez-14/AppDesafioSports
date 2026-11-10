package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.testutil.deportesDePrueba
import co.edu.ufps.desafiosports.testutil.inscripcionDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/** MOV-0007 · CA-1: las reglas de cada paso de "Crear torneo", iguales a las del backend. */
class BorradorTorneoTest {

    private val hoy = LocalDate.of(2026, 11, 9)
    private val futsal = deportesDePrueba().first()

    private val completo = BorradorTorneo(
        deporte = futsal,
        nombre = "Relámpago de Fútbol sala",
        formato = FormatoTorneo.LIGA,
        cupos = 8,
        fechaInicio = hoy.plusDays(7),
        visibilidad = Visibilidad.PUBLICO,
    )

    @Test
    fun `CA-1 el paso 1 pide el deporte`() {
        assertFalse(BorradorTorneo().pasoCompleto(1, hoy))
        assertTrue(BorradorTorneo(deporte = futsal).pasoCompleto(1, hoy))
    }

    @Test
    fun `CA-1 el paso 2 pide nombre de 3 a 150, formato, cupos en rango y fecha de hoy en adelante`() {
        assertTrue(completo.pasoCompleto(2, hoy))
        assertFalse(completo.copy(nombre = "ab").pasoCompleto(2, hoy))
        assertFalse("los espacios no cuentan", completo.copy(nombre = "  a  ").pasoCompleto(2, hoy))
        assertTrue(completo.copy(nombre = "a".repeat(150)).pasoCompleto(2, hoy))
        assertFalse(completo.copy(nombre = "a".repeat(151)).pasoCompleto(2, hoy))
        assertFalse(completo.copy(formato = null).pasoCompleto(2, hoy))
        assertFalse(completo.copy(fechaInicio = null).pasoCompleto(2, hoy))
        assertFalse(completo.copy(fechaInicio = hoy.minusDays(1)).pasoCompleto(2, hoy))
        assertTrue("hoy mismo sí se acepta", completo.copy(fechaInicio = hoy).pasoCompleto(2, hoy))
    }

    @Test
    fun `CA-1 los cupos dependen del formato`() {
        assertTrue(completo.copy(formato = FormatoTorneo.LIGA, cupos = 2).pasoCompleto(2, hoy))
        assertTrue(completo.copy(formato = FormatoTorneo.LIGA, cupos = 64).pasoCompleto(2, hoy))
        assertFalse(completo.copy(formato = FormatoTorneo.LIGA, cupos = 65).pasoCompleto(2, hoy))
        assertFalse(completo.copy(formato = FormatoTorneo.ELIMINACION_DIRECTA, cupos = 17).pasoCompleto(2, hoy))
        assertTrue(completo.copy(formato = FormatoTorneo.ELIMINACION_DIRECTA, cupos = 16).pasoCompleto(2, hoy))
        assertFalse(completo.copy(formato = FormatoTorneo.GRUPOS_ELIMINATORIAS, cupos = 3).pasoCompleto(2, hoy))
        assertTrue(completo.copy(formato = FormatoTorneo.GRUPOS_ELIMINATORIAS, cupos = 4).pasoCompleto(2, hoy))
    }

    @Test
    fun `CA-1 cambiar de formato ajusta los cupos a su rango`() {
        val conMuchos = completo.copy(cupos = 40)

        assertEquals(16, conMuchos.conFormato(FormatoTorneo.ELIMINACION_DIRECTA).cupos)
        assertEquals(40, conMuchos.conFormato(FormatoTorneo.GRUPOS_ELIMINATORIAS).cupos)
        assertEquals(4, completo.copy(cupos = 2).conFormato(FormatoTorneo.GRUPOS_ELIMINATORIAS).cupos)
    }

    @Test
    fun `CA-1 los cupos no salen del rango al sumar o restar`() {
        val eliminacion = completo.conFormato(FormatoTorneo.ELIMINACION_DIRECTA)

        assertEquals(16, eliminacion.conCupos(99).cupos)
        assertEquals(2, eliminacion.conCupos(0).cupos)
        assertEquals(2, BorradorTorneo().conCupos(-5).cupos)
        assertEquals(64, BorradorTorneo().conCupos(500).cupos)
    }

    @Test
    fun `CA-1 el paso 3 pide visibilidad y, si cobra, un valor mayor que cero`() {
        assertTrue(completo.pasoCompleto(3, hoy))
        assertFalse(completo.copy(visibilidad = null).pasoCompleto(3, hoy))
        assertFalse(completo.copy(cobraInscripcion = true).pasoCompleto(3, hoy))
        assertFalse(completo.copy(cobraInscripcion = true, valorInscripcion = 0).pasoCompleto(3, hoy))
        assertTrue(completo.copy(cobraInscripcion = true, valorInscripcion = 5_000).pasoCompleto(3, hoy))
    }

    @Test
    fun `CA-2 el borrador completo se convierte en el torneo a enviar, con el nombre limpio`() {
        val torneo = completo.copy(nombre = "  Relámpago  ", cobraInscripcion = true, valorInscripcion = 5_000).aNuevoTorneo(hoy)!!

        assertEquals("Relámpago", torneo.nombre)
        assertEquals("FUTSAL", torneo.deporte)
        assertEquals(FormatoTorneo.LIGA, torneo.formato)
        assertEquals(8, torneo.cupos)
        assertEquals(hoy.plusDays(7), torneo.fechaInicio)
        assertEquals(Visibilidad.PUBLICO, torneo.visibilidad)
        assertEquals(5_000L, torneo.valorInscripcion)
    }

    @Test
    fun `CA-2 si no cobra, el valor que quedo escrito no viaja`() {
        val torneo = completo.copy(cobraInscripcion = false, valorInscripcion = 5_000).aNuevoTorneo(hoy)!!

        assertFalse(torneo.cobraInscripcion)
        assertNull(torneo.valorInscripcion)
    }

    @Test
    fun `CA-2 un borrador incompleto no se convierte`() {
        assertNull(BorradorTorneo().aNuevoTorneo(hoy))
        assertNull(completo.copy(visibilidad = null).aNuevoTorneo(hoy))
        assertNull(completo.copy(fechaInicio = hoy.minusDays(1)).aNuevoTorneo(hoy))
    }

    @Test
    fun `CA-5 hay datos que perder en cuanto se elige o se escribe algo`() {
        assertFalse(BorradorTorneo(fechaInicio = hoy, visibilidad = Visibilidad.PUBLICO).tieneDatos)
        assertTrue(BorradorTorneo(deporte = futsal).tieneDatos)
        assertTrue(BorradorTorneo(nombre = "R").tieneDatos)
    }

    @Test
    fun `MOV-0008 CA-2 las inscripciones se reparten y las pendientes mas antiguas van primero`() {
        val inscripciones = listOf(
            inscripcionDePrueba("nueva", fecha = LocalDateTime.of(2026, 11, 8, 9, 0)),
            inscripcionDePrueba("zeta", "Zeta FC", EstadoInscripcion.APROBADO),
            inscripcionDePrueba("vieja", fecha = LocalDateTime.of(2026, 11, 1, 9, 0)),
            inscripcionDePrueba("rechazada", estado = EstadoInscripcion.RECHAZADO),
            inscripcionDePrueba("alfa", "alfa FC", EstadoInscripcion.APROBADO),
            inscripcionDePrueba("expulsada", estado = EstadoInscripcion.EXPULSADO),
            inscripcionDePrueba("rara", estado = EstadoInscripcion.DESCONOCIDO, fecha = null),
        )

        val repartidas = inscripciones.repartir()

        assertEquals(listOf("vieja", "nueva", "rara"), repartidas.porAprobar.map { it.id })
        assertEquals(listOf("alfa", "zeta"), repartidas.aprobadas.map { it.id })
        assertEquals(setOf("rechazada", "expulsada"), repartidas.rechazadas.map { it.id }.toSet())
        assertFalse(repartidas.vacio)
        assertTrue(emptyList<Inscripcion>().repartir().vacio)
    }
}
