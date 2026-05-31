package com.echoflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "chat_threads")
data class ChatThread(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatThread::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class ChatMessage(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val createdAt: Long,
    val reasoning: String? = null, // reasoning/"thinking" trace for reasoning-capable models
    val localAttachmentUri: String? = null,
    val localAttachmentMimeType: String? = null,
    val localAttachmentName: String? = null
)

@Entity(tableName = "custom_models")
data class CustomModel(
    @PrimaryKey val id: String, // e.g., "google/gemini-2.0-flash"
    val name: String // e.g., "Gemini 2.0 Flash"
)
