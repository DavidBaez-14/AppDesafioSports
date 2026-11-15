package co.edu.ufps.desafiosports.domain.use_case.finanzas

import co.edu.ufps.desafiosports.domain.model.Multa
import co.edu.ufps.desafiosports.domain.model.ordenadas
import co.edu.ufps.desafiosports.domain.repository.MultasRepository

/** Las multas de la persona, con las que siguen abiertas primero (MOV-0004 · CA-7). */
class ObtenerMisMultasUseCase(private val repositorio: MultasRepository) {
    suspend operator fun invoke(): Result<List<Multa>> = repositorio.misMultas().map { it.ordenadas() }
}
