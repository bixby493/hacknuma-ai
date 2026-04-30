package com.ruhan.ai.assistant.data.repository

import com.ruhan.ai.assistant.data.local.ConversationDao
import com.ruhan.ai.assistant.data.local.ConversationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao
) {

    fun getRecentConversations(limit: Int = 50): Flow<List<ConversationEntity>> {
        return conversationDao.getRecentConversations(limit)
    }

    suspend fun getContextMessages(limit: Int = 10): List<ConversationEntity> {
        return conversationDao.getRecentConversationsList(limit)
    }

    suspend fun saveMessage(message: String, isUser: Boolean) {
        conversationDao.insertMessage(
            ConversationEntity(
                message = message,
                isUser = isUser
            )
        )
    }

    suspend fun clearHistory() {
        conversationDao.clearAll()
    }
}
