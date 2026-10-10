package com.example.utils

import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val PREFIX = "pbkdf2_sha256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also(SecureRandom()::nextBytes)
        return encode(password, salt, ITERATIONS)
    }

    fun verify(password: String, encodedPassword: String): Boolean {
        val parts = encodedPassword.split('$')
        if (parts.size != 4 || parts[0] != PREFIX) return false

        val iterations = parts[1].toIntOrNull()?.takeIf { it in 10_000..1_000_000 } ?: return false
        val salt = decodeHex(parts[2]) ?: return false
        val expectedHash = decodeHex(parts[3]) ?: return false
        if (salt.size != SALT_LENGTH_BYTES || expectedHash.size != KEY_LENGTH_BITS / 8) return false

        return MessageDigest.isEqual(
            deriveKey(password, salt, iterations),
            expectedHash
        )
    }

    fun isHashed(value: String): Boolean = value.startsWith("$PREFIX$")

    private fun encode(password: String, salt: ByteArray, iterations: Int): String {
        val hash = deriveKey(password, salt, iterations)
        return "$PREFIX\$$iterations\$${salt.toHex()}\$${hash.toHex()}"
    }

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val keySpec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(keySpec)
                .encoded
        } catch (exception: GeneralSecurityException) {
            throw IllegalStateException("No se pudo verificar la contraseña.", exception)
        } finally {
            keySpec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        val digits = "0123456789abcdef"
        this@toHex.forEach { byte ->
            val value = byte.toInt() and 0xff
            append(digits[value ushr 4])
            append(digits[value and 0x0f])
        }
    }

    private fun decodeHex(value: String): ByteArray? {
        if (value.length % 2 != 0 || value.any { it.digitToIntOrNull(16) == null }) return null
        return ByteArray(value.length / 2) { index ->
            value.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
