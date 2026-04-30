package com.ruhan.ai.assistant.premium

import com.ruhan.ai.assistant.data.remote.NotionApiService
import com.ruhan.ai.assistant.data.remote.NotionBlock
import com.ruhan.ai.assistant.data.remote.NotionPageRequest
import com.ruhan.ai.assistant.data.remote.NotionParagraph
import com.ruhan.ai.assistant.data.remote.NotionParent
import com.ruhan.ai.assistant.data.remote.NotionProperty
import com.ruhan.ai.assistant.data.remote.NotionRichText
import com.ruhan.ai.assistant.data.remote.NotionSelect
import com.ruhan.ai.assistant.data.remote.NotionTextContent
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotionManager @Inject constructor(
    private val notionApiService: NotionApiService,
    private val preferencesManager: PreferencesManager
) {
    private fun authHeader(): String = "Bearer ${preferencesManager.notionApiKey}"
    private fun dbId(): String = preferencesManager.notionDatabaseId

    fun isConfigured(): Boolean =
        preferencesManager.notionApiKey.isNotBlank() && preferencesManager.notionDatabaseId.isNotBlank()

    suspend fun saveResearchReport(topic: String, report: String): String {
        if (!isConfigured()) return "Notion API key ya Database ID set karo Settings mein."

        return try {
            val blocks = splitToBlocks(report)
            val request = NotionPageRequest(
                parent = NotionParent(databaseId = dbId()),
                properties = mapOf(
                    "Name" to NotionProperty(
                        title = listOf(NotionRichText(NotionTextContent("Research: $topic")))
                    ),
                    "Type" to NotionProperty(select = NotionSelect("Research")),
                    "Status" to NotionProperty(select = NotionSelect("Complete"))
                ),
                children = blocks
            )

            val response = notionApiService.createPage(authHeader(), request = request)
            if (response.isSuccessful) {
                "Research report Notion mein save ho gaya! ${response.body()?.url ?: ""}"
            } else {
                "Notion save fail: ${response.code()}"
            }
        } catch (e: Exception) {
            "Notion error: ${e.message}"
        }
    }

    suspend fun saveNote(content: String, category: String = "general"): String {
        if (!isConfigured()) return "Notion API key ya Database ID set karo Settings mein."

        return try {
            val request = NotionPageRequest(
                parent = NotionParent(databaseId = dbId()),
                properties = mapOf(
                    "Name" to NotionProperty(
                        title = listOf(NotionRichText(NotionTextContent("Note: ${content.take(50)}")))
                    ),
                    "Type" to NotionProperty(select = NotionSelect("Note")),
                    "Status" to NotionProperty(select = NotionSelect(category))
                ),
                children = listOf(
                    NotionBlock(
                        type = "paragraph",
                        paragraph = NotionParagraph(
                            richText = listOf(NotionRichText(NotionTextContent(content)))
                        )
                    )
                )
            )

            val response = notionApiService.createPage(authHeader(), request = request)
            if (response.isSuccessful) {
                "Note Notion mein bhi save ho gaya!"
            } else {
                "Notion save fail: ${response.code()}"
            }
        } catch (e: Exception) {
            "Notion error: ${e.message}"
        }
    }

    suspend fun saveMemory(key: String, value: String): String {
        if (!isConfigured()) return ""

        return try {
            val request = NotionPageRequest(
                parent = NotionParent(databaseId = dbId()),
                properties = mapOf(
                    "Name" to NotionProperty(
                        title = listOf(NotionRichText(NotionTextContent("Memory: $key")))
                    ),
                    "Type" to NotionProperty(select = NotionSelect("Memory"))
                ),
                children = listOf(
                    NotionBlock(
                        type = "paragraph",
                        paragraph = NotionParagraph(
                            richText = listOf(NotionRichText(NotionTextContent(value)))
                        )
                    )
                )
            )

            val response = notionApiService.createPage(authHeader(), request = request)
            if (response.isSuccessful) "Memory Notion mein save." else ""
        } catch (_: Exception) { "" }
    }

    suspend fun testConnection(): Boolean {
        if (!isConfigured()) return false
        return try {
            val response = notionApiService.queryDatabase(authHeader(), databaseId = dbId())
            response.isSuccessful
        } catch (_: Exception) { false }
    }

    private fun splitToBlocks(text: String): List<NotionBlock> {
        return text.split("\n\n").filter { it.isNotBlank() }.map { paragraph ->
            if (paragraph.length <= 2000) {
                NotionBlock(
                    type = "paragraph",
                    paragraph = NotionParagraph(
                        richText = listOf(NotionRichText(NotionTextContent(paragraph)))
                    )
                )
            } else {
                NotionBlock(
                    type = "paragraph",
                    paragraph = NotionParagraph(
                        richText = listOf(NotionRichText(NotionTextContent(paragraph.take(2000))))
                    )
                )
            }
        }
    }
}
