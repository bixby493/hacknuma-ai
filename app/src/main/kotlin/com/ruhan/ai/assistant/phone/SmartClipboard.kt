package com.ruhan.ai.assistant.phone

import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ClipEntry(
    val text: String,
    val timestamp: Long,
    val category: String
)

@Singleton
class SmartClipboard @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val history = mutableListOf<ClipEntry>()
    private val maxHistory = 50
    private var listener: ClipboardManager.OnPrimaryClipChangedListener? = null

    fun startTracking() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        listener = ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString() ?: return@OnPrimaryClipChangedListener
                if (text.isNotBlank() && (history.isEmpty() || history.last().text != text)) {
                    val entry = ClipEntry(
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        category = categorize(text)
                    )
                    history.add(entry)
                    if (history.size > maxHistory) history.removeAt(0)
                }
            }
        }
        clipboard.addPrimaryClipChangedListener(listener)
    }

    fun stopTracking() {
        listener?.let {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.removePrimaryClipChangedListener(it)
        }
        listener = null
    }

    fun getHistory(): List<ClipEntry> = history.toList().reversed()

    fun getHistorySummary(): String {
        if (history.isEmpty()) return "Clipboard history khali hai."

        val sb = StringBuilder("Clipboard History (last ${minOf(history.size, 10)}):\n")
        history.takeLast(10).reversed().forEachIndexed { i, entry ->
            val preview = if (entry.text.length > 50) entry.text.take(50) + "..." else entry.text
            val timeAgo = getTimeAgo(entry.timestamp)
            sb.appendLine("${i + 1}. [$entry.category] $preview ($timeAgo)")
        }
        return sb.toString()
    }

    fun searchClipboard(query: String): String {
        val q = query.lowercase()
        val matches = history.filter { it.text.lowercase().contains(q) }
        if (matches.isEmpty()) return "Clipboard mein '$query' nahi mila."

        val sb = StringBuilder("${matches.size} match mili:\n")
        matches.takeLast(5).forEach { entry ->
            val preview = if (entry.text.length > 80) entry.text.take(80) + "..." else entry.text
            sb.appendLine("- $preview")
        }
        return sb.toString()
    }

    private fun categorize(text: String): String {
        return when {
            text.matches(Regex("^https?://.*")) -> "Link"
            text.matches(Regex("^[\\d\\s\\-+()]+$")) -> "Phone"
            text.matches(Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) -> "Email"
            text.matches(Regex(".*\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}.*")) -> "Card"
            text.length > 200 -> "Long Text"
            text.matches(Regex("^[A-Z0-9\\-]{6,20}$")) -> "Code/ID"
            else -> "Text"
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        val hours = minutes / 60
        return when {
            minutes < 1 -> "abhi"
            minutes < 60 -> "${minutes}m pehle"
            hours < 24 -> "${hours}h pehle"
            else -> "${hours / 24}d pehle"
        }
    }
}
