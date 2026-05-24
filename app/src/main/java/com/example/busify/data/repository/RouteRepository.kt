package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Route
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun updateRoute(route: Route): Resource<Boolean> {
        return try {
            firestore.collection("routes").document(route.id).set(route).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar la ruta")
        }
    }

    suspend fun deleteRoute(routeId: String): Resource<Boolean> {
        return try {
            firestore.collection("routes").document(routeId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar la ruta")
        }
    }

    suspend fun decrementCapacity(routeId: String, count: Int): Resource<Boolean> {
        return try {
            firestore.collection("routes").document(routeId)
                .update("capacity", com.google.firebase.firestore.FieldValue.increment(-count.toLong()))
                .await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar capacidad")
        }
    }

    suspend fun getPaginatedRoutes(lastVisibleId: String?, pageSize: Long = 20): Resource<Pair<List<Route>, String?>> {
        return try {
            var query = firestore.collection("routes")
                .orderBy("departureDate", Query.Direction.ASCENDING)
                .limit(pageSize)
            if (lastVisibleId != null) {
                val lastDoc = firestore.collection("routes").document(lastVisibleId).get().await()
                query = query.startAfter(lastDoc)
            }
            val snapshot = query.get().await()
            val routes = snapshot.toObjects(Route::class.java)
            val lastId = if (snapshot.documents.size == pageSize.toInt()) snapshot.documents.last().id else null
            Resource.Success(Pair(routes, lastId))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener rutas paginadas")
        }
    }
}