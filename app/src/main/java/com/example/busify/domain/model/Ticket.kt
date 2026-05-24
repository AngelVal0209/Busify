package com.example.busify.domain.model

data class Ticket(
    val id: String = "",
    val userId: String = "",
    val routeId: String = "",
    val company: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val seatNumbers: List<Long> = emptyList(),
    val totalPrice: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "confirmado",
    val createdAt: Long = System.currentTimeMillis()
)