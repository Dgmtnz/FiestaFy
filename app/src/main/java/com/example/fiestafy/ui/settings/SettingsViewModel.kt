package com.example.fiestafy.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fiestafy.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                document.toObject(User::class.java)?.let { user ->
                    _currentUser.value = user
                }
            }
    }
} 