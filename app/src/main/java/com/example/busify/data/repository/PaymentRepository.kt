package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Payment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun recordPayment(payment: Payment): Resource<String> {
        return try {
            val docRef = firestore.collection("payments").document()
            val paymentWithId = payment.copy(id = docRef.id)
            docRef.set(paymentWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al registrar el pago")
        }
    }
}