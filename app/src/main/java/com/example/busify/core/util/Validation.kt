package com.example.busify.core.util

import android.util.Patterns

object Validation {
    fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "La contraseña debe tener al menos 8 caracteres")
        }
        if (!password.any { it.isLetter() }) {
            return ValidationResult(false, "La contraseña debe contener al menos una letra")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "La contraseña debe contener al menos un número")
        }
        return ValidationResult(true)
    }

    fun isValidName(name: String): Boolean = name.isNotBlank() && name.length >= 2
}

data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null)
