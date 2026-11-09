package co.edu.ufps.desafiosports.ui.theme

import androidx.compose.ui.graphics.Color

// Tokens de Desafío Sports (MOV-0002), los mismos del prototipo aprobado (docs/prototipo-roles).
// Solo se usan dentro de ui/theme: las pantallas leen MaterialTheme.colorScheme, nunca estos valores.
//
// Oscuro (el principal): azul marino casi negro con el naranja de la web (Code Cup Design System).
// Claro: gris azulado claro con tinta azul marino; el naranja baja a #C23C00 para que el texto
// blanco del botón pase AA (5,3:1). Los bordes de campos (#6878A8) pasan 3:1 en los dos temas.

// ── Oscuro ──────────────────────────────────────────────────────────────
internal val OscuroFondo = Color(0xFF04070F)
internal val OscuroContenedorMinimo = Color(0xFF020409)
internal val OscuroContenedorBajo = Color(0xFF090E1C)
internal val OscuroContenedor = Color(0xFF0D1429)
internal val OscuroContenedorAlto = Color(0xFF151F3F)
internal val OscuroContenedorMaximo = Color(0xFF1E2D5A)
internal val OscuroTinta = Color(0xFFEEF0F7)
internal val OscuroTintaSecundaria = Color(0xFFA9B1CC)
internal val OscuroNaranja = Color(0xFFFF5500)
internal val OscuroNaranjaContenedor = Color(0xFF4A1700)
internal val OscuroSobreNaranjaContenedor = Color(0xFFFFDBC9)
internal val OscuroAzul = Color(0xFF93C5FD)
internal val OscuroAzulContenedor = Color(0xFF0D2350)
internal val OscuroAzulSobreContenedor = Color(0xFFDBEAFE)
internal val OscuroError = Color(0xFFF87171)
internal val OscuroErrorContenedor = Color(0xFF3B1114)
internal val OscuroSobreErrorContenedor = Color(0xFFFECACA)

// ── Claro ───────────────────────────────────────────────────────────────
internal val ClaroFondo = Color(0xFFF4F5F9)
internal val ClaroContenedorMinimo = Color(0xFFFFFFFF)
internal val ClaroContenedorBajo = Color(0xFFF0F2F7)
internal val ClaroContenedor = Color(0xFFEBEEF5)
internal val ClaroContenedorAlto = Color(0xFFE5E8F0)
internal val ClaroContenedorMaximo = Color(0xFFDFE3EC)
internal val ClaroTinta = Color(0xFF0D1429)
internal val ClaroTintaSecundaria = Color(0xFF4A5578)
internal val ClaroNaranja = Color(0xFFC23C00)
internal val ClaroNaranjaContenedor = Color(0xFFFFE4CC)
internal val ClaroSobreNaranjaContenedor = Color(0xFF5C1A00)
internal val ClaroAzul = Color(0xFF1D4ED8)
internal val ClaroAzulContenedor = Color(0xFFDBEAFE)
internal val ClaroAzulSobreContenedor = Color(0xFF0D2350)
internal val ClaroError = Color(0xFFB91C1C)
internal val ClaroErrorContenedor = Color(0xFFFEE2E2)
internal val ClaroSobreErrorContenedor = Color(0xFF7F1D1D)

// ── Comunes ─────────────────────────────────────────────────────────────
internal val Borde = Color(0xFF6878A8)
internal val Blanco = Color(0xFFFFFFFF)
internal val Negro = Color(0xFF000000)
