package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val content: String,
    val fileType: String = "TXT", // TXT, LOG, NOTE, CONFIG
    val sizeBytes: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
