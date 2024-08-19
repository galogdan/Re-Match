package com.example.re_match.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_match.domain.usecases.GetPendingFriendRequestsCountUseCase
import com.example.re_match.domain.usecases.GetPrivacySettingsUseCase
import com.example.re_match.domain.usecases.LogoutUserUseCase
import com.example.re_match.domain.usecases.UpdatePrivacySettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPendingFriendRequestsCountUseCase: GetPendingFriendRequestsCountUseCase,
    private val getPrivacySettingsUseCase: GetPrivacySettingsUseCase,
    private val updatePrivacySettingsUseCase: UpdatePrivacySettingsUseCase,
    private val logoutUserUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _friendRequestCount = MutableLiveData<Int>()
    val friendRequestCount: LiveData<Int> = _friendRequestCount

    private val _logoutStatus = MutableLiveData<LogoutStatus>()
    val logoutStatus: LiveData<LogoutStatus> = _logoutStatus

    private val _privacySettingsState = MutableLiveData<PrivacySettingsState>()
    val privacySettingsState: LiveData<PrivacySettingsState> = _privacySettingsState

    fun getFriendRequestCount() {
        viewModelScope.launch {
            val result = getPendingFriendRequestsCountUseCase()
            if (result.isSuccess) {
                _friendRequestCount.value = result.getOrNull() ?: 0
            } else {
                // handle
            }
        }
    }

    fun getPrivacySettings() {
        viewModelScope.launch {
            _privacySettingsState.value = PrivacySettingsState.Loading
            val result = getPrivacySettingsUseCase()
            _privacySettingsState.value = when {
                result.isSuccess -> PrivacySettingsState.Success(result.getOrNull() ?: emptyMap())
                else -> PrivacySettingsState.Error(result.exceptionOrNull()?.message ?: "Failed to get privacy settings")
            }
        }
    }

    fun updatePrivacySettings(updatedSettings: Map<String, Boolean>) {
        viewModelScope.launch {
            _privacySettingsState.value = PrivacySettingsState.Loading
            val result = updatePrivacySettingsUseCase(updatedSettings)
            _privacySettingsState.value = when {
                result.isSuccess -> PrivacySettingsState.UpdateSuccess
                else -> PrivacySettingsState.Error(result.exceptionOrNull()?.message ?: "Failed to update privacy settings")
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            val result = logoutUserUseCase()
            _logoutStatus.value = if (result.isSuccess) {
                LogoutStatus.Success
            } else {
                LogoutStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class PrivacySettingsState {
    object Loading : PrivacySettingsState()
    data class Success(val privacySettings: Map<String, Boolean>) : PrivacySettingsState()
    data class Error(val message: String) : PrivacySettingsState()
    object UpdateSuccess : PrivacySettingsState()
}

sealed class LogoutStatus {
    object Success : LogoutStatus()
    data class Error(val message: String) : LogoutStatus()
}