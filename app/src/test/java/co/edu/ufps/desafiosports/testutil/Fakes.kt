package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.data.remote.red.ProveedorToken
import co.edu.ufps.desafiosports.domain.model.Cuenta
import co.edu.ufps.desafiosports.domain.model.EstadoServidor
import co.edu.ufps.desafiosports.domain.model.EstadoSesion
import co.edu.ufps.desafiosports.domain.model.Rol
import co.edu.ufps.desafiosports.domain.repository.CuentaRepository
import co.edu.ufps.desafiosports.domain.repository.ServidorRepository
import co.edu.ufps.desafiosports.domain.repository.SesionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Implementaciones falsas de las interfaces. Se prefieren a los mocks: son explícitas y
// no necesitan librerías extra (inyección manual = basta con pasar otra implementación).

class FakeSesionRepository : SesionRepository {
    val estado = MutableStateFlow<EstadoSesion>(EstadoSesion.Inactiva)
    var resultadoInicio: Result<Unit> = Result.success(Unit)
    val llamadasInicio = mutableListOf<Pair<String, String>>()

    /** Si no es null, iniciarSesion espera a que se complete: sirve para probar el doble toque. */
    var bloqueoInicio: CompletableDeferred<Unit>? = null

    var resultadoRecuperacion: Result<Unit> = Result.success(Unit)
    val llamadasRecuperacion = mutableListOf<String>()

    var cierres = 0
        private set
    var fueObservada = false
        private set

    override val sesion: Flow<EstadoSesion>
        get() {
            fueObservada = true
            return estado
        }

    override suspend fun iniciarSesion(correo: String, clave: String): Result<Unit> {
        llamadasInicio += correo to clave
        bloqueoInicio?.await()
        if (resultadoInicio.isSuccess) estado.value = EstadoSesion.Activa(correo)
        return resultadoInicio
    }

    override suspend fun recuperarClave(correo: String): Result<Unit> {
        llamadasRecuperacion += correo
        return resultadoRecuperacion
    }

    override suspend fun cerrarSesion(): Result<Unit> {
        cierres++
        estado.value = EstadoSesion.Inactiva
        return Result.success(Unit)
    }
}

class FakeCuentaRepository(var resultado: Result<Cuenta> = Result.success(cuentaDePrueba())) : CuentaRepository {
    var llamadas = 0
        private set

    override suspend fun obtenerMiCuenta(): Result<Cuenta> {
        llamadas++
        return resultado
    }
}

class FakeServidorRepository(
    var resultado: Result<EstadoServidor> = Result.success(EstadoServidor("http://10.0.2.2:8082/", "UP")),
) : ServidorRepository {
    override suspend fun comprobar(): Result<EstadoServidor> = resultado
}

class FakeProveedorToken(
    var token: String? = "token-1",
    var tokenRenovado: String? = "token-2",
) : ProveedorToken {
    var renovaciones = 0
        private set
    var sesionCerrada = false
        private set

    override fun tokenActual(): String? = token

    override fun renovarToken(): String? {
        renovaciones++
        token = tokenRenovado
        return tokenRenovado
    }

    override fun cerrarSesionLocal() {
        sesionCerrada = true
        token = null
    }
}

fun cuentaDePrueba() = Cuenta(
    id = "c1",
    nombres = "Jugador",
    apellidos = "Uno",
    nombre = "Jugador Uno",
    correo = "jugador01@prueba.local",
    estado = "ACTIVA",
    roles = setOf(Rol.JUGADOR),
    rolesPendientes = emptySet(),
)
