package com.example.busify.features.viajes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.busify.core.util.Resource
import com.example.busify.features.buses.BusesViewModel
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
    val routesState = busesViewModel.routesState.value
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val allRoutes = if (routesState is Resource.Success) routesState.data ?: emptyList() else emptyList()

    val filteredRoutes = remember(allRoutes, originFilter, destinationFilter) {
        allRoutes.filter { route ->
            (originFilter.isBlank() || route.origin.contains(originFilter, true)) &&
                (destinationFilter.isBlank() || route.destination.contains(destinationFilter, true))
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            busesViewModel.getRoutes()
            isRefreshing = false
        }
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

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = originFilter,
            onValueChange = { originFilter = it },
            label = { Text("Origen") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = destinationFilter,
            onValueChange = { destinationFilter = it },
            label = { Text("Destino") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (routesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Text(
                    text = "Error al cargar rutas: ${routesState.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is Resource.Success -> {
                if (filteredRoutes.isEmpty()) {
                    Text(
                        text = if (allRoutes.isEmpty()) "No hay rutas disponibles creadas por el administrador."
                        else "No se encontraron rutas con esos filtros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { isRefreshing = true },
                        state = pullToRefreshState
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredRoutes) { route ->
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
private fun ViajeCard(route: com.example.busify.domain.model.Route, navController: NavController) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
    val departureStr = if (route.departureDate > 0) {
        "${dateFormat.format(Date(route.departureDate))} ${route.departureTime}"
    } else {
        route.departureTime
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${route.origin} → ${route.destination}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = route.company,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Salida: $departureStr",
                style = MaterialTheme.typography.bodySmall
            )
            if (route.driverName.isNotBlank()) {
                Text(
                    text = "Conductor: ${route.driverName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "S/ ${route.price}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    navController.navigate(
                        "seats/${route.id}/${route.company}/${route.origin}/${route.destination}/${route.price}/${route.departureTime}"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Seleccionar")
            }
        }
    }
}