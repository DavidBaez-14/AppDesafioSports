package co.edu.ufps.desafiosports.data.repository

import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.model.MiembroDePlantel
import co.edu.ufps.desafiosports.data.remote.dto.MiembroEquipoDto
import co.edu.ufps.desafiosports.data.remote.dto.CamisetaRequestDto
import co.edu.ufps.desafiosports.data.remote.dto.AgregarMiembroRequestDto
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

    override suspend fun plantel(equipoTorneoId: String): Result<List<MiembroDePlantel>> =
        llamarApi(errores) { api.plantel(equipoTorneoId) }.map { lista -> lista.mapNotNull { it.aDominio() } }

    override suspend fun agregarJugador(
        equipoTorneoId: String,
        tipoDocumento: TipoDocumento?,
        documento: String?,
        nombre: String?,
        camiseta: Int?,
    ): Result<MiembroDePlantel> =
        llamarApi(errores) {
            api.agregarJugador(equipoTorneoId, AgregarMiembroRequestDto(documento, tipoDocumento?.name, nombre, camiseta))
        }.map { it.aMiembro() }

    override suspend fun quitarJugador(equipoTorneoId: String, personaId: String): Result<MiembroDePlantel> =
        llamarApi(errores) { api.quitarJugador(equipoTorneoId, personaId) }.map { it.aMiembro() }

    override suspend fun cambiarCamiseta(equipoTorneoId: String, personaId: String, numero: Int?): Result<MiembroDePlantel> =
        llamarApi(errores) { api.cambiarCamiseta(equipoTorneoId, personaId, CamisetaRequestDto(numero)) }.map { it.aMiembro() }
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

/** Un miembro sin persona no se puede tocar: no entra a la lista. */
internal fun MiembroEquipoDto.aDominio(): MiembroDePlantel? {
    val suPersona = personaId?.takeIf { it.isNotBlank() } ?: return null
    return MiembroDePlantel(
        personaId = suPersona,
        nombre = nombre.orEmpty(),
        numeroDocumento = numeroDocumento?.takeIf { it.isNotBlank() },
        numeroCamiseta = numeroCamiseta,
        // Un estado que la app no conoce se trata como activo: es lo que muestra el backend.
        activo = estado != "RETIRADO",
        esDelegado = esDelegado == true,
        tieneCuenta = tieneCuenta != false,
        torneoFinalizado = torneoEstado == "FINALIZADO",
    )
}

/** La respuesta de una acción: si llegara sin persona, el plantel se vuelve a pedir igual. */
private fun MiembroEquipoDto.aMiembro(): MiembroDePlantel =
    aDominio() ?: MiembroDePlantel("", nombre.orEmpty(), null, numeroCamiseta, estado != "RETIRADO", esDelegado == true, tieneCuenta != false, false)
