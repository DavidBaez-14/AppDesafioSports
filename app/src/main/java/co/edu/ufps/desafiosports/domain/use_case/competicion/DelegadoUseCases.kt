package co.edu.ufps.desafiosports.domain.use_case.competicion

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
