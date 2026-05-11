package com.example.busify.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: Int = 1 // 1 = Usuario, 2 = Administrador, 3 = Chofer
)