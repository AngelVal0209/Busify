package com.example.busify.features.viajes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.RouteRepository
import com.example.busify.data.repository.TicketRepository
import com.example.busify.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    routeId: String,
    company: String,
    origin: String,
    destination: String,
    seats: String,
    price: Double,
    departureTime: String
) {
    val scope = rememberCoroutineScope()
    val ticketRepository = remember { TicketRepository() }
    val routeRepository = remember { RouteRepository() }
    var selectedMethod by remember { mutableStateOf("Yape") }
    var isPaying by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val seatList = remember(seats) {
        seats.split("_").mapNotNull { it.toLongOrNull() }
    }
    val totalPrice = remember(seatList, price) { seatList.size * price }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pago") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Resumen de Compra",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    DetailRow("Empresa", company)
                    DetailRow("Ruta", "$origin → $destination")
                    DetailRow("Salida", departureTime)
                    DetailRow("Asientos", seatList.sorted().joinToString(", "))
                    DetailRow("Cantidad", "${seatList.size}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailRow("Precio unitario", "S/ $price")
                    DetailRow("Total", "S/ ${"%.2f".format(totalPrice)}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Método de Pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selectedMethod == "Yape",
                    onClick = { selectedMethod = "Yape" },
                    label = { Text("Yape") },
                    shape = MaterialTheme.shapes.small
                )
                FilterChip(
                    selected = selectedMethod == "Visa",
                    onClick = { selectedMethod = "Visa" },
                    label = { Text("Visa") },
                    shape = MaterialTheme.shapes.small
                )
                FilterChip(
                    selected = selectedMethod == "Plin",
                    onClick = { selectedMethod = "Plin" },
                    label = { Text("Plin") },
                    shape = MaterialTheme.shapes.small
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isPaying = true
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    if (userId.isEmpty()) {
                        scope.launch { snackbarHostState.showSnackbar("Error: Usuario no autenticado") }
                        isPaying = false
                        return@Button
                    }
                    val ticket = Ticket(
                        userId = userId,
                        routeId = routeId,
                        company = company,
                        origin = origin,
                        destination = destination,
                        departureTime = departureTime,
                        seatNumbers = seatList,
                        totalPrice = totalPrice,
                        paymentMethod = selectedMethod,
                        status = "confirmado"
                    )

                    scope.launch {
                        val result = ticketRepository.saveTicket(ticket)
                        when (result) {
                            is Resource.Success -> {
                                routeRepository.decrementCapacity(routeId, seatList.size)
                                snackbarHostState.showSnackbar("Pago realizado con éxito")
                                navController.navigate(
                                    "ticket/$routeId/$company/$origin/$destination/$seats/$price/$selectedMethod/$departureTime"
                                ) {
                                    popUpTo("viajes") { inclusive = false }
                                }
                            }
                            is Resource.Error -> {
                                snackbarHostState.showSnackbar(result.message ?: "Error al procesar pago")
                            }
                            else -> {}
                        }
                        isPaying = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isPaying,
                shape = MaterialTheme.shapes.small
            ) {
                if (isPaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pagar S/ ${"%.2f".format(totalPrice)}", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
