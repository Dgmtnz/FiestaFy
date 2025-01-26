package com.example.fiestafy.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fiestafy.databinding.FragmentCalendarBinding
import com.example.fiestafy.EventDetailActivity
import com.example.fiestafy.models.Event
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CalendarViewModel
    private lateinit var eventsAdapter: CalendarEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupCalendarView()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventsAdapter.submitList(events)
        }
    }

    private fun setupRecyclerView() {
        eventsAdapter = CalendarEventAdapter { event ->
            showEventDetail(event)
        }
        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            viewModel.loadEventsForDate(calendar.timeInMillis)
        }
    }

    private fun showEventDetail(event: Event) {
        val intent = Intent(requireContext(), EventDetailActivity::class.java)
        intent.putExtra("eventId", event.id)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 