package com.example.busify.domain.model

import androidx.navigation.NavType

data class Route(
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val price: Double=0.0,
    val busType: String = "",
    val seatsAvailable: Int = 0,
    val totalSeats: Int = 0,
    val duration: String = "",
    val status: String = "Pendiente",
    val capacity: Int = 0,
    val driverId: String = "",
    val company: String = ""
)