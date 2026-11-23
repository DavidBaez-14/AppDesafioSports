package co.edu.ufps.desafiosports.domain.use_case.competicion

import co.edu.ufps.desafiosports.domain.model.Plantel
import co.edu.ufps.desafiosports.domain.model.MiembroDePlantel
import co.edu.ufps.desafiosports.domain.model.BorradorJugador
import co.edu.ufps.desafiosports.domain.error.CampoValidado
import co.edu.ufps.desafiosports.domain.error.AppError
import co.edu.ufps.desafiosports.domain.model.EquipoAInscribir
import co.edu.ufps.desafiosports.domain.model.EquipoPropio
import co.edu.ufps.desafiosports.domain.model.InscripcionHecha
import co.edu.ufps.desafiosports.domain.repository.DelegadoRepository

// Inscribir un equipo en un torneo (MOV-0014).

/** Los equipos base de la persona, para elegir cuál inscribir. */
class ObtenerMisEquiposBaseUseCase(private val repositorio: DelegadoRepository) {
    suspend operator fun invoke(): Result<List<EquipoPropio>> = repositorio.misEquipos()
}

/**
 * Inscribe un equipo. Un equipo nuevo necesita nombre, de hasta 150 caracteres y distinto de los
 * que la persona ya tiene (el backend también lo exige). Lo que no cumple no viaja.
 */
class InscribirEquipoUseCase(private val repositorio: DelegadoRepository) {
    suspend operator fun invoke(
        torneoId: String,
        equipo: EquipoAInscribir,
        enlaceToken: String?,
        propios: List<EquipoPropio>,
    ): Result<InscripcionHecha> = when (equipo) {
        is EquipoAInscribir.Existente -> repositorio.inscribir(torneoId, equipo.equipoId, null, enlaceToken)
        is EquipoAInscribir.Nuevo -> {
            val nombre = equipo.nombre.trim()
            when {
                nombre.isEmpty() -> Result.failure(AppError.Validacion(MENSAJE_NOMBRE_VACIO))
                nombre.length > NOMBRE_MAXIMO -> Result.failure(AppError.Validacion(MENSAJE_NOMBRE_LARGO))
                propios.any { it.nombre.trim().equals(nombre, ignoreCase = true) } ->
                    Result.failure(AppError.Validacion(mensajeRepetido(nombre)))
                else -> repositorio.inscribir(torneoId, null, nombre, enlaceToken)
            }
        }
    }

    companion object {
        const val NOMBRE_MAXIMO = 150
        const val MENSAJE_NOMBRE_VACIO = "Escribe el nombre del equipo."
        const val MENSAJE_NOMBRE_LARGO = "El nombre puede tener hasta 150 caracteres."

        fun mensajeRepetido(nombre: String) = "Ya tienes un equipo llamado \"$nombre\". Elígelo en la lista."
    }
}

// ── Plantel (MOV-0015) ──────────────────────────────────────────────────────

class ObtenerPlantelUseCase(private val repositorio: DelegadoRepository) {
    suspend operator fun invoke(equipoTorneoId: String): Result<Plantel> = repositorio.plantel(equipoTorneoId).map { Plantel(it) }
}

/** Lee el número de camiseta escrito: vacío es "sin número"; si no, del 0 al 99. */
internal fun leerCamiseta(texto: String): Result<Int?> {
    val limpio = texto.trim()
    if (limpio.isEmpty()) return Result.success(null)
    val numero = limpio.toIntOrNull()
    return if (numero == null || numero !in 0..CAMISETA_MAXIMA || !limpio.all { it.isDigit() }) {
        Result.failure(AppError.Validacion(MENSAJE_CAMISETA, CampoValidado.CAMISETA))
    } else {
        Result.success(numero)
    }
}

const val CAMISETA_MAXIMA = 99
const val MENSAJE_CAMISETA = "Usa un número del 0 al 99, o déjalo vacío."

fun mensajeNumeroOcupado(numero: Int, nombre: String) = "El $numero ya lo tiene $nombre."

