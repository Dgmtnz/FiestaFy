package com.example.fiestafy.models

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val isAdmin: Boolean = false,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) 