package com.example.re_match.di

import com.example.re_match.data.cache.UserCache
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.GetFriendRequestsUseCase
import com.example.re_match.domain.usecases.GetFriendRequestsWithProfilesUseCase
import com.example.re_match.domain.usecases.GetFriendsUseCase
import com.example.re_match.domain.usecases.GetPendingFriendRequestsCountUseCase
import com.example.re_match.domain.usecases.RemoveFriendUseCase
import com.example.re_match.domain.usecases.RespondToFriendRequestUseCase
import com.example.re_match.domain.usecases.SendFriendRequestUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object FriendRequestModule {

    @Provides
    @ViewModelScoped
    fun provideGetFriendRequestsUseCase(userRepository: IUserRepository): GetFriendRequestsUseCase {
        return GetFriendRequestsUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideRespondToFriendRequestUseCase(userRepository: IUserRepository): RespondToFriendRequestUseCase {
        return RespondToFriendRequestUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSendFriendRequestUseCase(userRepository: IUserRepository): SendFriendRequestUseCase {
        return SendFriendRequestUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetFriendsUseCase(userRepository: IUserRepository): GetFriendsUseCase {
        return GetFriendsUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideRemoveFriendUseCase(userRepository: IUserRepository): RemoveFriendUseCase {
        return RemoveFriendUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetPendingFriendRequestsCountUseCase(userRepository: IUserRepository): GetPendingFriendRequestsCountUseCase {
        return GetPendingFriendRequestsCountUseCase(userRepository)
    }


    @Provides
    fun provideGetFriendRequestsWithProfilesUseCase(
        userRepository: IUserRepository,
        userCache: UserCache
    ): GetFriendRequestsWithProfilesUseCase {
        return GetFriendRequestsWithProfilesUseCase(userRepository, userCache)
    }
}