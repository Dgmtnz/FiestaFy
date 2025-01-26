package com.example.fiestafy.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fiestafy.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import com.example.fiestafy.models.User
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    init {
        loadEvents()
        checkAdminStatus()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                firestore.collection("events")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("HomeViewModel", "Error loading events", e)
                            return@addSnapshotListener
                        }

                        val eventsList = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Event::class.java)
                        } ?: emptyList()

                        _events.value = eventsList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading events", e)
                _isLoading.value = false
            }
        }
    }

    private fun checkAdminStatus() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    try {
                        // Primero intentamos obtener el documento de usuario
                        val userDoc = firestore.collection("users")
                            .document(userId)
                            .get()
                            .await()

                        if (userDoc.exists() && userDoc.getBoolean("isAdmin") == true) {
                            _isAdmin.value = true
                            return@launch
                        }

                        // Si no es admin en users, verificamos en la colección admins
                        val adminDoc = firestore.collection("admins")
                            .document(userId)
                            .get()
                            .await()

                        _isAdmin.value = adminDoc.exists()
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error checking admin status", e)
                        // En caso de error de conexión, intentamos usar datos en caché
                        firestore.collection("users")
                            .document(userId)
                            .get(com.google.firebase.firestore.Source.CACHE)
                            .addOnSuccessListener { userDoc ->
                                _isAdmin.value = userDoc?.getBoolean("isAdmin") ?: false
                            }
                    }
                } else {
                    _isAdmin.value = false
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error checking admin status", e)
                _isAdmin.value = false
            }
        }
    }
}