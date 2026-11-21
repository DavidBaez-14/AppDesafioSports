package co.edu.ufps.desafiosports.testutil

import co.edu.ufps.desafiosports.domain.model.Programacion
import co.edu.ufps.desafiosports.domain.model.ArbitroAsignado
import co.edu.ufps.desafiosports.domain.model.ArbitroDeTorneo
import co.edu.ufps.desafiosports.domain.model.EstadoDePartidos
import co.edu.ufps.desafiosports.domain.model.EstadoPartido
import co.edu.ufps.desafiosports.domain.model.EstadoTorneo
import co.edu.ufps.desafiosports.domain.model.FasePartido
import co.edu.ufps.desafiosports.domain.model.InvitacionDeTorneo
import co.edu.ufps.desafiosports.domain.model.InvitacionRecibida
import co.edu.ufps.desafiosports.domain.model.PartidoDeTorneo
import co.edu.ufps.desafiosports.domain.model.RolInvitado
import co.edu.ufps.desafiosports.domain.repository.GestionTorneoRepository
import co.edu.ufps.desafiosports.domain.repository.InvitacionesRepository
import kotlinx.coroutines.CompletableDeferred
import java.time.LocalDateTime

/** Repositorio falso de la gestión de un torneo (MOV-0011). */
class FakeGestionTorneoRepository : GestionTorneoRepository {
    val llamadas = mutableListOf<String>()

    var arbitros: Result<List<ArbitroDeTorneo>> = Result.success(emptyList())
    var invitaciones: Result<List<InvitacionDeTorneo>> = Result.success(emptyList())
    var resultadoInvitar: Result<InvitacionDeTorneo> = Result.success(invitacionDePrueba("i-nueva"))
    var resultadoAccion: Result<Unit> = Result.success(Unit)

    var estado: Result<EstadoDePartidos> = Result.success(EstadoDePartidos(EstadoTorneo.EN_CURSO, 4, generados = true))
    var partidos: Result<List<PartidoDeTorneo>> = Result.success(emptyList())
    var resultadoIniciar: Result<Unit> = Result.success(Unit)
    var resultadoGenerar: Result<Unit> = Result.success(Unit)

    /** Se ejecuta al iniciar con éxito: el torneo pasa a estar en curso. */
    var alIniciar: (() -> Unit)? = null

    /** Se ejecuta al generar con éxito: deja listo lo que responderá la recarga. */
    var alGenerar: (() -> Unit)? = null

    /** Lo que responde asignar o quitar; null = la lista que resulta de aplicar el cambio. */
    var resultadoAsignacion: Result<List<ArbitroAsignado>>? = null

    /** Si no es null, la acción espera a que se complete (doble toque). */
    var bloqueo: CompletableDeferred<Unit>? = null

    override suspend fun arbitros(torneoId: String): Result<List<ArbitroDeTorneo>> {
        llamadas += "arbitros:$torneoId"
        return arbitros
    }

    override suspend fun quitarArbitro(torneoId: String, rolId: String): Result<Unit> {
        llamadas += "quitar:$torneoId:$rolId"
        bloqueo?.await()
        return resultadoAccion
    }

    override suspend fun cambiarTodosLosPartidos(torneoId: String, cuentaId: String, valor: Boolean): Result<Unit> {
        llamadas += "todos:$torneoId:$cuentaId:$valor"
        bloqueo?.await()
        return resultadoAccion
    }

    override suspend fun invitaciones(torneoId: String): Result<List<InvitacionDeTorneo>> {
        llamadas += "invitaciones:$torneoId"
        return invitaciones
    }

    override suspend fun invitarArbitro(torneoId: String, correo: String?): Result<InvitacionDeTorneo> {
        llamadas += "invitar:$torneoId:$correo"
        bloqueo?.await()
        return resultadoInvitar
    }

    override suspend fun cancelarInvitacion(torneoId: String, invitacionId: String): Result<Unit> {
        llamadas += "cancelar:$torneoId:$invitacionId"
        return resultadoAccion
    }

    override suspend fun estadoDePartidos(torneoId: String): Result<EstadoDePartidos> {
        llamadas += "estado:$torneoId"
        return estado
    }

    override suspend fun partidos(torneoId: String): Result<List<PartidoDeTorneo>> {
        llamadas += "partidos:$torneoId"
        return partidos
    }

    override suspend fun iniciarTorneo(torneoId: String): Result<Unit> {
        llamadas += "iniciar:$torneoId"
        if (resultadoIniciar.isSuccess) alIniciar?.invoke()
        return resultadoIniciar
    }

    override suspend fun generarPartidos(torneoId: String): Result<Unit> {
        llamadas += "generar:$torneoId"
        bloqueo?.await()
        if (resultadoGenerar.isSuccess) alGenerar?.invoke()
        return resultadoGenerar
    }

