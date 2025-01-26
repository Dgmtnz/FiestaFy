package com.example.fiestafy.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.fiestafy.R
import com.example.fiestafy.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: EditProfileViewModel

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadProfileImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
        setupObservers()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
    }

    private fun setupUI() {
        binding.changePhotoButton.setOnClickListener {
            openImagePicker()
        }

        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            if (name.isNotBlank()) {
                viewModel.updateProfile(name)
            } else {
                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.nameEditText.setText(user.name)
            binding.emailEditText.setText(user.email)
            
            user.profileImageUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.default_profile)
                    .into(binding.profileImageView)
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        getContent.launch(intent)
    }

    private fun uploadProfileImage(uri: Uri) {
        viewModel.uploadProfileImage(uri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}