package com.example.fiestafy.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.fiestafy.MainActivity
import com.example.fiestafy.R
import com.example.fiestafy.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserProfile()
        setupClickListeners()
    }

    private fun setupUserProfile() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.userNameText.text = user.name
            binding.userEmailText.text = user.email
            
            // Cargar imagen de perfil si existe
            user.profileImageUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.default_profile)
                    .into(binding.profileImage)
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            userProfileCard.setOnClickListener {
                findNavController().navigate(R.id.navigation_edit_profile)
            }

            announcementsBoardOption.setOnClickListener {
                findNavController().navigate(R.id.navigation_announcements)
            }

            bankAccountsOption.setOnClickListener {
                // TODO: Implementar gestión de cuentas bancarias
            }

            organizationChartOption.setOnClickListener {
                // TODO: Implementar visualización del organigrama
            }

            receiptsOption.setOnClickListener {
                // TODO: Implementar visualización de recibos
            }

            aboutOption.setOnClickListener {
                findNavController().navigate(R.id.navigation_about)
            }

            logoutOption.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 