package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.repository.SesionRepository

/** Vuelve a enviar el correo de confirmación del registro (MOV-0003 · N-1). */
class ReenviarConfirmacionUseCase(private val sesion: SesionRepository) {

    // El correo viene de la pantalla anterior, ya validado: aquí solo se limpia.
    suspend operator fun invoke(correo: String): Result<Unit> = sesion.reenviarConfirmacion(correo.trim())
}
