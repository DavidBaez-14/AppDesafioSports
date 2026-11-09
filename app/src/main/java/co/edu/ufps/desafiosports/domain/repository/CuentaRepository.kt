package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.DatosRegistro

/** Identidad en BackendCodeCup (`/api/auth/...`). */
interface CuentaRepository {
    suspend fun obtenerMiCuenta(): Result<Cuenta>

    /** `POST /api/auth/registro`: completa el registro tras confirmar el correo (MOV-0003). */
    suspend fun registrar(datos: DatosRegistro): Result<Cuenta>
}
