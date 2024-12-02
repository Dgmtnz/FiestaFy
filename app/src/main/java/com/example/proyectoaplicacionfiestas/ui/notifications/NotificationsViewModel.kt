package com.example.proyectoaplicacionfiestas.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoaplicacionfiestas.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadUserEvents() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("events")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { documents ->
                    val eventsList = documents.mapNotNull { doc ->
                        doc.toObject(Event::class.java)
                    }
                    _events.value = eventsList
                }
            }
    }

    fun toggleNotifications(eventId: String, enable: Boolean) {
        if (enable) {
            NotificationUtils.subscribeToEventNotifications(eventId)
        } else {
            NotificationUtils.unsubscribeFromEventNotifications(eventId)
        }

        // Update event notification preference in Firestore
        firestore.collection("events")
            .document(eventId)
            .update("notificationEnabled", enable)
    }
}