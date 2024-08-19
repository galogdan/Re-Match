package com.example.re_match.ui.viewmodels


import android.util.Log
import androidx.lifecycle.*
import com.example.re_match.data.cache.UserCache
import com.example.re_match.domain.models.Chat
import com.example.re_match.domain.models.Message
import com.example.re_match.domain.models.UserProfile
import com.example.re_match.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatsWithProfilesUseCase: GetChatsWithProfilesUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val searchFriendsUseCase: SearchFriendsUseCase,
    private val createOrGetChatUseCase: CreateOrGetChatUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getChatByIdUseCase: GetChatByIdUseCase,
    private val getUserProfileByIdUseCase: GetUserProfileByIdUseCase,
    private val userCache: UserCache
) : ViewModel() {



    private val _chats = MutableStateFlow<List<Pair<Chat, UserProfile?>>>(emptyList())
    val chats: StateFlow<List<Pair<Chat, UserProfile?>>> = _chats.asStateFlow()


    private val _selectedChat = MutableLiveData<Pair<Chat, UserProfile?>>()
    val selectedChat: LiveData<Pair<Chat, UserProfile?>> = _selectedChat

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()


    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _chatPartnerProfile = MutableLiveData<UserProfile>()
    val chatPartnerProfile: LiveData<UserProfile> = _chatPartnerProfile

    private val _state = MutableLiveData<ChatState>()
    val state: LiveData<ChatState> = _state



    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            currentUserId = getCurrentUserId()
            refreshChats()
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            _state.value = ChatState.Loading
            currentUserId?.let { userId ->
                try {
                    getChatsWithProfilesUseCase(userId).collect { chatsWithProfiles ->
                        _chats.value = chatsWithProfiles.sortedByDescending { (chat, _) -> chat.lastMessageTimestamp }
                        _state.value = ChatState.ChatsRefreshed
                    }
                } catch (error: Exception) {
                    _state.value = ChatState.Error("Failed to refresh chats: ${error.message}")
                }
            } ?: run {
                _state.value = ChatState.Error("Current user ID is null")
            }
        }
    }

    fun selectChat(chat: Chat) {
        viewModelScope.launch {
            val chatPartnerId = chat.participants.keys.find { it != currentUserId }
            chatPartnerId?.let { loadChatPartnerProfile(it) }
            _state.value = ChatState.Loading
            val selectedChatWithProfile = _chats.value?.find { (c, _) -> c.id == chat.id }
            if (selectedChatWithProfile != null) {
                _selectedChat.value = selectedChatWithProfile!!
                _state.value = ChatState.ChatSelected
                refreshMessages(chat.id)
            } else {
                try {
                    val userProfile = getCurrentUserUseCase().getOrNull()?.let { currentUser ->
                        val otherUserId = chat.participants.keys.find { it != currentUser.uid }
                        otherUserId?.let { userCache.getUser(it) }
                    }
                    _selectedChat.value = Pair(chat, userProfile)
                    _state.value = ChatState.ChatSelected
                    refreshMessages(chat.id)
                } catch (error: Exception) {
                    _state.value = ChatState.Error("Failed to select chat: ${error.message}")
                }
            }
        }
    }

    fun refreshMessages(chatId: String) {
        viewModelScope.launch {
            try {
                getMessagesUseCase(chatId).collect { messages ->
                    _messages.value = messages
                    _state.value = ChatState.MessagesRefreshed
                }
            } catch (error: Exception) {
                _state.value = ChatState.Error("Failed to refresh messages: ${error.message}")
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            _state.value = ChatState.Loading
            val selectedChat = _selectedChat.value?.first ?: run {
                _state.value = ChatState.Error("No chat selected")
                return@launch
            }

            val message = Message(
                chatId = selectedChat.id,
                content = content,
                timestamp = System.currentTimeMillis(),
                senderId = getCurrentUserId()
            )

            try {
                sendMessageUseCase(message)
                _state.value = ChatState.MessageSent
                refreshMessages(selectedChat.id)
            } catch (error: Exception) {
                _state.value = ChatState.Error("Failed to send message: ${error.message}")
            }
        }
    }

    fun searchFriends(query: String) {
        viewModelScope.launch {
            _state.value = ChatState.Loading
            try {
                val result = searchFriendsUseCase(currentUserId ?: "", query)
                result.onSuccess { friends ->
                    _searchResults.value = friends
                    _state.value = ChatState.SearchCompleted
                }.onFailure { error ->
                    _state.value = ChatState.Error("Failed to search friends: ${error.message}")
                }
            } catch (error: Exception) {
                _state.value = ChatState.Error("Failed to search friends: ${error.message}")
            }
        }
    }

    fun createOrGetChat(otherUserId: String) {
        viewModelScope.launch {
            if (!_isSearching.value) return@launch
            _state.value = ChatState.Loading
            try {
                val currentUserId = getCurrentUserId() ?: throw Exception("No current user")
                val result = createOrGetChatUseCase(listOf(currentUserId, otherUserId))
                result.onSuccess { chatId ->
                    getChatByIdUseCase(chatId).onSuccess { chat ->
                        selectChat(chat)
                        _state.value = ChatState.ChatSelected
                        refreshMessages(chatId)
                    }.onFailure { error ->
                        _state.value = ChatState.Error("Failed to get chat details: ${error.message}")
                    }
                }.onFailure { error ->
                    _state.value = ChatState.Error("Failed to create or get chat: ${error.message}")
                }
            } catch (error: Exception) {
                _state.value = ChatState.Error("Failed to create or get chat: ${error.message}")
            } finally {
                endSearch()
            }
        }
    }

    private suspend fun getChatDetails(chatId: String, currentUserId: String, otherUserId: String): Chat {
        return getChatByIdUseCase(chatId).getOrElse {
            Chat(
                id = chatId,
                participants = mapOf(currentUserId to true, otherUserId to true),
                lastMessage = "",
                lastMessageTimestamp = System.currentTimeMillis(),
                unreadCount = 0
            )
        }
    }

    private fun loadChatPartnerProfile(chatPartnerId: String) {
        viewModelScope.launch {
            getUserProfileByIdUseCase(chatPartnerId).onSuccess { profile ->
                _chatPartnerProfile.value = profile
            }.onFailure { error ->
                // handle error
            }
        }
    }

    suspend fun getCurrentUserId(): String? {
        return getCurrentUserUseCase().getOrNull()?.uid
    }

    fun startSearch() {
        _isSearching.value = true
    }

    fun endSearch() {
        _isSearching.value = false
    }



}

sealed class ChatState {
    object Loading : ChatState()
    object ChatsRefreshed : ChatState()
    object MessagesRefreshed : ChatState()
    object MessageSent : ChatState()
    object SearchCompleted : ChatState()
    object ChatSelected : ChatState()
    data class Error(val message: String) : ChatState()
}