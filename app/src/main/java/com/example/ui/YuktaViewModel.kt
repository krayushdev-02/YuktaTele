package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class YuktaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YuktaRepository(application)

    // --- State Holders ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedChatId = MutableStateFlow<Int?>(null)
    val selectedChatId: StateFlow<Int?> = _selectedChatId

    private val _chatSearchQuery = MutableStateFlow("")
    val chatSearchQuery: StateFlow<String> = _chatSearchQuery

    // --- Screen State Navigation ---
    // Represents which main tab/screen is active: "chats", "stories", "calls", "contacts", "settings"
    private val _activeTab = MutableStateFlow("chats")
    val activeTab: StateFlow<String> = _activeTab

    // --- Dynamic Streams ---
    val selfUser: StateFlow<UserEntity?> = flow {
        emit(repository.userDao.getSelfUser())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allUsers: StateFlow<List<UserEntity>> = repository.userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chats: StateFlow<List<ChatEntity>> = combine(
        repository.allChats,
        _searchQuery
    ) { chatList, query ->
        if (query.isBlank()) {
            chatList
        } else {
            chatList.filter { it.title.contains(query, ignoreCase = true) || it.lastMessageText.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMessages: StateFlow<List<MessageEntity>> = _selectedChatId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMessagesForChat(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMessages: StateFlow<List<MessageEntity>> = combine(
        activeMessages,
        _chatSearchQuery
    ) { messageList, query ->
        if (query.isBlank()) messageList
        else messageList.filter { it.content.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.getActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Smart Reply / Summary state ---
    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies: StateFlow<List<String>> = _smartReplies

    private val _summaryText = MutableStateFlow<String?>(null)
    val summaryText: StateFlow<String?> = _summaryText

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading

    // --- Call Screens State ---
    private val _activeCall = MutableStateFlow<CallEntity?>(null)
    val activeCall: StateFlow<CallEntity?> = _activeCall

    // --- Story Viewer State ---
    private val _viewingStoryUser = MutableStateFlow<String?>(null) // User ID whose stories are being viewed
    val viewingStoryUser: StateFlow<String?> = _viewingStoryUser

    // --- UI Actions ---
    fun selectTab(tab: String) {
        _activeTab.value = tab
        if (tab != "chats") {
            _selectedChatId.value = null
        }
    }

    fun searchChats(query: String) {
        _searchQuery.value = query
    }

    fun searchInChat(query: String) {
        _chatSearchQuery.value = query
    }

    fun selectChat(chatId: Int?) {
        _selectedChatId.value = chatId
        _chatSearchQuery.value = ""
        _summaryText.value = null
        _smartReplies.value = emptyList()
        if (chatId != null) {
            viewModelScope.launch {
                repository.clearUnread(chatId)
                generateSmartRepliesForLastMessage()
            }
        }
    }

    fun sendMessage(content: String, type: String = "text", viewOnce: Boolean = false) {
        val chatId = _selectedChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(chatId, content, type, viewOnce)
            // Scroll or regenerate replies can be triggered here
            _smartReplies.value = emptyList()
        }
    }

    fun togglePinChat(chatId: Int) {
        viewModelScope.launch {
            repository.togglePin(chatId)
        }
    }

    fun deleteChat(chatId: Int) {
        viewModelScope.launch {
            repository.chatDao.deleteChatById(chatId)
            repository.messageDao.deleteMessagesForChat(chatId)
            if (_selectedChatId.value == chatId) {
                _selectedChatId.value = null
            }
        }
    }

    fun startNewChat(user: UserEntity) {
        viewModelScope.launch {
            // Check if chat already exists
            val existingChats = repository.chatDao.getAllChatsFlow().firstOrNull() ?: emptyList()
            val matchedChat = existingChats.find { it.title == user.name && !it.isGroup && !it.isChannel }
            if (matchedChat != null) {
                selectChat(matchedChat.id)
            } else {
                val newChat = ChatEntity(
                    title = user.name,
                    isGroup = false,
                    isChannel = false,
                    avatarUrl = if (user.id == "gemini_bot") "gemini" else "contact",
                    lastMessageText = "Start messaging ${user.name}...",
                    lastMessageTime = System.currentTimeMillis()
                )
                val newId = repository.chatDao.insertChat(newChat).toInt()
                selectChat(newId)
            }
            selectTab("chats")
        }
    }

    fun createGroupChat(name: String, members: List<UserEntity>) {
        viewModelScope.launch {
            val newChat = ChatEntity(
                title = name,
                isGroup = true,
                isChannel = false,
                avatarUrl = "group",
                lastMessageText = "Group created by Ayush. Say hello!",
                lastMessageTime = System.currentTimeMillis()
            )
            val id = repository.chatDao.insertChat(newChat).toInt()
            repository.messageDao.insertMessage(
                MessageEntity(
                    chatId = id,
                    senderId = "self_user",
                    senderName = "Ayush Kumar",
                    type = "text",
                    content = "Welcome to $name group!"
                )
            )
            selectChat(id)
            selectTab("chats")
        }
    }

    fun createChannel(name: String, desc: String) {
        viewModelScope.launch {
            val newChat = ChatEntity(
                title = name,
                isGroup = false,
                isChannel = true,
                avatarUrl = "channel",
                lastMessageText = "Channel created: $desc",
                lastMessageTime = System.currentTimeMillis()
            )
            val id = repository.chatDao.insertChat(newChat).toInt()
            repository.messageDao.insertMessage(
                MessageEntity(
                    chatId = id,
                    senderId = "self_user",
                    senderName = "Ayush Kumar",
                    type = "text",
                    content = "Channel description: $desc"
                )
            )
            selectChat(id)
            selectTab("chats")
        }
    }

    fun addStory(caption: String, gradient: String) {
        viewModelScope.launch {
            repository.insertStory(caption, gradient)
        }
    }

    fun markStoryAsViewed(storyId: Int) {
        viewModelScope.launch {
            repository.markStoryAsViewed(storyId)
        }
    }

    fun startCall(contactName: String, avatar: String, isVideo: Boolean) {
        val type = if (isVideo) "video" else "voice"
        val mockCall = CallEntity(
            contactName = contactName,
            contactAvatar = avatar,
            type = type,
            isIncoming = false,
            timestamp = System.currentTimeMillis(),
            duration = "0s",
            status = "completed"
        )
        _activeCall.value = mockCall
        viewModelScope.launch {
            repository.makeCall(contactName, avatar, type)
        }
    }

    fun endCall() {
        _activeCall.value = null
    }

    fun openStoryViewer(userId: String?) {
        _viewingStoryUser.value = userId
    }

    fun updateSelfProfile(name: String, username: String, bio: String) {
        viewModelScope.launch {
            val current = repository.userDao.getSelfUser()
            if (current != null) {
                repository.userDao.updateUser(current.copy(name = name, username = username, bio = bio))
            }
        }
    }

    fun openSnap(msgId: Int) {
        viewModelScope.launch {
            repository.markSnapAsOpened(msgId)
        }
    }

    // --- AI Operations ---

    fun generateSmartRepliesForLastMessage() {
        val msgs = activeMessages.value
        val lastMsg = msgs.lastOrNull { it.senderId != "self_user" } ?: return
        viewModelScope.launch {
            _smartReplies.value = repository.generateSmartReplies(lastMsg.content)
        }
    }

    fun summarizeActiveChat() {
        val msgs = activeMessages.value
        if (msgs.isEmpty()) return
        _isAILoading.value = true
        viewModelScope.launch {
            _summaryText.value = repository.summarizeGroupChat(msgs)
            _isAILoading.value = false
        }
    }

    fun clearSummary() {
        _summaryText.value = null
    }

    fun generateAIMediaCaption(topic: String, onResult: (String) -> Unit) {
        _isAILoading.value = true
        viewModelScope.launch {
            val caption = repository.generateStoryCaption(topic)
            onResult(caption)
            _isAILoading.value = false
        }
    }
}
