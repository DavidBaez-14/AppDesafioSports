package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.EstadoInscripcion
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha
import co.edu.ufps.desafiosports.domain.model.MiembroDePlantel
import co.edu.ufps.desafiosports.domain.model.TipoDocumento
import co.edu.ufps.desafiosports.domain.repository.DelegadoRepository
import kotlinx.coroutines.CompletableDeferred

/** Repositorio falso del delegado: inscribir un equipo (MOV-0014) y su plantel (MOV-0015). */
class FakeDelegadoRepository : DelegadoRepository {
    val llamadas = mutableListOf<String>()

    var equipos: Result<List<EquipoPropio>> = Result.success(emptyList())

    /** Null = responde la inscripción pendiente del equipo enviado. */
    var resultadoInscribir: Result<InscripcionHecha>? = null

    /** Si no es null, las acciones que cambian algo esperan a que se complete (doble toque). */
    var bloqueo: CompletableDeferred<Unit>? = null

    // ── Plantel (MOV-0015) ──

    var plantel: Result<List<MiembroDePlantel>> = Result.success(emptyList())

    /** Null = responde un miembro activo nuevo con lo que se envió. */
    var resultadoAgregar: Result<MiembroDePlantel>? = null

    /** Null = responde al miembro del plantel con el número enviado. */
    var resultadoCamiseta: Result<MiembroDePlantel>? = null

    /** Null = responde al miembro del plantel, ya retirado. */
    var resultadoQuitar: Result<MiembroDePlantel>? = null

    override suspend fun misEquipos(): Result<List<EquipoPropio>> {
        llamadas += "equipos"
        return equipos
    }

    override suspend fun inscribir(
        torneoId: String,
        equipoId: String?,
        nombreEquipo: String?,
        enlaceToken: String?,
    ): Result<InscripcionHecha> {
        llamadas += "inscribir:$torneoId:$equipoId:$nombreEquipo:$enlaceToken"
        bloqueo?.await()
        return resultadoInscribir
            ?: Result.success(InscripcionHecha("et-nuevo", nombreEquipo ?: "Equipo $equipoId", EstadoInscripcion.PENDIENTE_PAGO))
    }

    override suspend fun plantel(equipoTorneoId: String): Result<List<MiembroDePlantel>> {
        llamadas += "plantel:$equipoTorneoId"
        return plantel
    }

    override suspend fun agregarJugador(
        equipoTorneoId: String,
        tipoDocumento: TipoDocumento?,
        documento: String?,
        nombre: String?,
        camiseta: Int?,
    ): Result<MiembroDePlantel> {
        llamadas += "agregar:$equipoTorneoId:$tipoDocumento:$documento:$nombre:$camiseta"
        bloqueo?.await()
        return resultadoAgregar
            ?: Result.success(miembro("p-nuevo", nombre ?: "Cuenta $documento", camiseta, documento = documento))
    }

    override suspend fun quitarJugador(equipoTorneoId: String, personaId: String): Result<MiembroDePlantel> {
        llamadas += "quitar:$equipoTorneoId:$personaId"
        bloqueo?.await()
        return resultadoQuitar ?: Result.success(delPlantel(personaId).copy(activo = false))
    }

    override suspend fun cambiarCamiseta(equipoTorneoId: String, personaId: String, numero: Int?): Result<MiembroDePlantel> {
        llamadas += "camiseta:$equipoTorneoId:$personaId:$numero"
        bloqueo?.await()
        return resultadoCamiseta ?: Result.success(delPlantel(personaId).copy(numeroCamiseta = numero))
    }

    private fun delPlantel(personaId: String): MiembroDePlantel =
        plantel.getOrNull()?.firstOrNull { it.personaId == personaId } ?: miembro(personaId, "Persona $personaId", null)
}

/** Un miembro de plantel para las pruebas, activo, con cuenta y en un torneo sin terminar. */
fun miembro(
    personaId: String,
    nombre: String,
    numero: Int?,
    activo: Boolean = true,
    delegado: Boolean = false,
    cuenta: Boolean = true,
    documento: String? = null,
    torneoFinalizado: Boolean = false,
) = MiembroDePlantel(
    personaId = personaId,
    nombre = nombre,
    numeroDocumento = documento,
    numeroCamiseta = numero,
    activo = activo,
    esDelegado = delegado,
    tieneCuenta = cuenta,
    torneoFinalizado = torneoFinalizado,
)
