package com.ruhan.ai.assistant.offline

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private var dictionary: Map<String, DictionaryEntry>? = null
    private var quotes: List<QuoteEntry>? = null
    private var knowledgeBase: Map<String, List<KnowledgeEntry>>? = null

    fun lookupWord(word: String): String {
        if (dictionary == null) loadDictionary()
        val entry = dictionary?.get(word.lowercase())
            ?: dictionary?.entries?.firstOrNull { it.key.startsWith(word.lowercase()) }?.value
        return if (entry != null) {
            "${word}: ${entry.english} — ${entry.meaning}"
        } else {
            "Word not found in offline dictionary."
        }
    }

    fun getRandomQuote(category: String? = null): String {
        if (quotes == null) loadQuotes()
        val filtered = if (category != null) {
            quotes?.filter { it.category == category }
        } else quotes
        val quote = filtered?.randomOrNull() ?: return "No quotes available."
        return "\"${quote.quoteHi}\" — ${quote.author}"
    }

    fun getKnowledgeFact(topic: String): String {
        if (knowledgeBase == null) loadKnowledge()
        val facts = knowledgeBase?.get(topic.lowercase()) ?: return "Topic not found."
        val fact = facts.randomOrNull() ?: return "No facts available."
        return "${fact.title}: ${fact.content}"
    }

    private fun loadDictionary() {
        try {
            val json = context.assets.open("hindi_dictionary.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, DictionaryEntry>>() {}.type
            dictionary = gson.fromJson(json, type)
        } catch (_: Exception) {
            dictionary = emptyMap()
        }
    }

    private fun loadQuotes() {
        try {
            val json = context.assets.open("motivational_quotes.json").bufferedReader().readText()
            val type = object : TypeToken<List<QuoteEntry>>() {}.type
            quotes = gson.fromJson(json, type)
        } catch (_: Exception) {
            quotes = emptyList()
        }
    }

    private fun loadKnowledge() {
        try {
            val json = context.assets.open("knowledge_base.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, List<KnowledgeEntry>>>() {}.type
            knowledgeBase = gson.fromJson(json, type)
        } catch (_: Exception) {
            knowledgeBase = emptyMap()
        }
    }
}

data class DictionaryEntry(
    val english: String,
    val meaning: String,
    val synonyms: List<String>,
    val examples: List<String>,
    val category: String,
    val difficulty: String
)

data class QuoteEntry(
    val quoteHi: String,
    val quoteEn: String,
    val author: String,
    val category: String
)

data class KnowledgeEntry(
    val title: String,
    val content: String,
    val source: String,
    val difficulty: String,
    val tags: List<String>
)
