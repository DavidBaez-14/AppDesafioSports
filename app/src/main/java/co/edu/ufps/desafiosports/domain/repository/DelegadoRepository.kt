package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha

/** Lo que hace quien inscribe un equipo (`/api/supercopa/delegado/…`, MOV-0014). */
interface DelegadoRepository {
    /** Sus equipos base, el más reciente primero. Vacía si no tiene ninguno. */
    suspend fun misEquipos(): Result<List<EquipoPropio>>

    /**
     * Inscribe [equipoId] (uno de los suyos) o crea e inscribe [nombreEquipo]. [enlaceToken] hace
     * falta en un torneo que solo se abre con su enlace.
     */
    suspend fun inscribir(torneoId: String, equipoId: String?, nombreEquipo: String?, enlaceToken: String?): Result<InscripcionHecha>
}
