package com.example.re_match.data.cache

import com.example.re_match.domain.models.UserProfile
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserCache @Inject constructor(private val firestore: FirebaseFirestore) {
    private val cache = mutableMapOf<String, UserProfile>()

    suspend fun getUser(userId: String): UserProfile {
        return cache[userId] ?: fetchAndCacheUser(userId)
    }

    private suspend fun fetchAndCacheUser(userId: String): UserProfile {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val userProfile = userDoc.toObject(UserProfile::class.java)
            ?: throw UserNotFoundException(userId)
        cache[userId] = userProfile
        return userProfile
    }


    class UserNotFoundException(userId: String) : Exception("User not found: $userId")
}