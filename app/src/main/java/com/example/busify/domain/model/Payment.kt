package com.example.busify.domain.model

data class Payment(
    val id: String = "",
    val ticketId: String = "",
    val amount: Double = 0.0,
    val method: String = "",
    val status: String = "completado",
    val timestamp: Long = System.currentTimeMillis()
)