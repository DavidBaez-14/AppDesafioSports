package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.auth.llamarSupabase
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.repository.SesionRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Sesión con Supabase Auth (supabase-kt). Reemplaza al AuthRepositoryImpl de Firebase de StreamFlix. */
class SesionRepositoryImpl(private val auth: Auth) : SesionRepository {

    override val sesion: Flow<EstadoSesion> = auth.sessionStatus.map { estado ->
        when (estado) {
            is SessionStatus.Authenticated -> EstadoSesion.Activa(estado.session.user?.email.orEmpty())
            is SessionStatus.NotAuthenticated -> EstadoSesion.Inactiva
            is SessionStatus.RefreshFailure -> EstadoSesion.SinRenovar
            SessionStatus.Initializing -> EstadoSesion.Cargando
        }
    }

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Unit> = llamarSupabase {
        auth.signInWith(Email) {
            email = correo
            password = clave
        }
    }

    // Sin redirectUrl, igual que la web: el enlace del correo (plantilla en español de Supabase)
    // abre la web, donde se crea la contraseña nueva.
    override suspend fun recuperarClave(correo: String): Result<Unit> = llamarSupabase {
        auth.resetPasswordForEmail(correo)
    }

    override suspend fun cerrarSesion(): Result<Unit> = llamarSupabase {
        // Si falla la llamada a Supabase (por ejemplo, sin red), igual se borra la sesión local.
        runCatching { auth.signOut() }.onFailure { auth.clearSession() }
        Unit
    }
}
