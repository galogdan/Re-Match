package com.example.re_match.domain.usecases

import com.example.re_match.data.cache.UserCache
import com.example.re_match.domain.models.Chat
import com.example.re_match.domain.models.Message
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.repositories.IAuthRepository
import com.example.re_match.domain.repositories.IChatRepository
import com.example.re_match.domain.repositories.IUserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// use classes related to chat

class CreateOrGetChatUseCase(private val chatRepository: IChatRepository) {
    suspend operator fun invoke(participants: List<String>): Result<String> {
        return chatRepository.createOrGetChat(participants)
    }
}

class SendMessageUseCase (private val chatRepository: IChatRepository) {
    suspend operator fun invoke(message: Message): Result<Unit> {
        return chatRepository.sendMessage(message)
    }
}

class GetMessagesUseCase (private val chatRepository: IChatRepository) {
    operator fun invoke(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessages(chatId)
    }
}

class GetChatsUseCase (private val chatRepository: IChatRepository) {
    operator fun invoke(userId: String): Flow<List<Chat>> {
        return chatRepository.getChats(userId)
    }
}

class MarkMessageAsReadUseCase (private val chatRepository: IChatRepository) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        return chatRepository.markMessageAsRead(messageId)
    }
}

class GetUnreadCountUseCase (private val chatRepository: IChatRepository) {
    suspend operator fun invoke(chatId: String): Result<Int> {
        return chatRepository.getUnreadCount(chatId)
    }
}

class SearchFriendsUseCase (
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(currentUserId: String, query: String): Result<List<UserProfile>> {
        return userRepository.searchFriends(currentUserId, query)
    }
}

class GetChatsWithProfilesUseCase (
    private val chatRepository: IChatRepository,
    private val userCache: UserCache
) {
    operator fun invoke(userId: String): Flow<List<Pair<Chat, UserProfile?>>> = flow {
        chatRepository.getChats(userId).collect { chats ->
            val chatsWithProfiles = chats.map { chat ->
                val otherUserId = chat.participants.keys.find { it != userId }
                val otherUserProfile = otherUserId?.let { userCache.getUser(it) }
                chat to otherUserProfile
            }
            emit(chatsWithProfiles)
        }
    }
}

class GetCurrentUserUseCase (
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(): Result<FirebaseUser> {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                Result.success(currentUser)
            } else {
                Result.failure(Exception("No user currently logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetChatByIdUseCase(private val chatRepository: IChatRepository) {
    suspend operator fun invoke(chatId: String): Result<Chat> {
        return try {
            val chat = chatRepository.getChatById(chatId)
            if (chat != null) {
                Result.success(chat)
            } else {
                Result.failure(Exception("Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



