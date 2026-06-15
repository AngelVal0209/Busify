package com.example.busify.domain.model

data class SavedCard(
    val id: String = "",
    val userId: String = "",
    val type: String = "", // Yape, Visa, Plin
    val holderName: String = "",
    val lastDigits: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
