package com.example.fiestafy.models

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val important: Boolean = false
) 