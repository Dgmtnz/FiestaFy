package com.example.fiestafy.models

data class Comment(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
) 