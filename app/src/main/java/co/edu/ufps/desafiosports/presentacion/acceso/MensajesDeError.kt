package co.edu.ufps.desafiosports.presentacion.acceso

import co.edu.ufps.desafiosports.domain.error.AppError

/** El texto que ve la persona: el de [AppError] o, si no es uno, el genérico. */
internal fun Throwable.mensajeParaMostrar(): String =
    (this as? AppError)?.mensaje ?: AppError.Desconocido(this).mensaje
