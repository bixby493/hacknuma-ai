package com.ruhan.ai.assistant.screen

import com.ruhan.ai.assistant.data.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenAnalyzer @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend fun analyzeImage(base64Image: String): String {
        return aiRepository.analyzeImage(
            base64Image,
            "Describe what's on this Android screen in Hinglish. Be concise. " +
                    "Identify any text, buttons, app name, and key content."
        ) ?: "Boss, screen analyze nahi ho payi."
    }

    suspend fun extractText(base64Image: String): String {
        return aiRepository.analyzeImage(
            base64Image,
            "Extract ALL text visible on this screen. Return only the text content, nothing else."
        ) ?: "Boss, text extract nahi ho paya."
    }

    suspend fun identifyFormFields(base64Image: String): String {
        return aiRepository.analyzeImage(
            base64Image,
            "Identify all form fields, input boxes, and buttons on this screen. " +
                    "List each with its label, type (text/dropdown/checkbox), and current value if visible."
        ) ?: "Boss, form fields identify nahi ho paye."
    }
}
