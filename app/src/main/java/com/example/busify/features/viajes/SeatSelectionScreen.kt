package com.example.busify.features.viajes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    navController: NavController,
    company: String,
    origin: String,
    destination: String,
    price: Double
) {

    val seats = (1..40).toList()

    var selectedSeat by remember {
        mutableStateOf(-1)
    }

    Scaffold(

        // =========================
        // TOP BAR
        // =========================
        topBar = {

            TopAppBar(

                title = {

                    Text("Seleccionar Asiento")
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
                text = "Empresa: $company",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "$origin → $destination"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Selecciona tu asiento",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(

                columns = GridCells.Fixed(4),

                horizontalArrangement = Arrangement.spacedBy(12.dp),

                verticalArrangement = Arrangement.spacedBy(12.dp),

                modifier = Modifier.weight(1f)
            ) {

                items(seats) { seat ->

                    Box(

                        modifier = Modifier
                            .size(60.dp)

                            .background(

                                if (selectedSeat == seat)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.LightGray
                            )

                            .clickable {

                                selectedSeat = seat
                            },

                        contentAlignment = Alignment.Center
                    ) {

                        Text(

                            text = seat.toString(),

                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(

                onClick = {

                    navController.navigate(
                        "payment/$company/$origin/$destination/$selectedSeat/$price"
                    )
                },

                modifier = Modifier.fillMaxWidth(),

                enabled = selectedSeat != -1
            ) {

                Text("Continuar")
            }
        }
    }
}