package com.example.fiestafy.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fiestafy.models.Event
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    fun loadEvents() {
        firestore.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val eventsList = documents.mapNotNull { it.toObject(Event::class.java) }
                _events.value = eventsList
            }
    }
} 