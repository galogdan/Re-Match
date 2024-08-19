package com.example.re_match.domain.usecases


import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IUserRepository

// use classes related to authentication

class RegisterUserUseCase(private val authRepository: IAuthRepository,  private val userRepository: IUserRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepository.registerUser(email, password)
    }
}


class LoginUserUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return authRepository.loginUser(email, password)
    }
}

class SendPasswordResetEmailUseCase(private val authRepository: IAuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }
}

class CreateUserProfileUseCase  constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(userProfile: UserProfile): Result<Unit> {
        return userRepository.createUserProfile(userProfile)
    }
}

