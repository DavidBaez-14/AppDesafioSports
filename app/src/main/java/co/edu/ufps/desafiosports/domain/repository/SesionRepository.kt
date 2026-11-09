package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.model.ResultadoRegistro
import kotlinx.coroutines.flow.Flow

/** Sesión de Supabase Auth. La implementación vive en data/repository. */
interface SesionRepository {
    val sesion: Flow<EstadoSesion>

    suspend fun iniciarSesion(correo: String, clave: String): Result<Unit>

    /** Pide a Supabase el correo de recuperación. La contraseña nueva se crea en la web. */
    suspend fun recuperarClave(correo: String): Result<Unit>

    /** Crea la cuenta en Supabase (MOV-0003). */
    suspend fun registrarse(correo: String, clave: String): Result<ResultadoRegistro>

    /** Vuelve a enviar el correo de confirmación del registro. */
    suspend fun reenviarConfirmacion(correo: String): Result<Unit>

    suspend fun cerrarSesion(): Result<Unit>
}
