package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.EstadoServidor

/** Salud del backend (`/actuator/health`, público). */
interface ServidorRepository {
    suspend fun comprobar(): Result<EstadoServidor>
}
