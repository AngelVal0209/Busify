package com.example.busify.features.buses

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.RouteRepository
import com.example.busify.domain.model.Route
import kotlinx.coroutines.launch

data class BusesFilter(
    val searchQuery: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 1000.0,
    val busType: String = "",
    val company: String = "",
    val sortBy: SortOption = SortOption.PRICE_ASC
)

enum class SortOption {
    PRICE_ASC, PRICE_DESC, TIME_ASC, NONE
}

class BusesViewModel(
    private val repository: RouteRepository = RouteRepository()
) : ViewModel() {

    private val _routesState = mutableStateOf<Resource<List<Route>>>(Resource.Loading())
    val routesState: State<Resource<List<Route>>> = _routesState

    private val _filter = mutableStateOf(BusesFilter())
    val filter: State<BusesFilter> = _filter

    init { getRoutes() }

    fun getRoutes() {
        viewModelScope.launch {
            _routesState.value = Resource.Loading()
            _routesState.value = repository.getRoutes()
        }
    }

    fun updateFilter(newFilter: BusesFilter) {
        _filter.value = newFilter
    }

    fun getFilteredRoutes(): List<Route> {
        val routes = (_routesState.value as? Resource.Success)?.data ?: return emptyList()
        val f = _filter.value
        return routes
            .filter { route ->
                (f.searchQuery.isBlank() ||
                    route.origin.contains(f.searchQuery, true) ||
                    route.destination.contains(f.searchQuery, true) ||
                    route.company.contains(f.searchQuery, true)) &&
                route.price >= f.minPrice &&
                route.price <= f.maxPrice &&
                (f.busType.isBlank() || route.busType == f.busType) &&
                (f.company.isBlank() || route.company == f.company)
            }
            .sortedWith(
                when (f.sortBy) {
                    SortOption.PRICE_ASC -> compareBy<Route> { it.price }
                    SortOption.PRICE_DESC -> compareByDescending<Route> { it.price }
                    SortOption.TIME_ASC -> compareBy<Route> { it.departureTime }
                    SortOption.NONE -> compareBy { it.id }
                }
            )
    }
}
