package com.example.proyectoaplicacionfiestas.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoaplicacionfiestas.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NotificationsViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        viewModel.loadUserEvents()

        return binding.root
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            onNotificationToggled = { event, enabled ->
                viewModel.toggleNotifications(event.id, enabled)
            },
            onItemClicked = { event ->
                findNavController().navigate(
                    NotificationsFragmentDirections.actionNotificationsToEventDetails(event.id)
                )
            }
        )

        binding.notificationsRecyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            notificationAdapter.submitList(events)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}