package com.example.busify.features.viajes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.busify.core.components.EmptyState
import com.example.busify.core.components.ErrorState
import com.example.busify.core.components.ShimmerBusCard
import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.example.busify.features.buses.BusesViewModel
import com.example.busify.features.buses.SortOption
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViajesScreen(
    navController: NavController,
    busesViewModel: BusesViewModel = viewModel()
) {
    var originFilter by remember { mutableStateOf("") }
    var destinationFilter by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    val routesState = busesViewModel.routesState.value
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val allRoutes = if (routesState is Resource.Success) routesState.data ?: emptyList() else emptyList()

    val filteredRoutes = remember(allRoutes, originFilter, destinationFilter, maxPrice) {
        val maxP = maxPrice.toDoubleOrNull() ?: Double.MAX_VALUE
        allRoutes.filter { route ->
            (originFilter.isBlank() || route.origin.contains(originFilter, true)) &&
            (destinationFilter.isBlank() || route.destination.contains(destinationFilter, true)) &&
            route.price <= maxP
        }.sortedBy { it.price }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) { busesViewModel.getRoutes(); isRefreshing = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Buscar Viajes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = originFilter,
            onValueChange = { originFilter = it },
            label = { Text("Origen") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = destinationFilter,
            onValueChange = { destinationFilter = it },
            label = { Text("Destino") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = maxPrice,
            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) maxPrice = it },
            label = { Text("Precio máximo (S/)") },
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (routesState) {
            is Resource.Loading -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(3) { ShimmerBusCard(); Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
            is Resource.Error -> {
                ErrorState(
                    message = routesState.message ?: "Error al cargar rutas",
                    onRetry = { busesViewModel.getRoutes() }
                )
            }
            is Resource.Success -> {
                if (filteredRoutes.isEmpty()) {
                    EmptyState(
                        icon = if (allRoutes.isEmpty()) Icons.Default.DirectionsBus else Icons.Default.SearchOff,
                        title = if (allRoutes.isEmpty()) "Sin rutas" else "Sin resultados",
                        message = if (allRoutes.isEmpty()) "El administrador no ha creado rutas." else "Intenta con otros filtros."
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { isRefreshing = true },
                        state = pullToRefreshState
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredRoutes, key = { it.id }) { route ->
                                ViajeCard(route = route, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViajeCard(route: Route, navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
    val departureStr = if (route.departureDate > 0) "${dateFormat.format(Date(route.departureDate))} ${route.departureTime}" else route.departureTime

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${route.origin} → ${route.destination}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = route.company,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "S/ ${"%.2f".format(route.price)}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$departureStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                if (route.duration.isNotBlank()) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(route.duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            if (route.busType.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
                    Text(route.busType, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("seats/${route.id}/${route.company}/${route.origin}/${route.destination}/${route.price}/${route.departureTime}")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.EventSeat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar Asientos", fontWeight = FontWeight.Bold)
            }
        }
    }
}
