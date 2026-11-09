package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.DatosRegistro
import co.edu.ufps.desafiosports.domain.repository.CuentaRepository

/**
 * Completa el registro en BackendCodeCup (MOV-0003 · N-2), con las mismas reglas del backend:
 * nombres y apellidos obligatorios (hasta 100), y documento completo (tipo y número) o ninguno,
 * con solo letras, números, puntos y espacios (hasta 20).
 *
 * Si la cuenta ya estaba registrada (por ejemplo, la completó en la web), no es un error:
 * devuelve la cuenta para seguir al inicio.
 */
class CompletarRegistroUseCase(private val cuentas: CuentaRepository) {

    suspend operator fun invoke(datos: DatosRegistro): Result<Cuenta> {
        val nombres = datos.nombres.trim()
        val apellidos = datos.apellidos.trim()
        val numero = datos.numeroDocumento?.trim()?.takeIf { it.isNotEmpty() }
        val tipo = datos.tipoDocumento

        val error = when {
            nombres.isEmpty() -> AppError.Validacion("Escribe tus nombres.", CampoValidado.NOMBRES)
            nombres.length > LONGITUD_MAXIMA_NOMBRE -> AppError.Validacion("Máximo $LONGITUD_MAXIMA_NOMBRE caracteres.", CampoValidado.NOMBRES)
            apellidos.isEmpty() -> AppError.Validacion("Escribe tus apellidos.", CampoValidado.APELLIDOS)
            apellidos.length > LONGITUD_MAXIMA_NOMBRE -> AppError.Validacion("Máximo $LONGITUD_MAXIMA_NOMBRE caracteres.", CampoValidado.APELLIDOS)
            tipo != null && numero == null -> AppError.Validacion("Escribe el número del documento o quita el tipo.", CampoValidado.DOCUMENTO)
            tipo == null && numero != null -> AppError.Validacion("Elige el tipo de documento.", CampoValidado.DOCUMENTO)
            numero != null && (numero.length > LONGITUD_MAXIMA_DOCUMENTO || !PATRON_DOCUMENTO.matches(numero)) ->
                AppError.Validacion("Usa solo letras, números, puntos y espacios (máximo $LONGITUD_MAXIMA_DOCUMENTO).", CampoValidado.DOCUMENTO)
            else -> null
        }
        if (error != null) return Result.failure(error)

        val resultado = cuentas.registrar(DatosRegistro(nombres, apellidos, tipo, numero))
        val fallo = resultado.exceptionOrNull()
        return if (fallo is AppError.Negocio && fallo.codigo == CUENTA_YA_REGISTRADA) cuentas.obtenerMiCuenta() else resultado
    }

    companion object {
        const val LONGITUD_MAXIMA_NOMBRE = 100
        const val LONGITUD_MAXIMA_DOCUMENTO = 20
        private const val CUENTA_YA_REGISTRADA = "CUENTA_YA_REGISTRADA"
        private val PATRON_DOCUMENTO = Regex("^[A-Za-z0-9. ]+$")
    }
}
