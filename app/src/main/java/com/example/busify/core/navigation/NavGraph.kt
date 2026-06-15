package com.example.busify.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

import com.example.busify.features.auth.AuthViewModel
import com.example.busify.features.auth.LoginScreen
import com.example.busify.features.auth.RegisterScreen
import com.example.busify.features.home.HomeScreen
import com.example.busify.features.buses.BusesScreen
import com.example.busify.features.profile.ProfileScreen
import com.example.busify.features.admin.AdminScreen
import com.example.busify.features.driver.DriverScreen
import com.example.busify.features.viajes.ViajesScreen
import com.example.busify.features.viajes.SeatSelectionScreen
import com.example.busify.features.viajes.PaymentScreen
import com.example.busify.features.viajes.TicketScreen

@Composable
fun BusifyNavigation(
    viewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentUser = viewModel.currentUserData.value

    val startDestination = remember(currentUser) {
        if (currentUser != null) Screen.Home.route else Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Home.route) {
            MainScaffold(currentRoute = Screen.Home.route, navController = navController, viewModel = viewModel) {
                HomeScreen(authViewModel = viewModel)
            }
        }
        composable(Screen.Buses.route) {
            MainScaffold(currentRoute = Screen.Buses.route, navController = navController, viewModel = viewModel) {
                BusesScreen()
            }
        }
        composable(Screen.Admin.route) {
            MainScaffold(currentRoute = Screen.Admin.route, navController = navController, viewModel = viewModel) {
                AdminScreen(authViewModel = viewModel)
            }
        }
        composable(Screen.Profile.route) {
            MainScaffold(currentRoute = Screen.Profile.route, navController = navController, viewModel = viewModel) {
                ProfileScreen(onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                })
            }
        }
        composable(Screen.Viajes.route) {
            MainScaffold(currentRoute = Screen.Viajes.route, navController = navController, viewModel = viewModel) {
                ViajesScreen(navController)
            }
        }
        composable(Screen.Driver.route) {
            MainScaffold(currentRoute = Screen.Driver.route, navController = navController, viewModel = viewModel) {
                DriverScreen()
            }
        }
        composable(route = Screen.Seats.route) {
            SeatSelectionScreen(
                navController = navController,
                routeId = it.arguments?.getString("routeId") ?: "",
                company = it.arguments?.getString("company") ?: "",
                origin = it.arguments?.getString("origin") ?: "",
                destination = it.arguments?.getString("destination") ?: "",
                price = it.arguments?.getString("price")?.toDouble() ?: 0.0,
                departureTime = it.arguments?.getString("departureTime") ?: ""
            )
        }
        composable(route = Screen.Payment.route) {
            PaymentScreen(
                navController = navController,
                routeId = it.arguments?.getString("routeId") ?: "",
                company = it.arguments?.getString("company") ?: "",
                origin = it.arguments?.getString("origin") ?: "",
                destination = it.arguments?.getString("destination") ?: "",
                seats = it.arguments?.getString("seats") ?: "",
                price = it.arguments?.getString("price")?.toDouble() ?: 0.0,
                departureTime = it.arguments?.getString("departureTime") ?: ""
            )
        }
        composable(route = Screen.Ticket.route) {
            TicketScreen(
                navController = navController,
                routeId = it.arguments?.getString("routeId") ?: "",
                company = it.arguments?.getString("company") ?: "",
                origin = it.arguments?.getString("origin") ?: "",
                destination = it.arguments?.getString("destination") ?: "",
                seats = it.arguments?.getString("seats") ?: "",
                price = it.arguments?.getString("price")?.toDouble() ?: 0.0,
                paymentMethod = it.arguments?.getString("paymentMethod") ?: "",
                departureTime = it.arguments?.getString("departureTime") ?: ""
            )
        }
    }
}

@Composable
fun MainScaffold(
    currentRoute: String,
    navController: androidx.navigation.NavHostController,
    viewModel: AuthViewModel,
    content: @Composable () -> Unit
) {
    val userData = viewModel.currentUserData.value

    val items = remember(userData) {
        mutableListOf(BottomNavItem.Home, BottomNavItem.Buses).apply {
            if (userData?.role == 2L) add(BottomNavItem.Admin)
            if (userData?.role == 3L) add(BottomNavItem.Driver)
            add(BottomNavItem.Viajes)
            add(BottomNavItem.Profile)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) { content() }
    }
}
