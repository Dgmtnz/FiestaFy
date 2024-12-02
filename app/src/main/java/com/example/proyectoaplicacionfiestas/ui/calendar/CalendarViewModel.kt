package com.example.proyectoaplicacionfiestas.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoaplicacionfiestas.data.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class CalendarViewModel : ViewModel() {
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val firestore = FirebaseFirestore.getInstance()

    fun loadEvents() {
        firestore.collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val eventsList = documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)
                }
                _events.value = eventsList
            }
    }

    fun getEventsForDate(date: Date): List<Event> {
        return _events.value?.filter { event ->
            // Compare the dates ignoring time
            isSameDay(event.date, date)
        } ?: emptyList()
    }

    private fun isSameDay(date1: Long, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
                cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH)
    }
} 