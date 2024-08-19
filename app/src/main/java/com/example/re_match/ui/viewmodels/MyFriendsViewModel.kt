package com.example.re_match.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.usecases.DeleteFriendUseCase
import com.example.re_match.domain.usecases.GetFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyFriendsViewModel @Inject constructor(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase
) : ViewModel() {

    private val _deleteFriendState = MutableLiveData<DeleteFriendState>()
    val deleteFriendState: LiveData<DeleteFriendState> = _deleteFriendState

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _state = MutableLiveData<MyFriendsState>()
    val state: LiveData<MyFriendsState> = _state

    fun getFriends() {
        viewModelScope.launch {
            _state.value = MyFriendsState.Loading
            try {
                val result = getFriendsUseCase()
                result.onSuccess { friendsList ->
                    _friends.value = friendsList
                    _state.value = MyFriendsState.Success
                }.onFailure { error ->
                    _state.value = MyFriendsState.Error(error.message ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                _state.value = MyFriendsState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteFriend(friendId: String) {
        viewModelScope.launch {
            _deleteFriendState.value = DeleteFriendState.Loading
            try {
                val result = deleteFriendUseCase(friendId)
                result.onSuccess {
                    _deleteFriendState.value = DeleteFriendState.Success
                    // Refresh the friends list after successful deletion
                    getFriends()
                }.onFailure { error ->
                    _deleteFriendState.value = DeleteFriendState.Error(error.message ?: "Failed to delete friend")
                }
            } catch (e: Exception) {
                _deleteFriendState.value = DeleteFriendState.Error(e.message ?: "An error occurred")
            }
        }
    }
}


sealed class MyFriendsState {
    object Loading : MyFriendsState()
    object Success : MyFriendsState()
    data class Error(val message: String) : MyFriendsState()
}

sealed class DeleteFriendState {
    object Loading : DeleteFriendState()
    object Success : DeleteFriendState()
    data class Error(val message: String) : DeleteFriendState()
}