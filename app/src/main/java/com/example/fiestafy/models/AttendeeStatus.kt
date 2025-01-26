package com.example.fiestafy.models

data class AttendeeStatus(
    val userId: String = "",
    val hasPaid: Boolean = false,
    val paymentDate: Long? = null,
    val paymentMethod: String? = null
)


