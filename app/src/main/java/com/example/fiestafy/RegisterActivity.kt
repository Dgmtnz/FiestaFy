package com.example.fiestafy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fiestafy.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fiestafy.models.User

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                registerUser(email, password)
            }
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill all fields")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address")
            return false
        }

        if (password.length < 6) {
            showToast("Password should be at least 6 characters")
            return false
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return false
        }

        return true
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isAdmin = binding.adminCheckBox.isChecked
                    
                    val userDoc = hashMapOf(
                        "uid" to (user?.uid ?: ""),
                        "email" to email,
                        "name" to binding.nameEditText.text.toString(),
                        "isAdmin" to isAdmin,
                        "createdAt" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("users")
                        .document(user?.uid ?: "")
                        .set(userDoc)
                        .addOnSuccessListener {
                            if (isAdmin) {
                                firestore.collection("admins")
                                    .document(user?.uid ?: "")
                                    .set(hashMapOf(
                                        "isAdmin" to true,
                                        "createdAt" to System.currentTimeMillis()
                                    ))
                            }
                            startActivity(Intent(this, HomeMainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e("RegisterActivity", "Error creating user document", e)
                            Toast.makeText(this, "Error al crear usuario: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}