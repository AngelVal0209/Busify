package com.example.busify.features.viajes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.data.repository.TicketRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    navController: NavController,
    routeId: String,
    company: String,
    origin: String,
    destination: String,
    price: Double,
    departureTime: String
) {
    val ticketRepository = remember { TicketRepository() }
    var selectedSeats by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var bookedSeats by remember { mutableStateOf<List<Long>>(emptyList()) }
    var loadingSeats by remember { mutableStateOf(true) }

    LaunchedEffect(routeId) {
        val result = ticketRepository.getBookedSeatsForRoute(routeId)
        if (result is com.example.busify.core.util.Resource.Success) {
            bookedSeats = result.data ?: emptyList()
        }
        loadingSeats = false
    }

    val seats = (1L..40L).toList()
    val maxSeats = 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Asientos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                text = "$company",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$origin → $destination",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "Salida: $departureTime",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Selecciona tus asientos",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Máximo $maxSeats asientos por compra",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = Color(0xFFFFCDD2),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(16.dp)
                ) {}
                Text("Ocupado", style = MaterialTheme.typography.labelSmall)
                Surface(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(16.dp)
                ) {}
                Text("Disponible", style = MaterialTheme.typography.labelSmall)
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(16.dp)
                ) {}
                Text("Seleccionado", style = MaterialTheme.typography.labelSmall)
            }

            if (selectedSeats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Asientos: ${selectedSeats.sorted().joinToString(", ")} (${selectedSeats.size}/$maxSeats)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total: S/ ${"%.2f".format(price * selectedSeats.size)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (loadingSeats) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(seats) { seat ->
                        val isBooked = bookedSeats.contains(seat)
                        val isSelected = selectedSeats.contains(seat)
                        val canSelect = !isBooked && (isSelected || selectedSeats.size < maxSeats)
                        val bgColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isBooked -> Color(0xFFFFCDD2)
                            else -> Color.LightGray
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(bgColor, shape = RoundedCornerShape(8.dp))
                                .clickable(enabled = canSelect || isSelected) {
                                    selectedSeats = if (isSelected) {
                                        selectedSeats - seat
                                    } else {
                                        selectedSeats + seat
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = seat.toString(),
                                color = if (isSelected) Color.White else Color(0xFF333333),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!loadingSeats) {
                val seatsArg = selectedSeats.sorted().joinToString("_")
                Button(
                    onClick = {
                        navController.navigate(
                            "payment/$routeId/$company/$origin/$destination/$seatsArg/$price/$departureTime"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedSeats.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (selectedSeats.isNotEmpty()) {
                        Text("Continuar (${selectedSeats.size} asientos — S/ ${"%.2f".format(price * selectedSeats.size)})")
                    } else {
                        Text("Selecciona tus asientos")
                    }
                }
            }
        }
    }
}