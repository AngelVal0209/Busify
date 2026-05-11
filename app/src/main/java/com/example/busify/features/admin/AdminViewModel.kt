package com.example.busify.features.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.RouteRepository
import com.example.busify.domain.model.Route
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val _createRouteState = mutableStateOf<Resource<String>?>(null)
    val createRouteState: State<Resource<String>?> = _createRouteState

    fun createRoute(
        origin: String,
        destination: String,
        departureTime: String,
        arrivalTime: String,
        status: String,
        capacity: Int
    ) {
        if (origin.isBlank() || destination.isBlank() || departureTime.isBlank() || arrivalTime.isBlank() || status.isBlank() || capacity <= 0) {
            _createRouteState.value = Resource.Error("Por favor completa todos los campos correctamente")
            return
        }

        viewModelScope.launch {
            _createRouteState.value = Resource.Loading()
            val route = Route(
                origin = origin,
                destination = destination,
                departureTime = departureTime,
                arrivalTime = arrivalTime,
                status = status,
                capacity = capacity
            )
            val result = repository.createRoute(route)
            _createRouteState.value = result
        }
    }
    
    fun resetState() {
        _createRouteState.value = null
    }
}