package co.edu.ufps.desafiosports.domain.use_case.identidad

import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.repository.SesionRepository
import kotlinx.coroutines.flow.Flow

class ObservarSesionUseCase(private val sesion: SesionRepository) {
    operator fun invoke(): Flow<EstadoSesion> = sesion.sesion
}
