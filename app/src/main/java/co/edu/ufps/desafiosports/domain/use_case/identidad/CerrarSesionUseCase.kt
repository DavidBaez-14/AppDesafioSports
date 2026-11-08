package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.repository.SesionRepository

class CerrarSesionUseCase(private val sesion: SesionRepository) {
    suspend operator fun invoke(): Result<Unit> = sesion.cerrarSesion()
}
