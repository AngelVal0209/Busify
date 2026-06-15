package com.example.busify.features.viajes

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.TicketRepository
import com.example.busify.domain.model.Ticket
import com.example.busify.features.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

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
    val context = LocalContext.current
    val seatList = remember(seats) { seats.split("_").mapNotNull { it.toLongOrNull() }.sorted() }
    val totalPrice = remember(seatList, price) { seatList.size * price }

    val qrContent = remember(routeId, company, origin, destination, seats, departureTime) {
        "Busify Ticket\nRuta: $origin → $destination\nEmpresa: $company\nSalida: $departureTime\nAsientos: ${seatList.joinToString(", ")}\nID: $routeId"
    }

    val qrBitmap = remember(qrContent) { generateQrCode(qrContent) }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val confettiProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ticket", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val numConfetti = 20
                for (i in 0 until numConfetti) {
                    val x = (size.width / numConfetti) * i + (size.width / numConfetti) * confettiProgress
                    val y = ((confettiProgress + i * 0.1f) % 1f) * size.height
                    val colors = listOf(
                        Color(0xFF6366F1), Color(0xFF10B981), Color(0xFFF59E0B),
                        Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFFEC4899)
                    )
                    drawCircle(
                        color = colors[i % colors.size].copy(alpha = 0.6f),
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(36.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Compra exitosa!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Gracias por viajar con Busify",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR del ticket",
                        modifier = Modifier.size(160.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            shareTicket(context, qrContent, qrBitmap)
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        navController.navigate(com.example.busify.core.navigation.Screen.Home.route) {
                            popUpTo(com.example.busify.core.navigation.Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Volver al inicio", fontWeight = FontWeight.Bold)
                }
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
    } catch (e: Exception) { null }
}

private fun shareTicket(context: Context, text: String, qrBitmap: Bitmap?) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir ticket"))
}
