package com.example.fiestafy.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fiestafy.R
import com.example.fiestafy.databinding.FragmentAnnouncementsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.switchmaterial.SwitchMaterial

class AnnouncementsFragment : Fragment() {
    private var _binding: FragmentAnnouncementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AnnouncementsViewModel
    private lateinit var announcementsAdapter: AnnouncementsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupViewModel()
        setupObservers()
        setupFabClickListener()
    }

    private fun setupRecyclerView() {
        announcementsAdapter = AnnouncementsAdapter()
        binding.announcementsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = announcementsAdapter
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AnnouncementsViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.announcements.observe(viewLifecycleOwner) { announcements ->
            announcementsAdapter.submitList(announcements)
        }

        viewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            binding.fabAddAnnouncement.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }
    }

    private fun setupFabClickListener() {
        binding.fabAddAnnouncement.setOnClickListener {
            showCreateAnnouncementDialog()
        }
    }

    private fun showCreateAnnouncementDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_announcement, null)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.titleInput)
        val contentInput = dialogView.findViewById<TextInputEditText>(R.id.contentInput)
        val importantSwitch = dialogView.findViewById<SwitchMaterial>(R.id.importantSwitch)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Crear anuncio")
            .setView(dialogView)
            .setPositiveButton("Crear") { dialog, _ ->
                val title = titleInput.text.toString()
                val content = contentInput.text.toString()
                val important = importantSwitch.isChecked

                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.createAnnouncement(title, content, important)
                    Toast.makeText(context, "Anuncio creado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 