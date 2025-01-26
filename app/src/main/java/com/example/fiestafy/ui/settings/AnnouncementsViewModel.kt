package com.example.fiestafy.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fiestafy.models.Announcement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.fiestafy.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.util.*

class AnnouncementsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    private val auth = FirebaseAuth.getInstance()

    init {
        loadAnnouncements()
        checkAdminStatus()
    }

    private fun loadAnnouncements() {
        firestore.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val announcementsList = snapshot?.documents?.mapNotNull {
                    it.toObject(Announcement::class.java)
                } ?: emptyList()

                _announcements.value = announcementsList
            }
    }

    private fun checkAdminStatus() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                _isAdmin.value = document.getBoolean("isAdmin") == true
            }
    }

    fun createAnnouncement(title: String, content: String, important: Boolean) {
        val announcement = Announcement(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            important = important,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("announcements")
            .document(announcement.id)
            .set(announcement)
            .addOnSuccessListener {
                val notificationTitle = if (important) "🔔 Anuncio Importante" else "📢 Nuevo Anuncio"
                viewModelScope.launch {
                    NotificationHelper.sendNotificationToAllUsers(
                        notificationTitle,
                        "$title: $content"
                    )
                }
            }
            .addOnFailureListener { e ->
                // Manejar el error
            }
    }
} 