package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getCurrentUser(): com.google.firebase.auth.FirebaseUser? = auth.currentUser

    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Usuario no encontrado")
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: User(uid = firebaseUser.uid, email = email)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    suspend fun register(name: String, email: String, password: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Error al crear usuario")
            val user = User(uid = firebaseUser.uid, name = name, email = email)
            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al registrarse")
        }
    }

    suspend fun updateUser(user: User): Resource<Boolean> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar perfil")
        }
    }

    suspend fun getUserDetails(uid: String): Resource<User> {
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: return Resource.Error("Datos no encontrados")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener datos")
        }
    }

    fun logout() {
        auth.signOut()
    }
}