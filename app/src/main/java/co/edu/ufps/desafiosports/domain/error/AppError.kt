package co.edu.ufps.desafiosports.domain.error

/**
 * Errores de la app, ya traducidos a algo que la interfaz entiende.
 *
 * Viajan dentro de [Result.failure], igual que en el proyecto del profesor, pero tipados:
 * la pantalla decide qué hacer según el tipo, nunca leyendo textos.
 * Los errores del backend llegan por su `code` estable (application/problem+json) y se
 * muestran con el `mensaje` que manda el backend (ver data/remote/red/ErroresHttp).
 */
sealed class AppError(val mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa) {

    /** Validación local, antes de llamar a la red (correo mal escrito, contraseña corta…). */
    class Validacion(mensaje: String) : AppError(mensaje)

    /** No se pudo hablar con el servidor: sin red, servidor apagado o tiempo agotado. */
    class SinConexion(causa: Throwable? = null) :
        AppError("No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.", causa)

    class CredencialesInvalidas : AppError("Correo o contraseña incorrectos.")

    class CorreoNoConfirmado :
        AppError("Confirma tu correo desde el enlace que te enviamos y vuelve a intentarlo.")

    /** 401: no hay sesión o venció y no se pudo renovar. */
    class NoAutenticado(mensaje: String = "Tu sesión venció. Inicia sesión de nuevo.") : AppError(mensaje)

    /** 404 CUENTA_NO_REGISTRADA: inició sesión en Supabase pero no ha completado el registro. */
    class CuentaNoRegistrada(mensaje: String) : AppError(mensaje)

    /** 403 CUENTA_BLOQUEADA: un administrador bloqueó la cuenta. La sesión local se cierra. */
    class CuentaBloqueada(mensaje: String) : AppError(mensaje)

    /** 403 ACCESO_DENEGADO: la cuenta no tiene permiso para esta acción. */
    class AccesoDenegado(mensaje: String) : AppError(mensaje)

    class NoEncontrado(mensaje: String) : AppError(mensaje)

    /** Cualquier otra regla de negocio del backend, con su `code` para decidir sin parsear textos. */
    class Negocio(val codigo: String, mensaje: String) : AppError(mensaje)

    /** Falta configurar claves en local.properties. */
    class ConfiguracionIncompleta(val faltantes: List<String>) :
        AppError("Falta configurar en local.properties: ${faltantes.joinToString()}.")

    class Desconocido(causa: Throwable? = null) : AppError("Ocurrió un error inesperado.", causa)
}
