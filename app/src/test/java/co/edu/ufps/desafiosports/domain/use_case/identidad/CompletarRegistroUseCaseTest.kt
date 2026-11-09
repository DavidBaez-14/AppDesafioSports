package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.model.DatosRegistro
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.testutil.FakeCuentaRepository
import co.edu.ufps.desafiosports.testutil.cuentaDePrueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** MOV-0003 · CA-8 y CA-9: completar el registro, validando antes de llamar al backend. */
class CompletarRegistroUseCaseTest {

    private val cuentas = FakeCuentaRepository()
    private val completar = CompletarRegistroUseCase(cuentas)

    private suspend fun campoDelError(datos: DatosRegistro): CampoValidado? =
        (completar(datos).exceptionOrNull() as AppError.Validacion).campo

    @Test
    fun `CA-8 nombres vacios fallan en el campo nombres sin llamar al backend`() = runTest {
        assertEquals(CampoValidado.NOMBRES, campoDelError(DatosRegistro("   ", "Peña")))
        assertTrue(cuentas.registros.isEmpty())
    }

    @Test
    fun `CA-8 apellidos vacios fallan en el campo apellidos`() = runTest {
        assertEquals(CampoValidado.APELLIDOS, campoDelError(DatosRegistro("Andrés", "")))
    }

    @Test
    fun `CA-8 tipo de documento sin numero falla en el campo documento`() = runTest {
        assertEquals(CampoValidado.DOCUMENTO, campoDelError(DatosRegistro("Andrés", "Peña", TipoDocumento.CC, " ")))
        assertTrue(cuentas.registros.isEmpty())
    }

    @Test
    fun `CA-8 numero sin tipo de documento falla en el campo documento`() = runTest {
        assertEquals(CampoValidado.DOCUMENTO, campoDelError(DatosRegistro("Andrés", "Peña", null, "1090123456")))
    }

    @Test
    fun `CA-8 numero con caracteres no validos falla en el campo documento`() = runTest {
        assertEquals(CampoValidado.DOCUMENTO, campoDelError(DatosRegistro("Andrés", "Peña", TipoDocumento.CC, "1090-123")))
    }

    @Test
    fun `CA-9 datos validos se envian sin espacios sobrantes`() = runTest {
        val resultado = completar(DatosRegistro("  Andrés ", " Peña Ruiz ", TipoDocumento.CC, " 1.090.123.456 "))

        assertTrue(resultado.isSuccess)
        assertEquals(listOf(DatosRegistro("Andrés", "Peña Ruiz", TipoDocumento.CC, "1.090.123.456")), cuentas.registros)
    }

    @Test
    fun `CA-9 sin documento se envia sin tipo ni numero`() = runTest {
        completar(DatosRegistro("Andrés", "Peña", null, "  "))

        assertEquals(listOf(DatosRegistro("Andrés", "Peña", null, null)), cuentas.registros)
    }

    @Test
    fun `CA-9 si la cuenta ya estaba registrada devuelve la cuenta igual`() = runTest {
        cuentas.resultadoRegistro = Result.failure(AppError.Negocio("CUENTA_YA_REGISTRADA", "Esta cuenta ya completó su registro."))

        val resultado = completar(DatosRegistro("Andrés", "Peña"))

        assertEquals(cuentaDePrueba(), resultado.getOrNull())
    }
}
