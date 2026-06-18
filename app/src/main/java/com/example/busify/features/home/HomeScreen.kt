package com.example.busify.features.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.busify.R
import com.example.busify.core.components.EmptyState
import com.example.busify.core.components.ErrorState
import com.example.busify.core.components.ShimmerBusCard
import com.example.busify.core.navigation.Screen
import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.example.busify.features.auth.AuthViewModel
import com.example.busify.features.buses.BusesViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
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
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${userData?.name?.split(" ")?.first() ?: "Usuario"}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "¿A dónde vamos hoy?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Surface(
                        onClick = { },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val routeCount = if (routesState is Resource.Success) routesState.data?.size ?: 0 else 0
                StatCard(
                    label = "Rutas Activas",
                    value = "$routeCount",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatCard(
                    label = "Tu Rol",
                    value = when(userData?.role) { 2L -> "Admin"; 3L -> "Chofer"; else -> "Pasajero" },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Notifications,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Rutas Disponibles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (routesState) {
                is Resource.Loading -> {
                    repeat(2) { ShimmerBusCard(); Spacer(modifier = Modifier.height(8.dp)) }
                }
                is Resource.Error -> {
                    ErrorState(
                        message = routesState.message ?: "Error al cargar rutas",
                        onRetry = { busesViewModel.getRoutes() }
                    )
                }
                is Resource.Success -> {
                    val routes = routesState.data ?: emptyList()
                    if (routes.isEmpty()) {
                        EmptyState(
                            modifier = Modifier.height(300.dp),
                            title = "Sin rutas aún",
                            message = "El administrador no ha creado rutas disponibles."
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(routes.take(5)) { route ->
                                RouteCard(route = route, isFavorite = userData?.favoriteRoutes?.contains(route.id) == true)
                            }
                        }
                    }
                }
            }
        }
        //promos
        val banners = listOf(
            R.drawable.promos,
            R.drawable.promobus
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                shape = RoundedCornerShape(16.dp)
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
                            onClick = {
                                navController.navigate(Screen.Promos.route)
                            },
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
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Nuestros Aliados",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val logos = listOf(
            R.drawable.allinbus,
            R.drawable.megabus,
            R.drawable.linea,
            R.drawable.palomino,
            R.drawable.perubus,
            R.drawable.transporteschiclayo,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 28.dp)
        ) {

            items(logos) { logo ->

                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .height(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {

                    Image(
                        painter = painterResource(id = logo),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = contentColor.copy(alpha = 0.8f))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
private fun RouteCard(
    route: Route,
    isFavorite: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale("es", "PE")) }
    val departureStr = if (route.departureDate > 0) "${dateFormat.format(Date(route.departureDate))} ${route.departureTime}" else route.departureTime

    Card(
        modifier = Modifier.width(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.company,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${route.origin} - ${route.destination}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Favorito" else "Marcar favorito",
                        modifier = Modifier.padding(6.dp).size(18.dp),
                        tint = if (isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = departureStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "S/ ${route.price}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (route.status) {
                    "A tiempo" -> Color(0xFF10B981).copy(alpha = 0.1f)
                    "Demorado" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                }
            ) {
                Text(
                    text = route.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (route.status) {
                        "A tiempo" -> Color(0xFF10B981)
                        "Demorado" -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}
