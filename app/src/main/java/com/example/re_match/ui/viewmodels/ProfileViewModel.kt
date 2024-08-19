package com.example.re_match.ui.viewmodels


import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_match.domain.models.GamingPlatform
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.usecases.GetPendingFriendRequestsCountUseCase
import com.example.re_match.domain.usecases.GetUserProfileUseCase
import com.example.re_match.domain.usecases.UpdateUserProfileUseCase
import com.example.re_match.domain.usecases.UploadUserPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadUserPhotoUseCase: UploadUserPhotoUseCase,
    private val getPendingFriendRequestsCountUseCase: GetPendingFriendRequestsCountUseCase
) : ViewModel() {

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    private val _preferredGames = MutableLiveData<List<String>>(emptyList())
    val preferredGames: LiveData<List<String>> = _preferredGames

    private var currentProfile: UserProfile? = null


    fun getUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            getUserProfileUseCase().fold(
                onSuccess = { profile ->
                    _profileState.value = ProfileState.Success(profile)
                    _preferredGames.value = profile.preferredGames
                },
                onFailure = { error ->
                    _profileState.value = ProfileState.Error(error.message ?: "Failed to get user profile")
                }
            )
        }
    }

    fun updateProfile(
        region: String,
        gamingHours: String,
        shortDescription: String,
        preferredPlatform: GamingPlatform,
        isFirstLogin: Boolean
    ) {
        viewModelScope.launch {
            val currentProfile = (_profileState.value as? ProfileState.Success)?.userProfile
                ?: run {
                    _profileState.value = ProfileState.Error("No current profile found")
                    return@launch
                }

            val updatedProfile = currentProfile.copy(
                region = region,
                gamingHours = gamingHours,
                preferredGames = _preferredGames.value ?: emptyList(),
                shortDescription = shortDescription,
                preferredPlatform = preferredPlatform,
                isFirstLogin = isFirstLogin  // Make sure this is being set
            )

            _profileState.value = ProfileState.Loading
            updateUserProfileUseCase(updatedProfile).fold(
                onSuccess = {
                    _profileState.value = ProfileState.UpdateSuccess
                    Log.d("ProfileViewModel", "Profile updated successfully. isFirstLogin = $isFirstLogin")
                },
                onFailure = { error ->
                    _profileState.value = ProfileState.Error(error.message ?: "Failed to update profile")
                    Log.e("ProfileViewModel", "Failed to update profile", error)
                }
            )
        }
    }



    fun addPreferredGame(game: String) {
        val currentGames = _preferredGames.value ?: emptyList()
        if (game !in currentGames) {
            _preferredGames.value = currentGames + game
        }



    }

    fun removePreferredGame(game: String) {
        val currentGames = _preferredGames.value ?: emptyList()
        _preferredGames.value = currentGames - game
    }

    fun setPreferredGames(games: List<String>) {
        _preferredGames.value = games
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val result = uploadUserPhotoUseCase(uri)
                when {
                    result.isSuccess -> {
                        val photoUrl = result.getOrNull()
                        if (photoUrl != null) {
                            _profileState.value = ProfileState.PhotoUploadSuccess(photoUrl)
                        } else {
                            _profileState.value = ProfileState.Error("Failed to get photo URL")
                        }
                    }
                    result.isFailure -> {
                        val error = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                        _profileState.value = ProfileState.Error("Failed to upload photo: $error")
                    }
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Failed to upload photo: ${e.message}")
            }
        }
    }




}



sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val userProfile: UserProfile) : ProfileState()
    object UpdateSuccess : ProfileState()
    data class PhotoUploadSuccess(val photoUrl: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

