package com.example.busify.features.promos

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.busify.R

data class Promo(
    val title: String,
    val description: String,
    val image: Int,
    val discount: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromosScreen(
    navController: NavController
) {

    val promos = listOf(
        Promo(
            "Viaja a Lima",
            "Obtén descuento en rutas seleccionadas.",
            R.drawable.promo1,
            "20% OFF"
        ),
        Promo(
            "Pasajes para estudiantes",
            "Promoción válida presentando carnet.",
            R.drawable.promo2,
            "15% OFF"
        ),
        Promo(
            "Viaja en grupo",
            "Compra 4 boletos y recibe un descuento especial.",
            R.drawable.promo3,
            "25% OFF"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Promociones",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Text(
                    text = "Aprovecha nuestras mejores ofertas para viajar.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Box {

                        Image(
                            painter = painterResource(R.drawable.promo1),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {

                            Text(
                                text = "¡Hasta 30% de descuento!",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            items(promos) { promo ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {

                    Column {

                        Image(
                            painter = painterResource(promo.image),
                            contentDescription = promo.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = promo.discount,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = promo.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = promo.description
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    // Acción futura
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Aprovechar oferta")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}