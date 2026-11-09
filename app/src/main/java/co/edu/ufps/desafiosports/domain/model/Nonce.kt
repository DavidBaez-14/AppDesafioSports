package co.edu.ufps.desafiosports.domain.model

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Nonce del login con Google (MOV-0006 · CA-8): evita que un ID token robado se reutilice.
 * A Google se le pide el token con el SHA-256 del nonce; a Supabase se le pasa el nonce crudo
 * y él comprueba que el hash coincida con el que trae el token.
 */
object Nonce {
    private val aleatorio = SecureRandom()

    /** 32 bytes aleatorios en hexadecimal (64 caracteres). */
    fun nuevo(): String = ByteArray(32).also(aleatorio::nextBytes).aHex()

    fun sha256(crudo: String): String = MessageDigest.getInstance("SHA-256").digest(crudo.toByteArray()).aHex()

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }
}
