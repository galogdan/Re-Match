package com.example.re_match.data.repositories

import android.util.Log
import com.example.re_match.domain.models.Chat
import com.example.re_match.domain.models.Message
import com.example.re_match.domain.repositories.IChatRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.snapshots
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase
) : IChatRepository {

    override suspend fun createOrGetChat(participants: List<String>): Result<String> {
        return try {
            val existingChatId = getChatIdForUsers(participants)
            if (existingChatId != null) {
                Result.success(existingChatId)
            } else {
                val chatRef = database.getReference("chats").push()
                val chatId = chatRef.key ?: throw Exception("Failed to generate chat ID")
                val participantsMap = participants.associateWith { true }
                val chat = Chat(id = chatId, participants = participantsMap)
                chatRef.setValue(chat).await()
                Result.success(chatId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            val messageRef = database.getReference("messages")
                .child(message.chatId)
                .push()

            val messageId = messageRef.key ?: throw Exception("Failed to generate message ID")
            val messageWithId = message.copy(id = messageId)

            messageRef.setValue(messageWithId).await()

            // Update the last message in the chat
            database.getReference("chats")
                .child(message.chatId)
                .updateChildren(mapOf(
                    "lastMessage" to message.content,
                    "lastMessageTimestamp" to message.timestamp
                )).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(chatId: String): Flow<List<Message>> {
        return database.getReference("messages")
            .child(chatId)
            .orderByChild("timestamp")
            .snapshots
            .map { snapshot ->
                snapshot.children.mapNotNull { it.getValue(Message::class.java) }
            }
    }

    override fun getChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val chatsRef = database.getReference("chats")
        val listener = chatsRef.orderByChild("participants/$userId").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chats = snapshot.children.mapNotNull { it.getValue(Chat::class.java) }
                    trySend(chats)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })

        awaitClose { chatsRef.removeEventListener(listener) }
    }


    override suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        return try {
            database.getReference("messages")
                .child(messageId)
                .updateChildren(mapOf("isRead" to true))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnreadCount(chatId: String): Result<Int> {
        return try {
            val snapshot = database.getReference("messages")
                .child(chatId)
                .orderByChild("isRead")
                .equalTo(false)
                .get()
                .await()
            Result.success(snapshot.childrenCount.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatIdForUsers(participants: List<String>): String? {
        val sortedParticipants = participants.sorted()
        val chatsRef = database.getReference("chats")
        val snapshot = chatsRef.get().await()

        for (chatSnapshot in snapshot.children) {
            val chat = chatSnapshot.getValue(Chat::class.java)
            if (chat != null && chat.participants.keys.toSet() == sortedParticipants.toSet()) {
                return chatSnapshot.key
            }
        }
        return null
    }

    override suspend fun createChat(participants: List<String>): Result<String> {
        return try {
            val chatRef = database.getReference("chats").push()
            val chatId = chatRef.key ?: throw Exception("Failed to generate chat ID")
            val participantsMap = participants.associateWith { true }
            val chat = Chat(id = chatId, participants = participantsMap)
            chatRef.setValue(chat).await()
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatById(chatId: String): Chat? {
        return try {
            val chatSnapshot = database.getReference("chats")
                .child(chatId)
                .get()
                .await()

            chatSnapshot.getValue(Chat::class.java)
        } catch (e: Exception) {
            null
        }
    }
}