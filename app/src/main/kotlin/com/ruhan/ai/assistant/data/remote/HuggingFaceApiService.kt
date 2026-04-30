package com.ruhan.ai.assistant.data.remote

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface HuggingFaceApiService {

    @POST("models/facebook/mms-tts-hin")
    suspend fun textToSpeech(
        @Header("Authorization") authHeader: String,
        @Body request: HuggingFaceRequest
    ): ResponseBody

    @POST
    suspend fun textToSpeechCustomModel(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: HuggingFaceRequest
    ): ResponseBody
}

data class HuggingFaceRequest(
    val inputs: String
)
