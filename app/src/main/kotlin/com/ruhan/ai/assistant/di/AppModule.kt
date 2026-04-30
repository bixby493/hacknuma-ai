package com.ruhan.ai.assistant.di

import android.content.Context
import androidx.room.Room
import com.ruhan.ai.assistant.brain.MemoryDao
import com.ruhan.ai.assistant.brain.WorkflowDao
import com.ruhan.ai.assistant.data.local.AppDatabase
import com.ruhan.ai.assistant.data.local.ConversationDao
import com.ruhan.ai.assistant.premium.NoteDao
import com.ruhan.ai.assistant.research.DocumentDao
import com.ruhan.ai.assistant.research.ResearchDao
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ruhan_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    @Singleton
    fun provideMemoryDao(database: AppDatabase): MemoryDao {
        return database.memoryDao()
    }

    @Provides
    @Singleton
    fun provideWorkflowDao(database: AppDatabase): WorkflowDao {
        return database.workflowDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideResearchDao(database: AppDatabase): ResearchDao {
        return database.researchDao()
    }

    @Provides
    @Singleton
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
