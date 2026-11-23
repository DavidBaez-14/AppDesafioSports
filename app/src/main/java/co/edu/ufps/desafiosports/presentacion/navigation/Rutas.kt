package co.edu.ufps.desafiosports.presentacion.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas tipadas de Navigation Compose. Una por pantalla.
 *
 * El acceso (MOV-0002, 0003 y 0006) lleva a [Principal] (MOV-0004), de donde cuelgan las
 * secciones Árbitro y Creador y la gestión de cada torneo (MOV-0011).
 */

/** Primera pantalla: muestra el logotipo mientras se lee la sesión guardada (A-1). */
@Serializable
data object Arranque

/** Login con correo (A-2). */
@Serializable
data object Login

/** Recuperar contraseña (N-3). Trae el correo escrito en el login, si lo hay. */
@Serializable
data class RecuperarClave(val correo: String = "")

/** Crear cuenta (MOV-0003 · A-3). */
@Serializable
data object Registro

/** Revisa tu correo tras crear la cuenta (MOV-0003 · N-1). */
@Serializable
data class RevisaTuCorreo(val correo: String)

/** Completar registro: sesión de Supabase abierta, sin cuenta en el backend (MOV-0003 · N-2). */
@Serializable
data object CompletarRegistro

/** La pantalla principal: la barra inferior con Inicio, Explorar y Perfil (MOV-0004). */
@Serializable
data object Principal

/** Diagnóstico de conexión (MOV-0001). Solo se abre en debug, desde Perfil o el login. */
@Serializable
data object Diagnostico

// Los nombres de las rutas de las secciones son los que fija MOV-0004 §11.1: Inicio las enlaza.

/** Sección Árbitro: los partidos que arbitra la persona (MOV-0009). */
@Serializable
data object SeccionArbitro

/** Planilla de un partido (MOV-0009). */
@Serializable
data class Planilla(val partidoId: String)

/** Sección Creador: los torneos que administra la persona (MOV-0008). */
@Serializable
data object SeccionCreador

/** Crear torneo en tres pasos (MOV-0007). */
@Serializable
data object CrearTorneo

/** Inscripciones de un torneo que administra: aprobar y rechazar (MOV-0008). */
@Serializable
data class InscripcionesTorneo(val torneoId: String, val nombre: String = "")

// Gestión de un torneo que administra (MOV-0011).

/** Las tres puertas de un torneo: inscripciones, árbitros y partidos. */
@Serializable
data class GestionTorneo(val torneoId: String, val nombre: String = "")

/** Árbitros del torneo e invitaciones pendientes. */
@Serializable
data class ArbitrosTorneo(val torneoId: String, val nombre: String = "")

/** Partidos del torneo: generarlos y asignarles árbitros. */
@Serializable
data class PartidosTorneo(val torneoId: String, val nombre: String = "")

/** Aceptar una invitación recibida: se pega el enlace. */
@Serializable
data object Invitacion

// Explorar y detalle del torneo (MOV-0005).

/** Explorar torneos públicos. Con [visitante], se entró sin cuenta desde el login. */
@Serializable
data class Explorar(val visitante: Boolean = false)

/** Detalle de un torneo, por su id o por el token de su enlace (los NO_LISTADO solo abren así). */
@Serializable
data class Torneo(val torneoId: String = "", val enlaceToken: String = "")

/**
 * Inscribir un equipo en un torneo (MOV-0014). Lleva lo que la pantalla muestra antes de pedir
 * nada: el nombre, la cuota (en pesos, 0 si no cobra) y si exige documento. [enlaceToken] hace
 * falta para inscribirse en un torneo que solo se abre con su enlace.
 */
@Serializable
data class InscribirEquipo(
    val torneoId: String,
    val nombre: String = "",
    val enlaceToken: String = "",
    val cuota: Long = 0,
    val exigeDocumento: Boolean = false,
)

/**
 * El plantel de un equipo en un torneo, para su delegado (MOV-0015). Se llama así y no `Plantel`
 * para no confundirse con el modelo del dominio.
 */
@Serializable
data class PlantelEquipo(
    val equipoTorneoId: String,
    val equipo: String = "",
    val torneo: String = "",
    val exigeDocumento: Boolean = false,
)

/** Agregar jugadores al plantel (MOV-0015). */
@Serializable
data class AgregarJugador(
    val equipoTorneoId: String,
    val equipo: String = "",
    val exigeDocumento: Boolean = false,
)
