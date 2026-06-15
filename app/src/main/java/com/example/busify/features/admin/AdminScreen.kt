package com.example.busify.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.example.busify.domain.model.User
import com.example.busify.features.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel(),
    authViewModel: AuthViewModel? = null
) {
    val formState = viewModel.formState.value
    val fieldErrors = viewModel.fieldErrors.value
    val createRouteState = viewModel.createRouteState.value
    val routesState = viewModel.routesState.value
    val deleteState = viewModel.deleteState.value
    val editingRoute = viewModel.editingRoute.value
    val selectedTab = viewModel.selectedTab.value
    val searchQuery = viewModel.searchQuery.value
    val usersState = viewModel.usersState.value
    val roleUpdateState = viewModel.roleUpdateState.value

    var showDeleteDialog by remember { mutableStateOf<Route?>(null) }
    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showArrivalDatePicker by remember { mutableStateOf(false) }
    var showDepartureTimePicker by remember { mutableStateOf(false) }
    var showArrivalTimePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createRouteState) {
        when (createRouteState) {
            is Resource.Success -> { snackbarHostState.showSnackbar(if (editingRoute != null) "Ruta actualizada" else "Ruta creada"); viewModel.resetForm(); viewModel.cancelEdit(); viewModel.loadRoutes() }
            is Resource.Error -> snackbarHostState.showSnackbar(createRouteState.message ?: "Error")
            else -> {}
        }
    }
    LaunchedEffect(deleteState) {
        if (deleteState is Resource.Success) { snackbarHostState.showSnackbar("Ruta eliminada"); viewModel.resetState() }
        else if (deleteState is Resource.Error) { snackbarHostState.showSnackbar(deleteState.message ?: "Error"); viewModel.resetState() }
    }
    LaunchedEffect(Unit) { viewModel.loadRoutes() }
    LaunchedEffect(roleUpdateState) {
        when (roleUpdateState) {
            is Resource.Success -> { snackbarHostState.showSnackbar("Rol actualizado"); viewModel.resetRoleState() }
            is Resource.Error -> { snackbarHostState.showSnackbar(roleUpdateState?.message ?: "Error"); viewModel.resetRoleState() }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Panel de Administración", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Gestiona rutas, usuarios y más", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

            val user = authViewModel?.currentUserData?.value
            if (user != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (user.role == 2L) Color(0xFF10B981).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "Rol: ${if (user.role == 1L) "Usuario" else if (user.role == 2L) "Admin" else if (user.role == 3L) "Conductor" else "?"}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (user.role == 2L) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(selected = selectedTab == 0, onClick = { viewModel.setSelectedTab(0) }, text = { Text(if (editingRoute != null) "Editar" else "Crear", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = selectedTab == 1, onClick = { viewModel.setSelectedTab(1); viewModel.loadRoutes() }, text = { Text("Rutas", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) })
                Tab(selected = selectedTab == 2, onClick = { viewModel.setSelectedTab(2); viewModel.loadUsers() }, text = { Text("Usuarios", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> CreateRouteTab(
                    formState = formState, fieldErrors = fieldErrors, editingRoute = editingRoute,
                    createRouteState = createRouteState,
                    onFieldChange = { field, value -> viewModel.updateField(field, value) },
                    onDepartureDateClick = { showDepartureDatePicker = true },
                    onArrivalDateClick = { showArrivalDatePicker = true },
                    onDepartureTimeClick = { showDepartureTimePicker = true },
                    onArrivalTimeClick = { showArrivalTimePicker = true },
                    onSubmit = { viewModel.submitRoute() }, onCancelEdit = { viewModel.cancelEdit() }
                )
                1 -> ManageRoutesTab(
                    routesState = routesState, searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    filteredRoutes = viewModel.getFilteredRoutes(),
                    onEdit = { viewModel.startEdit(it); viewModel.setSelectedTab(0) },
                    onDelete = { showDeleteDialog = it }
                )
                2 -> ManageUsersTab(usersState = usersState, onPromote = { viewModel.setUserRole(it, 2L) }, onDemote = { viewModel.setUserRole(it, 1L) })
            }
        }
    }

    if (showDepartureDatePicker) { DatePickerDialog(initialDateMillis = editingRoute?.departureDate, onDateSelected = { viewModel.setDepartureDate(it); showDepartureDatePicker = false }, onDismiss = { showDepartureDatePicker = false }) }
    if (showArrivalDatePicker) { DatePickerDialog(initialDateMillis = editingRoute?.arrivalDate, onDateSelected = { viewModel.setArrivalDate(it); showArrivalDatePicker = false }, onDismiss = { showArrivalDatePicker = false }) }
    if (showDepartureTimePicker) { TimePickerDialog(initialHour = (formState.departureTime.split(":").firstOrNull()?.toIntOrNull() ?: 8), initialMinute = (formState.departureTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0), onTimeSelected = { h, m -> viewModel.updateField("departureTime", "${"%02d".format(h)}:${"%02d".format(m)}"); showDepartureTimePicker = false }, onDismiss = { showDepartureTimePicker = false }) }
    if (showArrivalTimePicker) { TimePickerDialog(initialHour = (formState.arrivalTime.split(":").firstOrNull()?.toIntOrNull() ?: 18), initialMinute = (formState.arrivalTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0), onTimeSelected = { h, m -> viewModel.updateField("arrivalTime", "${"%02d".format(h)}:${"%02d".format(m)}"); showArrivalTimePicker = false }, onDismiss = { showArrivalTimePicker = false }) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar ruta") },
            text = { Text("¿Estás seguro de eliminar la ruta ${showDeleteDialog!!.origin} → ${showDeleteDialog!!.destination}?") },
            confirmButton = { Button(onClick = { viewModel.deleteRoute(showDeleteDialog!!.id); showDeleteDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") } },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRouteTab(
    formState: AdminFormState, fieldErrors: FieldError, editingRoute: Route?,
    createRouteState: Resource<*>?,
    onFieldChange: (String, String) -> Unit,
    onDepartureDateClick: () -> Unit, onArrivalDateClick: () -> Unit,
    onDepartureTimeClick: () -> Unit, onArrivalTimeClick: () -> Unit,
    onSubmit: () -> Unit, onCancelEdit: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
    var busTypeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    val busTypes = listOf("Semi cama", "VIP", "Económico", "Cama")
    val statuses = listOf("Pendiente", "A tiempo", "Demorado")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        if (editingRoute != null) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editando: ${editingRoute.origin} → ${editingRoute.destination}", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                    IconButton(onClick = onCancelEdit) { Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(value = formState.company, onValueChange = { onFieldChange("company", it) }, label = { Text("Empresa") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, placeholder = { Text("Ej. Cruz del Sur") }, isError = fieldErrors.company != null, supportingText = fieldErrors.company?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }, leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) })
        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(expanded = busTypeExpanded, onExpandedChange = { busTypeExpanded = !busTypeExpanded }) {
            OutlinedTextField(value = formState.busType, onValueChange = {}, readOnly = true, label = { Text("Tipo de Bus") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = busTypeExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = MaterialTheme.shapes.medium, leadingIcon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) })
            ExposedDropdownMenu(expanded = busTypeExpanded, onDismissRequest = { busTypeExpanded = false }) { busTypes.forEach { type -> DropdownMenuItem(text = { Text(type) }, onClick = { onFieldChange("busType", type); busTypeExpanded = false }) } }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = formState.origin, onValueChange = { onFieldChange("origin", it) }, label = { Text("Salida") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium, isError = fieldErrors.origin != null, supportingText = fieldErrors.origin?.let { { Text(it) } }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) })
            OutlinedTextField(value = formState.destination, onValueChange = { onFieldChange("destination", it) }, label = { Text("Llegada") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium, isError = fieldErrors.destination != null, supportingText = fieldErrors.destination?.let { { Text(it) } }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) })
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = if (formState.departureDate > 0) dateFormat.format(Date(formState.departureDate)) else "", onValueChange = {}, readOnly = true, label = { Text("Fecha de Salida") }, modifier = Modifier.fillMaxWidth().clickable { onDepartureDateClick() }, shape = MaterialTheme.shapes.medium, isError = fieldErrors.departureDate != null, supportingText = fieldErrors.departureDate?.let { { Text(it) } }, trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar") }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = if (fieldErrors.departureDate != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = if (formState.arrivalDate > 0) dateFormat.format(Date(formState.arrivalDate)) else "", onValueChange = {}, readOnly = true, label = { Text("Fecha de Llegada") }, modifier = Modifier.fillMaxWidth().clickable { onArrivalDateClick() }, shape = MaterialTheme.shapes.medium, isError = fieldErrors.arrivalDate != null, supportingText = fieldErrors.arrivalDate?.let { { Text(it) } }, trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar") }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = if (fieldErrors.arrivalDate != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = formState.departureTime, onValueChange = {}, readOnly = true, label = { Text("Hora Salida") }, modifier = Modifier.weight(1f).clickable { onDepartureTimeClick() }, shape = MaterialTheme.shapes.medium, isError = fieldErrors.departureTime != null, supportingText = fieldErrors.departureTime?.let { { Text(it) } }, trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar") }, enabled = false, placeholder = { Text("HH:MM") }, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = if (fieldErrors.departureTime != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
            OutlinedTextField(value = formState.arrivalTime, onValueChange = {}, readOnly = true, label = { Text("Hora Llegada") }, modifier = Modifier.weight(1f).clickable { onArrivalTimeClick() }, shape = MaterialTheme.shapes.medium, isError = fieldErrors.arrivalTime != null, supportingText = fieldErrors.arrivalTime?.let { { Text(it) } }, trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar") }, enabled = false, placeholder = { Text("HH:MM") }, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface, disabledBorderColor = if (fieldErrors.arrivalTime != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline, disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = formState.price, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onFieldChange("price", it) }, label = { Text("Precio (S/)") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium, placeholder = { Text("Ej. 70") }, isError = fieldErrors.price != null, supportingText = fieldErrors.price?.let { { Text(it) } }, leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) })
            OutlinedTextField(value = formState.capacity, onValueChange = { if (it.all { char -> char.isDigit() }) onFieldChange("capacity", it) }, label = { Text("Capacidad") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium, isError = fieldErrors.capacity != null, supportingText = fieldErrors.capacity?.let { { Text(it) } }, leadingIcon = { Icon(Icons.Default.People, contentDescription = null) })
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = !statusExpanded }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = formState.status, onValueChange = {}, readOnly = true, label = { Text("Estado") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = MaterialTheme.shapes.medium)
                ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) { statuses.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { onFieldChange("status", s); statusExpanded = false }) } }
            }
            OutlinedTextField(value = formState.driverName, onValueChange = { onFieldChange("driverName", it) }, label = { Text("Conductor") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium, placeholder = { Text("Nombre del conductor") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) })
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = createRouteState !is Resource.Loading
        ) {
            if (createRouteState is Resource.Loading) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp) }
            else {
                Icon(if (editingRoute != null) Icons.Default.Edit else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (editingRoute != null) "Actualizar Ruta" else "Crear Ruta", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageRoutesTab(routesState: Resource<List<Route>>?, searchQuery: String, onSearchChange: (String) -> Unit, filteredRoutes: List<Route>, onEdit: (Route) -> Unit, onDelete: (Route) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(value = searchQuery, onValueChange = onSearchChange, label = { Text("Buscar rutas...") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, singleLine = true)
        Spacer(modifier = Modifier.height(12.dp))
        when (routesState) {
            is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(routesState.message ?: "Error", color = MaterialTheme.colorScheme.error) }
            is Resource.Success -> {
                if (filteredRoutes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (searchQuery.isNotBlank()) "Sin resultados" else "No hay rutas", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredRoutes, key = { it.id }) { route -> RouteManagementCard(route = route, onEdit = { onEdit(route) }, onDelete = { onDelete(route) }) }
                    }
                }
            }
            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cargando...") }
        }
    }
}

