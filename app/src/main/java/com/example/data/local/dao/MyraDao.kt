package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ClipboardEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.FileEntity
import com.example.data.local.entities.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyraDao {
    // --- AI Memories ---
    @Query("SELECT * FROM ai_memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM ai_memories")
    suspend fun getAllMemoriesDirect(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Query("DELETE FROM ai_memories WHERE id = :id")
    suspend fun deleteMemory(id: Int)

    @Query("DELETE FROM ai_memories")
    suspend fun clearAllMemories()

    // --- Conversations ---
    @Query("SELECT * FROM conversations ORDER BY timestamp ASC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentConversations(limit: Int): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations")
    suspend fun clearConversations()

    // --- Files ---
    @Query("SELECT * FROM local_files ORDER BY updatedAt DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Query("DELETE FROM local_files WHERE id = :id")
    suspend fun deleteFile(id: Int)

    @Query("SELECT * FROM local_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Int): FileEntity?

    // --- Clipboard ---
    @Query("SELECT * FROM clipboard_history ORDER BY copiedAt DESC")
    fun getClipboardHistory(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboard(item: ClipboardEntity)

    @Query("DELETE FROM clipboard_history WHERE id = :id")
    suspend fun deleteClipboard(id: Int)

    @Query("DELETE FROM clipboard_history")
    suspend fun clearClipboardHistory()
}
