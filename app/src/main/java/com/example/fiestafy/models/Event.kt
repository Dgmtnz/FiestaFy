package com.example.fiestafy.models

import com.example.fiestafy.models.AttendeeStatus

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0,
    val location: EventLocation = EventLocation(),
    val price: Double = 0.0,
    val capacity: Int = 0,
    val currentAttendees: Int = 0,
    val organizerId: String = "",
    val organizerName: String = "",
    val imageUrl: String = "",
    val tags: List<String> = listOf(),
    val isPrivate: Boolean = false,
    val minimumAge: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val attendees: Map<String, AttendeeStatus> = mapOf()
)

data class EventLocation(
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) 