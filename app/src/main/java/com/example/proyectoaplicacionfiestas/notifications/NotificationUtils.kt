package com.example.proyectoaplicacionfiestas.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object NotificationUtils {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun updateFcmToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
    }

    fun subscribeToEventNotifications(eventId: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("event_$eventId")
    }

    fun unsubscribeFromEventNotifications(eventId: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("event_$eventId")
    }
} 