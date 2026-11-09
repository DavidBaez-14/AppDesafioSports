package co.edu.ufps.desafiosports.presentacion.acceso

/** Estado inmutable de Recuperar contraseña (MOV-0002 · N-3). */
data class RecuperarClaveState(
    val correo: String = "",
    val enProceso: Boolean = false,
    val errorCorreo: String? = null,
    val error: String? = null,
    /** Ya se pidió el correo: la pantalla muestra "Revisa tu correo". */
    val enviado: Boolean = false,
)
