package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.SavedCard
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentMethodRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getSavedCards(userId: String): Resource<List<SavedCard>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId).collection("cards")
                .orderBy("isDefault", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            val cards = snapshot.toObjects(SavedCard::class.java)
            Resource.Success(cards)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cargar tarjetas")
        }
    }

    suspend fun saveCard(card: SavedCard): Resource<String> {
        return try {
            val docRef = firestore.collection("users")
                .document(card.userId).collection("cards").document()
            val cardWithId = card.copy(id = docRef.id)
            docRef.set(cardWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al guardar tarjeta")
        }
    }

    suspend fun deleteCard(userId: String, cardId: String): Resource<Boolean> {
        return try {
            firestore.collection("users").document(userId)
                .collection("cards").document(cardId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar tarjeta")
        }
    }

    suspend fun setDefaultCard(userId: String, cardId: String): Resource<Boolean> {
        return try {
            val cards = firestore.collection("users").document(userId)
                .collection("cards").get().await()
            for (doc in cards.documents) {
                doc.reference.update("isDefault", doc.id == cardId).await()
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al establecer tarjeta por defecto")
        }
    }
}
