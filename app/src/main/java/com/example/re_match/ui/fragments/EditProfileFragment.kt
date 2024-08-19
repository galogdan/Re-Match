package com.example.re_match.ui.fragments

import android.R as rAndroid
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.re_match.R
import com.example.re_match.databinding.FragmentEditProfileBinding
import com.example.re_match.domain.models.GamingPlatform
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.ui.adapters.PreferredGamesAdapter
import com.example.re_match.ui.viewmodels.ProfileState
import com.example.re_match.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var preferredGamesAdapter: PreferredGamesAdapter

    private lateinit var spinnerGamingHours: Spinner
    private lateinit var spinnerPlatform: Spinner

    private var isFirstLogin: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFirstLogin = arguments?.getBoolean("isFirstLogin", false) ?: false

        setupViews()
        setupSpinners()
        setupPreferredGames()
        setupObservers()
        viewModel.getUserProfile()
    }

    private fun setupViews() {
        if (isFirstLogin) {
            binding.tvEditProfileTitle.text = "Complete Your Profile"
            binding.btnSaveProfile.text = "Get Started"
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
        binding.btnCancelChanges.setOnClickListener {
            cancelChanges()
        }
    }

    private fun setupSpinners() {
        val regions = resources.getStringArray(R.array.gaming_regions)
        val regionAdapter = ArrayAdapter(requireContext(), rAndroid.layout.simple_spinner_item, regions)
        binding.spinnerRegion.adapter = regionAdapter

        spinnerGamingHours = binding.spinnerGamingHours
        val gamingHours = resources.getStringArray(R.array.gaming_hours)
        val adapterHours = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gamingHours)
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGamingHours.adapter = adapterHours

        setupPlatformSpinner()
    }

    private fun setupPlatformSpinner() {
        spinnerPlatform = binding.spinnerPlatform
        val platforms = GamingPlatform.values().filter { it != GamingPlatform.ALL }.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platforms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlatform.adapter = adapter
    }

    private fun setupPreferredGames() {
        preferredGamesAdapter = PreferredGamesAdapter { game ->
            viewModel.removePreferredGame(game)
        }
        binding.rvPreferredGames.adapter = preferredGamesAdapter
        binding.rvPreferredGames.layoutManager = LinearLayoutManager(requireContext())

        binding.tilAddGame.setEndIconOnClickListener {
            val game = binding.etAddGame.text.toString().trim()
            if (game.isNotEmpty()) {
                viewModel.addPreferredGame(game)
                binding.etAddGame.text?.clear()

            }
        }
    }

    private fun setupObservers() {
        viewModel.profileState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ProfileState.Loading -> showLoading(true)
                is ProfileState.Success -> {
                    showLoading(false)
                    populateFields(state.userProfile)
                }
                is ProfileState.UpdateSuccess -> {
                    showLoading(false)
                    showMessage("Profile updated successfully")
                    if (isFirstLogin) {
                        navigateToMainScreen()
                    } else {
                        findNavController().navigateUp()
                    }
                }
                is ProfileState.Error -> {
                    showLoading(false)
                    showError(state.message)
                    if (isFirstLogin) {
                        viewModel.getUserProfile()
                    }
                }
                is ProfileState.PhotoUploadSuccess -> {
                    showLoading(false)
                    showMessage("Profile photo updated successfully")
                }
            }
        })

        viewModel.preferredGames.observe(viewLifecycleOwner, Observer { games ->
            preferredGamesAdapter.submitList(games)
        })
    }

    private fun populateFields(userProfile: UserProfile) {
        val regionIndex = resources.getStringArray(R.array.gaming_regions).indexOf(userProfile.region)
        if (regionIndex != -1) {
            binding.spinnerRegion.setSelection(regionIndex)
        }

        val gamingHoursArray = resources.getStringArray(R.array.gaming_hours)
        val gamingHoursIndex = gamingHoursArray.indexOf(userProfile.gamingHours)
        if (gamingHoursIndex != -1) {
            spinnerGamingHours.setSelection(gamingHoursIndex)
        }

        binding.etShortDescription.setText(userProfile.shortDescription)

        val platformIndex = GamingPlatform.values().indexOf(userProfile.preferredPlatform)
        if (platformIndex != -1) {
            spinnerPlatform.setSelection(platformIndex - 1) // Subtract 1 to account for ALL not being in the spinner
        }
    }

    private fun saveProfile() {
        val region = binding.spinnerRegion.selectedItem.toString()
        val gamingHours = spinnerGamingHours.selectedItem.toString()
        val shortDescription = binding.etShortDescription.text.toString().trim()
        val selectedPlatform = GamingPlatform.valueOf(spinnerPlatform.selectedItem.toString())

        viewModel.updateProfile(
            region = region,
            gamingHours = gamingHours,
            shortDescription = shortDescription,
            preferredPlatform = selectedPlatform,
            isFirstLogin = false
        )
    }

    private fun navigateToMainScreen() {
        findNavController().navigate(R.id.action_editProfileFragment_to_mainFragment)
    }

    private fun cancelChanges() {
        if (isFirstLogin) {
            // first login, update the profile with default values
            viewModel.updateProfile(
                region = "",
                gamingHours = "",
                shortDescription = "",
                preferredPlatform = GamingPlatform.OTHER,
                isFirstLogin = false
            )
            findNavController().navigate(R.id.action_editProfileFragment_to_mainFragment)
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}