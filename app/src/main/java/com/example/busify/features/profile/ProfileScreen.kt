package com.example.busify.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.components.BusifyButton
import com.example.busify.core.components.BusifyTextField
import com.example.busify.core.util.Resource
import android.widget.Toast

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val userState by viewModel.userState
    val updateState by viewModel.updateState
    val context = LocalContext.current
    
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            isEditing = false
            viewModel.resetUpdateState()
        } else if (updateState is Resource.Error) {
            Toast.makeText(context, updateState?.message ?: "Error", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Image Placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(40.dp))

        // Options List
        ProfileOptionItem(icon = Icons.Default.Settings, title = "Configuración")
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
        
        Spacer(modifier = Modifier.weight(1f))

        BusifyButton(
            text = "Cerrar Sesión",
            onClick = onLogout,
            containerColor = MaterialTheme.colorScheme.error
        )
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
