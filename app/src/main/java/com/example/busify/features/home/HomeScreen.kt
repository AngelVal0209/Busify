package com.example.busify.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.example.busify.features.auth.AuthViewModel
import com.example.busify.features.buses.BusesViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.busify.R
import androidx.navigation.NavController
import com.example.busify.core.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    busesViewModel: BusesViewModel = viewModel()
) {
    val userData = authViewModel.currentUserData.value
    val routesState = busesViewModel.routesState.value
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        //header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, ${userData?.name ?: "Usuario"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "¿A dónde vamos hoy?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            IconButton(
                onClick = {
                    //funnoti
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        //titulo
        Text(
            text = "Estado del Sistema",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        //card superiores
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val routeCount = if (routesState is Resource.Success) routesState.data?.size ?: 0 else 0
            StatCard(
                label = "Rutas Activas",
                value = routeCount.toString(),
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            StatCard(
                label = "Tu Rol",
                value = when(userData?.role) {
                    2L -> "Admin"
                    3L -> "Chofer"
                    else -> "Usuario"
                },
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        //titulo
        Text(
            text = "Rutas Disponibles",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        //mostrar todas las rutas
        when (routesState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val routes = routesState.data ?: emptyList()
                if (routes.isEmpty()) {
                    Text(
                        text = "No hay rutas disponibles creadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(routes) { route ->
                            RouteCard(route)
                        }
                    }
                }
            }
            is Resource.Error -> {
                Text(text = "Error al cargar rutas", color = MaterialTheme.colorScheme.error)
            }
        }
        //promos
        val banners = listOf(
            R.drawable.promos,
            R.drawable.promo_university
        )

        val pagerState = rememberPagerState(
            pageCount = { banners.size }
        )

        LaunchedEffect(Unit) {

            while (true) {

                kotlinx.coroutines.delay(3000)

                val nextPage =
                    (pagerState.currentPage + 1) % banners.size

                pagerState.animateScrollToPage(nextPage)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.large
            ) {

                Box {

                    Image(
                        painter = painterResource(id = banners[page]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),

                        verticalArrangement = Arrangement.Bottom
                    ) {

                        Text(
                            text = "Descuento en pasajes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Obtén acceso a descuentos exclusivos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        //BP
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {

                            Text("Ver promociones")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier, containerColor: Color) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RouteCard(route: Route) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "PE")) }
    val departureStr = if (route.departureDate > 0) {
        "${dateFormat.format(Date(route.departureDate))} ${route.departureTime}"
    } else {
        route.departureTime
    }

    Card(
        modifier = Modifier.width(240.dp).height(150.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${route.origin} - ${route.destination}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(
                text = route.company,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Salida: $departureStr",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "S/ ${route.price}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = route.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
