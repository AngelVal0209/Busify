package com.example.busify.features.viajes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.core.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(

    navController: NavController,

    company: String,

    origin: String,

    destination: String,

    seat: Int,

    price: Double,

    paymentMethod: String
) {

    Scaffold(

        // =========================
        // TOP BAR
        // =========================
        topBar = {

            TopAppBar(

                title = {

                    Text("Ticket")
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // =========================
            // TITULO
            // =========================
            Text(

                text = "Ticket Generado",

                style = MaterialTheme.typography.headlineMedium,

                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // =========================
            // INFORMACION
            // =========================
            Card(

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(

                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Empresa: $company"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ruta: $origin → $destination"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Asiento: $seat"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Método de pago: $paymentMethod"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Total pagado: S/ $price"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // =========================
            // MENSAJE
            // =========================
            Text(

                text = "Gracias por viajar con Busify",

                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // =========================
            // BOTON VOLVER
            // =========================
            Button(

                onClick = {

                    navController.navigate(Screen.Home.route) {

                        popUpTo(Screen.Home.route) {

                            inclusive = false
                        }

                        launchSingleTop = true
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    Icons.Default.Home,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Volver al inicio")
            }
        }
    }
}