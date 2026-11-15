package co.edu.ufps.desafiosports.domain.model

import co.edu.ufps.desafiosports.testutil.equipoDePrueba
import org.junit.Assert.assertEquals
import org.junit.Test

/** MOV-0004 · CA-5: el chip de "Mis equipos", un caso por fila de la tabla de §3.3. */
class EstadoTramiteTest {

    private fun tramite(estado: EstadoInscripcion, rol: RolEnEquipo, comprobante: String? = null) =
        equipoDePrueba(rol = rol, estado = estado, comprobante = comprobante).tramite

    @Test
    fun `CA-5 aprobado no muestra chip, juegue o delegue`() {
        assertEquals(EstadoTramite.AL_DIA, tramite(EstadoInscripcion.APROBADO, RolEnEquipo.JUGADOR))
        assertEquals(EstadoTramite.AL_DIA, tramite(EstadoInscripcion.APROBADO, RolEnEquipo.DELEGADO, "APROBADO"))
    }

    @Test
    fun `CA-5 delegado pendiente sin comprobante ve Falta comprobante`() {
        assertEquals(EstadoTramite.FALTA_COMPROBANTE, tramite(EstadoInscripcion.PENDIENTE_PAGO, RolEnEquipo.DELEGADO))
    }

    @Test
    fun `CA-5 delegado pendiente con comprobante por revisar ve En revision`() {
        assertEquals(EstadoTramite.EN_REVISION, tramite(EstadoInscripcion.PENDIENTE_PAGO, RolEnEquipo.DELEGADO, "PENDIENTE_REVISION"))
    }

    @Test
    fun `CA-5 delegado pendiente con comprobante rechazado ve Comprobante rechazado`() {
        assertEquals(EstadoTramite.COMPROBANTE_RECHAZADO, tramite(EstadoInscripcion.PENDIENTE_PAGO, RolEnEquipo.DELEGADO, "RECHAZADO"))
    }

    @Test
    fun `CA-5 jugador de un equipo pendiente ve Inscripcion pendiente`() {
        assertEquals(EstadoTramite.INSCRIPCION_PENDIENTE, tramite(EstadoInscripcion.PENDIENTE_PAGO, RolEnEquipo.JUGADOR))
    }

    @Test
    fun `CA-5 rechazado y expulsado se ven igual para los dos roles`() {
        RolEnEquipo.entries.forEach { rol ->
            assertEquals(EstadoTramite.RECHAZADA, tramite(EstadoInscripcion.RECHAZADO, rol))
            assertEquals(EstadoTramite.EXPULSADO, tramite(EstadoInscripcion.EXPULSADO, rol))
        }
    }

    @Test
    fun `CA-12 un estado de inscripcion desconocido no muestra chip`() {
        assertEquals(EstadoTramite.AL_DIA, tramite(EstadoInscripcion.DESCONOCIDO, RolEnEquipo.DELEGADO))
    }
}
