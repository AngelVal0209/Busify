package com.example.busify.domain.model

data class Route(
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val departureDate: Long = 0L,
    val arrivalDate: Long = 0L,
    val price: Double = 0.0,
    val busType: String = "",
    val duration: String = "",
    val status: String = "Pendiente",
    val capacity: Long = 0L,
    val company: String = "",
    val driverId: String = "",
    val driverName: String = ""
)