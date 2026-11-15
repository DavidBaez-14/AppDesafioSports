package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.Perfil

/** El perfil de jugador de quien inició sesión (MOV-0004). */
interface PerfilRepository {
    suspend fun miPerfil(): Result<Perfil>
}
