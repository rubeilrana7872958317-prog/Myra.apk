package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.MyraDao
import com.example.data.local.entities.ClipboardEntity
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.FileEntity
import com.example.data.local.entities.MemoryEntity

@Database(
    entities = [
        MemoryEntity::class,
        ConversationEntity::class,
        FileEntity::class,
        ClipboardEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MyraDatabase : RoomDatabase() {
    abstract fun myraDao(): MyraDao

    companion object {
        @Volatile
        private var INSTANCE: MyraDatabase? = null

        fun getDatabase(context: Context): MyraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyraDatabase::class.java,
                    "myra_assistant_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
