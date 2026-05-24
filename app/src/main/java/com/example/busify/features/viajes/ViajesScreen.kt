package com.example.busify.features.viajes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.domain.model.Route

@Composable
fun ViajesScreen(
    navController: NavController
) {

    var origin by remember { mutableStateOf("") }

    var destination by remember { mutableStateOf("") }

    val routes = listOf(

        Route(
            id = "1",
            origin = "Lima",
            destination = "Huánuco",
            company = "Movil Bus",
            departureTime = "08:00 PM",
            price = 70.0,
            seatsAvailable = 30
        ),

        Route(
            id = "2",
            origin = "Lima",
            destination = "Huánuco",
            company = "Cruz del Sur",
            departureTime = "10:00 PM",
            price = 90.0,
            seatsAvailable = 20
        )
    )

    var filteredRoutes by remember {
        mutableStateOf(routes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Buscar Viajes",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = origin,
            onValueChange = { origin = it },
            label = { Text("Origen") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Destino") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {

                filteredRoutes = routes.filter {

                    it.origin.contains(origin, true) &&
                            it.destination.contains(destination, true)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            items(filteredRoutes) { route ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text("${route.origin} → ${route.destination}")

                        Text(route.company)

                        Text(route.departureTime)

                        Text("S/ ${route.price}")

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {

                                navController.navigate(
                                    "seats/${route.company}/${route.origin}/${route.destination}/${route.price}"
                                )
                            }
                        ) {

                            Text("Seleccionar")
                        }
                    }
                }
            }
        }
    }
}