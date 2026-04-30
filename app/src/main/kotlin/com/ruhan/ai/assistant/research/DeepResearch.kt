package com.ruhan.ai.assistant.research

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.ruhan.ai.assistant.data.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "research_reports")
data class ResearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val summary: String,
    val findings: String,
    val sources: String,
    val confidence: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ResearchDao {
    @Query("SELECT * FROM research_reports ORDER BY createdAt DESC")
    suspend fun getAll(): List<ResearchEntity>

    @Insert
    suspend fun insert(report: ResearchEntity)

    @Query("SELECT * FROM research_reports WHERE topic LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun search(query: String): List<ResearchEntity>

    @Query("DELETE FROM research_reports WHERE id = :id")
    suspend fun delete(id: Long)
}

@Singleton
class DeepResearch @Inject constructor(
    private val aiRepository: AIRepository,
    private val researchDao: ResearchDao
) {
    suspend fun research(topic: String): String {
        val searchResults = aiRepository.searchWeb(topic)

        val prompt = """
            You are RUHAN, a research assistant. Create a research report in Hinglish on: "$topic"
            
            ${if (searchResults != null) "Web search results:\n$searchResults" else ""}
            
            Format the report as:
            EXECUTIVE SUMMARY: (3 lines max)
            
            KEY FINDINGS:
            1. ...
            2. ...
            3. ...
            4. ...
            5. ...
            
            DETAILED ANALYSIS: (200 words)
            
            Keep language Hinglish. Be concise and informative.
        """.trimIndent()

        val report = aiRepository.chat(prompt) ?: "Boss, research complete nahi ho payi. Internet check karo."

        try {
            researchDao.insert(
                ResearchEntity(
                    topic = topic,
                    summary = report.take(200),
                    findings = report,
                    sources = searchResults?.take(500) ?: "",
                    confidence = if (searchResults != null) 85 else 60
                )
            )
        } catch (_: Exception) {
        }

        return report
    }

    suspend fun getHistory(): List<ResearchEntity> = researchDao.getAll()
}
