package com.ruhan.ai.assistant.research

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.ruhan.ai.assistant.data.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val type: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    suspend fun getAll(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<DocumentEntity>

    @Insert
    suspend fun insert(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun count(): Int
}

@Singleton
class RagOracle @Inject constructor(
    private val documentDao: DocumentDao,
    private val aiRepository: AIRepository
) {
    suspend fun addDocument(title: String, content: String, type: String = "text") {
        documentDao.insert(DocumentEntity(title = title, content = content, type = type))
    }

    suspend fun searchDocuments(query: String): String {
        val results = documentDao.search(query)
        if (results.isEmpty()) {
            return "Boss, documents mein '$query' nahi mila."
        }

        val context = results.take(3).joinToString("\n---\n") {
            "Document: ${it.title}\n${it.content.take(1000)}"
        }

        val prompt = """
            Based on these documents, answer in Hinglish:
            
            $context
            
            Question: $query
            
            Give a concise, helpful answer based only on the documents above.
        """.trimIndent()

        return aiRepository.chat(prompt) ?: "Boss, document analysis nahi ho payi."
    }

    suspend fun getAllDocuments(): List<DocumentEntity> = documentDao.getAll()

    suspend fun getDocumentCount(): Int = documentDao.count()
}
