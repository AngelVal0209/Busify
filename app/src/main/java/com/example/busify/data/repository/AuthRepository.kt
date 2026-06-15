package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.toObjects(User::class.java)
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener usuarios")
        }
    }

    suspend fun updateUserRole(uid: String, role: Long): Resource<Boolean> {
        return try {
            firestore.collection("users").document(uid)
                .update("role", role).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar rol")
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun listenToUserDetails(uid: String): Flow<Resource<User>> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error al escuchar datos"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(Resource.Success(user))
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar correo de restablecimiento")
        }
    }

    suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar verificación de correo")
        }
    }

    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified ?: false

    suspend fun updatePassword(newPassword: String): Resource<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar contraseña")
        }
    }

    suspend fun updateEmail(newEmail: String): Resource<Unit> {
        return try {
            auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar correo electrónico")
        }
    }

    suspend fun reauthenticate(email: String, password: String): Resource<Unit> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            auth.currentUser?.reauthenticate(credential)?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al reautenticar")
        }
    }

    suspend fun deleteAccount(): Resource<Unit> {
        return try {
            auth.currentUser?.let { user ->
                firestore.collection("users").document(user.uid).delete().await()
                user.delete().await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar cuenta")
        }
    }
}
