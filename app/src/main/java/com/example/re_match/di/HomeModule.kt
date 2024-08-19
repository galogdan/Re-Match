package com.example.re_match.di

import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IProfileRepository
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.DeleteFriendUseCase
import com.example.re_match.domain.usecases.GetPendingFriendRequestsCountUseCase
import com.example.re_match.domain.usecases.GetPrivacySettingsUseCase
import com.example.re_match.domain.usecases.UpdatePrivacySettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object HomeModule {


    @Provides
    @ViewModelScoped
    fun provideGetPrivacySettingsUseCase(
        profileRepository: IProfileRepository,
        authRepository: IAuthRepository
    ): GetPrivacySettingsUseCase {
        return GetPrivacySettingsUseCase(profileRepository, authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdatePrivacySettingsUseCase(
        profileRepository: IProfileRepository,
        authRepository: IAuthRepository
    ): UpdatePrivacySettingsUseCase {
        return UpdatePrivacySettingsUseCase(profileRepository, authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteFriendUseCase(userRepository: IUserRepository): DeleteFriendUseCase {
        return DeleteFriendUseCase(userRepository)
    }



}