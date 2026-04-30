package com.ruhan.ai.assistant.data.remote

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface HuggingFaceApiService {

    @POST("models/facebook/mms-tts-hin")
    suspend fun textToSpeech(
        @Header("Authorization") authHeader: String,
        @Body request: HuggingFaceRequest
    ): ResponseBody
}

data class HuggingFaceRequest(
    val inputs: String
)
