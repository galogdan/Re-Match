package com.example.re_match.di

import com.example.re_match.data.repositories.AuthRepository
import com.example.re_match.data.repositories.UserRepository
import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.CreateUserProfileUseCase
import com.example.re_match.domain.usecases.LoginUserUseCase
import com.example.re_match.domain.usecases.LogoutUserUseCase
import com.example.re_match.domain.usecases.RegisterUserUseCase
import com.example.re_match.domain.usecases.SendPasswordResetEmailUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): IAuthRepository {
        return AuthRepository(firebaseAuth)
    }

    @Provides
    fun provideRegisterUserUseCase(
        authRepository: IAuthRepository,
        userRepository: IUserRepository
    ): RegisterUserUseCase = RegisterUserUseCase(authRepository, userRepository)

    @Provides
    fun provideLoginUserUseCase(repository: AuthRepository): LoginUserUseCase {
        return LoginUserUseCase(repository)
    }

    @Provides
    fun provideSendPasswordResetEmailUseCase(repository: AuthRepository): SendPasswordResetEmailUseCase {
        return SendPasswordResetEmailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore, auth: FirebaseAuth): IUserRepository {
        return UserRepository(firestore, auth)
    }

    @Provides
    fun provideCreateUserProfileUseCase(repository: UserRepository): CreateUserProfileUseCase {
        return CreateUserProfileUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideLogoutUserUseCase(authRepository: IAuthRepository): LogoutUserUseCase {
        return LogoutUserUseCase(authRepository)
    }



}