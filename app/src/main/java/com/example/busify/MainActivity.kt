package com.example.busify

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.busify.controller.AuthController
import com.example.busify.ui.theme.BusifyTheme
import com.example.busify.view.ForgotPasswordScreen
import com.example.busify.view.LoginScreen
import com.example.busify.view.RegisterScreen

class MainActivity : ComponentActivity() {
    private val authController = AuthController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusifyTheme {
                BusifyApp(authController)
            }
        }
    }
}

@Composable
fun BusifyApp(authController: AuthController) {
    val navController = rememberNavController()
    val context = LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    authController = authController,
                    onLoginSuccess = {
                        Toast.makeText(context, "¡Bienvenido a Busify!", Toast.LENGTH_SHORT).show()
                        // Aquí podrías navegar a una pantalla de Home en el futuro
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgot = { navController.navigate("forgot_password") }
                )
            }
            composable("register") {
                RegisterScreen(
                    authController = authController,
                    onRegisterSuccess = {
                        Toast.makeText(context, "Registro exitoso. Por favor inicia sesión.", Toast.LENGTH_LONG).show()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(
                    authController = authController,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
