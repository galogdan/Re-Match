package com.example.re_match.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.re_match.R
import com.example.re_match.databinding.FragmentLoginBinding
import com.example.re_match.ui.viewmodels.AuthViewModel
import com.example.re_match.ui.viewmodels.AuthState
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.LoginSuccess -> {
                    showLoading(false)
                    Log.d("LoginFragment", "Login successful, navigating to main screen")
                    navigateToMainScreen()
                }
                is AuthState.FirstTimeLogin -> {
                    showLoading(false)
                    Log.d("LoginFragment", "First time login, showing welcome dialog")
                    showWelcomeDialog()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    Log.e("LoginFragment", "Login error: ${state.message}")
                    showError(state.message)
                }

                is AuthState.PasswordResetEmailSent -> showLoading(false)
                is AuthState.RegisterSuccess -> showLoading(false)
                is AuthState.LoggedOut -> showLoading(false)
            }
        }
    }


    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.loginUser(email, password)
            }
        }

        binding.tvRegisterPrompt.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showError("Email cannot be empty")
            return false
        }
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address")
            return false
        }
        if (password.isEmpty()) {
            showError("Password cannot be empty")
            return false
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingView.startLoading()
        } else {
            binding.loadingView.stopLoading()
        }
    }

    private fun showWelcomeDialog() {
        navigateToEditProfile()
        val dialogView = layoutInflater.inflate(R.layout.dialog_welcome, null)
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)

        val btnGetStarted = dialogView.findViewById<MaterialButton>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            dialog.dismiss()

        }

        dialog.show()

        // Add animations (same as before)
        val welcomeIcon = dialogView.findViewById<ImageView>(R.id.ivWelcomeIcon)
        val welcomeTitle = dialogView.findViewById<TextView>(R.id.tvWelcomeTitle)
        val welcomeMessage = dialogView.findViewById<TextView>(R.id.tvWelcomeMessage)

        welcomeIcon.alpha = 0f
        welcomeTitle.alpha = 0f
        welcomeMessage.alpha = 0f
        btnGetStarted.alpha = 0f

        welcomeIcon.animate().alpha(1f).setDuration(500).start()
        welcomeTitle.animate().alpha(1f).setStartDelay(300).setDuration(500).start()
        welcomeMessage.animate().alpha(1f).setStartDelay(600).setDuration(500).start()
        btnGetStarted.animate().alpha(1f).setStartDelay(900).setDuration(500).start()
    }

    private fun navigateToEditProfile() {
        val bundle = Bundle().apply {
            putBoolean("isFirstLogin", true)
        }
        findNavController().navigate(R.id.action_loginFragment_to_editProfileFragment, bundle)
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMainScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}