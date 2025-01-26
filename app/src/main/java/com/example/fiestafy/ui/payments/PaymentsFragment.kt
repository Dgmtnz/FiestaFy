package com.example.fiestafy.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fiestafy.adapters.PendingPaymentsAdapter
import com.example.fiestafy.databinding.FragmentPaymentsBinding

class PaymentsFragment : Fragment() {
    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PaymentsViewModel
    private lateinit var adapter: PendingPaymentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[PaymentsViewModel::class.java]
        viewModel.loadUserEvents()
    }

    private fun setupRecyclerView() {
        adapter = PendingPaymentsAdapter { event ->
            viewModel.processPayment(event.id)
            Toast.makeText(context, "Pago procesado con éxito", Toast.LENGTH_SHORT).show()
        }

        binding.pendingPaymentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PaymentsFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.userEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
            binding.emptyStateText.visibility = 
                if (events.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 