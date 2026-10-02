package com.ruhan.ai.assistant.offline

import android.content.Context
import android.util.Log
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
        val dict = dictionary ?: return "Dictionary load nahi ho saka."
        val entry = dict[word.lowercase()]
            ?: dict.entries.firstOrNull { it.key.startsWith(word.lowercase()) }?.value
        return if (entry != null) {
            "📖 ${word}: ${entry.english}\n${entry.meaning}\nSynonyms: ${entry.synonyms.joinToString(", ")}"
        } else {
            "'$word' offline dictionary mein nahi mila. Online search try karo."
        }
    }

    fun getRandomQuote(category: String? = null): String {
        if (quotes == null) loadQuotes()
        val all = quotes ?: return "Quotes abhi available nahi hain."
        val filtered = if (category != null) {
            all.filter { it.category.equals(category, ignoreCase = true) }
        } else all
        val quote = filtered.randomOrNull() ?: all.randomOrNull() ?: return "Koi quote nahi mila."
        return "💬 \"${quote.quoteHi}\"\n— ${quote.author}"
    }

    fun getKnowledgeFact(topic: String): String {
        if (knowledgeBase == null) loadKnowledge()
        val kb = knowledgeBase ?: return "Knowledge base load nahi ho saka."
        val key = topic.lowercase()
        val facts = kb[key]
            ?: kb.entries.firstOrNull { it.key.contains(key) || key.contains(it.key) }?.value
        if (facts == null) {
            val allTopics = kb.keys.joinToString(", ")
            return "Topic '$topic' nahi mila. Available topics: $allTopics"
        }
        val fact = facts.randomOrNull() ?: return "Is topic mein koi fact nahi hai."
        return "🧠 ${fact.title}: ${fact.content}"
    }

    fun getAvailableTopics(): List<String> {
        if (knowledgeBase == null) loadKnowledge()
        return knowledgeBase?.keys?.toList() ?: emptyList()
    }

    private fun loadDictionary() {
        try {
            val json = context.assets.open("hindi_dictionary.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, DictionaryEntry>>() {}.type
            dictionary = gson.fromJson(json, type)
            Log.d("OfflineData", "Dictionary loaded: ${dictionary?.size ?: 0} words")
        } catch (e: Exception) {
            Log.e("OfflineData", "Failed to load dictionary", e)
            dictionary = emptyMap()
        }
    }

    private fun loadQuotes() {
        try {
            val json = context.assets.open("motivational_quotes.json").bufferedReader().readText()
            val type = object : TypeToken<List<QuoteEntry>>() {}.type
            quotes = gson.fromJson(json, type)
            Log.d("OfflineData", "Quotes loaded: ${quotes?.size ?: 0}")
        } catch (e: Exception) {
            Log.e("OfflineData", "Failed to load quotes", e)
            quotes = emptyList()
        }
    }

    private fun loadKnowledge() {
        try {
            val json = context.assets.open("knowledge_base.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, List<KnowledgeEntry>>>() {}.type
            knowledgeBase = gson.fromJson(json, type)
            Log.d("OfflineData", "Knowledge loaded: ${knowledgeBase?.size ?: 0} topics")
        } catch (e: Exception) {
            Log.e("OfflineData", "Failed to load knowledge base", e)
            knowledgeBase = emptyMap()
        }
    }
}

data class DictionaryEntry(
    val english: String = "",
    val meaning: String = "",
    val synonyms: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val category: String = "",
    val difficulty: String = ""
)

data class QuoteEntry(
    val quoteHi: String = "",
    val quoteEn: String = "",
    val author: String = "",
    val category: String = ""
)

data class KnowledgeEntry(
    val title: String = "",
    val content: String = "",
    val source: String = "",
    val difficulty: String = "",
    val tags: List<String> = emptyList()
)
