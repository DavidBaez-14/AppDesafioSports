package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.EstadoMulta
import co.edu.ufps.desafiosports.domain.model.MiEquipo
import co.edu.ufps.desafiosports.domain.model.Multa
import co.edu.ufps.desafiosports.domain.model.Perfil
import co.edu.ufps.desafiosports.domain.model.ProximoPartido
import co.edu.ufps.desafiosports.domain.model.RolEnEquipo
import co.edu.ufps.desafiosports.domain.model.Tarjetas
import co.edu.ufps.desafiosports.domain.repository.MisEquiposRepository
import co.edu.ufps.desafiosports.domain.repository.MultasRepository
import co.edu.ufps.desafiosports.domain.repository.PerfilRepository
import java.math.BigDecimal
import java.time.LocalDateTime

// Repositorios falsos de Inicio y Perfil (MOV-0004).

class FakeMisEquiposRepository(var equipos: Result<List<MiEquipo>> = Result.success(emptyList())) : MisEquiposRepository {
    var llamadas = 0
        private set

    override suspend fun misEquipos(): Result<List<MiEquipo>> {
        llamadas++
        return equipos
    }
}

class FakePerfilRepository(var perfil: Result<Perfil> = Result.success(perfilDePrueba())) : PerfilRepository {
    var llamadas = 0
        private set

    override suspend fun miPerfil(): Result<Perfil> {
        llamadas++
        return perfil
    }
}

class FakeMultasRepository(var multas: Result<List<Multa>> = Result.success(emptyList())) : MultasRepository {
    override suspend fun misMultas(): Result<List<Multa>> = multas
}

fun equipoDePrueba(
    id: String = "et1",
    equipo: String = "Halcones FC",
    rol: RolEnEquipo = RolEnEquipo.JUGADOR,
    estado: EstadoInscripcion = EstadoInscripcion.APROBADO,
    comprobante: String? = null,
    partido: LocalDateTime? = null,
    rival: String? = "Tiburones FC",
    local: Boolean = true,
) = MiEquipo(
    equipoTorneoId = id,
    equipo = equipo,
    torneoId = "t-$id",
    torneo = "Liga Relámpago (prueba)",
    deporte = "FUTSAL",
    rol = rol,
    estadoInscripcion = estado,
    comprobante = comprobante,
    proximoPartido = partido?.let { ProximoPartido("p-$id", rival, local, it, "Cancha 1") },
)

fun perfilDePrueba(
    partidos: Int = 0,
    goles: Int = 0,
    titulos: Int = 0,
    tarjetas: Tarjetas = Tarjetas(),
) = Perfil(
    nombre = "Juan Castro",
    alturaCm = null,
    piernaHabil = null,
    posicion = null,
    trayectoria = emptyList(),
    partidosJugados = partidos,
    goles = goles,
    titulos = titulos,
    tarjetas = tarjetas,
)

fun multaDePrueba(id: String, estado: EstadoMulta, generada: LocalDateTime?) =
    Multa(id = id, tipoSancion = "AMARILLA", monto = BigDecimal("10000"), estado = estado, fechaGeneracion = generada)
