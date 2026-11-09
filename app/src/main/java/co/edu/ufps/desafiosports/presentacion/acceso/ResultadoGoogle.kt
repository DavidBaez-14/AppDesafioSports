package co.edu.ufps.desafiosports.presentacion.acceso

/**
 * Lo que devuelve el selector de cuentas de Google (MOV-0006). Kotlin puro: el ViewModel lo
 * recibe sin conocer Credential Manager, y se prueba en la JVM.
 */
sealed interface ResultadoGoogle {
    /** La persona eligió una cuenta. [nonce] es el crudo; a Google se le envió su SHA-256. */
    data class Token(val idToken: String, val nonce: String) : ResultadoGoogle

    /** Cerró el selector sin elegir: no es un error. */
    data object Cancelado : ResultadoGoogle

    /** Sin cuentas en el teléfono, mala configuración u otro fallo, con el texto para mostrar. */
    data class Fallo(val mensaje: String) : ResultadoGoogle
}
