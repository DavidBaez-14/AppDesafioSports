package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.InvitacionRecibida

/** Invitaciones que recibe la persona (`/api/auth/invitaciones/…`, MOV-0011). */
interface InvitacionesRepository {
    suspend fun ver(token: String): Result<InvitacionRecibida>
    suspend fun aceptar(token: String): Result<Unit>
}
