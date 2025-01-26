package com.example.fiestafy.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fiestafy.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.fiestafy.CreateEventActivity
import com.example.fiestafy.adapters.EventAdapter
import com.example.fiestafy.EventDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var viewModel: HomeViewModel
    private lateinit var eventAdapter: EventAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        
        binding.fabAddEvent.setOnClickListener {
            startActivity(Intent(requireContext(), CreateEventActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter()
        binding.eventsRecyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        eventAdapter.setOnItemClickListener { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("eventId", event.id)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            binding.fabAddEvent.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}