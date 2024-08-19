package com.example.re_match.domain.repositories

import com.example.re_match.domain.models.FriendRequest
import com.example.re_match.domain.models.FriendRequestStatus
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.ui.viewmodels.DiscoverFilters

// user repository interface
interface IUserRepository {
    suspend fun createUserProfile(userProfile: UserProfile): Result<Unit>
    suspend fun searchUsers(query: String, filters: DiscoverFilters, currentUserId: String): Result<List<UserProfile>>
    suspend fun getDiscoveredUsers(filters: DiscoverFilters, currentUserId: String): Result<List<UserProfile>>
    suspend fun sendFriendRequest(receiverId: String): Result<Unit>
    suspend fun getFriendRequests(): Result<List<FriendRequest>>
    suspend fun respondToFriendRequest(requestId: String, accept: Boolean): Result<Unit>
    suspend fun getFriends(): Result<List<UserProfile>>
    suspend fun removeFriend(friendId: String): Result<Unit>
    suspend fun getPendingFriendRequestsCount(): Result<Int>
    suspend fun cancelFriendRequest(receiverId: String): Result<Unit>
    suspend fun checkFriendRequestStatus(receiverId: String): Result<FriendRequestStatus?>
    suspend fun searchFriends(currentUserId: String, query: String): Result<List<UserProfile>>
    suspend fun isFriend(currentUserId: String, potentialFriendId: String): Boolean
    suspend fun getUserProfileById(userId: String): UserProfile


}