package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.testutil.cuentaDePrueba
import co.edu.ufps.desafiosports.testutil.equipoDePrueba
import co.edu.ufps.desafiosports.testutil.multaDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

/** MOV-0004 · CA-4, CA-7 y CA-12: las reglas de Inicio y Perfil que no dependen de la pantalla. */
class MisEquiposTest {

    private val base = LocalDateTime.of(2026, 11, 14, 16, 0)

    @Test
    fun `CA-4 el proximo partido es el mas cercano`() {
        val equipos = listOf(
            equipoDePrueba("lejos", partido = base.plusDays(5)),
            equipoDePrueba("cerca", equipo = "Las Águilas", partido = base.plusDays(1), rival = "Pumas FC", local = false),
            equipoDePrueba("sin-partido"),
        )

        val proximo = equipos.proximoPartido()!!

        assertEquals("cerca", proximo.equipo.equipoTorneoId)
        assertEquals("Pumas FC", proximo.partido.rival)
        assertEquals(false, proximo.partido.local)
        assertEquals(base.plusDays(1), proximo.fechaHora)
    }

    @Test
    fun `CA-4 sin fechas no hay proximo partido`() {
        val sinFecha = equipoDePrueba("a").copy(proximoPartido = ProximoPartido("p", "Pumas FC", true, null, null))

        assertNull(listOf(sinFecha, equipoDePrueba("b")).proximoPartido())
        assertNull(emptyList<MiEquipo>().proximoPartido())
    }

    @Test
    fun `CA-4 un partido con fecha gana a uno sin fecha`() {
        val sinFecha = equipoDePrueba("a").copy(proximoPartido = ProximoPartido("p", null, true, null, null))

        assertEquals("b", listOf(sinFecha, equipoDePrueba("b", partido = base)).proximoPartido()!!.equipo.equipoTorneoId)
    }

    @Test
    fun `CA-7 las multas abiertas van primero y dentro de cada grupo la mas reciente arriba`() {
        val multas = listOf(
            multaDePrueba("pagada-vieja", EstadoMulta.PAGADA, base.minusDays(30)),
            multaDePrueba("pendiente-vieja", EstadoMulta.PENDIENTE, base.minusDays(10)),
            multaDePrueba("condonada-nueva", EstadoMulta.CONDONADA, base.minusDays(1)),
            multaDePrueba("revision-nueva", EstadoMulta.EN_REVISION, base.minusDays(2)),
            multaDePrueba("sin-fecha", EstadoMulta.PENDIENTE, null),
        )

        assertEquals(
            listOf("revision-nueva", "pendiente-vieja", "sin-fecha", "condonada-nueva", "pagada-vieja"),
            multas.ordenadas().map { it.id },
        )
    }

    @Test
    fun `CA-7 el nombre publico es el nombre y la inicial del apellido`() {
        assertEquals("Carlos R.", cuentaDePrueba().copy(nombres = "Carlos", apellidos = "Ramírez").nombrePublico())
        assertEquals("Ana María P.", cuentaDePrueba().copy(nombres = " Ana María ", apellidos = "pérez gómez").nombrePublico())
        // Sin apellidos no se inventa una inicial.
        assertEquals("Carlos", cuentaDePrueba().copy(nombres = "Carlos", apellidos = "").nombrePublico())
    }

    @Test
    fun `CA-12 un rol o un estado de multa desconocido no falla`() {
        assertEquals(RolEnEquipo.JUGADOR, RolEnEquipo.desde("CAPITAN"))
        assertEquals(RolEnEquipo.JUGADOR, RolEnEquipo.desde(null))
        assertEquals(EstadoMulta.DESCONOCIDO, EstadoMulta.desde("EN_COBRO"))
        assertNull(Puesto.desde("CUARTO"))
    }
}
