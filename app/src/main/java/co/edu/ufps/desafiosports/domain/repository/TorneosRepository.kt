package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.Pagina
import co.edu.ufps.desafiosports.domain.model.TorneoDetalle
import co.edu.ufps.desafiosports.domain.model.TorneoResumen

/** Explorar torneos y ver su detalle (MOV-0005). Funciona con y sin sesión. */
interface TorneosRepository {
    /** [deporte] es el código (FUTSAL…); [busqueda] el texto escrito. [pagina] empieza en 0. */
    suspend fun explorar(deporte: String?, busqueda: String?, pagina: Int): Result<Pagina<TorneoResumen>>
    suspend fun torneo(id: String): Result<TorneoDetalle>
    suspend fun torneoPorEnlace(enlaceToken: String): Result<TorneoDetalle>
}
