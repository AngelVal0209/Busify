package com.example.busify.features.profile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
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
fun CuentaScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState
    val updateState by viewModel.updateState
    val snackbarHostState = remember { SnackbarHostState() }

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mi Cuenta",
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
                            label = "Nombre completo"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BusifyTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            label = "Número de celular"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.updateUser(editedName, editedPhone) },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Guardar")
                            }
                            OutlinedButton(
                                onClick = { isEditing = false },
                                shape = MaterialTheme.shapes.small
                            ) {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (user.phone.isNotEmpty()) {
                            Text(
                                text = user.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                is Resource.Error -> Text("Error: ${state.message}")
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileOptionItem(
                icon = Icons.Default.Edit,
                title = "Editar Datos",
                onClick = {
                    if (userState is Resource.Success) {
                        val u = (userState as Resource.Success).data!!
                        editedName = u.name
                        editedPhone = u.phone
                        isEditing = true
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            BusifyButton(
                text = "Cerrar Sesión",
                onClick = onLogout,
                containerColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ProfileOptionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit = {}) {
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
