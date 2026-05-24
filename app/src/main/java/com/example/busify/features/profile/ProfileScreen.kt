package com.example.busify.features.profile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.components.BusifyButton
import com.example.busify.core.components.BusifyTextField
import com.example.busify.core.util.Resource
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState
    val updateState by viewModel.updateState
    val ticketsState by viewModel.ticketsState
    val snackbarHostState = remember { SnackbarHostState() }

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var showTickets by remember { mutableStateOf(false) }
    var selectedTicket by remember { mutableStateOf<com.example.busify.domain.model.Ticket?>(null) }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            snackbarHostState.showSnackbar("Perfil actualizado")
            isEditing = false
            viewModel.resetUpdateState()
        } else if (updateState is Resource.Error) {
            snackbarHostState.showSnackbar(updateState?.message ?: "Error")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = userState) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Success -> {
                    val user = state.data!!
                    if (isEditing) {
                        BusifyTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = "Nombre"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(onClick = { viewModel.updateUser(editedName) }) {
                                Text("Guardar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(onClick = { isEditing = false }) {
                                Text("Cancelar")
                            }
                        }
                    } else {
                        Text(
                            text = user.name.ifEmpty { "Sin nombre" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                is Resource.Error -> Text("Error: ${state.message}")
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileOptionItem(
                icon = Icons.Default.Person,
                title = "Editar Datos",
                onClick = {
                    if (userState is Resource.Success) {
                        editedName = (userState as Resource.Success).data?.name ?: ""
                        isEditing = true
                    }
                }
            )

            ProfileOptionItem(
                icon = Icons.Default.History,
                title = "Mis Viajes",
                onClick = { showTickets = !showTickets }
            )

            if (showTickets) {
                Spacer(modifier = Modifier.height(12.dp))
                when (ticketsState) {
                    is Resource.Loading -> CircularProgressIndicator()
                    is Resource.Error -> Text(
                        "Error al cargar viajes",
                        color = MaterialTheme.colorScheme.error
                    )
                    is Resource.Success -> {
                        val tickets = ticketsState.data ?: emptyList()
                        if (tickets.isEmpty()) {
                            Text(
                                "No tienes viajes realizados",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        } else {
                            Text(
                                "Tus Viajes (${tickets.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tickets) { ticket ->
                                    TicketHistoryCard(
                                        ticket = ticket,
                                        onClick = { selectedTicket = ticket }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            BusifyButton(
                text = "Cerrar Sesión",
                onClick = onLogout,
                containerColor = MaterialTheme.colorScheme.error
            )
        }
    }

    selectedTicket?.let { ticket ->
        TicketDetailDialog(
            ticket = ticket,
            onDismiss = { selectedTicket = null }
        )
    }
}

@Composable
private fun TicketDetailDialog(
    ticket: com.example.busify.domain.model.Ticket,
    onDismiss: () -> Unit
) {
    val qrContent = remember(ticket) {
        "Busify Ticket\nRuta: ${ticket.origin} → ${ticket.destination}\nEmpresa: ${ticket.company}\nSalida: ${ticket.departureTime}\nAsientos: ${ticket.seatNumbers.sorted().joinToString(", ")}\nID: ${ticket.routeId}"
    }
    val qrBitmap = remember(qrContent) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Detalle del Viaje",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Empresa", ticket.company)
                        DetailRow("Ruta", "${ticket.origin} → ${ticket.destination}")
                        DetailRow("Salida", ticket.departureTime)
                        DetailRow("Asientos", ticket.seatNumbers.sorted().joinToString(", "))
                        DetailRow("Total", "S/ ${"%.2f".format(ticket.totalPrice)}")
                        DetailRow("Pago", ticket.paymentMethod)
                        DetailRow("Estado", ticket.status.replaceFirstChar { it.uppercase() })
                    }
                }

                if (qrBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Código QR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR del ticket",
                        modifier = Modifier.size(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TicketHistoryCard(ticket: com.example.busify.domain.model.Ticket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${ticket.origin} → ${ticket.destination}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${ticket.company} | ${ticket.departureTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Asientos: ${ticket.seatNumbers.sorted().joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "S/ ${"%.2f".format(ticket.totalPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        }
    }
}
