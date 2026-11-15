package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.MiEquipo

/** Los equipos en los que juega o delega quien inició sesión (MOV-0004). */
interface MisEquiposRepository {
    suspend fun misEquipos(): Result<List<MiEquipo>>
}
