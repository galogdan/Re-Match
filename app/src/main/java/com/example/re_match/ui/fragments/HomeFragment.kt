package com.example.re_match.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.re_match.R
import com.example.re_match.databinding.FragmentHomeBinding
import com.example.re_match.ui.viewmodels.HomeViewModel
import com.example.re_match.ui.viewmodels.LogoutStatus
import com.example.re_match.ui.viewmodels.PrivacySettingsState
import com.example.re_match.ui.viewmodels.ProfileState
import com.example.re_match.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private var privacyDialog: AlertDialog? = null
    private var isDialogShowing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePrivacySettings()
        setupClickListeners()
        setupObservers()
        viewModel.getFriendRequestCount()
        observeLogoutStatus()
    }


    private fun setupClickListeners() {
        binding.cardFriendRequests.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_friendRequestFragment)
        }

        binding.cardMyFriends.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_myFriendsFragment)
        }

        binding.fabPrivacySetup.setOnClickListener {
            showPrivacySetupDialog()
        }

        binding.fabLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun createPrivacySetupDialog(): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.privacy_setup, null)
        val showFullNameSwitch = dialogView.findViewById<SwitchCompat>(R.id.switchShowFullName)
        val showGenderSwitch = dialogView.findViewById<SwitchCompat>(R.id.switchShowGender)
        val showAgeSwitch = dialogView.findViewById<SwitchCompat>(R.id.switchShowAge)


        return AlertDialog.Builder(requireContext())
            .setTitle("Privacy Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedSettings = mapOf(
                    "showFullName" to showFullNameSwitch.isChecked,
                    "showGender" to showGenderSwitch.isChecked,
                    "showAge" to showAgeSwitch.isChecked
                )
                viewModel.updatePrivacySettings(updatedSettings)
            }
            .setNegativeButton("Cancel", null)
            .setOnDismissListener {
                isDialogShowing = false
            }
            .create()
    }

    private fun setupObservers() {
        viewModel.friendRequestCount.observe(viewLifecycleOwner) { count ->
            binding.tvFriendRequests.text = getString(R.string.friend_requests_button_text, count)
        }

        viewModel.logoutStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is LogoutStatus.Success -> {
                    findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                }
                is LogoutStatus.Error -> {
                    Toast.makeText(requireContext(), "Logout failed: ${status.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPrivacySetupDialog() {
        if (privacyDialog == null) {
            privacyDialog = createPrivacySetupDialog()
        }

        viewModel.getPrivacySettings()
        isDialogShowing = true
        privacyDialog?.show()
    }

    private fun observePrivacySettings() {
        viewModel.privacySettingsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PrivacySettingsState.Loading -> {
                    showLoading(true)
                }
                is PrivacySettingsState.Success -> {
                    showLoading(false)
                    privacyDialog?.let { dialog ->
                        val showFullNameSwitch = dialog.findViewById<SwitchCompat>(R.id.switchShowFullName)
                        val showGenderSwitch = dialog.findViewById<SwitchCompat>(R.id.switchShowGender)

                        showFullNameSwitch?.isChecked = state.privacySettings["showFullName"] ?: false
                        showGenderSwitch?.isChecked = state.privacySettings["showGender"] ?: false
                    }
                }
                is PrivacySettingsState.Error -> {
                    showLoading(false)
                    //Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_SHORT).show()
                    isDialogShowing = false
                    privacyDialog?.dismiss()
                }
                is PrivacySettingsState.UpdateSuccess -> {
                    showLoading(false)
                    //Toast.makeText(context, "Privacy settings updated successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeLogoutStatus() {
        viewModel.logoutStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is LogoutStatus.Success -> {
                    if (findNavController().currentDestination?.id != R.id.loginFragment) {
                        findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                    }
                }
                is LogoutStatus.Error -> {
                    Toast.makeText(requireContext(), "Logout failed: ${status.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        privacyDialog?.dismiss()
        privacyDialog = null
        _binding = null
    }
}

