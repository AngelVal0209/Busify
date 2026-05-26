package com.example.busify.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object MisViajes : Screen("misviajes")
    object Cuenta : Screen("cuenta")
    object Admin : Screen("admin")
    object Viajes : Screen("viajes")

    object Seats : Screen("seats/{routeId}/{company}/{origin}/{destination}/{price}/{departureTime}/{capacity}")

    object Payment : Screen("payment/{routeId}/{company}/{origin}/{destination}/{seats}/{price}/{departureTime}")

    object Ticket : Screen("ticket/{routeId}/{company}/{origin}/{destination}/{seats}/{price}/{paymentMethod}/{departureTime}")
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home)
    object Viajes : BottomNavItem(Screen.Viajes.route, "Viajes", Icons.Default.DirectionsBus)
    object MisViajes : BottomNavItem(Screen.MisViajes.route, "Mis Viajes", Icons.Default.History)
    object Admin : BottomNavItem(Screen.Admin.route, "Admin", Icons.Default.AdminPanelSettings)
    object Cuenta : BottomNavItem(Screen.Cuenta.route, "Cuenta", Icons.Default.Person)
}
