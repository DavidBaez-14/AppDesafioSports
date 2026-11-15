package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.model.Perfil
import co.edu.ufps.desafiosports.domain.repository.PerfilRepository

/** Cifras, trayectoria y perfil deportivo de la persona (MOV-0004). */
class ObtenerMiPerfilUseCase(private val repositorio: PerfilRepository) {
    suspend operator fun invoke(): Result<Perfil> = repositorio.miPerfil()
}