/**
 * Agrega a un jugador al plantel (MOV-0015). Lo que se puede saber antes de enviar no viaja: el
 * documento que el torneo exige, el nombre de quien no da documento, los largos y el número.
 */
class AgregarJugadorUseCase(private val repositorio: DelegadoRepository) {
    suspend operator fun invoke(
        equipoTorneoId: String,
        borrador: BorradorJugador,
        exigeDocumento: Boolean,
        plantel: Plantel? = null,
    ): Result<MiembroDePlantel> {
        val documento = borrador.documento.trim()
        val nombre = borrador.nombre.trim()
        val falla = when {
            exigeDocumento && documento.isEmpty() -> AppError.Validacion(MENSAJE_DOCUMENTO_EXIGIDO, CampoValidado.DOCUMENTO)
            documento.length > DOCUMENTO_MAXIMO -> AppError.Validacion(MENSAJE_DOCUMENTO_LARGO, CampoValidado.DOCUMENTO)
            // Sin documento la persona es nueva: hace falta su nombre.
            documento.isEmpty() && nombre.isEmpty() -> AppError.Validacion(MENSAJE_NOMBRE, CampoValidado.NOMBRES)
            nombre.length > NOMBRE_MAXIMO -> AppError.Validacion(MENSAJE_NOMBRE_LARGO, CampoValidado.NOMBRES)
            else -> null
        }
        if (falla != null) return Result.failure(falla)
        val camiseta = leerCamiseta(borrador.camiseta).getOrElse { return Result.failure(it) }
        if (camiseta != null) {
            plantel?.duenoDelNumero(camiseta)?.let {
                return Result.failure(AppError.Validacion(mensajeNumeroOcupado(camiseta, it.nombre), CampoValidado.CAMISETA))
            }
        }
        return repositorio.agregarJugador(
            equipoTorneoId = equipoTorneoId,
            tipoDocumento = if (documento.isEmpty()) null else borrador.tipoDocumento,
            documento = documento.ifEmpty { null },
            nombre = nombre.ifEmpty { null },
            camiseta = camiseta,
        )
    }

    companion object {
        const val DOCUMENTO_MAXIMO = 20
        const val NOMBRE_MAXIMO = 150
        const val MENSAJE_DOCUMENTO_EXIGIDO = "Este torneo exige documento: escribe el del jugador."
        const val MENSAJE_DOCUMENTO_LARGO = "El documento puede tener hasta 20 caracteres."
        const val MENSAJE_NOMBRE = "Escribe su nombre completo, o su documento si ya tiene cuenta."
        const val MENSAJE_NOMBRE_LARGO = "El nombre puede tener hasta 150 caracteres."
    }
}

/** Cambia (o quita, si queda vacío) el número de camiseta. Si no cambió, no viaja. */
class CambiarCamisetaUseCase(private val repositorio: DelegadoRepository) {
    suspend operator fun invoke(
        equipoTorneoId: String,
        miembro: MiembroDePlantel,
        texto: String,
        plantel: Plantel,
    ): Result<MiembroDePlantel> {
        val numero = leerCamiseta(texto).getOrElse { return Result.failure(it) }
        if (numero == miembro.numeroCamiseta) return Result.success(miembro)
        if (numero != null) {
            plantel.duenoDelNumero(numero, salvo = miembro.personaId)?.let {
                return Result.failure(AppError.Validacion(mensajeNumeroOcupado(numero, it.nombre), CampoValidado.CAMISETA))
            }
        }
        return repositorio.cambiarCamiseta(equipoTorneoId, miembro.personaId, numero)
    }
}

/** Quita a alguien del plantel. Al delegado no: él es quien responde por el equipo. */
class QuitarDelPlantelUseCase(private val repositorio: DelegadoRepository) {
    suspend operator fun invoke(equipoTorneoId: String, miembro: MiembroDePlantel): Result<MiembroDePlantel> {
        if (miembro.esDelegado) return Result.failure(AppError.Validacion(MENSAJE_DELEGADO))
        return repositorio.quitarJugador(equipoTorneoId, miembro.personaId)
    }

    companion object {
        const val MENSAJE_DELEGADO = "No puedes quitarte del plantel: eres su delegado."
    }
}
