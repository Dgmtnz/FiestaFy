package com.example.fiestafy

import com.example.fiestafy.viewmodels.EventDetailViewModel
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.fiestafy.adapters.CommentAdapter
import com.example.fiestafy.databinding.ActivityEventDetailBinding
import com.example.fiestafy.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.fiestafy.models.Comment
import com.example.fiestafy.utils.NotificationHelper

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var eventId: String? = null
    private var isAdmin = false
    private lateinit var viewModel: EventDetailViewModel
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        eventId = intent.getStringExtra("eventId")
        eventId?.let { 
            loadEventDetails(it)
            checkAdminStatus()
        }

        setupViewModel()
        setupRecyclerView()
        setupCommentFab()
    }

    private fun loadEventDetails(eventId: String) {
        firestore.collection("events").document(eventId)
            .get()
            .addOnSuccessListener { document ->
                val event = document.toObject(Event::class.java)
                event?.let { setupEventDetails(it) }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar el evento: ${e.message}", 
                    Toast.LENGTH_LONG).show()
            }
    }

    private fun checkAdminStatus() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                isAdmin = document.getBoolean("isAdmin") == true
                invalidateOptionsMenu() // Esto forzará que se recree el menú
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Solo inflar el menú si el usuario es admin
        if (isAdmin) {
            menuInflater.inflate(R.menu.menu_event_detail, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            R.id.action_edit -> {
                val intent = Intent(this, EditEventActivity::class.java).apply {
                    putExtra("eventId", eventId)
                }
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar evento")
            .setMessage("¿Estás seguro de que quieres eliminar este evento? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, _ ->
                deleteEvent()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteEvent() {
        eventId?.let { id ->
            // Primero obtener el título del evento antes de eliminarlo
            firestore.collection("events")
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    val eventTitle = document.getString("title") ?: "Evento"
                    
                    // Ahora procedemos a eliminar
                    firestore.collection("events")
                        .document(id)
                        .delete()
                        .addOnSuccessListener {
                            // Enviar notificación
                            NotificationHelper.sendNotificationToAllUsers(
                                "🚫 Evento Cancelado",
                                "Se ha cancelado el evento: $eventTitle"
                            )
                            Toast.makeText(this, "Evento eliminado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al eliminar el evento: ${e.message}", 
                                Toast.LENGTH_LONG).show()
                        }
                }
        }
    }

    private fun setupEventDetails(event: Event) {
        binding.apply {
            collapsingToolbar.title = event.title
            eventDescription.text = event.description
            eventLocation.text = event.location.name
            eventDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(event.date))
            eventPrice.text = if (event.price > 0) "€${event.price}" else "Gratis"

            Glide.with(this@EventDetailActivity)
                .load(event.imageUrl)
                .placeholder(R.drawable.event_placeholder)
                .into(eventImage)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[EventDetailViewModel::class.java]
        eventId?.let { viewModel.loadEvent(it) }
        
        viewModel.comments.observe(this) { comments: List<Comment> ->
            commentAdapter.submitList(comments)
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@EventDetailActivity)
            adapter = commentAdapter
        }
    }

    private fun setupCommentFab() {
        binding.addCommentFab.setOnClickListener {
            showAddCommentDialog()
        }
    }

    private fun showAddCommentDialog() {
        val editText = EditText(this).apply {
            hint = "Escribe tu comentario"
            setPadding(32, 16, 32, 16)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Nuevo comentario")
            .setView(editText)
            .setPositiveButton("Publicar") { _, _ ->
                val commentText = editText.text.toString()
                if (commentText.isNotBlank()) {
                    eventId?.let { viewModel.addComment(it, commentText) }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
} 