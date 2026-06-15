package com.example.busify.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.busify.core.components.NetworkBanner
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
import com.example.busify.features.onboarding.OnboardingScreen

@Composable
fun BusifyNavigation(
    viewModel: AuthViewModel = viewModel(),
    onboardingComplete: Boolean = false,
    onOnboardingComplete: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentUser by remember { mutableStateOf(viewModel.currentUserData.value) }
    val startDestination = when {
        !onboardingComplete -> Screen.Onboarding.route
        currentUser != null -> Screen.Home.route
        else -> Screen.Login.route
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Buses.route, Screen.Admin.route,
        Screen.Profile.route, Screen.Viajes.route, Screen.Driver.route
    )

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
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
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
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable(Screen.Onboarding.route) {
                    NetworkBanner()
                    OnboardingScreen(
                        onComplete = {
                            onOnboardingComplete()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Login.route) {
                    NetworkBanner()
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                    )
                }
                composable(Screen.Register.route) {
                    NetworkBanner()
                    RegisterScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Home.route) {
                    NetworkBanner()
                    HomeScreen(authViewModel = viewModel)
                }
                composable(Screen.Buses.route) {
                    NetworkBanner()
                    BusesScreen()
                }
                composable(Screen.Admin.route) {
                    NetworkBanner()
                    AdminScreen(authViewModel = viewModel)
                }
                composable(Screen.Profile.route) {
                    NetworkBanner()
                    ProfileScreen(onLogout = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    })
                }
                composable(Screen.Viajes.route) {
                    NetworkBanner()
                    ViajesScreen(navController)
                }
                composable(Screen.Driver.route) {
                    NetworkBanner()
                    DriverScreen()
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
    }
}
