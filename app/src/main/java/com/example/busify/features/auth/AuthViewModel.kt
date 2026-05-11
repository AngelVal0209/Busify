package com.example.busify.features.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.AuthRepository
import com.example.busify.domain.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _loginState = mutableStateOf<Resource<User>?>(null)
    val loginState: State<Resource<User>?> = _loginState

    private val _registerState = mutableStateOf<Resource<User>?>(null)
    val registerState: State<Resource<User>?> = _registerState

    private val _currentUserData = mutableStateOf<User?>(null)
    val currentUserData: State<User?> = _currentUserData

    init {
        val fbUser = repository.getCurrentUser()
        if (fbUser != null) {
            getUserData(fbUser.uid)
        }
    }

    private fun getUserData(uid: String) {
        viewModelScope.launch {
            repository.listenToUserDetails(uid).collect { result ->
                if (result is Resource.Success) {
                    _currentUserData.value = result.data
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = repository.login(email, password)
            _loginState.value = result
            if (result is Resource.Success) {
                _currentUserData.value = result.data
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = repository.register(name, email, password)
            _registerState.value = result
            if (result is Resource.Success) {
                _currentUserData.value = result.data
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUserData.value = null
        _loginState.value = null
        _registerState.value = null
    }
}