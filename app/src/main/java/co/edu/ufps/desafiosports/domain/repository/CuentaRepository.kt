package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.Cuenta

/** Identidad en BackendCodeCup (`/api/auth/...`). */
interface CuentaRepository {
    suspend fun obtenerMiCuenta(): Result<Cuenta>
}
