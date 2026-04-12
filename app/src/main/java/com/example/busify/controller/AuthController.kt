package com.example.busify.controller

import com.example.busify.model.User

class AuthController {
    // Simulación de una base de datos de usuarios
    private val mockUsers = mutableListOf(
        User("admin@busify.com", "Admin")
    )

    fun login(email: String, pass: String): Boolean {
        // Lógica de validación simple
        return email.isNotBlank() && pass.length >= 6
    }

    fun register(email: String, pass: String, nombre: String): Boolean {
        if (email.isBlank() || pass.length < 6) return false
        mockUsers.add(User(email, nombre))
        return true
    }

    fun recoverPassword(email: String): Boolean {
        return email.contains("@")
    }
}
