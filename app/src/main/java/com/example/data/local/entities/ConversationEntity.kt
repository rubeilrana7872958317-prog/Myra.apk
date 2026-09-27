package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sender: String, // USER, MYRA
    val message: String,
    val actionType: String = "CHAT", // CHAT, VOICE, COMMAND, SYSTEM
    val language: String = "en", // bn, hi, en
    val timestamp: Long = System.currentTimeMillis()
)
