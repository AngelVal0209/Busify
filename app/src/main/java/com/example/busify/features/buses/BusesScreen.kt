package com.example.busify.features.buses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.components.EmptyState
import com.example.busify.core.components.ErrorState
import com.example.busify.core.components.NoResultsState
import com.example.busify.core.components.ShimmerBusCard
import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusesScreen(
    viewModel: BusesViewModel = viewModel()
) {
    val routesState = viewModel.routesState.value
    val filter = viewModel.filter.value
    var isRefreshing by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val filteredRoutes = remember(routesState, filter) { viewModel.getFilteredRoutes() }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) { viewModel.getRoutes(); isRefreshing = false }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Buses", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            OutlinedTextField(
                value = filter.searchQuery,
                onValueChange = { viewModel.updateFilter(filter.copy(searchQuery = it)) },
                placeholder = { Text("Buscar origen, destino o empresa...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filtros", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = if (filter.minPrice > 0) filter.minPrice.toInt().toString() else "",
                                onValueChange = { viewModel.updateFilter(filter.copy(minPrice = it.toDoubleOrNull() ?: 0.0)) },
                                label = { Text("Min S/") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = MaterialTheme.shapes.small
                            )
                            OutlinedTextField(
                                value = if (filter.maxPrice < 1000) filter.maxPrice.toInt().toString() else "",
                                onValueChange = { viewModel.updateFilter(filter.copy(maxPrice = it.toDoubleOrNull() ?: 1000.0)) },
                                label = { Text("Max S/") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = MaterialTheme.shapes.small
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ordenar por:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SortOption.entries.filter { it != SortOption.NONE }.forEach { option ->
                                FilterChip(
                                    selected = filter.sortBy == option,
                                    onClick = { viewModel.updateFilter(filter.copy(sortBy = option)) },
                                    label = {
                                        Text(
                                            when (option) {
                                                SortOption.PRICE_ASC -> "Menor precio"
                                                SortOption.PRICE_DESC -> "Mayor precio"
                                                SortOption.TIME_ASC -> "Más temprano"
                                                else -> ""
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            when (routesState) {
                is Resource.Loading -> {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(5) { ShimmerBusCard(); Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
                is Resource.Error -> {
                    ErrorState(
                        message = routesState.message ?: "Error al cargar rutas",
                        onRetry = { viewModel.getRoutes() }
                    )
                }
                is Resource.Success -> {
                    if (filteredRoutes.isEmpty()) {
                        NoResultsState(query = filter.searchQuery)
                    } else {
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = { isRefreshing = true },
                            state = pullToRefreshState
                        ) {
                            LazyColumn(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(filteredRoutes, key = { it.id }) { route ->
                                    BusCard(route)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusCard(route: Route) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${route.origin} → ${route.destination}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = route.company,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                StatusBadge(route.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(modifier = Modifier.alpha(0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(Icons.Default.AccessTime, departureStr)
                InfoItem(Icons.Default.AccessTime, "Llega: ${route.arrivalTime}")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(Icons.Default.People, "Cap: ${route.capacity}")
                InfoItem(Icons.Default.AttachMoney, "S/ ${"%.2f".format(route.price)}")
            }

            if (route.driverName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                InfoItem(Icons.Default.Person, "Conductor: ${route.driverName}")
            }

            if (route.busType.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = route.busType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "A tiempo" -> Color(0xFF10B981)
        "Demorado" -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
