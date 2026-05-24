package com.example.busify.domain.model

data class Ticket(

    val userId: String = "",

    val routeId: String = "",

    val company: String = "",

    val origin: String = "",

    val destination: String = "",

    val seatNumber: Int = 0,

    val totalPrice: Double = 0.0,

    val paymentMethod: String = "",

    val status: String = ""
)