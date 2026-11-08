package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.repository.CuentaRepository

/**
 * Pide la cuenta a `GET /api/auth/me`.
 *
 * Errores esperados: [co.edu.ufps.desafiosports.domain.error.AppError.CuentaNoRegistrada] (404),
 * [co.edu.ufps.desafiosports.domain.error.AppError.CuentaBloqueada] (403) y
 * [co.edu.ufps.desafiosports.domain.error.AppError.NoAutenticado] (401).
 */
class ObtenerCuentaUseCase(private val cuentas: CuentaRepository) {
    suspend operator fun invoke(): Result<Cuenta> = cuentas.obtenerMiCuenta()
}
