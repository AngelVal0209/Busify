package com.example.busify.domain.model

data class Route(
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val status: String = "Pendiente",
    val capacity: Int = 0,
    val driverId: String = ""
)