package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.model.MiEquipo
import co.edu.ufps.desafiosports.domain.repository.MisEquiposRepository

/** Los equipos de la persona, con el trámite y el próximo partido de cada uno (MOV-0004). */
class ObtenerMisEquiposUseCase(private val repositorio: MisEquiposRepository) {
    suspend operator fun invoke(): Result<List<MiEquipo>> = repositorio.misEquipos()
}
