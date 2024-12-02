package com.example.proyectoaplicacionfiestas.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoaplicacionfiestas.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore

class MapViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val firestore = FirebaseFirestore.getInstance()

    fun loadEvents() {
        firestore.collection("events")
            .whereNotEqualTo("location", null)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val eventsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                } ?: emptyList()
                _events.value = eventsList
            }
    }
} 