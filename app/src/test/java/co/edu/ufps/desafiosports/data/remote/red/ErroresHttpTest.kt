package co.edu.ufps.desafiosports.data.remote.red

import co.edu.ufps.desafiosports.domain.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0001 · CA-10: los errores del backend se leen por `code` y se muestran con su `mensaje`. */
class ErroresHttpTest {

    private val errores = ErroresHttp(ClienteHttp.json)

    private fun problema(status: Int, code: String, mensaje: String) =
        """{"type":"about:blank","title":"x","status":$status,"detail":"$mensaje","code":"$code","mensaje":"$mensaje"}"""

    @Test
    fun `CA-10 404 CUENTA_NO_REGISTRADA usa el mensaje del backend`() {
        val error = errores.desde(404, problema(404, "CUENTA_NO_REGISTRADA", "Completa tu registro para continuar."))

        assertTrue(error is AppError.CuentaNoRegistrada)
        assertEquals("Completa tu registro para continuar.", error.mensaje)
    }

    @Test
    fun `CA-10 403 CUENTA_BLOQUEADA se distingue de ACCESO_DENEGADO`() {
        assertTrue(errores.desde(403, problema(403, "CUENTA_BLOQUEADA", "Bloqueada.")) is AppError.CuentaBloqueada)
        assertTrue(errores.desde(403, problema(403, "ACCESO_DENEGADO", "Sin permiso.")) is AppError.AccesoDenegado)
    }

    @Test
    fun `CA-10 un code de negocio desconocido conserva su code y su mensaje`() {
        val error = errores.desde(409, problema(409, "DOCUMENTO_REGISTRADO", "Ese documento ya tiene cuenta."))

        assertTrue(error is AppError.Negocio)
        error as AppError.Negocio
        assertEquals("DOCUMENTO_REGISTRADO", error.codigo)
        assertEquals("Ese documento ya tiene cuenta.", error.mensaje)
    }

    @Test
    fun `CA-10 sin cuerpo JSON decide por el status`() {
        assertTrue(errores.desde(401, null) is AppError.NoAutenticado)
        assertTrue(errores.desde(502, "<html>Bad gateway</html>") is AppError.Desconocido)
    }

    @Test
    fun `codigo lee solo el code del problema`() {
        assertEquals("CUENTA_BLOQUEADA", errores.codigo(problema(403, "CUENTA_BLOQUEADA", "x")))
        assertEquals(null, errores.codigo("no es json"))
    }
}
