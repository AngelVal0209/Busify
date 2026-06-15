package com.example.busify.features.viajes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.busify.core.components.LoadingOverlay
import com.example.busify.core.util.Resource
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
        if (result is Resource.Success) bookedSeats = result.data ?: emptyList()
        loadingSeats = false
    }

    val seats = (1L..40L).toList()
    val maxSeats = 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asientos", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(company, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$origin → $destination", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Salida: $departureTime", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Selecciona tus asientos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Máximo $maxSeats asientos por compra",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                LegendItem(color = Color(0xFFFCA5A5), label = "Ocupado")
                LegendItem(color = Color(0xFFE2E8F0), label = "Disponible")
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Seleccionado")
            }

            if (selectedSeats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Asientos: ${selectedSeats.sorted().joinToString(", ")}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${selectedSeats.size}/$maxSeats seleccionados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        }
                        Text(
                            "S/ ${"%.2f".format(price * selectedSeats.size)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loadingSeats) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            items(seats) { seat ->
                                val isBooked = bookedSeats.contains(seat)
                                val isSelected = selectedSeats.contains(seat)
                                val canSelect = !isBooked && (isSelected || selectedSeats.size < maxSeats)
                                val bgColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isBooked -> Color(0xFFFCA5A5)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val borderColor = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isBooked -> Color.Transparent
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor, RoundedCornerShape(12.dp))
                                        .then(
                                            if (!isSelected && !isBooked) Modifier.border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                                            else Modifier
                                        )
                                        .clickable(enabled = canSelect || isSelected) {
                                            selectedSeats = if (isSelected) selectedSeats - seat
                                            else selectedSeats + seat
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = seat.toString(),
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isBooked -> Color(0xFF991B1B)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!loadingSeats) {
                val seatsArg = selectedSeats.sorted().joinToString("_")
                Button(
                    onClick = {
                        navController.navigate("payment/$routeId/$company/$origin/$destination/$seatsArg/$price/$departureTime")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selectedSeats.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (selectedSeats.isNotEmpty()) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ir a Pagar — S/ ${"%.2f".format(price * selectedSeats.size)}", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Selecciona tus asientos")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
