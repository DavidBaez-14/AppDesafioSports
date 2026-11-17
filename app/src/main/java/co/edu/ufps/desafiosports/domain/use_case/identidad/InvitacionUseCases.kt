package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.InvitacionRecibida
import co.edu.ufps.desafiosports.domain.model.tokenDeInvitacion
import co.edu.ufps.desafiosports.domain.repository.InvitacionesRepository

// Aceptar una invitación recibida (MOV-0011).

/** Busca la invitación del enlace o código pegado. Lo que no parece una invitación no viaja. */
class VerInvitacionUseCase(private val repositorio: InvitacionesRepository) {
    suspend operator fun invoke(texto: String): Result<InvitacionRecibida> {
        val token = tokenDeInvitacion(texto) ?: return Result.failure(AppError.Validacion(MENSAJE_ENLACE))
        return repositorio.ver(token)
    }

    companion object {
        const val MENSAJE_ENLACE = "Eso no parece una invitación. Pega el enlace completo que te enviaron."
    }
}

class AceptarInvitacionUseCase(private val repositorio: InvitacionesRepository) {
    suspend operator fun invoke(token: String): Result<Unit> = repositorio.aceptar(token)
}

/**
 * Las invitaciones que le hicieron a la persona por su correo y aún no acepta (MOV-0012). Así no
 * depende de que alguien le pase el enlace.
 */
class ObtenerMisInvitacionesUseCase(private val repositorio: InvitacionesRepository) {
    suspend operator fun invoke(): Result<List<InvitacionRecibida>> = repositorio.mias()
}
