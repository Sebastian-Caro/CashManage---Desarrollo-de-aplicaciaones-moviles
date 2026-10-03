package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordSecurity {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH_BITS = 256
    private const val ITERATIONS = 120_000
    private const val SALT_BYTES = 16

    fun hash(password: String): String {
        val normalized = password.trim()
        require(normalized.isNotEmpty()) { "Password cannot be blank" }

        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)

        val hash = pbkdf2(normalized.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return "pbkdf2:$ITERATIONS:${base64(salt)}:${base64(hash)}"
    }

    fun verify(rawPassword: String, storedHash: String): Boolean {
        val password = rawPassword.trim()
        if (password.isEmpty() || storedHash.isBlank()) {
            return false
        }

        if (storedHash == password) {
            return true
        }

        val parts = storedHash.split(":")
        if (parts.size != 4 || parts[0] != "pbkdf2") {
            return false
        }

        return try {
            val iterations = parts[1].toInt()
            val salt = Base64.getDecoder().decode(parts[2])
            val expectedHash = Base64.getDecoder().decode(parts[3])
            val computedHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.size * 8)
            MessageDigest.isEqual(expectedHash, computedHash)
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: NumberFormatException) {
            false
        }
    }

    private fun pbkdf2(
        password: CharArray,
        salt: ByteArray,
        iterations: Int,
        keyLengthBits: Int
    ): ByteArray {
        val keySpec = PBEKeySpec(password, salt, iterations, keyLengthBits)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(keySpec).encoded
    }

    private fun base64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
}
