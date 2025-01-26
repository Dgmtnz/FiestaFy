package com.example.fiestafy

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fiestafy.databinding.ActivityCreateEventBinding
import com.example.fiestafy.models.Event
import com.example.fiestafy.models.EventLocation
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import com.example.fiestafy.utils.NotificationHelper

class EditEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedDate: Long = 0
    private var selectedPlace: Place? = null
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null
    private var eventId: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.eventImagePreview.apply {
                    visibility = View.VISIBLE
                    Glide.with(this@EditEventActivity)
                        .load(uri)
                        .into(this)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra("eventId")
        if (eventId == null) {
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.maps_api_key))
        }

        setupUI()
        loadEventData()
    }

    private fun setupUI() {
        binding.createEventButton.text = "Actualizar Evento"
        
        binding.dateTimeInput.setOnClickListener {
            showDateTimePicker()
        }

        binding.selectImageButton.setOnClickListener {
            openImagePicker()
        }

        binding.createEventButton.setOnClickListener {
            updateEvent()
        }

        setupPlacesAutocomplete()
    }

    private fun loadEventData() {
        eventId?.let { id ->
            firestore.collection("events").document(id)
                .get()
                .addOnSuccessListener { document ->
                    val event = document.toObject(Event::class.java)
                    event?.let { populateFields(it) }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar el evento: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun populateFields(event: Event) {
        binding.apply {
            titleInput.setText(event.title)
            descriptionInput.setText(event.description)
            locationNameInput.setText(event.location.name)
            dateTimeInput.setText(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(event.date)))
            capacityInput.setText(event.capacity.toString())
            minimumAgeInput.setText(event.minimumAge.toString())
            priceInput.setText(event.price.toString())
            priceInput.isEnabled = false
            tagsInput.setText(event.tags.joinToString(","))
            privateEventSwitch.isChecked = event.isPrivate

            currentImageUrl = event.imageUrl
            if (event.imageUrl.isNotEmpty()) {
                eventImagePreview.visibility = View.VISIBLE
                Glide.with(this@EditEventActivity)
                    .load(event.imageUrl)
                    .into(eventImagePreview)
            }
        }
        selectedDate = event.date
    }

    // Reutilizar los métodos de CreateEventActivity pero modificar updateEvent()
    private fun updateEvent() {
        val title = binding.titleInput.text.toString()
        val description = binding.descriptionInput.text.toString()
        val capacity = binding.capacityInput.text.toString().toIntOrNull() ?: 0
        val minimumAge = binding.minimumAgeInput.text.toString().toIntOrNull() ?: 0
        val tags = binding.tagsInput.text.toString().split(",").map { it.trim() }
        val isPrivate = binding.privateEventSwitch.isChecked
        val locationName = binding.locationNameInput.text.toString()
        val price = binding.priceInput.text.toString().toDoubleOrNull() ?: 0.0

        if (title.isEmpty() || description.isEmpty() || locationName.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos obligatorios", 
                Toast.LENGTH_SHORT).show()
            return
        }

        // Si hay una nueva imagen, subirla primero
        if (selectedImageUri != null) {
            uploadEventImage { imageUrl ->
                updateEventInFirestore(title, description, locationName, capacity, 
                    minimumAge, tags, isPrivate, price, imageUrl)
            }
        } else {
            // Si no hay nueva imagen, usar la URL actual
            updateEventInFirestore(title, description, locationName, capacity, 
                minimumAge, tags, isPrivate, price, currentImageUrl ?: "")
        }
    }

    private fun uploadEventImage(callback: (String) -> Unit) {
        selectedImageUri?.let { uri ->
            val imageFileName = "event_images/$eventId/${UUID.randomUUID()}"
            val imageRef = storage.reference.child(imageFileName)
            
            imageRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    callback(downloadUri.toString())
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateEventInFirestore(
        title: String,
        description: String,
        locationName: String,
        capacity: Int,
        minimumAge: Int,
        tags: List<String>,
        isPrivate: Boolean,
        price: Double,
        imageUrl: String
    ) {
        eventId?.let { id ->
            val updates = hashMapOf(
                "title" to title,
                "description" to description,
                "location.name" to locationName,
                "date" to selectedDate,
                "capacity" to capacity,
                "minimumAge" to minimumAge,
                "tags" to tags,
                "isPrivate" to isPrivate,
                "imageUrl" to imageUrl
            )

            firestore.collection("events")
                .document(id)
                .update(updates)
                .addOnSuccessListener {
                    NotificationHelper.sendNotificationToAllUsers(
                        "📝 Evento Actualizado",
                        "Se han realizado cambios en el evento: $title"
                    )
                    Toast.makeText(this, "Evento actualizado con éxito", 
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar el evento: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
        }
    }

    // Reutilizar los métodos de CreateEventActivity para el resto de funcionalidades
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        getContent.launch(intent)
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute)
                        selectedDate = calendar.timeInMillis
                        
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        binding.dateTimeInput.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupPlacesAutocomplete() {
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        ))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                selectedPlace = place
                binding.locationNameInput.setText(place.name)
            }

            override fun onError(status: Status) {
                Log.e(TAG, "An error occurred: $status")
            }
        })
    }

    companion object {
        private const val TAG = "EditEventActivity"
    }
} 