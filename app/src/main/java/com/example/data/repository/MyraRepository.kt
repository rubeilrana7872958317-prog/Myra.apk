package com.example.data.repository

import com.example.data.local.dao.MyraDao
import com.example.data.local.entities.ClipboardEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.FileEntity
import com.example.data.local.entities.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MyraRepository(private val dao: MyraDao) {
    val memories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    val conversations: Flow<List<ConversationEntity>> = dao.getAllConversations()
    val files: Flow<List<FileEntity>> = dao.getAllFiles()
    val clipboardHistory: Flow<List<ClipboardEntity>> = dao.getClipboardHistory()

    suspend fun getAllMemoriesDirect(): List<MemoryEntity> = dao.getAllMemoriesDirect()
    suspend fun saveMemory(key: String, value: String, category: String = "GENERAL") {
        dao.insertMemory(MemoryEntity(key = key, value = value, category = category))
    }
    suspend fun deleteMemory(id: Int) = dao.deleteMemory(id)
    suspend fun clearMemories() = dao.clearAllMemories()

    suspend fun saveConversation(sender: String, message: String, actionType: String = "CHAT", language: String = "en") {
        dao.insertConversation(
            ConversationEntity(
                sender = sender,
                message = message,
                actionType = actionType,
                language = language
            )
        )
    }
    suspend fun getRecentConversations(limit: Int = 10) = dao.getRecentConversations(limit)
    suspend fun clearConversations() = dao.clearConversations()

    suspend fun saveFile(fileName: String, content: String, type: String = "TXT") {
        val size = content.toByteArray().size.toLong()
        dao.insertFile(FileEntity(fileName = fileName, content = content, fileType = type, sizeBytes = size))
    }
    suspend fun deleteFile(id: Int) = dao.deleteFile(id)

    suspend fun saveClipboard(content: String) {
        if (content.isNotBlank()) {
            dao.insertClipboard(ClipboardEntity(content = content.trim()))
        }
    }
    suspend fun deleteClipboard(id: Int) = dao.deleteClipboard(id)
    suspend fun clearClipboard() = dao.clearClipboardHistory()
}
