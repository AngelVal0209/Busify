package com.example.busify.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Buses : Screen("buses")
    object Profile : Screen("profile")
    object Admin : Screen("admin")
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home)
    object Buses : BottomNavItem(Screen.Buses.route, "Buses", Icons.Default.DirectionsBus)
    object Admin : BottomNavItem(Screen.Admin.route, "Admin", Icons.Default.AdminPanelSettings)
    object Profile : BottomNavItem(Screen.Profile.route, "Perfil", Icons.Default.Person)
}
