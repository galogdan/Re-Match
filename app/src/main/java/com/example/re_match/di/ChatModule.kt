package com.example.re_match.di

import com.example.re_match.data.cache.UserCache
import com.example.re_match.data.repositories.ChatRepository
import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IChatRepository
import com.example.re_match.domain.repositories.IUserRepository
import com.example.re_match.domain.usecases.CreateOrGetChatUseCase
import com.example.re_match.domain.usecases.GetChatByIdUseCase
import com.example.re_match.domain.usecases.GetChatsUseCase
import com.example.re_match.domain.usecases.GetChatsWithProfilesUseCase
import com.example.re_match.domain.usecases.GetCurrentUserUseCase
import com.example.re_match.domain.usecases.GetMessagesUseCase
import com.example.re_match.domain.usecases.GetUnreadCountUseCase
import com.example.re_match.domain.usecases.GetUserProfileByIdUseCase
import com.example.re_match.domain.usecases.MarkMessageAsReadUseCase
import com.example.re_match.domain.usecases.SearchFriendsUseCase
import com.example.re_match.domain.usecases.SendMessageUseCase
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {


    @Provides
    @Singleton
    fun provideChatRepository(database: FirebaseDatabase): IChatRepository {
        return ChatRepository(database)
    }

    @Provides
    fun provideCreateOrGetChatUseCase(repository: IChatRepository): CreateOrGetChatUseCase {
        return CreateOrGetChatUseCase(repository)
    }

    @Provides
    fun provideSendMessageUseCase(repository: IChatRepository): SendMessageUseCase {
        return SendMessageUseCase(repository)
    }

    @Provides
    fun provideGetMessagesUseCase(repository: IChatRepository): GetMessagesUseCase {
        return GetMessagesUseCase(repository)
    }

    @Provides
    fun provideGetChatsUseCase(repository: IChatRepository): GetChatsUseCase {
        return GetChatsUseCase(repository)
    }

    @Provides
    fun provideMarkMessageAsReadUseCase(repository: IChatRepository): MarkMessageAsReadUseCase {
        return MarkMessageAsReadUseCase(repository)
    }

    @Provides
    fun provideGetUnreadCountUseCase(repository: IChatRepository): GetUnreadCountUseCase {
        return GetUnreadCountUseCase(repository)
    }

    @Provides
    fun provideSearchFriendsUseCase(userRepository: IUserRepository): SearchFriendsUseCase {
        return SearchFriendsUseCase(userRepository)
    }

    @Provides
    fun provideGetChatsWithProfilesUseCase(
        chatRepository: IChatRepository,
        userCache: UserCache
    ): GetChatsWithProfilesUseCase {
        return GetChatsWithProfilesUseCase(chatRepository, userCache)
    }

    @Provides
    fun provideGetCurrentUserUseCase(authRepository: IAuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(authRepository)
    }

    @Provides
    fun provideGetChatByIdUseCase(chatRepository: IChatRepository): GetChatByIdUseCase {
        return GetChatByIdUseCase(chatRepository)
    }

    @Provides
    fun provideGetUserProfileByIdUseCase(userRepository: IUserRepository): GetUserProfileByIdUseCase {
        return GetUserProfileByIdUseCase(userRepository)
    }


}