package com.ruhan.ai.assistant.domain.usecase

import com.ruhan.ai.assistant.data.repository.AIRepository
import javax.inject.Inject

class AnalyzeScreenUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend fun execute(base64Image: String): String {
        return aiRepository.analyzeImage(
            base64Image = base64Image,
            prompt = "You are RUHAN, an AI assistant. Describe what you see on this Android screen in Hinglish. Be concise and helpful. Call the user 'Boss'."
        )
    }
}
