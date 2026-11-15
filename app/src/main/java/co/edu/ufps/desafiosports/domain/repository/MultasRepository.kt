package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.Multa

/** Las multas de quien inició sesión (MOV-0004). */
interface MultasRepository {
    suspend fun misMultas(): Result<List<Multa>>
}
