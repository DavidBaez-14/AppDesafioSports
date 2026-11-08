package co.edu.ufps.desafiosports.presentacion.navigation

import kotlinx.serialization.Serializable

/**
 * Rutas tipadas de Navigation Compose. Una por pantalla.
 *
 * Hoy solo existe el diagnóstico (MOV-0001). La spec de acceso (F0) agrega arranque, login,
 * registro… y un grafo por modo (Jugador, Árbitro, Creador) con su barra inferior.
 */
@Serializable
data object Diagnostico
