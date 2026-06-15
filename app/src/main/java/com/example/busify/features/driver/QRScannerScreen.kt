package com.example.busify.features.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.TicketRepository
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onBack: () -> Unit,
    ticketRepository: TicketRepository = remember { TicketRepository() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var scannedData by remember { mutableStateOf<String?>(null) }
    var ticketInfo by remember { mutableStateOf<TicketScanResult?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var isValid by remember { mutableStateOf<Boolean?>(null) }

    val scannerOptions = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }

    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context, scannerOptions)
    }

    fun startScan() {
        isScanning = true
        scannedData = null
        ticketInfo = null
        isValid = null
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                scannedData = barcode.rawValue
                isScanning = false
                barcode.rawValue?.let { data ->
                    val routeId = data.lines().find { it.startsWith("ID: ") }?.removePrefix("ID: ") ?: ""
                    if (routeId.isNotEmpty()) {
                        scope.launch {
                            val result = ticketRepository.getTicketsByRoute(routeId)
                            if (result is Resource.Success) {
                                val tickets = result.data ?: emptyList()
                                val pendingTickets = tickets.filter { it.status != "usado" }
                                if (pendingTickets.isNotEmpty()) {
                                    val first = pendingTickets.first()
                                    ticketInfo = TicketScanResult(
                                        origin = first.origin,
                                        destination = first.destination,
                                        company = first.company,
                                        seats = first.seatNumbers.sorted().joinToString(", "),
                                        ticketId = first.id,
                                        passengerCount = pendingTickets.size
                                    )
                                    isValid = true
                                } else {
                                    isValid = false
                                    scope.launch { snackbarHostState.showSnackbar("Todos los tickets ya fueron usados") }
                                }
                            } else {
                                isValid = false
                                scope.launch { snackbarHostState.showSnackbar("No se encontraron tickets para esta ruta") }
                            }
                        }
                    } else {
                        isValid = false
                        scope.launch { snackbarHostState.showSnackbar("QR inválido o no reconocido") }
                    }
                }
            }
            .addOnCanceledListener { isScanning = false }
            .addOnFailureListener { e ->
                isScanning = false
                scope.launch { snackbarHostState.showSnackbar("Error al escanear: ${e.message}") }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Escanea el código QR del pasajero",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Apunta al código QR del ticket para validar el ingreso",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { startScan() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Escanear QR", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // Result card
            if (ticketInfo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isValid == true) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (isValid == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = if (isValid == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (isValid == true) "Ticket Válido" else "Ticket Inválido",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isValid == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        if (ticketInfo != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${ticketInfo!!.origin} \u2192 ${ticketInfo!!.destination}")
                            Text(ticketInfo!!.company, style = MaterialTheme.typography.bodySmall)
                            Text("Asientos: ${ticketInfo!!.seats}", style = MaterialTheme.typography.bodySmall)
                            if (ticketInfo!!.passengerCount > 1) {
                                Text("+${ticketInfo!!.passengerCount - 1} pasajero(s) más en esta ruta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = {
                                scope.launch {
                                    ticketRepository.updateTicketStatus(ticketInfo!!.ticketId, "usado")
                                    snackbarHostState.showSnackbar("Ticket marcado como usado")
                                    ticketInfo = null; isValid = null
                                }
                            }) { Text("Marcar como Usado") }
                        }
                    }
                }
            }
        }
    }
}

data class TicketScanResult(
    val origin: String,
    val destination: String,
    val company: String,
    val seats: String,
    val ticketId: String,
    val passengerCount: Int
)
