package com.example.re_match.domain.repositories

import android.net.Uri
import com.example.re_match.domain.models.UserProfile

// profile repository interface
interface IProfileRepository {
    suspend fun getUserProfile(uid: String): Result<UserProfile>
    suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit>
    suspend fun uploadUserPhoto(uid: String, photoUri: Uri): Result<String>
    suspend fun updateFirstLoginStatus(uid: String, isFirstLogin: Boolean): Result<Unit>
}