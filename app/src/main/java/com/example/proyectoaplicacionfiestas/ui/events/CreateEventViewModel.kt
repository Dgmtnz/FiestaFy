package com.example.proyectoaplicacionfiestas.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoaplicacionfiestas.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.UUID

class CreateEventViewModel : ViewModel() {
    private val _eventCreated = MutableLiveData<Boolean>()
    val eventCreated: LiveData<Boolean> = _eventCreated

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createEvent(
        title: String,
        description: String,
        date: Long,
        price: Double,
        location: GeoPoint?
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        val event = Event(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            date = date,
            location = location,
            price = price,
            createdBy = currentUserId
        )

        firestore.collection("events")
            .document(event.id)
            .set(event)
            .addOnSuccessListener {
                _eventCreated.value = true
            }
            .addOnFailureListener {
                _eventCreated.value = false
            }
    }
} 