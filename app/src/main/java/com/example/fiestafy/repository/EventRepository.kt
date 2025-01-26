package com.example.fiestafy.repository

import com.example.fiestafy.models.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class AttendeeStatus(
    val userId: String = "",
    val hasPaid: Boolean = false,
    val paymentDate: Long? = null,
    val paymentMethod: String? = null
)

class EventRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")

    fun getEventsStream() = eventsCollection
        .orderBy("date")
        .snapshots()
        .map { snapshot -> 
            snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
        }

    suspend fun createEvent(event: Event): Result<Unit> = try {
        eventsCollection.add(event).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isUserAdmin(userId: String): Boolean {
        val adminDoc = firestore.collection("admins")
            .document(userId)
            .get()
            .await()
        return adminDoc.exists()
    }

    suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val attendeeStatus = AttendeeStatus(
                userId = userId,
                hasPaid = false,
                paymentDate = null,
                paymentMethod = null
            )
            
            val updates = hashMapOf<String, Any>(
                "attendees.$userId" to attendeeStatus,
                "currentAttendees" to FieldValue.increment(1)
            )
            
            firestore.collection("events")
                .document(eventId)
                .update(updates)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 