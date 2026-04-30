package com.ruhan.ai.assistant.premium

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAll(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getByCategory(category: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun search(query: String): List<NoteEntity>

    @Insert
    suspend fun insert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun count(): Int
}

@Singleton
class NotesManager @Inject constructor(
    private val noteDao: NoteDao
) {
    suspend fun addNote(content: String, category: String = "general") {
        val autoCategory = categorize(content)
        noteDao.insert(NoteEntity(content = content, category = autoCategory))
    }

    suspend fun getAllNotes(): List<NoteEntity> = noteDao.getAll()

    suspend fun searchNotes(query: String): List<NoteEntity> = noteDao.search(query)

    suspend fun deleteNote(id: Long) = noteDao.delete(id)

    suspend fun getNoteCount(): Int = noteDao.count()

    private fun categorize(content: String): String {
        val lower = content.lowercase()
        return when {
            lower.contains("meeting") || lower.contains("office") ||
                    lower.contains("client") || lower.contains("project") ||
                    lower.contains("kaam") || lower.contains("deadline") -> "work"

            lower.contains("idea") || lower.contains("thought") ||
                    lower.contains("socha") || lower.contains("plan") -> "ideas"

            lower.contains("buy") || lower.contains("kharid") ||
                    lower.contains("list") || lower.contains("grocery") -> "shopping"

            else -> "personal"
        }
    }

    suspend fun getNoteSummary(): String {
        val notes = noteDao.getAll()
        if (notes.isEmpty()) return "Boss, koi notes nahi hain abhi."

        val sb = StringBuilder("Boss, tumhare ${notes.size} notes hain:\n")
        notes.take(10).forEachIndexed { i, note ->
            sb.appendLine("${i + 1}. [${note.category}] ${note.content.take(50)}")
        }
        return sb.toString()
    }
}
