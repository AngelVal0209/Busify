package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RouteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createRoute(route: Route): Resource<String> {
        return try {
            val documentRef = firestore.collection("routes").document()
            val routeWithId = route.copy(id = documentRef.id)
            documentRef.set(routeWithId).await()
            Resource.Success(documentRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear la ruta")
        }
    }

    suspend fun getRoutes(): Resource<List<Route>> {
        return try {
            val snapshot = firestore.collection("routes").get().await()
            val routes = snapshot.toObjects(Route::class.java)
            Resource.Success(routes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener las rutas")
        }
    }
}