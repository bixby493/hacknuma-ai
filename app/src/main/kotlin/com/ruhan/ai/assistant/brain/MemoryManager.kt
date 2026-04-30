package com.ruhan.ai.assistant.brain

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.ruhan.ai.assistant.premium.NotionManager
import com.ruhan.ai.assistant.util.PreferencesManager
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getByType(type: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE `key` LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%' ORDER BY importance DESC")
    suspend fun search(query: String): List<MemoryEntity>

    @Insert
    suspend fun insert(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun count(): Int
}

@Singleton
class MemoryManager @Inject constructor(
    private val memoryDao: MemoryDao,
    private val preferencesManager: PreferencesManager,
    private val notionManager: NotionManager
) {
    companion object {
        const val TYPE_FACT = "fact"
        const val TYPE_PREFERENCE = "preference"
        const val TYPE_NICKNAME = "nickname"
        const val TYPE_CONVERSATION = "conversation"
        const val TYPE_HABIT = "habit"
        private const val KEYSTORE_ALIAS = "ruhan_memory_enc_key"
        private const val ENC_PREFIX = "ENC:"
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun encryptValue(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            ENC_PREFIX + Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
        } catch (_: Exception) {
            plainText
        }
    }

    private fun decryptValue(storedValue: String): String {
        if (!storedValue.startsWith(ENC_PREFIX)) return storedValue
        return try {
            val data = Base64.decode(storedValue.removePrefix(ENC_PREFIX), Base64.NO_WRAP)
            val iv = data.copyOfRange(0, 12)
            val encrypted = data.copyOfRange(12, data.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) {
            storedValue.removePrefix(ENC_PREFIX)
        }
    }

    private fun maybeEncrypt(value: String): String {
        return if (preferencesManager.memoryEncryption) encryptValue(value) else value
    }

    suspend fun remember(key: String, value: String, type: String = TYPE_FACT, importance: Int = 1) {
        memoryDao.insert(
            MemoryEntity(
                type = type,
                key = key,
                value = maybeEncrypt(value),
                importance = importance
            )
        )
        if (notionManager.isConfigured()) {
            try { notionManager.saveMemory(key, "$type: $value") } catch (_: Exception) {}
        }
    }

    suspend fun rememberFact(fact: String) {
        remember(key = fact, value = fact, type = TYPE_FACT, importance = 2)
    }

    suspend fun rememberNickname(nickname: String, realName: String) {
        remember(
            key = nickname.lowercase(),
            value = realName,
            type = TYPE_NICKNAME,
            importance = 3
        )
    }

    suspend fun rememberPreference(key: String, value: String) {
        remember(key = key, value = value, type = TYPE_PREFERENCE, importance = 2)
    }

    suspend fun saveConversationSummary(summary: String) {
        remember(key = "conversation_summary", value = summary, type = TYPE_CONVERSATION)
    }

    suspend fun resolveNickname(name: String): String? {
        val results = memoryDao.search(name.lowercase())
        return results.firstOrNull { it.type == TYPE_NICKNAME }?.let { decryptValue(it.value) }
    }

    suspend fun getAllMemories(): List<MemoryEntity> {
        return memoryDao.getAllMemories().map { it.copy(value = decryptValue(it.value)) }
    }

    suspend fun searchMemories(query: String): List<MemoryEntity> {
        return memoryDao.search(query).map { it.copy(value = decryptValue(it.value)) }
    }

    suspend fun getMemorySummary(): String {
        val memories = getAllMemories()
        if (memories.isEmpty()) return "Abhi tak koi special memory nahi hai, Boss."

        val sb = StringBuilder("Boss, mujhe yeh yaad hai:\n\n")
        val grouped = memories.groupBy { it.type }

        grouped[TYPE_FACT]?.let { facts ->
            sb.appendLine("Facts:")
            facts.take(10).forEach { sb.appendLine("• ${it.value}") }
        }
        grouped[TYPE_NICKNAME]?.let { nicks ->
            sb.appendLine("\nNicknames:")
            nicks.forEach { sb.appendLine("• ${it.key} = ${it.value}") }
        }
        grouped[TYPE_PREFERENCE]?.let { prefs ->
            sb.appendLine("\nPreferences:")
            prefs.forEach { sb.appendLine("• ${it.key}: ${it.value}") }
        }

        return sb.toString()
    }

    suspend fun clearAllMemories() = memoryDao.clearAll()

    suspend fun parseAndStore(text: String): Boolean {
        val lower = text.lowercase().trim()
        return when {
            lower.startsWith("ruhan yaad rakho") || lower.startsWith("yaad rakho") ||
                    lower.startsWith("remember that") || lower.startsWith("ruhan remember") ||
                    lower.startsWith("yaad rakh") -> {
                val fact = text
                    .replace(Regex("^(ruhan\\s+)?(yaad\\s+rakh(o)?\\s*(ki\\s+)?|remember\\s+(that\\s+)?)", RegexOption.IGNORE_CASE), "")
                    .trim()
                if (fact.isNotBlank()) {
                    rememberFact(fact)
                    true
                } else false
            }

            lower.contains("matlab") && !lower.contains("kya matlab") -> {
                val parts = text.split(Regex("\\s+matlab\\s+", RegexOption.IGNORE_CASE))
                if (parts.size == 2 && parts[0].trim().isNotBlank() && parts[1].trim().isNotBlank()) {
                    rememberNickname(parts[0].trim(), parts[1].trim())
                    true
                } else false
            }

            lower.matches(Regex(".*mera\\s+(naam|name)\\s+(.+)\\s+(hai|he|h).*")) -> {
                val nameMatch = Regex("mera\\s+(naam|name)\\s+(.+?)\\s+(hai|he|h)", RegexOption.IGNORE_CASE).find(text)
                val name = nameMatch?.groupValues?.get(2)?.trim()
                if (!name.isNullOrBlank()) {
                    rememberFact("Boss ka naam $name hai")
                    rememberPreference("boss_real_name", name)
                    true
                } else false
            }

            lower.matches(Regex(".*mera\\s+(.+?)\\s+(ka|ki|ke)\\s+(number|phone)\\s+(.+)\\s+(hai|he|h).*")) -> {
                val match = Regex("mera\\s+(.+?)\\s+(ka|ki|ke)\\s+(number|phone)\\s+(.+?)\\s+(hai|he|h)", RegexOption.IGNORE_CASE).find(text)
                val person = match?.groupValues?.get(1)?.trim()
                val number = match?.groupValues?.get(4)?.trim()
                if (!person.isNullOrBlank() && !number.isNullOrBlank()) {
                    rememberFact("$person ka number $number hai")
                    true
                } else false
            }

            lower.matches(Regex(".*mujhe\\s+(.+?)\\s+(pasand|favorite|favourite).*")) ||
                    lower.matches(Regex(".*mera\\s+favorite\\s+(.+?)\\s+(hai|he|h).*")) -> {
                val fact = text.replace(Regex("^(ruhan\\s+)?", RegexOption.IGNORE_CASE), "").trim()
                rememberPreference("preference", fact)
                true
            }

            lower.matches(Regex(".*main\\s+(.+?)\\s+ko\\s+(.+?)\\s+(bolta|bulata|kehta).*")) -> {
                val match = Regex("main\\s+(.+?)\\s+ko\\s+(.+?)\\s+(bolta|bulata|kehta)", RegexOption.IGNORE_CASE).find(text)
                val realName = match?.groupValues?.get(1)?.trim()
                val nickname = match?.groupValues?.get(2)?.trim()
                if (!realName.isNullOrBlank() && !nickname.isNullOrBlank()) {
                    rememberNickname(nickname, realName)
                    true
                } else false
            }

            else -> false
        }
    }
}
