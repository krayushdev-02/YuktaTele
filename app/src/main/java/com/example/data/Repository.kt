package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Retrofit API Interface ---

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content?)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class YuktaRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val chatDao = database.chatDao()
    val messageDao = database.messageDao()
    val userDao = database.userDao()
    val storyDao = database.storyDao()
    val callDao = database.callDao()

    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChatsFlow()
    val allCalls: Flow<List<CallEntity>> = callDao.getAllCallsFlow()

    fun getMessagesForChat(chatId: Int): Flow<List<MessageEntity>> =
        messageDao.getMessagesForChatFlow(chatId)

    fun getActiveStories(): Flow<List<StoryEntity>> =
        storyDao.getActiveStoriesFlow(System.currentTimeMillis())

    init {
        // Pre-populate data asynchronously on first run
        CoroutineScope(Dispatchers.IO).launch {
            try {
                prepopulateDatabase()
            } catch (e: Exception) {
                Log.e("YuktaRepository", "Error prepopulating database", e)
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        val currentSelf = userDao.getSelfUser()
        if (currentSelf == null) {
            // Create Self user
            userDao.insertUser(
                UserEntity(
                    id = "self_user",
                    username = "ayush_k",
                    name = "Ayush Kumar",
                    avatarUrl = "https://example.com/avatar_self.png",
                    bio = "Building YuktaTele 📱 | Tech Enthusiast",
                    isOnline = true,
                    lastSeen = System.currentTimeMillis(),
                    isSelf = true
                )
            )

            // Create some Contacts
            val contacts = listOf(
                UserEntity("gemini_bot", "gemini_ai", "Gemini AI Assistant 🤖", "", "YuktaTele AI bot. Ask me anything!", true, System.currentTimeMillis(), streakCount = 5),
                UserEntity("sneha", "sneha_r", "Sneha Reddy 🌸", "", "Live, Laugh, Code", true, System.currentTimeMillis(), streakCount = 12),
                UserEntity("vikram", "vikram_s", "Vikram Singh ⚡", "", "Always on the move", false, System.currentTimeMillis() - 45 * 60 * 1000, streakCount = 0),
                UserEntity("ria", "ria_p", "Ria Patel ✨", "", "Coffee & Creativity ☕🎨", true, System.currentTimeMillis(), streakCount = 8)
            )
            contacts.forEach { userDao.insertUser(it) }

            // Create some Chats
            val chat1 = ChatEntity(
                title = "Gemini AI Assistant 🤖",
                isGroup = false,
                isChannel = false,
                avatarUrl = "gemini",
                lastMessageText = "Hello Ayush! I can generate smart replies, summarize chats, and chat with you here.",
                lastMessageTime = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
                unreadCount = 1,
                pinned = true
            )
            val id1 = chatDao.insertChat(chat1).toInt()

            val chat2 = ChatEntity(
                title = "Sneha Reddy 🌸",
                isGroup = false,
                isChannel = false,
                avatarUrl = "sneha",
                lastMessageText = "Are you joining the tech talk tomorrow?",
                lastMessageTime = System.currentTimeMillis() - 50 * 60 * 1000,
                unreadCount = 0
            )
            val id2 = chatDao.insertChat(chat2).toInt()

            val chat3 = ChatEntity(
                title = "Yukta Developers 💻",
                isGroup = true,
                isChannel = false,
                avatarUrl = "group",
                lastMessageText = "Vikram: I updated the UI branch. Please check it.",
                lastMessageTime = System.currentTimeMillis() - 15 * 60 * 1000,
                unreadCount = 2
            )
            val id3 = chatDao.insertChat(chat3).toInt()

            val chat4 = ChatEntity(
                title = "Yukta Official Channel 📣",
                isGroup = false,
                isChannel = true,
                avatarUrl = "channel",
                lastMessageText = "Welcome to YuktaTele! Check out the Snapchat-style disappearing stories above!",
                lastMessageTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                unreadCount = 0
            )
            val id4 = chatDao.insertChat(chat4).toInt()

            // Insert initial messages
            messageDao.insertMessage(MessageEntity(chatId = id1, senderId = "gemini_bot", senderName = "Gemini AI", type = "text", content = "Hello Ayush! I can generate smart replies, summarize chats, and chat with you here.", timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000))
            
            messageDao.insertMessage(MessageEntity(chatId = id2, senderId = "sneha", senderName = "Sneha Reddy", type = "text", content = "Hey, did you see the new Compose updates?", timestamp = System.currentTimeMillis() - 60 * 60 * 1000))
            messageDao.insertMessage(MessageEntity(chatId = id2, senderId = "self_user", senderName = "Ayush Kumar", type = "text", content = "Yes! The type-safe navigation is amazing.", timestamp = System.currentTimeMillis() - 55 * 60 * 1000))
            messageDao.insertMessage(MessageEntity(chatId = id2, senderId = "sneha", senderName = "Sneha Reddy", type = "text", content = "Are you joining the tech talk tomorrow?", timestamp = System.currentTimeMillis() - 50 * 60 * 1000))

            messageDao.insertMessage(MessageEntity(chatId = id3, senderId = "vikram", senderName = "Vikram Singh", type = "text", content = "Hello team, let's deploy the alpha build today.", timestamp = System.currentTimeMillis() - 30 * 60 * 1000))
            messageDao.insertMessage(MessageEntity(chatId = id3, senderId = "ria", senderName = "Ria Patel", type = "text", content = "Sure! I've finished the onboarding screen animations.", timestamp = System.currentTimeMillis() - 25 * 60 * 1000))
            messageDao.insertMessage(MessageEntity(chatId = id3, senderId = "vikram", senderName = "Vikram Singh", type = "text", content = "Vikram: I updated the UI branch. Please check it.", timestamp = System.currentTimeMillis() - 15 * 60 * 1000))

            messageDao.insertMessage(MessageEntity(chatId = id4, senderId = "self_user", senderName = "Ayush Kumar", type = "text", content = "Welcome to YuktaTele! Check out the Snapchat-style disappearing stories above!", timestamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000))

            // Add some initial Call Logs
            callDao.insertCall(CallEntity(contactName = "Sneha Reddy 🌸", contactAvatar = "sneha", type = "video", isIncoming = true, timestamp = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, duration = "4m 23s", status = "completed"))
            callDao.insertCall(CallEntity(contactName = "Vikram Singh ⚡", contactAvatar = "vikram", type = "voice", isIncoming = false, timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000, duration = "0s", status = "missed"))
            callDao.insertCall(CallEntity(contactName = "Ria Patel ✨", contactAvatar = "ria", type = "voice", isIncoming = true, timestamp = System.currentTimeMillis() - 10 * 60 * 1000, duration = "12m 45s", status = "completed"))

            // Add some initial active stories
            storyDao.insertStory(StoryEntity(userId = "sneha", userName = "Sneha Reddy", userAvatar = "sneha", mediaUrl = "gradient_sunset", caption = "Beautiful sunset today! 🌇✨"))
            storyDao.insertStory(StoryEntity(userId = "ria", userName = "Ria Patel", userAvatar = "ria", mediaUrl = "gradient_blue", caption = "Coffee + Code = Morning ritual ☕💻"))
        }
    }

    // --- Core Functions ---

    suspend fun sendMessage(chatId: Int, content: String, type: String = "text", viewOnce: Boolean = false) {
        val self = userDao.getSelfUser() ?: return
        val message = MessageEntity(
            chatId = chatId,
            senderId = self.id,
            senderName = self.name,
            type = type,
            content = content,
            timestamp = System.currentTimeMillis(),
            viewOnce = viewOnce,
            isRead = true
        )
        messageDao.insertMessage(message)
        chatDao.updateLastMessage(chatId, "You: $content", System.currentTimeMillis(), 0)

        // Trigger automatic reply scenario
        val chat = chatDao.getChatById(chatId) ?: return
        if (!chat.isChannel) {
            triggerAutomatedReply(chat, content)
        }
    }

    suspend fun clearUnread(chatId: Int) {
        chatDao.clearUnreadCount(chatId)
        val self = userDao.getSelfUser()
        if (self != null) {
            messageDao.markMessagesAsRead(chatId, self.id)
        }
    }

    suspend fun togglePin(chatId: Int) {
        val chat = chatDao.getChatById(chatId) ?: return
        chatDao.updateChat(chat.copy(pinned = !chat.pinned))
    }

    suspend fun markSnapAsOpened(msgId: Int) {
        messageDao.markSnapAsOpened(msgId)
    }

    suspend fun insertStory(caption: String, gradientName: String) {
        val self = userDao.getSelfUser() ?: return
        val story = StoryEntity(
            userId = self.id,
            userName = self.name,
            userAvatar = "self",
            mediaUrl = gradientName,
            caption = caption,
            timestamp = System.currentTimeMillis()
        )
        storyDao.insertStory(story)
    }

    suspend fun markStoryAsViewed(storyId: Int) {
        storyDao.markStoryAsViewed(storyId)
    }

    suspend fun makeCall(contactName: String, avatar: String, type: String) {
        callDao.insertCall(
            CallEntity(
                contactName = contactName,
                contactAvatar = avatar,
                type = type,
                isIncoming = false,
                timestamp = System.currentTimeMillis(),
                duration = "1m 30s",
                status = "completed"
            )
        )
    }

    suspend fun simulateIncomingCall(contactName: String, avatar: String, type: String) {
        callDao.insertCall(
            CallEntity(
                contactName = contactName,
                contactAvatar = avatar,
                type = type,
                isIncoming = true,
                timestamp = System.currentTimeMillis(),
                duration = "0s",
                status = "missed"
            )
        )
    }

    // --- Gemini AI Core Methods ---

    suspend fun callGeminiAPI(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // No real key, return a mock response that describes why
            return@withContext "I am the YuktaTele AI Assistant! (To unlock real-time Gemini responses, configure your real GEMINI_API_KEY in the Secrets panel!). Here is a quick simulation answer: Let's focus on building high-fidelity chats and snappy stories!"
        }

        try {
            val req = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(prompt)))),
                systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(it))) }
            )
            val res = RetrofitClient.service.generateContent(apiKey, req)
            res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Oops! No response from Gemini."
        } catch (e: Exception) {
            Log.e("YuktaRepository", "Gemini API error", e)
            "Error calling Gemini: ${e.localizedMessage}. Please verify your network and API key configuration in the Secrets panel."
        }
    }

    suspend fun generateSmartReplies(lastMsg: String): List<String> {
        val prompt = "Generate exactly three short, friendly chat reply options for this message: \"$lastMsg\". Return only a comma-separated list like 'Option 1, Option 2, Option 3'. No other text."
        val response = callGeminiAPI(prompt, "You are a concise smart reply assistant.")
        return if (response.contains(",")) {
            response.split(",").map { it.trim().trim('\'').trim('"') }
        } else {
            listOf("Awesome!", "Sounds great!", "Tell me more!")
        }
    }

    suspend fun summarizeGroupChat(messages: List<MessageEntity>): String {
        if (messages.isEmpty()) return "No messages to summarize!"
        val messagesText = messages.takeLast(15).joinToString("\n") { "${it.senderName}: ${it.content}" }
        val prompt = "Summarize the following group conversation in a conversational 2-3 sentence 'catch me up' summary:\n$messagesText"
        return callGeminiAPI(prompt, "You are a helpful chat summary bot. Summarize concisely.")
    }

    suspend fun generateStoryCaption(topic: String): String {
        val prompt = "Generate a very short, catchy, snappy social media story caption (including emojis) on the topic: \"$topic\""
        return callGeminiAPI(prompt, "You are a creative social media editor.")
    }

    // --- Automatic Reply / Bot Response Scenario ---

    private suspend fun triggerAutomatedReply(chat: ChatEntity, userText: String) {
        // Wait 1.5s to simulate real typing
        delay(1500)

        if (chat.title.contains("Gemini")) {
            val response = callGeminiAPI(userText, "You are Gemini Assistant, the friendly built-in AI helper for YuktaTele messaging app.")
            val replyMessage = MessageEntity(
                chatId = chat.id,
                senderId = "gemini_bot",
                senderName = "Gemini AI",
                type = "text",
                content = response,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(replyMessage)
            chatDao.updateLastMessage(chat.id, "Gemini AI: $response", System.currentTimeMillis(), 1)
        } else {
            // General Simulated Response
            val (senderId, senderName) = if (chat.isGroup) {
                val members = listOf("sneha" to "Sneha Reddy", "vikram" to "Vikram Singh", "ria" to "Ria Patel")
                members.random()
            } else {
                val contactId = when {
                    chat.title.contains("Sneha") -> "sneha"
                    chat.title.contains("Vikram") -> "vikram"
                    else -> "ria"
                }
                contactId to chat.title
            }

            val replyText = when {
                userText.contains("hello", true) || userText.contains("hi", true) -> "Hey there! Hope you're having a great day!"
                userText.contains("snap", true) || userText.contains("story", true) -> "Oh! I love the snap feature on YuktaTele! Check out my stories."
                userText.contains("streak", true) -> "Yes, let's keep our snap streak going! 🔥"
                chat.isGroup -> "I totally agree with that! Let's schedule a call later to discuss details."
                else -> "That sounds very interesting! Tell me more about it."
            }

            val replyMessage = MessageEntity(
                chatId = chat.id,
                senderId = senderId,
                senderName = senderName,
                type = "text",
                content = replyText,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(replyMessage)
            chatDao.updateLastMessage(chat.id, "$senderName: $replyText", System.currentTimeMillis(), 1)
        }
    }
}
