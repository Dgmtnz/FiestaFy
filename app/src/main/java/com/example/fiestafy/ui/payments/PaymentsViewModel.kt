package com.example.fiestafy.ui.payments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fiestafy.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fiestafy.models.AttendeeStatus

class PaymentsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _userEvents = MutableLiveData<List<Event>>()
    val userEvents: LiveData<List<Event>> = _userEvents
    
    fun loadUserEvents() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("events")
            .whereGreaterThan("price", 0)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("PaymentsViewModel", "Total events with price > 0: ${documents.size()}")
                
                val pendingPaymentEvents = documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                }
                Log.d("PaymentsViewModel", "Events after mapping: ${pendingPaymentEvents.size}")
                
                val filteredEvents = pendingPaymentEvents.filter { event ->
                    val isAttendee = event.attendees[userId] != null
                    val hasPaid = event.attendees[userId]?.hasPaid ?: true
                    Log.d("PaymentsViewModel", "Event ${event.title}: isAttendee=$isAttendee, hasPaid=$hasPaid")
                    isAttendee && !hasPaid
                }
                
                Log.d("PaymentsViewModel", "Final filtered events: ${filteredEvents.size}")
                _userEvents.value = filteredEvents
            }
            .addOnFailureListener { e ->
                Log.e("PaymentsViewModel", "Error loading events", e)
            }
    }
    
    fun processPayment(eventId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        val updates = hashMapOf<String, Any>(
            "attendees.$userId.hasPaid" to true,
            "attendees.$userId.paymentDate" to System.currentTimeMillis(),
            "attendees.$userId.paymentMethod" to "SIMULATED"
        )
        
        firestore.collection("events")
            .document(eventId)
            .update(updates)
            .addOnSuccessListener {
                loadUserEvents() // Recargar la lista después del pago
            }
    }
} 