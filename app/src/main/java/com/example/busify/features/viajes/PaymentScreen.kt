package com.example.busify.features.viajes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.core.components.BusifyButton
import com.example.busify.core.components.LoadingOverlay
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.PaymentMethodRepository
import com.example.busify.data.repository.RouteRepository
import com.example.busify.data.repository.TicketRepository
import com.example.busify.domain.model.SavedCard
import com.example.busify.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val cardRepository = remember { PaymentMethodRepository() }
    var selectedMethod by remember { mutableStateOf("Yape") }
    var isPaying by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val seatList = remember(seats) { seats.split("_").mapNotNull { it.toLongOrNull() } }
    val totalPrice = remember(seatList, price) { seatList.size * price }

    var savedCards by remember { mutableStateOf<List<SavedCard>>(emptyList()) }
    var loadingCards by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val result = cardRepository.getSavedCards(userId)
            if (result is Resource.Success) {
                savedCards = result.data ?: emptyList()
                val defaultCard = savedCards.find { it.isDefault }
                if (defaultCard != null) selectedMethod = defaultCard.type
            }
            loadingCards = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pago", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    StepIndicator(step = 1, label = "Asientos", active = false)
                    HorizontalDivider(modifier = Modifier.width(32.dp))
                    StepIndicator(step = 2, label = "Pago", active = true)
                    HorizontalDivider(modifier = Modifier.width(32.dp))
                    StepIndicator(step = 3, label = "Ticket", active = false)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        PaymentDetailRow("Empresa", company)
                        PaymentDetailRow("Ruta", "$origin → $destination")
                        PaymentDetailRow("Salida", departureTime)
                        PaymentDetailRow("Asientos", seatList.sorted().joinToString(", "))
                        PaymentDetailRow("Cantidad", "${seatList.size}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        PaymentDetailRow("Precio unitario", "S/ ${"%.2f".format(price)}")
                        PaymentDetailRow("Total", "S/ ${"%.2f".format(totalPrice)}", bold = true)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Método de Pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { showAddCardDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (savedCards.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(savedCards) { card ->
                            SavedCardChip(card = card, isSelected = selectedMethod == card.type, onClick = { selectedMethod = card.type })
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Yape", "Visa", "Plin").forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) },
                            leadingIcon = {
                                Icon(
                                    when (method) {
                                        "Yape" -> Icons.Default.Phone
                                        "Visa" -> Icons.Default.CreditCard
                                        "Plin" -> Icons.Default.AccountBalance
                                        else -> Icons.Default.Payment
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                BusifyButton(
                    text = "Pagar S/ ${"%.2f".format(totalPrice)}",
                    onClick = {
                        isPaying = true
                        if (userId.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Error: Usuario no autenticado") }
                            isPaying = false; return@BusifyButton
                        }
                        val ticket = Ticket(
                            userId = userId, routeId = routeId, company = company,
                            origin = origin, destination = destination, departureTime = departureTime,
                            seatNumbers = seatList, totalPrice = totalPrice,
                            paymentMethod = selectedMethod, status = "confirmado"
                        )
                        scope.launch {
                            val result = ticketRepository.saveTicket(ticket)
                            when (result) {
                                is Resource.Success -> {
                                    routeRepository.decrementCapacity(routeId, seatList.size)
                                    snackbarHostState.showSnackbar("Pago exitoso!")
                                    navController.navigate("ticket/$routeId/$company/$origin/$destination/$seats/$price/$selectedMethod/$departureTime") {
                                        popUpTo("viajes") { inclusive = false }
                                    }
                                }
                                is Resource.Error -> { snackbarHostState.showSnackbar(result.message ?: "Error al procesar pago") }
                                else -> {}
                            }
                            isPaying = false
                        }
                    },
                    icon = Icons.Default.Lock,
                    loading = isPaying
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            LoadingOverlay(visible = isPaying, message = "Procesando pago...")
        }
    }

    if (showAddCardDialog) {
        AddCardDialog(userId = userId, cardRepository = cardRepository, onDismiss = { showAddCardDialog = false }, onSaved = {
            showAddCardDialog = false
            scope.launch {
                val result = cardRepository.getSavedCards(userId)
                if (result is Resource.Success) savedCards = result.data ?: emptyList()
            }
        })
    }
}

@Composable
private fun StepIndicator(step: Int, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (active) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("$step", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun SavedCardChip(card: SavedCard, isSelected: Boolean, onClick: () -> Unit) {
    val icon = when (card.type) { "Yape" -> Icons.Default.Phone; "Visa" -> Icons.Default.CreditCard; "Plin" -> Icons.Default.AccountBalance; else -> Icons.Default.Payment }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(card.type, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("***${card.lastDigits}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (card.isDefault) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Default.CheckCircle, contentDescription = "Default", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
private fun AddCardDialog(userId: String, cardRepository: PaymentMethodRepository, onDismiss: () -> Unit, onSaved: () -> Unit) {
    var type by remember { mutableStateOf("Visa") }
    var holderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Tarjeta") },
        text = {
            Column {
                Text("Tipo", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Yape", "Visa", "Plin").forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }) } }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = holderName, onValueChange = { holderName = it }, label = { Text("Titular") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = cardNumber, onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 16) cardNumber = it }, label = { Text("Número") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = MaterialTheme.shapes.medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = isDefault, onCheckedChange = { isDefault = it }); Text("Establecer como método por defecto", style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (holderName.isBlank() || cardNumber.length < 4) { scope.launch { snackbarHostState.showSnackbar("Completa todos los campos") }; return@TextButton }
                    saving = true
                    scope.launch {
                        val card = SavedCard(userId = userId, type = type, holderName = holderName, lastDigits = cardNumber.takeLast(4), isDefault = isDefault)
                        val result = cardRepository.saveCard(card)
                        if (result is Resource.Success && isDefault) cardRepository.setDefaultCard(userId, result.data ?: return@launch)
                        saving = false; onSaved()
                    }
                },
                enabled = !saving
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PaymentDetailRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}
