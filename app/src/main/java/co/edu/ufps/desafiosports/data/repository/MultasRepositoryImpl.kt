package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.FinanzasApi
import co.edu.ufps.desafiosports.data.remote.dto.MultaDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.EstadoMulta
import co.edu.ufps.desafiosports.domain.model.Multa
import co.edu.ufps.desafiosports.domain.repository.MultasRepository
import java.math.BigDecimal

class MultasRepositoryImpl(
    private val api: FinanzasApi,
    private val errores: ErroresHttp,
) : MultasRepository {

    override suspend fun misMultas(): Result<List<Multa>> =
        llamarApi(errores) { api.misMultas() }.map { lista -> lista.map { it.aDominio() } }
}

internal fun MultaDto.aDominio(): Multa = Multa(
    id = id.orEmpty(),
    tipoSancion = tipoSancion.orEmpty(),
    monto = BigDecimal.valueOf(monto ?: 0.0),
    estado = EstadoMulta.desde(estado),
    fechaGeneracion = fechaGeneracion.aFechaHora(),
)
