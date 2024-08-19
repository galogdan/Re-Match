package com.example.re_match.domain.repositories

import com.example.re_match.domain.models.Chat
import com.example.re_match.domain.models.Message
import kotlinx.coroutines.flow.Flow

// chat repository interface
interface IChatRepository {
    suspend fun createOrGetChat(participants: List<String>): Result<String> // Returns chatId
    suspend fun sendMessage(message: Message): Result<Unit>
    fun getMessages(chatId: String): Flow<List<Message>>
    fun getChats(userId: String): Flow<List<Chat>>
    suspend fun markMessageAsRead(messageId: String): Result<Unit>
    suspend fun getUnreadCount(chatId: String): Result<Int>
    suspend fun getChatById(chatId: String): Chat?
    suspend fun getChatIdForUsers(participants: List<String>): String?
    suspend fun createChat(participants: List<String>): Result<String>
}
