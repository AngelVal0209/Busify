package com.example.busify.domain.model

data class SavedCard(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val holderName: String = "",
    val lastDigits: String = "",
    val ccv: String = "",
    val expiryDate: String = "",
    val phoneNumber: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
