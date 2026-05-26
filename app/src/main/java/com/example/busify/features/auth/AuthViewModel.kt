package com.example.busify.features.auth

import android.util.Log
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
            loadUserData(fbUser.uid)
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            val result = repository.getUserDetails(uid)
            if (result is Resource.Success) {
                Log.d("AuthVM", "getUserDetails success: role=${result.data?.role}")
                _currentUserData.value = result.data
            } else {
                Log.e("AuthVM", "getUserDetails failed: ${result.message}")
            }
            repository.listenToUserDetails(uid).collect { result ->
                if (result is Resource.Success) {
                    Log.d("AuthVM", "snapshot listener: role=${result.data?.role}")
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
                Log.d("AuthVM", "login success: role=${result.data?.role}")
                _currentUserData.value = result.data
            }
        }
    }
    fun register(name: String, email: String, password: String, phone: String = "") {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = repository.register(name, email, password, phone)
            _registerState.value = result
            if (result is Resource.Success) {
                Log.d("AuthVM", "register success: role=${result.data?.role}")
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