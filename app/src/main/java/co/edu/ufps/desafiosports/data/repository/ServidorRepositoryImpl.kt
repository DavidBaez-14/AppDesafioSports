package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.SistemaApi
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.EstadoServidor
import co.edu.ufps.desafiosports.domain.repository.ServidorRepository

class ServidorRepositoryImpl(
    private val api: SistemaApi,
    private val errores: ErroresHttp,
    private val baseUrl: String,
) : ServidorRepository {

    override suspend fun comprobar(): Result<EstadoServidor> =
        llamarApi(errores) { api.health() }.map { EstadoServidor(url = baseUrl, estado = it.status) }
}
