package com.ruhan.ai.assistant.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface TavilyApiService {

    @POST("search")
    suspend fun search(
        @Body request: TavilyRequest
    ): TavilyResponse
}

data class TavilyRequest(
    @SerializedName("api_key")
    val apiKey: String,
    val query: String,
    @SerializedName("search_depth")
    val searchDepth: String = "basic",
    @SerializedName("max_results")
    val maxResults: Int = 3
)

data class TavilyResponse(
    val results: List<TavilyResult>?
)

data class TavilyResult(
    val title: String?,
    val content: String?,
    val url: String?
)
