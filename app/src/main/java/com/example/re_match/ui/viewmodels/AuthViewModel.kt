package com.example.re_match.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_match.domain.models.Gender
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.usecases.CheckFirstLoginUseCase
import com.example.re_match.domain.usecases.CreateUserProfileUseCase
import com.example.re_match.domain.usecases.GetCurrentUserUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.re_match.domain.usecases.LoginUserUseCase
import com.example.re_match.domain.usecases.RegisterUserUseCase
import com.example.re_match.domain.usecases.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val checkFirstLoginUseCase: CheckFirstLoginUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    fun registerUser(email: String, password: String, fullName: String, nickname: String, gender: Gender, birthDateTimestamp: Long?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = registerUserUseCase(email, password)
            when {
                result.isSuccess -> {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val profile = UserProfile(uid, email, fullName, nickname, gender, birthDateTimestamp = birthDateTimestamp)
                        val profileResult = createUserProfileUseCase(profile)
                        _authState.value = if (profileResult.isSuccess) {
                            AuthState.RegisterSuccess
                        } else {
                            AuthState.Error(profileResult.exceptionOrNull()?.message ?: "Failed to create user profile")
                        }
                    } else {
                        _authState.value = AuthState.Error("Failed to get user ID")
                    }
                }
                else -> _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            loginUserUseCase(email, password).fold(
                onSuccess = {
                    checkFirstLogin()
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                }
            )
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = sendPasswordResetEmailUseCase(email)
            _authState.value = when {
                result.isSuccess -> AuthState.PasswordResetEmailSent
                else -> AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to send password reset email")
            }
        }
    }

    private fun checkFirstLogin() {
        viewModelScope.launch {
            checkFirstLoginUseCase().fold(
                onSuccess = { isFirstLogin ->
                    Log.d("AuthViewModel", "isFirstLogin: $isFirstLogin") // Add this log
                    if (isFirstLogin) {
                        _authState.value = AuthState.FirstTimeLogin
                    } else {
                        _authState.value = AuthState.LoginSuccess
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Failed to check first login status")
                }
            )
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            if (currentUser.getOrNull() != null) {
                checkFirstLogin()
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object RegisterSuccess : AuthState()
    object LoginSuccess : AuthState()
    object PasswordResetEmailSent : AuthState()
    object FirstTimeLogin : AuthState()
    object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
}