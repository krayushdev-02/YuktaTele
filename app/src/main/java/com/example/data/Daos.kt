package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY pinned DESC, lastMessageTime DESC")
    fun getAllChatsFlow(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    suspend fun getChatById(chatId: Int): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET lastMessageText = :text, lastMessageTime = :time, unreadCount = unreadCount + :unreadIncrement WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: Int, text: String, time: Long, unreadIncrement: Int)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun clearUnreadCount(chatId: Int)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: Int)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: Int): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isRead = 1 WHERE chatId = :chatId AND senderId != :selfId")
    suspend fun markMessagesAsRead(chatId: Int, selfId: String)

    @Query("UPDATE messages SET isOpened = 1 WHERE id = :msgId")
    suspend fun markSnapAsOpened(msgId: Int)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Int)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isSelf = 1 LIMIT 1")
    suspend fun getSelfUser(): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE expiryTime > :currentTime ORDER BY timestamp DESC")
    fun getActiveStoriesFlow(currentTime: Long): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryAsViewed(storyId: Int)

    @Query("DELETE FROM stories WHERE expiryTime <= :currentTime")
    suspend fun deleteExpiredStories(currentTime: Long)
}

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun getAllCallsFlow(): Flow<List<CallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Query("DELETE FROM calls")
    suspend fun clearCallLog()
}
