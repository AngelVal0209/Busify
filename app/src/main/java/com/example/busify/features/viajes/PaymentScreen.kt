package com.example.busify.features.viajes

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    company: String,
    origin: String,
    destination: String,
    seat: Int,
    price: Double
) {

    val context = LocalContext.current

    var selectedMethod by remember {
        mutableStateOf("Yape")
    }

    Scaffold(

        // =========================
        // TOP BAR
        // =========================
        topBar = {

            TopAppBar(

                title = {

                    Text("Pago")
                },

                navigationIcon = {

                    IconButton(
                        onClick = {

                            navController.popBackStack()
                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
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

            Text(
                text = "Resumen de Compra",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Empresa: $company",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Ruta: $origin → $destination"
            )

            Text(
                text = "Asiento: $seat"
            )

            Text(
                text = "Precio: S/ $price"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Método de Pago",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row {

                Button(
                    onClick = {

                        selectedMethod = "Yape"
                    }
                ) {

                    Text("Yape")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {

                        selectedMethod = "Visa"
                    }
                ) {

                    Text("Visa")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Seleccionado: $selectedMethod"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(

                onClick = {

                    Toast.makeText(
                        context,
                        "Pago realizado",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(
                        "ticket/$company/$origin/$destination/$seat/$price/$selectedMethod"
                    )
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Pagar")
            }
        }
    }
}