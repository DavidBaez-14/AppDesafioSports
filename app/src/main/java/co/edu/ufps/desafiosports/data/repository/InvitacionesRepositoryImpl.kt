package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.AuthApi
import co.edu.ufps.desafiosports.data.remote.dto.InvitacionVistaDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.InvitacionRecibida
import co.edu.ufps.desafiosports.domain.model.RolInvitado
import co.edu.ufps.desafiosports.domain.repository.InvitacionesRepository

class InvitacionesRepositoryImpl(
    private val api: AuthApi,
    private val errores: ErroresHttp,
) : InvitacionesRepository {

    override suspend fun ver(token: String): Result<InvitacionRecibida> =
        llamarApi(errores) { api.verInvitacion(token) }.map { it.aDominio(token) }

    override suspend fun aceptar(token: String): Result<Unit> =
        llamarApi(errores) { api.aceptarInvitacion(token) }
}

internal fun InvitacionVistaDto.aDominio(token: String): InvitacionRecibida = InvitacionRecibida(
    token = token,
    rol = RolInvitado.desde(rol),
    torneo = ambitoDescripcion?.takeIf { ambito == "TORNEO" && it.isNotBlank() },
    invitadoPor = invitadoPor?.takeIf { it.isNotBlank() },
    paraCorreo = paraCorreo?.takeIf { it.isNotBlank() },
    pendiente = estado == "PENDIENTE",
)
