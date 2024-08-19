package com.example.re_match.domain.repositories

import com.google.firebase.auth.FirebaseUser

// auth repository interface
interface IAuthRepository {
    suspend fun registerUser(email: String, password: String): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun getCurrentUser(): FirebaseUser?
    suspend fun logoutUser()

}