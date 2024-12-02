package com.example.proyectoaplicacionfiestas.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.proyectoaplicacionfiestas.R
import com.example.proyectoaplicacionfiestas.databinding.FragmentEventDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailsFragment : Fragment() {
    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EventDetailsViewModel
    private val args: EventDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[EventDetailsViewModel::class.java]

        setupObservers()
        setupClickListeners()
        viewModel.loadEvent(args.eventId)

        return binding.root
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            updateUI(event)
        }

        viewModel.isJoined.observe(viewLifecycleOwner) { isJoined ->
            binding.joinEventButton.text = getString(
                if (isJoined) R.string.leave_event else R.string.join_event
            )
        }

        viewModel.hasPaid.observe(viewLifecycleOwner) { hasPaid ->
            binding.paymentButton.isEnabled = !hasPaid
            binding.paymentButton.text = getString(
                if (hasPaid) R.string.payment_completed else R.string.make_payment
            )
        }
    }

    private fun setupClickListeners() {
        binding.joinEventButton.setOnClickListener {
            viewModel.joinEvent()
        }

        binding.paymentButton.setOnClickListener {
            viewModel.makePayment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 