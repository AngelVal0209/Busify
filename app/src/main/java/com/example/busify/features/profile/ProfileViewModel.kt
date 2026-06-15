package com.example.busify.features.profile

import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val ticketRepository: TicketRepository = TicketRepository(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _userState = mutableStateOf<Resource<User>>(Resource.Loading())
    val userState: State<Resource<User>> = _userState

    private val _updateState = mutableStateOf<Resource<Boolean>?>(null)
    val updateState: State<Resource<Boolean>?> = _updateState

    private val _ticketsState = mutableStateOf<Resource<List<Ticket>>>(Resource.Loading())
    val ticketsState: State<Resource<List<Ticket>>> = _ticketsState

    private val _passwordUpdateState = mutableStateOf<Resource<Unit>?>(null)
    val passwordUpdateState: State<Resource<Unit>?> = _passwordUpdateState

    private val _emailUpdateState = mutableStateOf<Resource<Unit>?>(null)
    val emailUpdateState: State<Resource<Unit>?> = _emailUpdateState

    private val _deleteAccountState = mutableStateOf<Resource<Unit>?>(null)
    val deleteAccountState: State<Resource<Unit>?> = _deleteAccountState

    private val _photoUploadState = mutableStateOf<Resource<String>?>(null)
    val photoUploadState: State<Resource<String>?> = _photoUploadState

    private val _reauthenticateState = mutableStateOf<Resource<Unit>?>(null)
    val reauthenticateState: State<Resource<Unit>?> = _reauthenticateState

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

    fun uploadProfilePhoto(uri: Uri) {
        val uid = repository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _photoUploadState.value = Resource.Loading()
            try {
                val storageRef = storage.reference.child("profile_photos/${uid}.jpg")
                storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                val urlString = downloadUrl.toString()
                firestore.collection("users").document(uid)
                    .update("photoUrl", urlString).await()
                _photoUploadState.value = Resource.Success(urlString)
                loadUserProfile()
            } catch (e: Exception) {
                _photoUploadState.value = Resource.Error(e.message ?: "Error al subir foto")
            }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        val user = _userState.value.data ?: return
        viewModelScope.launch {
            _passwordUpdateState.value = Resource.Loading()
            val reauth = repository.reauthenticate(user.email, currentPassword)
            if (reauth is Resource.Error) {
                _passwordUpdateState.value = Resource.Error(reauth.message ?: "Error de autenticación")
                return@launch
            }
            val result = repository.updatePassword(newPassword)
            _passwordUpdateState.value = result
        }
    }

    fun updateEmail(currentPassword: String, newEmail: String) {
        val user = _userState.value.data ?: return
        viewModelScope.launch {
            _emailUpdateState.value = Resource.Loading()
            val reauth = repository.reauthenticate(user.email, currentPassword)
            if (reauth is Resource.Error) {
                _emailUpdateState.value = Resource.Error(reauth.message ?: "Error de autenticación")
                return@launch
            }
            val result = repository.updateEmail(newEmail)
            _emailUpdateState.value = result
            if (result is Resource.Success) {
                firestore.collection("users").document(user.uid)
                    .update("email", newEmail).await()
            }
        }
    }

    fun deleteAccount(password: String) {
        val user = _userState.value.data ?: return
        viewModelScope.launch {
            _deleteAccountState.value = Resource.Loading()
            val reauth = repository.reauthenticate(user.email, password)
            if (reauth is Resource.Error) {
                _deleteAccountState.value = Resource.Error(reauth.message ?: "Error de autenticación")
                return@launch
            }
            val result = repository.deleteAccount()
            _deleteAccountState.value = result
        }
    }

    fun resetUpdateState() { _updateState.value = null }
    fun resetPasswordUpdateState() { _passwordUpdateState.value = null }
    fun resetEmailUpdateState() { _emailUpdateState.value = null }
    fun resetDeleteAccountState() { _deleteAccountState.value = null }
    fun resetPhotoUploadState() { _photoUploadState.value = null }
}
