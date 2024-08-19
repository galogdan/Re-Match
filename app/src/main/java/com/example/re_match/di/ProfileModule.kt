package com.example.re_match.di

import com.example.re_match.data.repositories.ProfileRepository
import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IProfileRepository
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.CheckFirstLoginUseCase
import com.example.re_match.domain.usecases.GetUserProfileUseCase
import com.example.re_match.domain.usecases.SendFriendRequestUseCase
import com.example.re_match.domain.usecases.UpdateUserProfileUseCase
import com.example.re_match.domain.usecases.UploadUserPhotoUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ProfileModule {

    @Provides
    @ViewModelScoped
    fun provideGetUserProfileUseCase(
        profileRepository: IProfileRepository,
        authRepository: IAuthRepository
    ): GetUserProfileUseCase {
        return GetUserProfileUseCase(profileRepository, authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateUserProfileUseCase(
        profileRepository: IProfileRepository
    ): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(profileRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUploadUserPhotoUseCase(
        profileRepository: IProfileRepository,
        authRepository: IAuthRepository
    ): UploadUserPhotoUseCase {
        return UploadUserPhotoUseCase(profileRepository, authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideProfileRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): IProfileRepository {
        return ProfileRepository(firestore, storage)
    }


    @Provides
    fun provideCheckFirstLoginUseCase(
        profileRepository: IProfileRepository,
        authRepository: IAuthRepository
    ): CheckFirstLoginUseCase {
        return CheckFirstLoginUseCase(profileRepository, authRepository)
    }






}