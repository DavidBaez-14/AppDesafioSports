package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.dto.MeResponseDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.Rol
import co.edu.ufps.desafiosports.domain.repository.CuentaRepository

class CuentaRepositoryImpl(
    private val api: AuthApi,
    private val errores: ErroresHttp,
) : CuentaRepository {

    override suspend fun obtenerMiCuenta(): Result<Cuenta> =
        llamarApi(errores) { api.me() }.map { it.aDominio() }
}

/** DTO → modelo. Los roles que la app no conoce se ignoran. */
internal fun MeResponseDto.aDominio(): Cuenta = Cuenta(
    id = cuentaId.orEmpty(),
    nombres = nombres.orEmpty(),
    apellidos = apellidos.orEmpty(),
    nombre = nombre ?: listOfNotNull(nombres, apellidos).joinToString(" "),
    correo = correo.orEmpty(),
    estado = estado.orEmpty(),
    roles = roles.aRoles(),
    rolesPendientes = rolesPendientes.aRoles(),
)

private fun List<String>.aRoles(): Set<Rol> =
    mapNotNull { nombre -> Rol.entries.firstOrNull { it.name == nombre } }.toSet()
