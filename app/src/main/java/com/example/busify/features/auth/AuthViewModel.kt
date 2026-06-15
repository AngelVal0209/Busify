package com.example.busify.features.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.busify.core.util.Resource
import com.example.busify.data.repository.AuthRepository
import com.example.busify.domain.model.User
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

    private val _resetPasswordState = mutableStateOf<Resource<Unit>?>(null)
    val resetPasswordState: State<Resource<Unit>?> = _resetPasswordState

    private val _emailVerificationState = mutableStateOf<Resource<Unit>?>(null)
    val emailVerificationState: State<Resource<Unit>?> = _emailVerificationState

    private val _reauthenticateState = mutableStateOf<Resource<Unit>?>(null)
    val reauthenticateState: State<Resource<Unit>?> = _reauthenticateState

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
                _currentUserData.value = result.data
            }
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

    fun isEmailVerified(): Boolean = repository.isEmailVerified()

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            _resetPasswordState.value = repository.resetPassword(email)
        }
    }

    fun checkEmailVerification() {
        val isVerified = repository.isEmailVerified()
        if (isVerified) {
            _currentUserData.value?.let { user ->
                loadUserData(user.uid)
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            _emailVerificationState.value = Resource.Loading()
            _emailVerificationState.value = repository.sendEmailVerification()
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            val result = repository.updatePassword(newPassword)
            if (result is Resource.Error) {
                _reauthenticateState.value = Resource.Error(result.message ?: "Error al actualizar contraseña")
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            val result = repository.updateEmail(newEmail)
            if (result is Resource.Error) {
                _reauthenticateState.value = Resource.Error(result.message ?: "Error al actualizar correo")
            }
        }
    }

    fun reauthenticate(email: String, password: String) {
        viewModelScope.launch {
            _reauthenticateState.value = Resource.Loading()
            _reauthenticateState.value = repository.reauthenticate(email, password)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = repository.deleteAccount()
            if (result is Resource.Success) {
                repository.logout()
                _currentUserData.value = null
                _loginState.value = null
                _registerState.value = null
            } else {
                _reauthenticateState.value = Resource.Error(result.message ?: "Error al eliminar cuenta")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentUserData.value = null
        _loginState.value = null
        _registerState.value = null
    }

    fun clearResetPasswordState() {
        _resetPasswordState.value = null
    }

    fun clearEmailVerificationState() {
        _emailVerificationState.value = null
    }

    fun clearReauthenticateState() {
        _reauthenticateState.value = null
    }
}
