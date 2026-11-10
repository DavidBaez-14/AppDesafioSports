package co.edu.ufps.desafiosports.presentacion.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas tipadas de Navigation Compose. Una por pantalla.
 *
 * MOV-0002 agrega el acceso: arranque, login y recuperar contraseña. Inicio, con las
 * secciones Creador y Árbitro (PRODUCT.md), llega con su propia spec; mientras tanto el
 * diagnóstico de MOV-0001 hace de inicio provisional.
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

/** Diagnóstico de conexión (MOV-0001). Hoy también es el inicio provisional. */
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
