package com.example.re_match.data.repositories

import android.net.Uri
import android.util.Log
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.repositories.IProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : IProfileRepository {

    override suspend fun getUserProfile(uid: String): Result<UserProfile> {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            val userProfile = document.toObject(UserProfile::class.java)
            if (userProfile != null) {
                Log.d("ProfileRepository", "Retrieved profile for $uid: isFirstLogin = ${userProfile.isFirstLogin}")
                Result.success(userProfile)
            } else {
                Log.w("ProfileRepository", "User profile not found for $uid")
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error getting user profile", e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            firestore.collection("users").document(userProfile.uid)
                .set(userProfile)
                .await()
            Log.d("ProfileRepository", "Updated profile for ${userProfile.uid}: isFirstLogin = ${userProfile.isFirstLogin}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error updating user profile", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadUserPhoto(uid: String, photoUri: Uri): Result<String> {
        return try {
            val photoRef = storage.reference.child("profile_photos/$uid.jpg")
            val uploadTask = photoRef.putFile(photoUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            firestore.collection("users").document(uid)
                .update("photoUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFirstLoginStatus(uid: String, isFirstLogin: Boolean): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("isFirstLogin", isFirstLogin)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}