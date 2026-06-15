package com.example.busify.data.repository

import com.example.busify.core.util.Resource
import com.example.busify.domain.model.Ticket
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TicketRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun saveTicket(ticket: Ticket): Resource<String> {
        return try {
            val docRef = firestore.collection("tickets").document()
            val ticketWithId = ticket.copy(id = docRef.id)
            docRef.set(ticketWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al guardar el ticket")
        }
    }

    suspend fun getBookedSeatsForRoute(routeId: String): Resource<List<Long>> {
        return try {
            val snapshot = firestore.collection("tickets")
                .whereEqualTo("routeId", routeId)
                .get()
                .await()
            val bookedSeats = snapshot.documents.flatMap { doc ->
                doc.toObject(Ticket::class.java)?.seatNumbers ?: emptyList()
            }
            Resource.Success(bookedSeats)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener asientos ocupados")
        }
    }

    suspend fun getUserTickets(userId: String): Resource<List<Ticket>> {
        return try {
            val snapshot = firestore.collection("tickets")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val tickets = snapshot.toObjects(Ticket::class.java)
                .sortedByDescending { it.createdAt }
            Resource.Success(tickets)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener tus tickets")
        }
    }

    suspend fun getTicketsByRoute(routeId: String): Resource<List<Ticket>> {
        return try {
            val snapshot = firestore.collection("tickets")
                .whereEqualTo("routeId", routeId)
                .get()
                .await()
            val tickets = snapshot.toObjects(Ticket::class.java)
            Resource.Success(tickets)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener tickets de la ruta")
        }
    }

    suspend fun updateTicketStatus(ticketId: String, status: String): Resource<Boolean> {
        return try {
            firestore.collection("tickets").document(ticketId)
                .update("status", status).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar estado del ticket")
        }
    }
}