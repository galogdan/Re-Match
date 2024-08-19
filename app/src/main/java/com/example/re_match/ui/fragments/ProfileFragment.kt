package com.example.re_match.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.re_match.R
import com.example.re_match.databinding.FragmentProfileBinding
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.ui.viewmodels.ProfileState
import com.example.re_match.ui.viewmodels.ProfileViewModel
import com.example.re_match.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.util.Log

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            tempPhotoUri?.let { viewModel.uploadProfilePhoto(it) }
        }
    }

    private var tempPhotoUri: Uri? = null

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        viewModel.getUserProfile()
        binding.btnChangePhoto.setOnClickListener {
            showPhotoSelectionDialog()
        }
    }

    private fun showPhotoSelectionDialog() {
        val options = arrayOf("Take photo", "Choose from gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Profile Picture")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> checkGalleryPermission()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (PermissionUtils.checkPermission(requireContext(), Manifest.permission.CAMERA)) {
            takePhotoFromCamera()
        } else {
            PermissionUtils.requestPermission(this, Manifest.permission.CAMERA, PermissionUtils.CAMERA_PERMISSION_CODE)
        }
    }

    private fun checkGalleryPermission() {
        val permission = PermissionUtils.getRequiredPermissionForGallery()
        if (PermissionUtils.checkPermission(requireContext(), permission)) {
            choosePhotoFromGallery()
        } else {
            PermissionUtils.requestPermission(this, permission, PermissionUtils.GALLERY_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionUtils.CAMERA_PERMISSION_CODE -> {
                PermissionUtils.handlePermissionResult(
                    requestCode, permissions, grantResults,
                    onGranted = { takePhotoFromCamera() },
                    onDenied = { showPermissionDeniedMessage("Camera") }
                )
            }
            PermissionUtils.GALLERY_PERMISSION_CODE -> {
                PermissionUtils.handlePermissionResult(
                    requestCode, permissions, grantResults,
                    onGranted = { choosePhotoFromGallery() },
                    onDenied = { showPermissionDeniedMessage("Gallery") }
                )
            }
        }
    }

    private fun showPermissionDeniedMessage(feature: String) {
        Toast.makeText(requireContext(), "$feature permission denied. Some features may not work.", Toast.LENGTH_SHORT).show()
    }

    private fun takePhotoFromCamera() {
        tempPhotoUri = createTempImageUri()
        takePhoto.launch(tempPhotoUri)
    }

    private fun choosePhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedImage(uri)
            }
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                // Create a temporary file
                val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
                tempFile.outputStream().use { output ->
                    stream.copyTo(output)
                }

                val tempUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", tempFile)
                viewModel.uploadProfilePhoto(tempUri)
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error handling selected image", e)
            Toast.makeText(requireContext(), "Error processing the selected image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createTempImageUri(): Uri {
        val tempFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir)
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", tempFile)
    }

    private fun setupObservers() {
        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Loading -> showLoading(true)
                is ProfileState.Success -> {
                    showLoading(false)
                    displayUserProfile(state.userProfile)
                }
                is ProfileState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is ProfileState.PhotoUploadSuccess -> {
                    showLoading(false)
                    loadProfilePhoto(state.photoUrl)
                    //showMessage("Profile photo updated successfully")
                }

                else -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun loadProfilePhoto(photoUrl: String?) {
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.default_profile_photo)
            .error(R.drawable.default_profile_photo)
            .into(binding.ivProfilePhoto)
    }

    private fun updateProfilePhoto(photoUrl: String) {
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.default_profile_photo)
            .error(R.drawable.default_profile_photo)
            .into(binding.ivProfilePhoto)
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            parentFragmentManager.setFragmentResult("NAVIGATE_TO_EDIT_PROFILE", Bundle())
        }

    }
    fun refreshProfile() {
        viewModel.getUserProfile()
    }

    private fun navigateToEditProfile() {
        findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
    }

    private fun displayUserProfile(userProfile: UserProfile) {
        binding.tvFullName.text = userProfile.fullName
        binding.tvNickname.text = userProfile.nickname
        binding.tvEmail.text = userProfile.email
        binding.tvGender.text = userProfile.gender.toString()
        binding.tvRegion.text = userProfile.region
        binding.tvGamingHours.text = userProfile.gamingHours
        binding.tvPreferredPlatform.text = userProfile.preferredPlatform.toString()
        binding.tvPreferredGames.text = userProfile.preferredGames.joinToString(", ")
        binding.tvShortDescription.text = userProfile.shortDescription
        binding.tvAge.text = userProfile.age.toString()
        // If the description is empty.
        binding.tvShortDescription.visibility = if (userProfile.shortDescription.isNotEmpty()) View.VISIBLE else View.GONE

        loadProfilePhoto(userProfile.photoUrl)
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
