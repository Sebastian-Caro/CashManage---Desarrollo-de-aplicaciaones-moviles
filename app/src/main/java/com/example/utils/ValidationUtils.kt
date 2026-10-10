package com.example.utils

data class PasswordValidationResult(
    val isValid: Boolean,
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val errorMessage: String? = null
)

object PasswordValidator {
    const val MIN_LENGTH = 8

    fun validate(password: String): PasswordValidationResult {
        val hasMinLength = password.length >= MIN_LENGTH
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }

        val isValid = hasMinLength && hasUppercase && hasLowercase

        val errorMessage = when {
            !hasMinLength -> "La contraseña debe tener al menos $MIN_LENGTH caracteres."
            !hasUppercase -> "La contraseña debe contener al menos una letra mayúscula."
            !hasLowercase -> "La contraseña debe contener al menos una letra minúscula."
            else -> null
        }

        return PasswordValidationResult(
            isValid = isValid,
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            errorMessage = errorMessage
        )
    }
}
