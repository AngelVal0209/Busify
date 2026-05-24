package com.example.busify.features.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.AuthRepository
import com.example.busify.data.repository.RouteRepository
import com.example.busify.domain.model.Route
import com.example.busify.domain.model.User
import kotlinx.coroutines.launch

data class AdminFormState(
    val origin: String = "",
    val destination: String = "",
    val departureDate: Long = 0L,
    val arrivalDate: Long = 0L,
    val departureTime: String = "",
    val arrivalTime: String = "",
    val status: String = "Pendiente",
    val capacity: String = "40",
    val company: String = "",
    val price: String = "",
    val busType: String = "Semi cama",
    val driverId: String = "",
    val driverName: String = ""
)

data class FieldError(
    val origin: String? = null,
    val destination: String? = null,
    val departureDate: String? = null,
    val arrivalDate: String? = null,
    val departureTime: String? = null,
    val arrivalTime: String? = null,
    val price: String? = null,
    val capacity: String? = null,
    val company: String? = null
)

class AdminViewModel(
    private val repository: RouteRepository = RouteRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _createRouteState = mutableStateOf<Resource<String>?>(null)
    val createRouteState: State<Resource<String>?> = _createRouteState

    private val _routesState = mutableStateOf<Resource<List<Route>>?>(null)
    val routesState: State<Resource<List<Route>>?> = _routesState

    private val _deleteState = mutableStateOf<Resource<Boolean>?>(null)
    val deleteState: State<Resource<Boolean>?> = _deleteState

    private val _editingRoute = mutableStateOf<Route?>(null)
    val editingRoute: State<Route?> = _editingRoute

    private val _formState = mutableStateOf(AdminFormState())
    val formState: State<AdminFormState> = _formState

    private val _fieldErrors = mutableStateOf(FieldError())
    val fieldErrors: State<FieldError> = _fieldErrors

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _selectedTab = mutableStateOf(0)
    val selectedTab: State<Int> = _selectedTab

    private val _usersState = mutableStateOf<Resource<List<User>>?>(null)
    val usersState: State<Resource<List<User>>?> = _usersState

    private val _roleUpdateState = mutableStateOf<Resource<Boolean>?>(null)
    val roleUpdateState: State<Resource<Boolean>?> = _roleUpdateState

    fun setSelectedTab(tab: Int) { _selectedTab.value = tab }

    fun updateField(field: String, value: String) {
        _formState.value = when (field) {
            "origin" -> _formState.value.copy(origin = value)
            "destination" -> _formState.value.copy(destination = value)
            "status" -> _formState.value.copy(status = value)
            "capacity" -> _formState.value.copy(capacity = value)
            "company" -> _formState.value.copy(company = value)
            "price" -> _formState.value.copy(price = value)
            "busType" -> _formState.value.copy(busType = value)
            "departureTime" -> _formState.value.copy(departureTime = value)
            "arrivalTime" -> _formState.value.copy(arrivalTime = value)
            "driverName" -> _formState.value.copy(driverName = value)
            "driverId" -> _formState.value.copy(driverId = value)
            else -> _formState.value
        }
        validateField(field, value)
    }

    fun setDepartureDate(millis: Long) {
        _formState.value = _formState.value.copy(departureDate = millis)
        validateField("departureDate", millis.toString())
    }

    fun setArrivalDate(millis: Long) {
        _formState.value = _formState.value.copy(arrivalDate = millis)
        validateField("arrivalDate", millis.toString())
    }

    private fun validateField(field: String, value: String) {
        val errors = _fieldErrors.value
        _fieldErrors.value = when (field) {
            "origin" -> errors.copy(origin = if (value.isBlank()) "El origen es obligatorio" else null)
            "destination" -> errors.copy(destination = if (value.isBlank()) "El destino es obligatorio" else null)
            "departureDate" -> errors.copy(departureDate = if (_formState.value.departureDate == 0L) "Selecciona fecha de salida" else null)
            "arrivalDate" -> errors.copy(arrivalDate = if (_formState.value.arrivalDate == 0L) "Selecciona fecha de llegada" else null)
            "departureTime" -> errors.copy(departureTime = if (value.isBlank()) "Hora de salida obligatoria" else null)
            "arrivalTime" -> errors.copy(arrivalTime = if (value.isBlank()) "Hora de llegada obligatoria" else null)
            "price" -> errors.copy(price = if (value.toDoubleOrNull() == null || value.toDouble() <= 0) "Precio inválido" else null)
            "capacity" -> errors.copy(capacity = if (value.toLongOrNull() == null || value.toLong() <= 0L) "Capacidad inválida" else null)
            "company" -> errors.copy(company = if (value.isBlank()) "La empresa es obligatoria" else null)
            else -> errors
        }
    }

    fun validateAll(): Boolean {
        val form = _formState.value
        updateField("origin", form.origin)
        updateField("destination", form.destination)
        updateField("departureDate", if (form.departureDate > 0) "ok" else "")
        updateField("arrivalDate", if (form.arrivalDate > 0) "ok" else "")
        updateField("departureTime", form.departureTime)
        updateField("arrivalTime", form.arrivalTime)
        updateField("price", form.price)
        updateField("capacity", form.capacity)
        updateField("company", form.company)
        val err = _fieldErrors.value
        return err.origin == null && err.destination == null &&
                err.departureDate == null && err.arrivalDate == null &&
                err.departureTime == null && err.arrivalTime == null &&
                err.price == null && err.capacity == null && err.company == null
    }

    fun submitRoute() {
        if (!validateAll()) {
            _createRouteState.value = Resource.Error("Corrige los errores del formulario")
            return
        }
        val form = _formState.value
        viewModelScope.launch {
            _createRouteState.value = Resource.Loading()
            val editing = _editingRoute.value
            val route = Route(
                id = editing?.id ?: "",
                origin = form.origin.trim(),
                destination = form.destination.trim(),
                departureTime = form.departureTime,
                arrivalTime = form.arrivalTime,
                departureDate = form.departureDate,
                arrivalDate = form.arrivalDate,
                status = form.status,
                capacity = form.capacity.toLongOrNull() ?: 0L,
                company = form.company.trim(),
                price = form.price.toDoubleOrNull() ?: 0.0,
                busType = form.busType,
                driverId = form.driverId,
                driverName = form.driverName
            )
            val result = if (editing != null) {
                when (val updateResult = repository.updateRoute(route)) {
                    is Resource.Success -> Resource.Success(editing.id)
                    is Resource.Error -> Resource.Error(updateResult.message ?: "Error al actualizar la ruta")
                    else -> Resource.Error("Error al actualizar la ruta")
                }
            } else {
                repository.createRoute(route)
            }
            _createRouteState.value = result
        }
    }

    fun loadRoutes() {
        viewModelScope.launch {
            _routesState.value = Resource.Loading()
            val result = repository.getRoutes()
            _routesState.value = result
        }
    }

    fun deleteRoute(routeId: String) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            val result = repository.deleteRoute(routeId)
            _deleteState.value = result
            if (result is Resource.Success) loadRoutes()
        }
    }

    fun startEdit(route: Route) {
        _editingRoute.value = route
        _formState.value = AdminFormState(
            origin = route.origin,
            destination = route.destination,
            departureDate = route.departureDate,
            arrivalDate = route.arrivalDate,
            departureTime = route.departureTime,
            arrivalTime = route.arrivalTime,
            status = route.status,
            capacity = route.capacity.toString(),
            company = route.company,
            price = route.price.toString(),
            busType = route.busType,
            driverId = route.driverId,
            driverName = route.driverName
        )
        _selectedTab.value = 0
        _fieldErrors.value = FieldError()
    }

    fun cancelEdit() {
        _editingRoute.value = null
        resetForm()
    }

    fun resetForm() {
        _formState.value = AdminFormState()
        _fieldErrors.value = FieldError()
        _createRouteState.value = null
    }

    fun resetState() {
        _createRouteState.value = null
        _deleteState.value = null
        if (_editingRoute.value == null) resetForm()
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun getFilteredRoutes(): List<Route> {
        val routes = (_routesState.value as? Resource.Success)?.data ?: return emptyList()
        val query = _searchQuery.value.lowercase().trim()
        if (query.isBlank()) return routes
        return routes.filter {
            it.origin.lowercase().contains(query) ||
            it.destination.lowercase().contains(query) ||
            it.company.lowercase().contains(query)
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = Resource.Loading()
            val result = authRepository.getAllUsers()
            _usersState.value = result
        }
    }

    fun setUserRole(uid: String, role: Long) {
        viewModelScope.launch {
            _roleUpdateState.value = Resource.Loading()
            val result = authRepository.updateUserRole(uid, role)
            _roleUpdateState.value = result
            if (result is Resource.Success) loadUsers()
        }
    }

    fun resetRoleState() {
        _roleUpdateState.value = null
    }
}