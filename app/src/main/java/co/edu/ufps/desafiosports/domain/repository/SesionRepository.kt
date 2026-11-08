package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import kotlinx.coroutines.flow.Flow

/** Sesión de Supabase Auth. La implementación vive en data/repository. */
interface SesionRepository {
    val sesion: Flow<EstadoSesion>

    suspend fun iniciarSesion(correo: String, clave: String): Result<Unit>

    suspend fun cerrarSesion(): Result<Unit>
}
