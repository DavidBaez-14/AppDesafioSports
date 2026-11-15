package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.CompeticionApi
import co.edu.ufps.desafiosports.data.remote.dto.MiEquipoDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.MiEquipo
import co.edu.ufps.desafiosports.domain.model.ProximoPartido
import co.edu.ufps.desafiosports.domain.model.RolEnEquipo
import co.edu.ufps.desafiosports.domain.repository.MisEquiposRepository

class MisEquiposRepositoryImpl(
    private val api: CompeticionApi,
    private val errores: ErroresHttp,
) : MisEquiposRepository {

    override suspend fun misEquipos(): Result<List<MiEquipo>> =
        llamarApi(errores) { api.misEquipos() }.map { lista -> lista.mapNotNull { it.aDominio() } }
}

/** Un equipo sin torneo no se puede abrir: se descarta. Los códigos que la app no conoce no fallan. */
internal fun MiEquipoDto.aDominio(): MiEquipo? {
    val torneoId = torneoId?.takeIf { it.isNotBlank() } ?: return null
    return MiEquipo(
        equipoTorneoId = equipoTorneoId.orEmpty(),
        equipo = equipo.orEmpty(),
        torneoId = torneoId,
        torneo = torneo.orEmpty(),
        deporte = deporte.orEmpty(),
        rol = RolEnEquipo.desde(rol),
        estadoInscripcion = EstadoInscripcion.desde(estadoInscripcion),
        comprobante = comprobante?.takeIf { it.isNotBlank() },
        proximoPartido = proximoPartido?.let { p ->
            ProximoPartido(
                partidoId = p.partidoId.orEmpty(),
                rival = p.rival?.takeIf { it.isNotBlank() },
                local = p.local ?: true,
                fechaHora = p.fechaHora.aFechaHora(),
                lugar = p.lugar?.takeIf { it.isNotBlank() },
            )
        },
    )
}
