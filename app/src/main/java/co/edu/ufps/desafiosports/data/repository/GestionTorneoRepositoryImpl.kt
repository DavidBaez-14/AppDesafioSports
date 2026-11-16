package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.data.remote.api.GestionTorneoApi
import co.edu.ufps.desafiosports.data.remote.dto.ArbitroAsignadoDto
import co.edu.ufps.desafiosports.data.remote.dto.AsignarArbitroRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.InvitacionDto
import co.edu.ufps.desafiosports.data.remote.dto.InvitarATorneoRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.PartidoAdminDto
import co.edu.ufps.desafiosports.data.remote.dto.RolTorneoDto
import co.edu.ufps.desafiosports.data.remote.dto.TodosLosPartidosRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.TorneoConfigDto
import co.edu.ufps.desafiosports.data.remote.red.ErroresHttp
import co.edu.ufps.desafiosports.data.remote.red.llamarApi
import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.EstadoDePartidos
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FasePartido
import co.edu.ufps.desafiosports.domain.model.InvitacionDeTorneo
import co.edu.ufps.desafiosports.domain.model.PartidoDeTorneo
import co.edu.ufps.desafiosports.domain.repository.GestionTorneoRepository

class GestionTorneoRepositoryImpl(
    private val api: GestionTorneoApi,
    private val errores: ErroresHttp,
) : GestionTorneoRepository {

    override suspend fun arbitros(torneoId: String): Result<List<ArbitroDeTorneo>> =
        llamarApi(errores) { api.roles(torneoId) }.map { roles -> roles.mapNotNull { it.aArbitro() } }

    override suspend fun quitarArbitro(torneoId: String, rolId: String): Result<Unit> =
        llamarApi(errores) { api.quitarRol(torneoId, rolId) }

    override suspend fun cambiarTodosLosPartidos(torneoId: String, cuentaId: String, valor: Boolean): Result<Unit> =
        llamarApi(errores) { api.todosLosPartidos(torneoId, cuentaId, TodosLosPartidosRequestDto(valor)) }

    override suspend fun invitaciones(torneoId: String): Result<List<InvitacionDeTorneo>> =
        llamarApi(errores) { api.invitaciones(torneoId) }.map { lista -> lista.mapNotNull { it.aInvitacionDeArbitro() } }

    override suspend fun invitarArbitro(torneoId: String, correo: String?): Result<InvitacionDeTorneo> =
        llamarApi(errores) { api.invitar(torneoId, InvitarATorneoRequestDto(rol = ROL_ARBITRO, correo = correo)) }
            .map { it.aInvitacion() }

    override suspend fun cancelarInvitacion(torneoId: String, invitacionId: String): Result<Unit> =
        llamarApi(errores) { api.revocarInvitacion(torneoId, invitacionId) }

    override suspend fun estadoDePartidos(torneoId: String): Result<EstadoDePartidos> =
        llamarApi(errores) { api.configuracion(torneoId) }.map { it.aDominio() }

    override suspend fun partidos(torneoId: String): Result<List<PartidoDeTorneo>> =
        llamarApi(errores) { api.partidos(torneoId) }.map { lista -> lista.map { it.aDominio() } }

    override suspend fun iniciarTorneo(torneoId: String): Result<Unit> =
        llamarApi(errores) { api.iniciar(torneoId) }

    override suspend fun generarPartidos(torneoId: String): Result<Unit> =
        llamarApi(errores) { api.generarFixture(torneoId) }

    override suspend fun asignarArbitro(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>> =
        llamarApi(errores) { api.asignarArbitro(partidoId, AsignarArbitroRequestDto(cuentaId)) }
            .map { lista -> lista.mapNotNull { it.aDominio() } }

    override suspend fun quitarArbitroDePartido(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>> =
        llamarApi(errores) { api.quitarArbitro(partidoId, cuentaId) }.map { lista -> lista.mapNotNull { it.aDominio() } }

    private companion object {
        const val ROL_ARBITRO = "ARBITRO"
    }
}

// ── DTO → modelo ────────────────────────────────────────────────────────────

/** Solo los árbitros: un administrador, o un rol sin id o sin cuenta, no entra. */
internal fun RolTorneoDto.aArbitro(): ArbitroDeTorneo? {
    if (rol != "ARBITRO") return null
    val rolId = id?.takeIf { it.isNotBlank() } ?: return null
    val cuenta = cuentaId?.takeIf { it.isNotBlank() } ?: return null
    val correoLimpio = correo?.takeIf { it.isNotBlank() }
    return ArbitroDeTorneo(
        rolId = rolId,
        cuentaId = cuenta,
        // Una cuenta sin persona no tiene nombre: se muestra su correo.
        nombre = nombre?.takeIf { it.isNotBlank() } ?: correoLimpio.orEmpty(),
        correo = correoLimpio,
        todosLosPartidos = todosLosPartidos == true,
    )
}

internal fun InvitacionDto.aInvitacion(): InvitacionDeTorneo = InvitacionDeTorneo(
    id = id.orEmpty(),
    correo = correo?.takeIf { it.isNotBlank() },
    enlace = enlace.orEmpty(),
    fecha = createdAt.aFechaHora(),
)

/** De la lista del torneo, solo las de árbitro que siguen pendientes. */
internal fun InvitacionDto.aInvitacionDeArbitro(): InvitacionDeTorneo? {
    if (rol != "ARBITRO") return null
    if (estado != null && estado != "PENDIENTE") return null
    if (id.isNullOrBlank()) return null
    return aInvitacion()
}

internal fun TorneoConfigDto.aDominio(): EstadoDePartidos = EstadoDePartidos(
    estadoTorneo = EstadoTorneo.desde(estado),
    equiposAprobados = equiposAprobados ?: 0,
    generados = fixtureGenerado == true,
)

internal fun ArbitroAsignadoDto.aDominio(): ArbitroAsignado? {
    val cuenta = cuentaId?.takeIf { it.isNotBlank() } ?: return null
    return ArbitroAsignado(cuentaId = cuenta, nombre = nombre.orEmpty())
}

internal fun PartidoAdminDto.aDominio(): PartidoDeTorneo = PartidoDeTorneo(
    id = id.orEmpty(),
    fecha = fecha.aFechaHora(),
    estado = EstadoPartido.desde(estado),
    estadoCodigo = estado.orEmpty(),
    local = localNombre?.takeIf { it.isNotBlank() },
    visitante = visitanteNombre?.takeIf { it.isNotBlank() },
    golesLocal = golesLocal ?: 0,
    golesVisitante = golesVisitante ?: 0,
    fase = FasePartido.desde(fase),
    faseCodigo = fase.orEmpty(),
    jornada = jornada,
    grupo = grupo?.takeIf { it.isNotBlank() },
    lugar = lugar?.takeIf { it.isNotBlank() },
    arbitros = arbitros.orEmpty().mapNotNull { it.aDominio() },
)
