package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_history")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val copiedAt: Long = System.currentTimeMillis()
)
