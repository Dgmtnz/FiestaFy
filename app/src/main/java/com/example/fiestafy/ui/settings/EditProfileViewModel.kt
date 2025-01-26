package com.example.fiestafy.ui.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fiestafy.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EditProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    _userData.value = user ?: User()
                } else {
                    _userData.value = User()
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditProfileViewModel", "Error loading user data", e)
            }
    }

    fun updateProfile(name: String) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: return
        
        val updates = hashMapOf<String, Any>(
            "name" to name
        )

        firestore.collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener {
                _updateSuccess.value = true
                _isLoading.value = false
                // Actualizar los datos locales
                _userData.value = _userData.value?.copy(name = name)
            }
            .addOnFailureListener {
                _updateSuccess.value = false
                _isLoading.value = false
            }
    }

    fun uploadProfileImage(imageUri: Uri) {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: return
        val imageFileName = "profile_images/$userId/${UUID.randomUUID()}"
        
        val imageRef = storage.reference.child(imageFileName)
        
        imageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                // Actualizar la URL de la imagen en Firestore
                firestore.collection("users")
                    .document(userId)
                    .update("profileImageUrl", downloadUri.toString())
                    .addOnSuccessListener {
                        _userData.value = _userData.value?.copy(profileImageUrl = downloadUri.toString())
                        _updateSuccess.value = true
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener {
                _updateSuccess.value = false
                _isLoading.value = false
            }
    }
} 