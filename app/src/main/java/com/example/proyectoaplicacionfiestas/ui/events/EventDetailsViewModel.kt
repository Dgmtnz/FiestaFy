package com.example.proyectoaplicacionfiestas.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoaplicacionfiestas.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsViewModel : ViewModel() {
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _isJoined = MutableLiveData<Boolean>()
    val isJoined: LiveData<Boolean> = _isJoined

    private val _hasPaid = MutableLiveData<Boolean>()
    val hasPaid: LiveData<Boolean> = _hasPaid

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun loadEvent(eventId: String) {
        firestore.collection("events").document(eventId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                snapshot?.toObject(Event::class.java)?.let { event ->
                    _event.value = event
                    checkParticipationStatus(event)
                    checkPaymentStatus(event)
                }
            }
    }

    private fun checkParticipationStatus(event: Event) {
        val currentUserId = auth.currentUser?.uid ?: return
        _isJoined.value = event.participants.contains(currentUserId)
    }

    private fun checkPaymentStatus(event: Event) {
        val currentUserId = auth.currentUser?.uid ?: return
        _hasPaid.value = event.payments[currentUserId] == true
    }

    fun joinEvent() {
        val currentUserId = auth.currentUser?.uid ?: return
        val eventId = _event.value?.id ?: return

        firestore.collection("events").document(eventId)
            .update("participants", arrayListOf(currentUserId))
    }

    fun makePayment() {
        val currentUserId = auth.currentUser?.uid ?: return
        val eventId = _event.value?.id ?: return

        firestore.collection("events").document(eventId)
            .update("payments.$currentUserId", true)
    }
} 