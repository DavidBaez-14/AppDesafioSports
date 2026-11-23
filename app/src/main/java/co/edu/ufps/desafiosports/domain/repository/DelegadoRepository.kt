package co.edu.ufps.desafiosports.domain.repository

import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.model.MiembroDePlantel
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

    // ── Plantel (MOV-0015) ──

    /** Todo el plantel: activos y retirados. 403 si no es su delegado. */
    suspend fun plantel(equipoTorneoId: String): Result<List<MiembroDePlantel>>

    /**
     * Agrega a alguien. Con [documento] de una persona registrada se usa su nombre; sin documento
     * (o si no tiene cuenta), [nombre] es obligatorio. Responde cómo quedó en el plantel.
     */
    suspend fun agregarJugador(
        equipoTorneoId: String,
        tipoDocumento: TipoDocumento?,
        documento: String?,
        nombre: String?,
        camiseta: Int?,
    ): Result<MiembroDePlantel>

    /** Lo retira del plantel; su historial se conserva. */
    suspend fun quitarJugador(equipoTorneoId: String, personaId: String): Result<MiembroDePlantel>

    /** [numero] null le quita el número. */
    suspend fun cambiarCamiseta(equipoTorneoId: String, personaId: String, numero: Int?): Result<MiembroDePlantel>
}