@Composable
private fun RouteManagementCard(route: Route, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
    val dateStr = if (route.departureDate > 0) dateFormat.format(Date(route.departureDate)) else route.departureTime

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${route.origin} → ${route.destination}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(route.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("$dateStr | S/ ${route.price} | Cap: ${route.capacity}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                if (route.driverName.isNotBlank()) { Text("Conductor: ${route.driverName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(initialDateMillis: Long? = null, onDateSelected: (Long) -> Unit, onDismiss: () -> Unit) {
    val stateHolder = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    androidx.compose.material3.DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { stateHolder.selectedDateMillis?.let { onDateSelected(it) } ?: onDismiss() }) { Text("Aceptar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) {
        DatePicker(state = stateHolder)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(initialHour: Int, initialMinute: Int, onTimeSelected: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Seleccionar Hora") }, text = { TimePicker(state = state) }, confirmButton = { TextButton(onClick = { onTimeSelected(state.hour, state.minute); onDismiss() }) { Text("Aceptar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun ManageUsersTab(usersState: Resource<List<User>>?, onPromote: (String) -> Unit, onDemote: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Usuarios del Sistema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Administra los roles de los usuarios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        when (usersState) {
            is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(usersState.message ?: "Error", color = MaterialTheme.colorScheme.error) }
            is Resource.Success -> {
                val users = usersState.data ?: emptyList()
                if (users.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay usuarios registrados") } }
                else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(users, key = { it.uid }) { user -> UserManagementCard(user = user, onPromote = { onPromote(user.uid) }, onDemote = { onDemote(user.uid) }) }
                    }
                }
            }
            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cargando usuarios...") }
        }
    }
}

@Composable
private fun UserManagementCard(user: User, onPromote: () -> Unit, onDemote: () -> Unit) {
    val roleLabel = when (user.role) { 2L -> "Admin"; 3L -> "Chofer"; else -> "Usuario" }
    val roleColor = when (user.role) { 2L -> Color(0xFF10B981); 3L -> Color(0xFFF59E0B); else -> Color(0xFF6B7280) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Text(text = user.name.take(2).uppercase().ifEmpty { "U" }, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name.ifEmpty { "Sin nombre" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = roleColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) { Text(roleLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = roleColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
            }
            if (user.role != 2L) { IconButton(onClick = onPromote) { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Hacer admin", tint = Color(0xFF10B981)) } }
            if (user.role == 2L) { IconButton(onClick = onDemote) { Icon(Icons.Default.Person, contentDescription = "Quitar admin", tint = Color(0xFF6B7280)) } }
        }
    }
}
