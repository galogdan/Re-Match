package com.example.re_match.domain.usecases

import android.net.Uri
import com.example.re_match.data.cache.UserCache
import com.example.re_match.domain.models.FriendRequest
import com.example.re_match.domain.models.FriendRequestStatus
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IProfileRepository
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.ui.viewmodels.DiscoverFilters
import javax.inject.Inject

// use cases related to users actions

class GetUserProfileUseCase(
    private val profileRepository: IProfileRepository,
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<UserProfile> {
        val uid = authRepository.getCurrentUser()?.uid ?: return Result.failure(Exception("User not logged in"))
        return profileRepository.getUserProfile(uid)
    }
}


class UpdateUserProfileUseCase(
    private val profileRepository: IProfileRepository
) {
    suspend operator fun invoke(userProfile: UserProfile): Result<Unit> {
        return profileRepository.updateUserProfile(userProfile)
    }
}


class UploadUserPhotoUseCase(
    private val profileRepository: IProfileRepository,
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(photoUri: Uri): Result<String> {
        val uid = authRepository.getCurrentUser()?.uid ?: return Result.failure(Exception("User not logged in"))
        return profileRepository.uploadUserPhoto(uid, photoUri)
    }
}

class DiscoverUsersUseCase(
    private val userRepository: IUserRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(filters: DiscoverFilters): Result<List<UserProfile>> {
        val currentUser = getCurrentUserUseCase().getOrNull() ?: return Result.failure(Exception("No current user"))
        return userRepository.getDiscoveredUsers(filters, currentUser.uid)
    }
}

class SearchUsersUseCase(
    private val userRepository: IUserRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) {
    suspend operator fun invoke(query: String, filters: DiscoverFilters): Result<List<UserProfile>> {
        val currentUser = getCurrentUserUseCase().getOrNull() ?: return Result.failure(Exception("No current user"))
        return userRepository.searchUsers(query, filters, currentUser.uid)
    }
}

class SendFriendRequestUseCase (private val userRepository: IUserRepository) {
    suspend operator fun invoke(receiverId: String): Result<Unit> {
        return userRepository.sendFriendRequest(receiverId)
    }
}

class GetFriendRequestsUseCase (private val userRepository: IUserRepository) {
    suspend operator fun invoke(): Result<List<FriendRequest>> {
        return userRepository.getFriendRequests()
    }
}

class RespondToFriendRequestUseCase (private val userRepository: IUserRepository) {
    suspend operator fun invoke(requestId: String, accept: Boolean): Result<Unit> {
        return userRepository.respondToFriendRequest(requestId, accept)
    }
}

class GetFriendsUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): Result<List<UserProfile>> {
        return userRepository.getFriends()
    }
}

class RemoveFriendUseCase (private val userRepository: IUserRepository) {
    suspend operator fun invoke(friendId: String): Result<Unit> {
        return userRepository.removeFriend(friendId)
    }
}

class GetPendingFriendRequestsCountUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return userRepository.getPendingFriendRequestsCount()
    }
}

class CancelFriendRequestUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(receiverId: String): Result<Unit> {
        return userRepository.cancelFriendRequest(receiverId)
    }
}

// File: domain/usecases/CheckFriendRequestStatusUseCase.kt

class CheckFriendRequestStatusUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(receiverId: String): Result<FriendRequestStatus?> {
        return userRepository.checkFriendRequestStatus(receiverId)
    }
}

class GetPrivacySettingsUseCase (
    private val profileRepository: IProfileRepository,
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<Map<String, Boolean>> {
        val currentUser = authRepository.getCurrentUser()
        return if (currentUser != null) {
            profileRepository.getUserProfile(currentUser.uid).map { it.privacySettings }
        } else {
            Result.failure(Exception("User not logged in"))
        }
    }
}

class UpdatePrivacySettingsUseCase (
    private val profileRepository: IProfileRepository,
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(updatedSettings: Map<String, Boolean>): Result<Unit> {
        val currentUser = authRepository.getCurrentUser()
        return if (currentUser != null) {
            val profileResult = profileRepository.getUserProfile(currentUser.uid)
            if (profileResult.isSuccess) {
                val currentProfile = profileResult.getOrNull()
                if (currentProfile != null) {
                    val updatedProfile = currentProfile.copy(privacySettings = updatedSettings)
                    profileRepository.updateUserProfile(updatedProfile)
                } else {
                    Result.failure(Exception("Current profile not found"))
                }
            } else {
                Result.failure(profileResult.exceptionOrNull() ?: Exception("Failed to get current profile"))
            }
        } else {
            Result.failure(Exception("User not logged in"))
        }
    }
}

class LogoutUserUseCase (
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.logoutUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetFriendRequestsWithProfilesUseCase (
    private val userRepository: IUserRepository,
    private val userCache: UserCache
) {
    suspend operator fun invoke(): Result<List<Pair<FriendRequest, UserProfile?>>> {
        return try {
            val friendRequests = userRepository.getFriendRequests().getOrThrow()
            val requestsWithProfiles = friendRequests.map { request ->
                val senderProfile = userCache.getUser(request.senderId)
                request to senderProfile
            }
            Result.success(requestsWithProfiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteFriendUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(friendId: String): Result<Unit> {
        return userRepository.removeFriend(friendId)
    }
}

class GetUserProfileByIdUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(userId: String): Result<UserProfile> {
        return try {
            val profile = userRepository.getUserProfileById(userId)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class CheckFirstLoginUseCase (
    private val profileRepository: IProfileRepository,
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        val currentUser = authRepository.getCurrentUser() ?: return Result.failure(Exception("No authenticated user"))
        return profileRepository.getUserProfile(currentUser.uid).map { profile ->
            profile.isFirstLogin
        }
    }
}





