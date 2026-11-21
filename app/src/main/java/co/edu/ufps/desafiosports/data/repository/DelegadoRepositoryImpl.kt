package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.DelegadoApi
import co.edu.ufps.desafiosports.data.remote.dto.EquipoBaseDto
import co.edu.ufps.desafiosports.data.remote.dto.InscribirEquipoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.InscripcionDelegadoDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha
import co.edu.ufps.desafiosports.domain.repository.DelegadoRepository

class DelegadoRepositoryImpl(
    private val api: DelegadoApi,
    private val errores: ErroresHttp,
) : DelegadoRepository {

    override suspend fun misEquipos(): Result<List<EquipoPropio>> =
        llamarApi(errores) { api.misEquipos() }.map { lista -> lista.mapNotNull { it.aDominio() } }

    override suspend fun inscribir(
        torneoId: String,
        equipoId: String?,
        nombreEquipo: String?,
        enlaceToken: String?,
    ): Result<InscripcionHecha> =
        llamarApi(errores) {
            api.inscribir(torneoId, InscribirEquipoRequestDto(equipoId, nombreEquipo, enlaceToken?.takeIf { it.isNotBlank() }))
        }.map { it.aDominio(nombreEquipo) }
}

// ── DTO → modelo ────────────────────────────────────────────────────────────

/** Un equipo sin id no se puede inscribir: no entra a la lista. */
internal fun EquipoBaseDto.aDominio(): EquipoPropio? {
    val suId = id?.takeIf { it.isNotBlank() } ?: return null
    return EquipoPropio(suId, nombre.orEmpty())
}

/** Si la respuesta no trae el nombre, se usa el que se escribió. */
internal fun InscripcionDelegadoDto.aDominio(nombreEscrito: String?): InscripcionHecha = InscripcionHecha(
    equipoTorneoId = id.orEmpty(),
    equipo = equipoNombre?.takeIf { it.isNotBlank() } ?: nombreEscrito.orEmpty(),
    estado = EstadoInscripcion.desde(estadoInscripcion),
)
