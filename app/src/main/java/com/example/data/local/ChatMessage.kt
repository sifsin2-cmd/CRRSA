package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "BOT" or "SYSTEM"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "EN", // "EN", "AM", "OM"
    val attachmentPath: String? = null,
    val attachmentType: String? = null, // "IMAGE", "DOCUMENT"
    val categoryTag: String? = null,
    val isPinned: Boolean = false
)
