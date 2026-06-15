package com.example.busify.features.driver

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.AuthRepository
import com.example.busify.data.repository.RouteRepository
import com.example.busify.data.repository.TicketRepository
import com.example.busify.domain.model.Route
import com.example.busify.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DriverViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val routeRepository: RouteRepository = RouteRepository(),
    private val ticketRepository: TicketRepository = TicketRepository()
) : ViewModel() {

    private val _assignedRoutes = mutableStateOf<Resource<List<Route>>>(Resource.Loading())
    val assignedRoutes: State<Resource<List<Route>>> = _assignedRoutes

    private val _routeTickets = mutableStateOf<Resource<List<Ticket>>?>(null)
    val routeTickets: State<Resource<List<Ticket>>?> = _routeTickets

    private val _selectedRoute = mutableStateOf<Route?>(null)
    val selectedRoute: State<Route?> = _selectedRoute

    private val _ticketStatusState = mutableStateOf<Resource<Boolean>?>(null)
    val ticketStatusState: State<Resource<Boolean>?> = _ticketStatusState

    init {
        loadAssignedRoutes()
    }

    fun loadAssignedRoutes() {
        val driverId = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _assignedRoutes.value = Resource.Loading()
            val result = routeRepository.getRoutes()
            if (result is Resource.Success) {
                val driverRoutes = result.data?.filter { it.driverId == driverId || it.driverId.isEmpty() } ?: emptyList()
                _assignedRoutes.value = Resource.Success(driverRoutes)
            } else {
                _assignedRoutes.value = result
            }
        }
    }

    fun selectRoute(route: Route) {
        _selectedRoute.value = route
        viewModelScope.launch {
            _routeTickets.value = Resource.Loading()
            _routeTickets.value = ticketRepository.getTicketsByRoute(route.id)
        }
    }

    fun markTicketUsed(ticketId: String) {
        viewModelScope.launch {
            _ticketStatusState.value = ticketRepository.updateTicketStatus(ticketId, "usado")
        }
    }

    fun clearSelection() {
        _selectedRoute.value = null
        _routeTickets.value = null
    }
}
