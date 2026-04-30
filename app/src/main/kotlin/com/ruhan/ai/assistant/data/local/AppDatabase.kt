package com.ruhan.ai.assistant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ruhan.ai.assistant.brain.MemoryDao
import com.ruhan.ai.assistant.brain.MemoryEntity
import com.ruhan.ai.assistant.brain.WorkflowDao
import com.ruhan.ai.assistant.brain.WorkflowEntity
import com.ruhan.ai.assistant.premium.NoteDao
import com.ruhan.ai.assistant.premium.NoteEntity
import com.ruhan.ai.assistant.research.DocumentDao
import com.ruhan.ai.assistant.research.DocumentEntity
import com.ruhan.ai.assistant.research.ResearchDao
import com.ruhan.ai.assistant.research.ResearchEntity

@Database(
    entities = [
        ConversationEntity::class,
        MemoryEntity::class,
        WorkflowEntity::class,
        NoteEntity::class,
        ResearchEntity::class,
        DocumentEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun memoryDao(): MemoryDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun noteDao(): NoteDao
    abstract fun researchDao(): ResearchDao
    abstract fun documentDao(): DocumentDao
}
