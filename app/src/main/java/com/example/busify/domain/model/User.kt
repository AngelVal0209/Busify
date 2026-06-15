package com.example.busify.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: Long = 1L,
    val favoriteRoutes: List<String> = emptyList()
)