    override suspend fun asignarArbitro(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>> {
        llamadas += "asignar:$partidoId:$cuentaId"
        bloqueo?.await()
        return resultadoAsignacion ?: Result.success(listOf(ArbitroAsignado(cuentaId, "Confirmado $cuentaId")))
    }

    override suspend fun quitarArbitroDePartido(partidoId: String, cuentaId: String): Result<List<ArbitroAsignado>> {
        llamadas += "desasignar:$partidoId:$cuentaId"
        bloqueo?.await()
        return resultadoAsignacion ?: Result.success(emptyList())
    }

    /** Lo que responde programar; null = devuelve lo que se envió (MOV-0013). */
    var resultadoProgramar: Result<Programacion>? = null
    var resultadoBorrar: Result<Unit> = Result.success(Unit)

    /** Se ejecuta al borrar con éxito: deja listo lo que responderá la recarga. */
    var alBorrar: (() -> Unit)? = null

    override suspend fun programarPartido(partidoId: String, fecha: LocalDateTime?, lugar: String): Result<Programacion> {
        llamadas += "programar:$partidoId:$fecha:$lugar"
        bloqueo?.await()
        return resultadoProgramar ?: Result.success(Programacion(fecha, lugar.ifEmpty { null }))
    }

    override suspend fun borrarPartidos(torneoId: String): Result<Unit> {
        llamadas += "borrar:$torneoId"
        bloqueo?.await()
        if (resultadoBorrar.isSuccess) alBorrar?.invoke()
        return resultadoBorrar
    }
}

class FakeInvitacionesRepository : InvitacionesRepository {
    val llamadas = mutableListOf<String>()
    var resultadoVer: Result<InvitacionRecibida> = Result.success(invitacionRecibidaDePrueba())
    var resultadoAceptar: Result<Unit> = Result.success(Unit)

    /** Las invitaciones dirigidas al correo de la sesión (MOV-0012). */
    var resultadoMias: Result<List<InvitacionRecibida>> = Result.success(emptyList())
    var bloqueo: CompletableDeferred<Unit>? = null

    /** Va aparte de [llamadas]: se pide sola al abrir la pantalla. */
    var pedidosDeMias = 0

    override suspend fun mias(): Result<List<InvitacionRecibida>> {
        pedidosDeMias++
        return resultadoMias
    }

    override suspend fun ver(token: String): Result<InvitacionRecibida> {
        llamadas += "ver:$token"
        return resultadoVer.map { it.copy(token = token) }
    }

    override suspend fun aceptar(token: String): Result<Unit> {
        llamadas += "aceptar:$token"
        bloqueo?.await()
        return resultadoAceptar
    }
}

fun arbitroDePrueba(n: Int, nombre: String = "Árbitro $n", todosLosPartidos: Boolean = false) = ArbitroDeTorneo(
    rolId = "r$n",
    cuentaId = "c$n",
    nombre = nombre,
    correo = "arbitro$n@correo.com",
    todosLosPartidos = todosLosPartidos,
)

fun invitacionDePrueba(id: String, correo: String? = null, fecha: LocalDateTime? = LocalDateTime.of(2026, 11, 16, 10, 0)) =
    InvitacionDeTorneo(id = id, correo = correo, enlace = "https://desafio.test/invitacion/token-$id", fecha = fecha)

fun partidoDeTorneoDePrueba(
    id: String,
    fase: FasePartido = FasePartido.GRUPOS,
    faseCodigo: String = fase.name,
    jornada: Int? = 1,
    grupo: String? = null,
    fecha: LocalDateTime? = LocalDateTime.of(2026, 11, 21, 8, 0),
    estado: EstadoPartido = EstadoPartido.PROGRAMADO,
    estadoCodigo: String = estado.name,
    arbitros: List<ArbitroAsignado> = emptyList(),
) = PartidoDeTorneo(
    id = id,
    fecha = fecha,
    estado = estado,
    estadoCodigo = estadoCodigo,
    local = "Local $id",
    visitante = "Visitante $id",
    golesLocal = 0,
    golesVisitante = 0,
    fase = fase,
    faseCodigo = faseCodigo,
    jornada = jornada,
    grupo = grupo,
    lugar = null,
    arbitros = arbitros,
)

/** Un token como los del backend: 43 caracteres de letras, dígitos, guion y guion bajo. */
const val TOKEN_DE_PRUEBA = "AbC-123_defGHI456jklMNO789pqrSTU012vwxYZ345"

fun invitacionRecibidaDePrueba(pendiente: Boolean = true) = InvitacionRecibida(
    token = TOKEN_DE_PRUEBA,
    rol = RolInvitado.ARBITRO,
    torneo = "Copa Prueba 2026",
    invitadoPor = "Carlos Rojas",
    paraCorreo = null,
    pendiente = pendiente,
)
