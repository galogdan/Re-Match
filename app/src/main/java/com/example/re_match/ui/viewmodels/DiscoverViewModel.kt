package com.example.re_match.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_match.domain.models.FriendRequestStatus
import com.example.re_match.domain.models.GamingPlatform
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.CancelFriendRequestUseCase
import com.example.re_match.domain.usecases.CheckFriendRequestStatusUseCase
import com.example.re_match.domain.usecases.DiscoverUsersUseCase
import com.example.re_match.domain.usecases.GetCurrentUserUseCase
import com.example.re_match.domain.usecases.SearchUsersUseCase
import com.example.re_match.domain.usecases.SendFriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val discoverUsersUseCase: DiscoverUsersUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendFriendRequestUseCase: SendFriendRequestUseCase,
    private val cancelFriendRequestUseCase: CancelFriendRequestUseCase,
    private val checkFriendRequestStatusUseCase: CheckFriendRequestStatusUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _discoveredUsers = MutableLiveData<List<UserProfile>>()
    val discoveredUsers: LiveData<List<UserProfile>> = _discoveredUsers

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _friendRequestStatusState = MutableLiveData<FriendRequestStatusState>()
    val friendRequestStatusState: LiveData<FriendRequestStatusState> = _friendRequestStatusState

    private val _friendRequestStatuses = MutableLiveData<Map<String, FriendRequestStatus?>>()
    val friendRequestStatuses: LiveData<Map<String, FriendRequestStatus?>> = _friendRequestStatuses

    private var currentFilters: DiscoverFilters = DiscoverFilters()

    fun applyFilters(
        region: String?,
        preferredGames: List<String>,
        gamingHours: String?,
        preferredPlatform: GamingPlatform
    ) {
        currentFilters = DiscoverFilters(
            region = region?.takeIf { it != "All Regions" },
            preferredGames = preferredGames.filter { it.isNotBlank() },
            gamingHours = gamingHours?.takeIf { it != "Any Time" },
            preferredPlatform = preferredPlatform.takeIf { it != GamingPlatform.ALL }
        )
        refreshDiscoveredUsers()
    }


    fun refreshDiscoveredUsers() {
        viewModelScope.launch {
            _discoveredUsers.value = emptyList() // Clear the current list
            Log.d("DiscoverViewModel", "Refreshing discovered users with filters: $currentFilters")
            val result = discoverUsersUseCase(currentFilters)
            handleResult(result)
        }
    }

    fun searchUsers(query: String) {
        Log.d("DiscoverViewModel", "Searching users with query: $query and filters: $currentFilters")
        viewModelScope.launch {
            val result = searchUsersUseCase(query, currentFilters)
            handleResult(result)
        }
    }

    private fun handleResult(result: Result<List<UserProfile>>) {
        when {
            result.isSuccess -> {
                val users = result.getOrNull()
                if (!users.isNullOrEmpty()) {
                    Log.d("DiscoverViewModel", "Found ${users.size} users: ${users.map { it.nickname }}")
                    _discoveredUsers.value = users!!
                } else {
                    Log.d("DiscoverViewModel", "No users found")
                    _errorMessage.value = "No users found"
                }
            }
            result.isFailure -> {
                val exception = result.exceptionOrNull()
                Log.e("DiscoverViewModel", "Error fetching users", exception)
                _errorMessage.value = exception?.message ?: "An error occurred"
            }
        }
    }

    fun clearFilters() {
        currentFilters = DiscoverFilters()
        Log.d("DiscoverViewModel", "Cleared filters")
        refreshDiscoveredUsers()
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            _friendRequestStatusState.value = FriendRequestStatusState.Loading
            val result = sendFriendRequestUseCase(userId)
            _friendRequestStatusState.value = when {
                result.isSuccess -> FriendRequestStatusState.Success
                else -> FriendRequestStatusState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun checkFriendRequestStatuses(userIds: List<String>) {
        viewModelScope.launch {
            val statuses = userIds.associateWith { userId ->
                checkFriendRequestStatusUseCase(userId).getOrNull()
            }
            _friendRequestStatuses.value = statuses
        }
    }

    fun sendOrCancelFriendRequest(userId: String) {
        viewModelScope.launch {
            val currentStatus = _friendRequestStatuses.value?.get(userId)
            val result = if (currentStatus == FriendRequestStatus.PENDING) {
                cancelFriendRequestUseCase(userId)
            } else {
                sendFriendRequestUseCase(userId)
            }

            if (result.isSuccess) {
                val newStatuses = _friendRequestStatuses.value?.toMutableMap() ?: mutableMapOf()
                newStatuses[userId] = if (currentStatus == FriendRequestStatus.PENDING) null else FriendRequestStatus.PENDING
                _friendRequestStatuses.value = newStatuses
            } else {
                // handle error
            }
        }
    }
}


data class DiscoverFilters(
    val region: String? = null,
    val preferredGames: List<String> = emptyList(),
    val gamingHours: String? = null,
    val preferredPlatform: GamingPlatform? = null
)

sealed class FriendRequestStatusState {
    object Loading : FriendRequestStatusState()
    object Success : FriendRequestStatusState()
    data class Error(val message: String) : FriendRequestStatusState()
}