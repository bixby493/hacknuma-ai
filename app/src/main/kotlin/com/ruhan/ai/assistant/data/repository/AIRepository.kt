package com.ruhan.ai.assistant.data.repository

import com.ruhan.ai.assistant.data.local.ConversationEntity
import com.ruhan.ai.assistant.data.remote.GeminiApiService
import com.ruhan.ai.assistant.data.remote.GeminiContent
import com.ruhan.ai.assistant.data.remote.GeminiInlineData
import com.ruhan.ai.assistant.data.remote.GeminiPart
import com.ruhan.ai.assistant.data.remote.GeminiRequest
import com.ruhan.ai.assistant.data.remote.GroqApiService
import com.ruhan.ai.assistant.data.remote.GroqMessage
import com.ruhan.ai.assistant.data.remote.GroqRequest
import com.ruhan.ai.assistant.data.remote.TavilyApiService
import com.ruhan.ai.assistant.data.remote.TavilyRequest
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val groqApiService: GroqApiService,
    private val geminiApiService: GeminiApiService,
    private val tavilyApiService: TavilyApiService,
    private val huggingFaceApiService: com.ruhan.ai.assistant.data.remote.HuggingFaceApiService,
    private val preferencesManager: PreferencesManager
) {

    private fun buildSystemPrompt(): String {
        val bossName = preferencesManager.bossName
        val langInstruction = when (preferencesManager.language) {
            "hindi" -> "Speak in pure Hindi only."
            "english" -> "Speak in pure English only."
            else -> "Speak in Hinglish (mix of Hindi and English)."
        }
        val timeStr = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            .format(java.util.Date())
        val dateStr = java.text.SimpleDateFormat("dd MMMM yyyy, EEEE", java.util.Locale("hi", "IN"))
            .format(java.util.Date())

        return """You are RUHAN, a Jarvis-like AI assistant running on Android. $langInstruction
IDENTITY: You are Ruhan. Never say you are an AI model/LLM. Be calm, confident, intelligent like Jarvis. Always call user '$bossName'.
CONTEXT: Time=$timeStr, Date=$dateStr, Platform=Android.
RULES: Keep replies SHORT (2-3 sentences max unless detail asked). You can control $bossName's phone — calls, SMS, apps, settings, everything.
If $bossName asks to remember something, confirm you've saved it. If asked what you know, share stored memories.
For complex requests, break them down and explain what you're doing step by step."""
    }

    suspend fun chat(
        userMessage: String,
        conversationHistory: List<ConversationEntity> = emptyList()
    ): String {
        if (!preferencesManager.hasGroqKey()) {
            return "Boss, pehle Settings mein jaake Groq API key set karo. Bina uske main soch nahi sakta."
        }
        return try {
            val messages = mutableListOf<GroqMessage>()
            messages.add(GroqMessage(role = "system", content = buildSystemPrompt()))
            conversationHistory.reversed().forEach { msg ->
                messages.add(
                    GroqMessage(
                        role = if (msg.isUser) "user" else "assistant",
                        content = msg.message
                    )
                )
            }
            messages.add(GroqMessage(role = "user", content = userMessage))

            val response = groqApiService.chatCompletion(
                authHeader = "Bearer ${preferencesManager.groqApiKey}",
                request = GroqRequest(messages = messages)
            )
            response.choices.firstOrNull()?.message?.content
                ?: "Boss, kuch samajh nahi aaya. Dobara bolo?"
        } catch (e: Exception) {
            "Boss, abhi connection mein problem hai. Thodi der mein try karo."
        }
    }

    suspend fun analyzeImage(base64Image: String, prompt: String = "Describe what you see on this screen in Hinglish. Be concise."): String {
        if (!preferencesManager.hasGeminiKey()) {
            return "Boss, screen dekhne ke liye Gemini API key chahiye. Settings mein set karo."
        }
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                )
            )
            val response = geminiApiService.generateContent(
                apiKey = preferencesManager.geminiApiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Boss, screen samajh nahi aaya."
        } catch (e: Exception) {
            "Boss, screen analyze karne mein dikkat aa rahi hai."
        }
    }

    suspend fun webSearch(query: String): String {
        if (!preferencesManager.hasTavilyKey()) {
            return chat("Web search karo: $query")
        }
        return try {
            val response = tavilyApiService.search(
                TavilyRequest(
                    apiKey = preferencesManager.tavilyApiKey,
                    query = query
                )
            )
            val results = response.results?.take(3)?.joinToString("\n") { result ->
                "${result.title}: ${result.content}"
            } ?: "Kuch nahi mila"

            chat("User ne yeh search kiya: '$query'. Web results: $results. Iska short summary Hinglish mein do.")
        } catch (e: Exception) {
            chat(query)
        }
    }

    suspend fun testGroqKey(apiKey: String): Boolean {
        return try {
            val response = groqApiService.chatCompletion(
                authHeader = "Bearer $apiKey",
                request = GroqRequest(
                    messages = listOf(GroqMessage(role = "user", content = "Say hi")),
                    maxTokens = 10
                )
            )
            response.choices.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun testGeminiKey(apiKey: String): Boolean {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "Say hi"))
                    )
                )
            )
            val response = geminiApiService.generateContent(apiKey = apiKey, request = request)
            response.candidates != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun testHuggingFaceKey(apiKey: String): Boolean {
        return try {
            val response = huggingFaceApiService.textToSpeech(
                authHeader = "Bearer $apiKey",
                request = com.ruhan.ai.assistant.data.remote.HuggingFaceRequest(inputs = "test")
            )
            response.contentLength() > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun testTavilyKey(apiKey: String): Boolean {
        return try {
            val response = tavilyApiService.search(
                TavilyRequest(
                    apiKey = apiKey,
                    query = "test",
                    maxResults = 1
                )
            )
            response.results != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun searchWeb(query: String): String? {
        return try {
            val result = webSearch(query)
            result
        } catch (_: Exception) {
            null
        }
    }
}
