package com.ruhan.ai.assistant.brain

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getByType(type: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE `key` LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%' ORDER BY importance DESC")
    suspend fun search(query: String): List<MemoryEntity>

    @Insert
    suspend fun insert(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun count(): Int
}

@Singleton
class MemoryManager @Inject constructor(
    private val memoryDao: MemoryDao
) {
    companion object {
        const val TYPE_FACT = "fact"
        const val TYPE_PREFERENCE = "preference"
        const val TYPE_NICKNAME = "nickname"
        const val TYPE_CONVERSATION = "conversation"
        const val TYPE_HABIT = "habit"
    }

    suspend fun remember(key: String, value: String, type: String = TYPE_FACT, importance: Int = 1) {
        memoryDao.insert(
            MemoryEntity(
                type = type,
                key = key,
                value = value,
                importance = importance
            )
        )
    }

    suspend fun rememberFact(fact: String) {
        remember(key = fact, value = fact, type = TYPE_FACT, importance = 2)
    }

    suspend fun rememberNickname(nickname: String, realName: String) {
        remember(
            key = nickname.lowercase(),
            value = realName,
            type = TYPE_NICKNAME,
            importance = 3
        )
    }

    suspend fun rememberPreference(key: String, value: String) {
        remember(key = key, value = value, type = TYPE_PREFERENCE, importance = 2)
    }

    suspend fun saveConversationSummary(summary: String) {
        remember(key = "conversation_summary", value = summary, type = TYPE_CONVERSATION)
    }

    suspend fun resolveNickname(name: String): String? {
        val results = memoryDao.search(name.lowercase())
        return results.firstOrNull { it.type == TYPE_NICKNAME }?.value
    }

    suspend fun getAllMemories(): List<MemoryEntity> = memoryDao.getAllMemories()

    suspend fun searchMemories(query: String): List<MemoryEntity> = memoryDao.search(query)

    suspend fun getMemorySummary(): String {
        val memories = memoryDao.getAllMemories()
        if (memories.isEmpty()) return "Abhi tak koi special memory nahi hai, Boss."

        val sb = StringBuilder("Boss, mujhe yeh yaad hai:\n\n")
        val grouped = memories.groupBy { it.type }

        grouped[TYPE_FACT]?.let { facts ->
            sb.appendLine("Facts:")
            facts.take(10).forEach { sb.appendLine("• ${it.value}") }
        }
        grouped[TYPE_NICKNAME]?.let { nicks ->
            sb.appendLine("\nNicknames:")
            nicks.forEach { sb.appendLine("• ${it.key} = ${it.value}") }
        }
        grouped[TYPE_PREFERENCE]?.let { prefs ->
            sb.appendLine("\nPreferences:")
            prefs.forEach { sb.appendLine("• ${it.key}: ${it.value}") }
        }

        return sb.toString()
    }

    suspend fun clearAllMemories() = memoryDao.clearAll()

    suspend fun parseAndStore(text: String): Boolean {
        val lower = text.lowercase()
        return when {
            lower.contains("yaad rakho") || lower.contains("remember") -> {
                val fact = text
                    .replace(Regex("(ruhan |yaad rakho |remember |ki )", RegexOption.IGNORE_CASE), "")
                    .trim()
                if (fact.isNotBlank()) {
                    rememberFact(fact)
                    true
                } else false
            }

            lower.contains("matlab") || lower.contains("means") -> {
                val parts = text.split(Regex("(matlab|means)", RegexOption.IGNORE_CASE))
                if (parts.size == 2) {
                    rememberNickname(parts[0].trim(), parts[1].trim())
                    true
                } else false
            }

            lower.contains("mera") && lower.contains("hai") -> {
                rememberFact(text)
                true
            }

            else -> false
        }
    }
}
