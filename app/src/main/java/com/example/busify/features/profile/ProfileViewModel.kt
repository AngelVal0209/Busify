package com.example.busify.features.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.AuthRepository
import com.example.busify.data.repository.TicketRepository
import com.example.busify.domain.model.Ticket
import com.example.busify.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val ticketRepository: TicketRepository = TicketRepository()
) : ViewModel() {

    private val _userState = mutableStateOf<Resource<User>>(Resource.Loading())
    val userState: State<Resource<User>> = _userState

    private val _updateState = mutableStateOf<Resource<Boolean>?>(null)
    val updateState: State<Resource<Boolean>?> = _updateState

    private val _ticketsState = mutableStateOf<Resource<List<Ticket>>>(Resource.Loading())
    val ticketsState: State<Resource<List<Ticket>>> = _ticketsState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                _userState.value = repository.getUserDetails(currentUser.uid)
                _ticketsState.value = ticketRepository.getUserTickets(currentUser.uid)
            }
        }
    }

    fun updateUser(name: String) {
        val currentUser = _userState.value.data ?: return
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            val updatedUser = currentUser.copy(name = name)
            val result = repository.updateUser(updatedUser)
            _updateState.value = result
            if (result is Resource.Success) {
                _userState.value = Resource.Success(updatedUser)
            }
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = null
    }
}