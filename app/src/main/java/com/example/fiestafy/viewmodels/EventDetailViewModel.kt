package com.example.fiestafy.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.fiestafy.models.Event
import com.example.fiestafy.models.Comment

class EventDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments
    
    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    fun loadEvent(eventId: String) {
        firestore.collection("events").document(eventId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(Event::class.java)?.let { event ->
                    _event.value = event
                    loadComments(eventId)
                }
            }
            .addOnFailureListener { e ->
                Log.e("EventDetailViewModel", "Error loading event", e)
            }
    }

    private fun loadComments(eventId: String) {
        firestore.collection("events")
            .document(eventId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("EventDetailViewModel", "Error loading comments", e)
                    return@addSnapshotListener
                }
                
                val commentsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)
                } ?: emptyList()
                
                _comments.value = commentsList
            }
    }

    fun addComment(eventId: String, text: String) {
        val currentUser = auth.currentUser ?: return
        
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Usuario Anónimo"
                
                val comment = Comment(
                    id = firestore.collection("events").document(eventId)
                        .collection("comments").document().id,
                    eventId = eventId,
                    userId = currentUser.uid,
                    userName = userName,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("events")
                    .document(eventId)
                    .collection("comments")
                    .document(comment.id)
                    .set(comment)
                    .addOnFailureListener { e ->
                        Log.e("EventDetailViewModel", "Error adding comment", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("EventDetailViewModel", "Error getting user name", e)
            }
    }
} 