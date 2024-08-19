package com.example.re_match.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.re_match.domain.models.FriendRequest
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.usecases.GetFriendRequestsUseCase
import com.example.re_match.domain.usecases.GetFriendRequestsWithProfilesUseCase
import com.example.re_match.domain.usecases.RespondToFriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val respondToFriendRequestUseCase: RespondToFriendRequestUseCase,
    private val getFriendRequestsWithProfilesUseCase: GetFriendRequestsWithProfilesUseCase
) : ViewModel() {

    private val _friendRequests = MutableLiveData<List<Pair<FriendRequest, UserProfile?>>>()
    val friendRequests: LiveData<List<Pair<FriendRequest, UserProfile?>>> = _friendRequests


    private val _responseStatus = MutableLiveData<ResponseStatus>()
    val responseStatus: LiveData<ResponseStatus> = _responseStatus

    fun getFriendRequests() {
        viewModelScope.launch {
            val result = getFriendRequestsWithProfilesUseCase()
            if (result.isSuccess) {
                _friendRequests.value = result.getOrNull() ?: emptyList()
            } else {
                _responseStatus.value = ResponseStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
    fun respondToFriendRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            val result = respondToFriendRequestUseCase(requestId, accept)
            _responseStatus.value = if (result.isSuccess) {
                ResponseStatus.Success
            } else {
                ResponseStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    sealed class ResponseStatus {
        object Success : ResponseStatus()
        data class Error(val message: String) : ResponseStatus()
    }
}