package com.example.re_match.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.re_match.R
import com.example.re_match.databinding.FragmentRegisterBinding
import com.example.re_match.ui.viewmodels.AuthViewModel
import com.example.re_match.data.repositories.AuthRepository
import com.example.re_match.domain.models.Gender
import com.example.re_match.ui.viewmodels.AuthState
import com.example.re_match.utils.DateUtils
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.Calendar


@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnSelectBirthDate: Button
    private var selectedBirthDateTimestamp: Long? = null

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        setupBirthDatePicker()
        setupGenderChips()
    }

    private fun setupBirthDatePicker() {
        binding.btnSelectBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                selectedBirthDateTimestamp = selectedDate.timeInMillis
                updateBirthDateButton()
            }, year, month, day).show()
        }
    }

    private fun updateBirthDateButton() {
        selectedBirthDateTimestamp?.let { timestamp ->
            val formattedDate = DateUtils.formatRelativeTimestamp(timestamp)
            binding.btnSelectBirthDate.text = "Birth Date: $formattedDate"
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.RegisterSuccess -> {
                    showLoading(false)
                    showMessage("Registration successful. Please verify your email.")
                    navigateToLoginScreen()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLoginPrompt.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupGenderChips() {
        binding.chipMale.setOnCheckedChangeListener { _, isChecked ->
            updateChipColors(binding.chipMale, isChecked)
        }

        binding.chipFemale.setOnCheckedChangeListener { _, isChecked ->
            updateChipColors(binding.chipFemale, isChecked)
        }

        binding.chipNotSpecified.setOnCheckedChangeListener { _, isChecked ->
            updateChipColors(binding.chipNotSpecified, isChecked)
        }
    }

    private fun updateChipColors(chip: Chip, isChecked: Boolean) {
        if (isChecked) {
            chip.setChipBackgroundColorResource(R.color.chip_background_color_selected)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text_color_selected))
        } else {
            chip.setChipBackgroundColorResource(R.color.chip_background_color_unselected)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_text_color_unselected))
        }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val nickname = binding.etNickname.text.toString().trim()
        val gender = when (binding.cgGender.checkedChipId) {
            R.id.chipMale -> Gender.MALE
            R.id.chipFemale -> Gender.FEMALE
            R.id.chipNotSpecified -> Gender.NOT_SPECIFIED
            else -> null
        }

        if (validateInput(email, password, confirmPassword, fullName, nickname, gender, selectedBirthDateTimestamp)) {
            viewModel.registerUser(email, password, fullName, nickname, gender!!, selectedBirthDateTimestamp)
        }
    }

    private fun validateInput(email: String, password: String, confirmPassword: String, fullName: String, nickname: String, gender: Gender?, selectedBirthDateTimestamp: Long?): Boolean {
        if (email.isEmpty() || !isValidEmail(email)) {
            showError("Please enter a valid email address")
            return false
        }
        if (password.isEmpty() || password.length < 8) {
            showError("Password must be at least 8 characters long")
            return false
        }
        if (password != confirmPassword) {
            showError("Passwords do not match")
            return false
        }
        if (fullName.isEmpty()) {
            showError("Full name cannot be empty")
            return false
        }
        if (nickname.isEmpty()) {
            showError("Nickname cannot be empty")
            return false
        }
        if (gender == null) {
            showError("Please select a gender")
            return false
        }
        if (selectedBirthDateTimestamp == null) {
            showError("Please select your birth date")
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

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToLoginScreen() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}