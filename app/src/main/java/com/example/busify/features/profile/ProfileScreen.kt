package com.example.busify.features.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.busify.core.components.BusifyButton
import com.example.busify.core.components.BusifyTextField
import com.example.busify.core.components.EmptyState
import com.example.busify.core.components.ErrorState
import com.example.busify.core.components.ShimmerProfileHeader
import com.example.busify.core.components.ShimmerEffect
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.PaymentMethodRepository
import com.example.busify.domain.model.SavedCard
import com.example.busify.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState
    val updateState by viewModel.updateState
    val ticketsState by viewModel.ticketsState
    val passwordUpdateState by viewModel.passwordUpdateState
    val emailUpdateState by viewModel.emailUpdateState
    val deleteAccountState by viewModel.deleteAccountState
    val photoUploadState by viewModel.photoUploadState
    val snackbarHostState = remember { SnackbarHostState() }

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var showTickets by remember { mutableStateOf(false) }
    var selectedTicket by remember { mutableStateOf<com.example.busify.domain.model.Ticket?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCards by remember { mutableStateOf(false) }
    var savedCards by remember { mutableStateOf<List<SavedCard>>(emptyList()) }
    var loadingCards by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { viewModel.uploadProfilePhoto(it) } }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) { snackbarHostState.showSnackbar("Perfil actualizado"); isEditing = false; viewModel.resetUpdateState() }
        else if (updateState is Resource.Error) { snackbarHostState.showSnackbar(updateState?.message ?: "Error") }
    }
    LaunchedEffect(passwordUpdateState) {
        when (passwordUpdateState) {
            is Resource.Success -> { showPasswordDialog = false; snackbarHostState.showSnackbar("Contraseña actualizada"); viewModel.resetPasswordUpdateState() }
            is Resource.Error -> { snackbarHostState.showSnackbar(passwordUpdateState?.message ?: "Error"); viewModel.resetPasswordUpdateState() }
            else -> {}
        }
    }
    LaunchedEffect(emailUpdateState) {
        when (emailUpdateState) {
            is Resource.Success -> { showEmailDialog = false; snackbarHostState.showSnackbar("Correo actualizado. Revisa tu bandeja."); viewModel.resetEmailUpdateState() }
            is Resource.Error -> { snackbarHostState.showSnackbar(emailUpdateState?.message ?: "Error"); viewModel.resetEmailUpdateState() }
            else -> {}
        }
    }
    LaunchedEffect(deleteAccountState) {
        when (deleteAccountState) {
            is Resource.Success -> { showDeleteDialog = false; onLogout() }
            is Resource.Error -> { snackbarHostState.showSnackbar(deleteAccountState?.message ?: "Error"); viewModel.resetDeleteAccountState() }
            else -> {}
        }
    }
    LaunchedEffect(photoUploadState) {
        when (photoUploadState) {
            is Resource.Success -> snackbarHostState.showSnackbar("Foto actualizada")
            is Resource.Error -> snackbarHostState.showSnackbar(photoUploadState?.message ?: "Error al subir foto")
            else -> {}
        }
        viewModel.resetPhotoUploadState()
    }

    if (showPasswordDialog) { EditPasswordDialog(viewModel = viewModel, onDismiss = { showPasswordDialog = false }) }
    if (showEmailDialog) { EditEmailDialog(viewModel = viewModel, onDismiss = { showEmailDialog = false }) }
    if (showDeleteDialog) { DeleteAccountDialog(viewModel = viewModel, onDismiss = { showDeleteDialog = false }) }

    LaunchedEffect(showCards) {
        if (showCards) {
            loadingCards = true
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val result = PaymentMethodRepository().getSavedCards(uid)
            if (result is Resource.Success) savedCards = result.data ?: emptyList()
            loadingCards = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val state = userState) {
                        is Resource.Loading -> {
                            ShimmerEffect(modifier = Modifier.size(88.dp), height = 88.dp, width = 88.dp, shape = CircleShape)
                            Spacer(modifier = Modifier.height(12.dp))
                            ShimmerEffect(height = 24.dp, width = 160.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerEffect(height = 16.dp, width = 200.dp)
                        }
                        is Resource.Success -> {
                            val user = state.data!!
                            Box(modifier = Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).clickable { photoLauncher.launch("image/*") }, contentAlignment = Alignment.Center) {
                                if (user.photoUrl != null) {
                                    AsyncImage(model = user.photoUrl, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Text(
                                        text = user.name.take(2).uppercase().ifEmpty { "U" },
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isEditing) {
                                BusifyTextField(value = editedName, onValueChange = { editedName = it }, label = "Nombre")
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BusifyButton(text = "Guardar", onClick = { viewModel.updateUser(editedName) }, modifier = Modifier.weight(1f), icon = Icons.Default.Check)
                                    OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                                }
                            } else {
                                Text(text = user.name.ifEmpty { "Sin nombre" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        is Resource.Error -> { Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            ProfileOptionItem(Icons.Outlined.Person, "Editar nombre", onClick = {
                                if (userState is Resource.Success) { editedName = (userState as Resource.Success).data?.name ?: ""; isEditing = true }
                            })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ProfileOptionItem(Icons.Outlined.Email, "Cambiar correo", onClick = { showEmailDialog = true })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ProfileOptionItem(Icons.Outlined.Lock, "Cambiar contraseña", onClick = { showPasswordDialog = true })
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            ProfileOptionItem(Icons.Outlined.CreditCard, "Mis tarjetas", onClick = { showCards = !showCards })
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            ProfileOptionItem(Icons.Outlined.History, "Mis viajes", onClick = { showTickets = !showTickets })
                        }
                    }
                }

                if (showCards) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tarjetas guardadas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (loadingCards) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else if (savedCards.isEmpty()) {
                            EmptyState(
                                modifier = Modifier.height(200.dp),
                                icon = Icons.Outlined.CreditCard,
                                title = "Sin tarjetas",
                                message = "Agrega una tarjeta desde la pantalla de pago",
                                actionText = "Cerrar",
                                onAction = { showCards = false }
                            )
                        } else {
                            savedCards.forEach { card ->
                                val icon = when (card.type) { "Yape" -> Icons.Default.Phone; "Visa" -> Icons.Default.CreditCard; "Plin" -> Icons.Default.AccountBalance; else -> Icons.Default.Payment }
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(card.type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text("***${card.lastDigits} - ${card.holderName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        }
                                        if (card.isDefault) {
                                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                                                Text("Default", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showTickets) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        when (ticketsState) {
                            is Resource.Loading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            is Resource.Error -> ErrorState(message = "Error al cargar viajes", onRetry = { viewModel.loadUserProfile() })
                            is Resource.Success -> {
                                val tickets = ticketsState.data ?: emptyList()
                                if (tickets.isEmpty()) {
                                    EmptyState(modifier = Modifier.height(200.dp), icon = Icons.Outlined.ConfirmationNumber, title = "Sin viajes", message = "Cuando compres un ticket aparecerá aquí")
                                } else {
                                    Text("Mis Viajes (${tickets.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(tickets) { ticket ->
                                            TicketHistoryCard(ticket = ticket, onClick = { selectedTicket = ticket })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            ProfileOptionItem(Icons.Outlined.Delete, "Eliminar cuenta", onClick = { showDeleteDialog = true }, titleColor = MaterialTheme.colorScheme.error)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    BusifyButton(text = "Cerrar sesión", onClick = onLogout, containerColor = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    selectedTicket?.let { ticket ->
        TicketDetailDialog(ticket = ticket, onDismiss = { selectedTicket = null })
    }
}

@Composable
private fun EditPasswordDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val state by viewModel.passwordUpdateState

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("Contraseña actual") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Nueva contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (newPassword == confirmPassword && newPassword.length >= 8) viewModel.updatePassword(currentPassword, newPassword) },
                enabled = state !is Resource.Loading
            ) { Text("Actualizar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun EditEmailDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    val state by viewModel.emailUpdateState

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar correo") },
        text = {
            Column {
                Text("Se enviará un correo de verificación a la nueva dirección.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Nuevo correo") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña actual") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.updateEmail(password, newEmail) }, enabled = state !is Resource.Loading) { Text("Actualizar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DeleteAccountDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    val state by viewModel.deleteAccountState

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) },
        text = {
            Column {
                Text("Esta acción es irreversible. Todos tus datos serán eliminados permanentemente.")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Ingresa tu contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
            }
        },
        confirmButton = { Button(onClick = { viewModel.deleteAccount(password) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), enabled = state !is Resource.Loading) { Text("Eliminar mi cuenta") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun TicketDetailDialog(ticket: com.example.busify.domain.model.Ticket, onDismiss: () -> Unit) {
    val qrContent = remember(ticket) { "Busify Ticket\nRuta: ${ticket.origin} → ${ticket.destination}\nEmpresa: ${ticket.company}\nSalida: ${ticket.departureTime}\nAsientos: ${ticket.seatNumbers.sorted().joinToString(", ")}\nID: ${ticket.routeId}" }
    val qrBitmap = remember(qrContent) {
        try {
            val writer = QRCodeWriter(); val bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) for (y in 0 until 512) bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            bitmap
        } catch (e: Exception) { null }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Detalle del viaje", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
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
                    Text("Código QR", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR del ticket", modifier = Modifier.size(180.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.medium) { Text("Cerrar") }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TicketHistoryCard(ticket: com.example.busify.domain.model.Ticket, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${ticket.origin} → ${ticket.destination}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("${ticket.company} | ${ticket.departureTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Row {
                    Text("Asientos: ${ticket.seatNumbers.sorted().joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("S/ ${"%.2f".format(ticket.totalPrice)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, onClick: () -> Unit = {}, titleColor: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), color = titleColor)
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
        }
    }
}
