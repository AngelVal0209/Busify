package com.example.busify.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.busify.features.auth.AuthViewModel
import com.example.busify.features.auth.LoginScreen
import com.example.busify.features.auth.RegisterScreen
import com.example.busify.features.buses.BusesScreen
import com.example.busify.features.home.HomeScreen
import com.example.busify.features.profile.ProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.busify.features.admin.AdminScreen

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
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Home.route) {
            MainScaffold(Screen.Home.route, navController, viewModel) { HomeScreen() }
        }
        composable(Screen.Buses.route) {
            MainScaffold(Screen.Buses.route, navController, viewModel) { BusesScreen() }
        }
        composable(Screen.Admin.route) {
            MainScaffold(Screen.Admin.route, navController, viewModel) { AdminScreen() }
        }
        composable(Screen.Profile.route) {
            MainScaffold(Screen.Profile.route, navController, viewModel) { 
                ProfileScreen(
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
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
        mutableListOf(
            BottomNavItem.Home,
            BottomNavItem.Buses
        ).apply {
            if (userData?.role == 2) {
                add(BottomNavItem.Admin)
            }
            add(BottomNavItem.Profile)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
        Surface(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
