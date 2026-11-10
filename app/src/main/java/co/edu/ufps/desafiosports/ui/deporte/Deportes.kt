package co.edu.ufps.desafiosports.ui.deporte

import androidx.annotation.DrawableRes
import co.edu.ufps.desafiosports.R

// Todo lo que cambia según el deporte se resuelve aquí, por el código que manda el backend
// (arquitectura §5.2, regla 8). Un código que la app no conoce usa el caso genérico.

/** Icono del deporte por su código del catálogo (`GET /api/public/deportes`). */
@DrawableRes
fun iconoDeDeporte(codigo: String?): Int = when (codigo) {
    "FUTSAL" -> R.drawable.ic_sports_soccer
    "VOLEIBOL" -> R.drawable.ic_sports_volleyball
    "BALONCESTO" -> R.drawable.ic_sports_basketball
    "TENIS_MESA" -> R.drawable.ic_sports_tennis
    else -> R.drawable.ic_trophy
}
