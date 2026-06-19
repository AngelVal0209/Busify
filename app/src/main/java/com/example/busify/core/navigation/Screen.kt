package com.example.busify.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Buses : Screen("buses")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
    object Viajes : Screen("viajes")
    object Driver : Screen("driver")

    object Promos : Screen("promos")

    object Seats : Screen("seats/{routeId}/{company}/{origin}/{destination}/{price}/{departureTime}")
    object Payment : Screen("payment/{routeId}/{company}/{origin}/{destination}/{seats}/{price}/{departureTime}")
    object Ticket : Screen("ticket/{routeId}/{company}/{origin}/{destination}/{seats}/{price}/{paymentMethod}/{departureTime}")
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Outlined.Home, Icons.Outlined.Home)
    object Buses : BottomNavItem(Screen.Buses.route, "Buses", Icons.Outlined.DirectionsBus, Icons.Outlined.DirectionsBus)
    object Admin : BottomNavItem(Screen.Admin.route, "Admin", Icons.Outlined.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
    object Profile : BottomNavItem(Screen.Profile.route, "Perfil", Icons.Outlined.Person, Icons.Outlined.Person)
    object Viajes : BottomNavItem(Screen.Viajes.route, "Viajes", Icons.Outlined.ConfirmationNumber, Icons.Outlined.ConfirmationNumber)
    object Driver : BottomNavItem(Screen.Driver.route, "Conducir", Icons.Outlined.Navigation, Icons.Outlined.Navigation)
}
