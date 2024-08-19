package com.example.re_match.di

import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.CancelFriendRequestUseCase
import com.example.re_match.domain.usecases.CheckFriendRequestStatusUseCase
import com.example.re_match.domain.usecases.DiscoverUsersUseCase
import com.example.re_match.domain.usecases.GetCurrentUserUseCase
import com.example.re_match.domain.usecases.SearchUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object DiscoverModule {

    @Provides
    fun provideDiscoverUsersUseCase(
        userRepository: IUserRepository,
        getCurrentUserUseCase: GetCurrentUserUseCase
    ): DiscoverUsersUseCase {
        return DiscoverUsersUseCase(userRepository, getCurrentUserUseCase)
    }

    @Provides
    fun provideSearchUsersUseCase(
        userRepository: IUserRepository,
        getCurrentUserUseCase: GetCurrentUserUseCase
    ): SearchUsersUseCase {
        return SearchUsersUseCase(userRepository, getCurrentUserUseCase)
    }


    @Provides
    fun provideCancelFriendRequestUseCase(userRepository: IUserRepository): CancelFriendRequestUseCase {
        return CancelFriendRequestUseCase(userRepository)
    }

    @Provides
    fun provideCheckFriendRequestStatusUseCase(userRepository: IUserRepository): CheckFriendRequestStatusUseCase {
        return CheckFriendRequestStatusUseCase(userRepository)
    }
}


