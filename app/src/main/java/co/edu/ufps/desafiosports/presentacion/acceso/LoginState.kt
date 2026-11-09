package co.edu.ufps.desafiosports.presentacion.acceso

/** Estado inmutable del login (MOV-0002). El ViewModel lo reemplaza con copy(). */
data class LoginState(
    val correo: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val enProceso: Boolean = false,
    /** Error bajo el campo de correo (validación). */
    val errorCorreo: String? = null,
    /** Error bajo el campo de contraseña (validación). */
    val errorClave: String? = null,
    /** Aviso general: credenciales, correo sin confirmar, cuenta bloqueada, sin conexión… */
    val error: String? = null,
    /** Evento: entró bien y la pantalla debe navegar al inicio. Se apaga con onIngresoAtendido(). */
    val ingresado: Boolean = false,
)
