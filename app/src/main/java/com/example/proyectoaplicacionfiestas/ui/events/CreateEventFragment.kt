package com.example.proyectoaplicacionfiestas.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.proyectoaplicacionfiestas.R
import com.example.proyectoaplicacionfiestas.databinding.FragmentCreateEventBinding
import com.example.proyectoaplicacionfiestas.ui.location.LocationPickerActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateEventFragment : Fragment() {
    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CreateEventViewModel
    private val calendar = Calendar.getInstance()
    private var selectedLocation: GeoPoint? = null

    private val locationPickerResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val latitude = data.getDoubleExtra("latitude", 0.0)
                val longitude = data.getDoubleExtra("longitude", 0.0)
                selectedLocation = GeoPoint(latitude, longitude)
                binding.locationText.text = getString(R.string.location_selected)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[CreateEventViewModel::class.java]

        setupDatePicker()
        setupLocationPicker()
        setupObservers()
        setupCreateButton()

        return binding.root
    }

    private fun setupDatePicker() {
        binding.eventDateInput.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.eventDateInput.setText(dateFormat.format(calendar.time))
    }

    private fun setupLocationPicker() {
        binding.selectLocationButton.setOnClickListener {
            val intent = Intent(requireContext(), LocationPickerActivity::class.java)
            locationPickerResult.launch(intent)
        }
    }

    private fun setupCreateButton() {
        binding.createEventButton.setOnClickListener {
            createEvent()
        }
    }

    private fun createEvent() {
        val title = binding.eventTitleInput.text.toString()
        val description = binding.eventDescriptionInput.text.toString()
        val price = binding.eventPriceInput.text.toString().toDoubleOrNull() ?: 0.0

        viewModel.createEvent(
            title = title,
            description = description,
            date = calendar.timeInMillis,
            price = price,
            location = selectedLocation
        )
    }

    private fun setupObservers() {
        viewModel.eventCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigateUp()
            } else {
                Toast.makeText(context, "Failed to create event", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 