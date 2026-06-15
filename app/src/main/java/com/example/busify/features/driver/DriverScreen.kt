package com.example.busify.features.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.components.EmptyState
import com.example.busify.core.components.ErrorState
import com.example.busify.core.components.ShimmerBusCard
import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.example.busify.domain.model.Ticket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    viewModel: DriverViewModel = viewModel()
) {
    var showQRScanner by remember { mutableStateOf(false) }
    val assignedRoutes by viewModel.assignedRoutes
    val selectedRoute by viewModel.selectedRoute
    val routeTickets by viewModel.routeTickets
    val ticketStatusState by viewModel.ticketStatusState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(ticketStatusState) {
        when (ticketStatusState) {
            is Resource.Success -> { snackbarHostState.showSnackbar("Ticket marcado como usado"); viewModel.selectedRoute.value?.let { viewModel.selectRoute(it) } }
            is Resource.Error -> snackbarHostState.showSnackbar(ticketStatusState?.message ?: "Error")
            else -> {}
        }
    }

    if (showQRScanner) {
        QRScannerScreen(onBack = { showQRScanner = false }, ticketRepository = com.example.busify.data.repository.TicketRepository())
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (selectedRoute != null) "Pasajeros" else "Conductor", fontWeight = FontWeight.Bold) },
                    navigationIcon = { if (selectedRoute != null) { IconButton(onClick = { viewModel.clearSelection() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } } },
                    actions = {
                        IconButton(onClick = { showQRScanner = true }) { Icon(Icons.Default.QrCode, contentDescription = "Escanear") }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = { showQRScanner = true }) { Icon(Icons.Default.QrCode, contentDescription = "Escanear QR") }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
                if (selectedRoute != null) {
                    RoutePassengersContent(route = selectedRoute!!, tickets = routeTickets, onMarkUsed = { viewModel.markTicketUsed(it) })
                } else {
                    RoutesContent(routes = assignedRoutes, onRouteClick = { viewModel.selectRoute(it) })
                }
            }
        }
    }
}

@Composable
private fun RoutesContent(routes: Resource<List<Route>>, onRouteClick: (Route) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Rutas Asignadas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        when (routes) {
            is Resource.Loading -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(3) { ShimmerBusCard() }
                }
            }
            is Resource.Error -> ErrorState(message = routes.message ?: "Error", onRetry = { })
            is Resource.Success -> {
                val list = routes.data ?: emptyList()
                if (list.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.DirectionsBus,
                        title = "Sin rutas asignadas",
                        message = "Usa el botón QR para escanear tickets",
                        actionText = "Escanear QR",
                        onAction = { }
                    )
                } else {
                    Text("${list.size} ruta(s) asignada(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(list) { route -> DriverRouteCard(route = route, onClick = { onRouteClick(route) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverRouteCard(route: Route, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${route.origin} → ${route.destination}", fontWeight = FontWeight.Bold)
                Text(route.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("${route.departureTime} | Cap: ${route.capacity}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        }
    }
}

@Composable
private fun RoutePassengersContent(route: Route, tickets: Resource<List<Ticket>>?, onMarkUsed: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("${route.origin} → ${route.destination}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Salida: ${route.departureTime} | ${route.company}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Pasajeros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        when (tickets) {
            is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Error -> ErrorState(message = tickets.message ?: "Error")
            is Resource.Success -> {
                val list = tickets.data ?: emptyList()
                if (list.isEmpty()) {
                    EmptyState(icon = Icons.Default.Person, title = "Sin pasajeros", message = "No hay pasajeros registrados para esta ruta")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(list) { ticket -> TicketPassengerCard(ticket = ticket, onMarkUsed = { onMarkUsed(ticket.id) }) }
                    }
                }
            }
            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Selecciona una ruta") }
        }
    }
}

@Composable
private fun TicketPassengerCard(ticket: Ticket, onMarkUsed: () -> Unit) {
    val isUsed = ticket.status == "usado"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUsed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${ticket.origin} → ${ticket.destination}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Asientos: ${ticket.seatNumbers.sorted().joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text("Pago: ${ticket.paymentMethod}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            if (!isUsed) {
                Button(onClick = onMarkUsed, contentPadding = PaddingValues(16.dp, 8.dp), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Usado", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Surface(color = Color(0xFF10B981).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("USADO", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
