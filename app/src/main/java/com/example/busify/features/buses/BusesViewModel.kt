package com.example.busify.features.buses

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.RouteRepository
import com.example.busify.domain.model.Route
import kotlinx.coroutines.launch

class BusesViewModel(
    private val repository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val _routesState = mutableStateOf<Resource<List<Route>>>(Resource.Loading())
    val routesState: State<Resource<List<Route>>> = _routesState

    init {
        getRoutes()
    }

    fun getRoutes() {
        viewModelScope.launch {
            _routesState.value = Resource.Loading()
            val result = repository.getRoutes()
            _routesState.value = result
        }
    }
}