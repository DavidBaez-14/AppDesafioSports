package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.CompeticionApi
import co.edu.ufps.desafiosports.data.remote.dto.PerfilDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.PasoPorEquipo
import co.edu.ufps.desafiosports.domain.model.Perfil
import co.edu.ufps.desafiosports.domain.model.Puesto
import co.edu.ufps.desafiosports.domain.model.Tarjetas
import co.edu.ufps.desafiosports.domain.repository.PerfilRepository

class PerfilRepositoryImpl(
    private val api: CompeticionApi,
    private val errores: ErroresHttp,
) : PerfilRepository {

    override suspend fun miPerfil(): Result<Perfil> =
        llamarApi(errores) { api.miPerfil() }.map { it.aDominio() }
}

/** El puesto de cada renglón sale de `titulos`, cuando coinciden el torneo y el equipo. */
internal fun PerfilDto.aDominio(): Perfil {
    val puestos = titulos.orEmpty().mapNotNull { titulo ->
        val puesto = Puesto.desde(titulo.puesto) ?: return@mapNotNull null
        (titulo.torneo.orEmpty() to titulo.equipo.orEmpty()) to puesto
    }.toMap()
    val tarjetas = resumen?.tarjetas
    return Perfil(
        nombre = nombre.orEmpty(),
        alturaCm = alturaCm,
        piernaHabil = piernaHabil?.takeIf { it.isNotBlank() },
        posicion = posicion?.takeIf { it.isNotBlank() },
        trayectoria = equipos.orEmpty().map { e ->
            PasoPorEquipo(
                equipo = e.nombre.orEmpty(),
                torneo = e.torneo.orEmpty(),
                puesto = puestos[e.torneo.orEmpty() to e.nombre.orEmpty()],
            )
        },
        partidosJugados = resumen?.partidosJugados ?: 0,
        goles = resumen?.goles ?: 0,
        titulos = resumen?.titulos ?: 0,
        tarjetas = Tarjetas(tarjetas?.amarillas ?: 0, tarjetas?.azules ?: 0, tarjetas?.rojas ?: 0),
    )
}
