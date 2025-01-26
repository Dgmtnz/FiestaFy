package com.example.fiestafy.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fiestafy.models.Event
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CalendarViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    fun loadEventsForDate(timestamp: Long) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        firestore.collection("events")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThan("date", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                _events.value = documents.mapNotNull { it.toObject(Event::class.java) }
            }
    }
} 