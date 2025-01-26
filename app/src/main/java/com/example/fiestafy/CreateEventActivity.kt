package com.example.fiestafy

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.fiestafy.databinding.ActivityCreateEventBinding
import com.example.fiestafy.models.Event
import com.example.fiestafy.models.EventLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.common.api.Status
import com.example.fiestafy.models.AttendeeStatus
import com.example.fiestafy.utils.NotificationHelper

class CreateEventActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CreateEventActivity"
    }
    
    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedDate: Long = 0
    private var selectedPlace: Place? = null
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.eventImagePreview.apply {
                    visibility = View.VISIBLE
                    Glide.with(this@CreateEventActivity)
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
        
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.maps_api_key))
        }

        setupPlacesAutocomplete()
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.dateTimeInput.setOnClickListener {
            showDateTimePicker()
        }

        binding.createEventButton.setOnClickListener {
            createEvent()
        }

        binding.selectImageButton.setOnClickListener {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        getContent.launch(intent)
    }

    private fun uploadEventImage(eventId: String, callback: (String?) -> Unit) {
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
                    callback(null)
                }
        } ?: callback(null)
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
                        binding.dateTimeInput.setText(
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(calendar.time)
                        )
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

    private fun createEvent() {
        val title = binding.titleInput.text.toString()
        val description = binding.descriptionInput.text.toString()
        val capacity = binding.capacityInput.text.toString().toIntOrNull() ?: 0
        val minimumAge = binding.minimumAgeInput.text.toString().toIntOrNull() ?: 0
        val tags = binding.tagsInput.text.toString().split(",").map { it.trim() }
        val isPrivate = binding.privateEventSwitch.isChecked
        val locationName = binding.locationNameInput.text.toString()

        // Usar la dirección del lugar seleccionado o una cadena vacía si no hay lugar seleccionado
        val address = selectedPlace?.address ?: ""

        if (title.isEmpty() || description.isEmpty() || locationName.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Si tenemos un lugar seleccionado, usar sus coordenadas directamente
        if (selectedPlace != null) {
            val location = EventLocation(
                name = locationName,
                address = selectedPlace?.address ?: "",
                latitude = selectedPlace?.latLng?.latitude ?: 0.0,
                longitude = selectedPlace?.latLng?.longitude ?: 0.0
            )

            createEventWithLocation(location, title, description, capacity, minimumAge, tags, isPrivate)
        } else {
            // Si no hay lugar seleccionado, intentar obtener coordenadas de la dirección
            getLocationFromAddress(locationName) { latitude, longitude ->
                val location = EventLocation(
                    name = locationName,
                    address = address,
                    latitude = latitude,
                    longitude = longitude
                )
                
                createEventWithLocation(location, title, description, capacity, minimumAge, tags, isPrivate)
            }
        }
    }

    private fun createEventWithLocation(
        location: EventLocation,
        title: String,
        description: String,
        capacity: Int,
        minimumAge: Int,
        tags: List<String>,
        isPrivate: Boolean
    ) {
        val price = binding.priceInput.text.toString().toDoubleOrNull() ?: 0.0
        val eventId = firestore.collection("events").document().id
        
        // Primero subir la imagen si existe
        uploadEventImage(eventId) { imageUrl ->
            // Obtener todos los usuarios
            firestore.collection("users").get()
                .addOnSuccessListener { userDocuments ->
                    val attendees = mutableMapOf<String, AttendeeStatus>()
                    
                    userDocuments.forEach { userDoc ->
                        val userId = userDoc.id
                        attendees[userId] = AttendeeStatus(
                            userId = userId,
                            hasPaid = false,
                            paymentDate = null,
                            paymentMethod = null
                        )
                    }
                    
                    val event = Event(
                        id = eventId,
                        title = title,
                        description = description,
                        date = selectedDate,
                        location = location,
                        price = price,
                        capacity = capacity,
                        minimumAge = minimumAge,
                        tags = tags,
                        isPrivate = isPrivate,
                        organizerId = auth.currentUser?.uid ?: "",
                        organizerName = "",
                        createdAt = System.currentTimeMillis(),
                        isPaid = price > 0,
                        imageUrl = imageUrl ?: "",
                        attendees = attendees
                    )

                    firestore.collection("events")
                        .document(event.id)
                        .set(event)
                        .addOnSuccessListener {
                            // Enviar notificación
                            NotificationHelper.sendNotificationToAllUsers(
                                "🎉 Nuevo Evento",
                                "Se ha creado el evento: $title"
                            )
                            Toast.makeText(this, "Evento creado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al crear el evento: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al obtener usuarios: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getLocationFromAddress(address: String, callback: (Double, Double) -> Unit) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                callback(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "No se pudo encontrar la ubicación", Toast.LENGTH_SHORT).show()
                callback(0.0, 0.0)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener coordenadas: ${e.message}", Toast.LENGTH_SHORT).show()
            callback(0.0, 0.0)
        }
    }
}