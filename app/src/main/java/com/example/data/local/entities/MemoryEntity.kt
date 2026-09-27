package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val key: String,
    val value: String,
    val category: String = "GENERAL", // USER_INFO, PREFERENCE, NOTE, TASK
    val updatedAt: Long = System.currentTimeMillis()
)
