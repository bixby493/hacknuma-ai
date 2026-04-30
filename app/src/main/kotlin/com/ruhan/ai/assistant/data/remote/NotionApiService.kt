package com.ruhan.ai.assistant.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotionApiService {

    @POST("v1/pages")
    suspend fun createPage(
        @Header("Authorization") authorization: String,
        @Header("Notion-Version") version: String = "2022-06-28",
        @Body request: NotionPageRequest
    ): Response<NotionPageResponse>

    @POST("v1/databases/{database_id}/query")
    suspend fun queryDatabase(
        @Header("Authorization") authorization: String,
        @Header("Notion-Version") version: String = "2022-06-28",
        @Path("database_id") databaseId: String,
        @Body request: NotionQueryRequest = NotionQueryRequest()
    ): Response<NotionQueryResponse>
}

data class NotionPageRequest(
    val parent: NotionParent,
    val properties: Map<String, NotionProperty>,
    val children: List<NotionBlock>? = null
)

data class NotionParent(
    @SerializedName("database_id")
    val databaseId: String
)

data class NotionProperty(
    val title: List<NotionRichText>? = null,
    @SerializedName("rich_text")
    val richText: List<NotionRichText>? = null,
    val select: NotionSelect? = null
)

data class NotionRichText(
    val text: NotionTextContent
)

data class NotionTextContent(
    val content: String
)

data class NotionSelect(
    val name: String
)

data class NotionBlock(
    val type: String = "paragraph",
    @SerializedName("object")
    val obj: String = "block",
    val paragraph: NotionParagraph? = null,
    val heading_2: NotionHeading? = null
)

data class NotionParagraph(
    @SerializedName("rich_text")
    val richText: List<NotionRichText>
)

data class NotionHeading(
    @SerializedName("rich_text")
    val richText: List<NotionRichText>
)

data class NotionPageResponse(
    val id: String?,
    val url: String?
)

data class NotionQueryRequest(
    @SerializedName("page_size")
    val pageSize: Int = 10
)

data class NotionQueryResponse(
    val results: List<NotionPageResult>?
)

data class NotionPageResult(
    val id: String?,
    val properties: Map<String, Any>?
)
