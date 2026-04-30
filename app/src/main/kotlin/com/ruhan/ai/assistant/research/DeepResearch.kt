package com.ruhan.ai.assistant.research

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.ruhan.ai.assistant.data.remote.TavilyApiService
import com.ruhan.ai.assistant.data.remote.TavilyRequest
import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.premium.NotionManager
import com.ruhan.ai.assistant.util.PreferencesManager
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
    private val researchDao: ResearchDao,
    private val tavilyApiService: TavilyApiService,
    private val preferencesManager: PreferencesManager,
    private val notionManager: NotionManager
) {
    suspend fun research(topic: String): String {
        val webResults = fetchWebSources(topic)

        val prompt = buildResearchPrompt(topic, webResults)
        val report = try { aiRepository.chat(prompt) } catch (_: Exception) { "Boss, research complete nahi ho payi. Internet check karo." }

        try {
            researchDao.insert(
                ResearchEntity(
                    topic = topic,
                    summary = report.take(200),
                    findings = report,
                    sources = webResults?.take(500) ?: "",
                    confidence = calculateConfidence(webResults, report)
                )
            )
        } catch (_: Exception) { }

        if (notionManager.isConfigured()) {
            try {
                val notionResult = notionManager.saveResearchReport(topic, report)
                return "$report\n\n$notionResult"
            } catch (_: Exception) { }
        }

        return report
    }

    private suspend fun fetchWebSources(topic: String): String? {
        if (!preferencesManager.hasTavilyKey()) {
            return aiRepository.searchWeb(topic)
        }

        return try {
            val response = tavilyApiService.search(
                TavilyRequest(
                    apiKey = preferencesManager.tavilyApiKey,
                    query = topic,
                    searchDepth = "advanced",
                    maxResults = 8,
                    includeAnswer = true
                )
            )

            val answer = response.answer?.let { "Direct Answer: $it\n\n" } ?: ""
            val sources = response.results?.mapIndexed { i, r ->
                "Source ${i + 1}: ${r.title}\nURL: ${r.url}\nContent: ${r.content}"
            }?.joinToString("\n\n") ?: ""

            answer + sources
        } catch (_: Exception) {
            aiRepository.searchWeb(topic)
        }
    }

    private fun buildResearchPrompt(topic: String, webResults: String?): String {
        return """You are RUHAN's research engine. Create a comprehensive research report on: "$topic"

${if (webResults != null) "WEB SOURCES:\n$webResults" else "No web sources available. Use your knowledge."}

FORMAT YOUR REPORT EXACTLY LIKE THIS:

📋 EXECUTIVE SUMMARY
(3 concise lines summarizing the key finding)

🔑 KEY FINDINGS
1. [Most important finding]
2. [Second finding]
3. [Third finding]
4. [Fourth finding]
5. [Fifth finding]

📊 DETAILED ANALYSIS
(200-300 words of in-depth analysis)

📎 SOURCES
(List URLs if available)

🎯 CONFIDENCE: [HIGH/MEDIUM/LOW]

Keep language Hinglish. Be authoritative and precise. Boss ko impress karna hai."""
    }

    private fun calculateConfidence(webResults: String?, report: String): Int {
        var score = 50
        if (webResults != null) score += 20
        if (webResults != null && webResults.length > 500) score += 10
        if (report.length > 300) score += 10
        if (report.contains("Source") || report.contains("http")) score += 10
        return score.coerceAtMost(100)
    }

    suspend fun getHistory(): List<ResearchEntity> = researchDao.getAll()
}
