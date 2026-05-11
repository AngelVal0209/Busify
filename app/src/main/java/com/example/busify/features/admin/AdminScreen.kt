package com.example.busify.features.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = viewModel()
) {
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pendiente") }
    var capacity by remember { mutableStateOf("") }

    val createRouteState = viewModel.createRouteState.value

    LaunchedEffect(createRouteState) {
        if (createRouteState is Resource.Success) {
            // Reset fields on success
            origin = ""
            destination = ""
            departureTime = ""
            arrivalTime = ""
            status = "Pendiente"
            capacity = ""
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Panel de Administración",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Crear nueva ruta de transporte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = origin,
            onValueChange = { origin = it },
            label = { Text("Lugar de Salida") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            label = { Text("Lugar de Llegada") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = departureTime,
                onValueChange = { departureTime = it },
                label = { Text("Hora Salida") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("HH:MM") }
            )
            OutlinedTextField(
                value = arrivalTime,
                onValueChange = { arrivalTime = it },
                label = { Text("Hora Llegada") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("HH:MM") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = capacity,
            onValueChange = { if (it.all { char -> char.isDigit() }) capacity = it },
            label = { Text("Capacidad de Pasajeros") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text("Ej. 50") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            label = { Text("Estado") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.createRoute(
                    origin,
                    destination,
                    departureTime,
                    arrivalTime,
                    status,
                    capacity.toIntOrNull() ?: 0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(16.dp),
            enabled = createRouteState !is Resource.Loading
        ) {
            if (createRouteState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Ruta")
            }
        }

        if (createRouteState is Resource.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = createRouteState.message ?: "Error desconocido",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}