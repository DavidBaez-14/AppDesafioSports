package co.edu.ufps.desafiosports.domain.use_case.sistema

import co.edu.ufps.desafiosports.domain.model.EstadoServidor
import co.edu.ufps.desafiosports.domain.repository.ServidorRepository

class ComprobarServidorUseCase(private val servidor: ServidorRepository) {
    suspend operator fun invoke(): Result<EstadoServidor> = servidor.comprobar()
}
