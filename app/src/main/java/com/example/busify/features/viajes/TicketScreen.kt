package com.example.busify.features.viajes

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.core.navigation.Screen
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    navController: NavController,
    routeId: String,
    company: String,
    origin: String,
    destination: String,
    seats: String,
    price: Double,
    paymentMethod: String,
    departureTime: String
) {
    val seatList = remember(seats) {
        seats.split("_").mapNotNull { it.toLongOrNull() }.sorted()
    }
    val totalPrice = remember(seatList, price) { seatList.size * price }

    val qrContent = remember(routeId, company, origin, destination, seats, departureTime) {
        "Busify Ticket\nRuta: $origin → $destination\nEmpresa: $company\nSalida: $departureTime\nAsientos: ${seatList.joinToString(", ")}\nID: $routeId"
    }

    val qrBitmap = remember(qrContent) {
        generateQrCode(qrContent)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ticket") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF10B981)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ticket Generado",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Gracias por viajar con Busify",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    TicketRow("Empresa", company)
                    TicketRow("Ruta", "$origin → $destination")
                    TicketRow("Salida", departureTime)
                    TicketRow("Asientos", seatList.joinToString(", "))
                    TicketRow("Cantidad", "${seatList.size}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    TicketRow("Método de pago", paymentMethod)
                    TicketRow(
                        label = "Total pagado",
                        value = "S/ ${"%.2f".format(totalPrice)}",
                        valueWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (qrBitmap != null) {
                Text(
                    text = "Código QR del viaje",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR del ticket",
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Volver al inicio")
            }
        }
    }
}

@Composable
private fun TicketRow(label: String, value: String, valueWeight: FontWeight = FontWeight.Normal) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = valueWeight
        )
    }
}

private fun generateQrCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
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