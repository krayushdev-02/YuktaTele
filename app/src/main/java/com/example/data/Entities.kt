package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val name: String,
    val avatarUrl: String,
    val bio: String,
    val isOnline: Boolean,
    val lastSeen: Long,
    val isSelf: Boolean = false,
    val streakCount: Int = 0
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isGroup: Boolean,
    val isChannel: Boolean,
    val avatarUrl: String,
    val lastMessageText: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val pinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: Int,
    val senderId: String,
    val senderName: String,
    val type: String, // "text", "image", "voice", "snap", "video"
    val content: String,
    val mediaUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val viewOnce: Boolean = false,
    val isOpened: Boolean = false,
    val expiryTime: Long = 0L // Snapchat-style disappearing messages support
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val mediaUrl: String,
    val caption: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTime: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000, // 24 hours
    val isViewed: Boolean = false
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val contactAvatar: String,
    val type: String, // "voice", "video"
    val isIncoming: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val duration: String,
    val status: String // "completed", "missed", "declined"
)
