package com.ruhan.ai.assistant.phone

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageMinutes: Long
)

@Singleton
class AppUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val socialMediaApps = mapOf(
        "com.instagram.android" to "Instagram",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.google.android.youtube" to "YouTube",
        "com.facebook.katana" to "Facebook",
        "com.twitter.android" to "Twitter/X",
        "com.snapchat.android" to "Snapchat",
        "com.reddit.frontpage" to "Reddit",
        "com.pinterest" to "Pinterest",
        "com.linkedin.android" to "LinkedIn"
    )

    private val overdoseWarnings = listOf(
        "Bhai, %d minute ho gaye %s pe! Ab tumhara brain dopamine se numb ho raha hai. Break lo!",
        "%s pe %d minute? Kuch productive karo. Breathing exercise try karo?",
        "Alert: %s pe %d minute beet gaye. Ek glass paani piyo aur phone rakh do 5 minute ke liye.",
        "%d minute %s dekh liye! Yaad hai wo kaam jo karna tha? Ab kar lo, warna deadline miss hoga.",
        "Boss, %s pe %d minute? Aankhon ko rest do. 20-20-20 rule: 20 feet door dekho 20 seconds ke liye.",
        "%s addiction alert! %d minute. Ek walk pe jao, phone yahan rakh do.",
        "Bhai %d minute %s pe beet gaye. Koi kitaab padho ya kuch naya seekho!",
        "%s pe %d minute hogaye. Tumhari productivity ka graph neeche ja raha hai!"
    )

    fun getSocialMediaUsageToday(): List<AppUsageInfo> {
        val usageManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: return emptyList()

        return stats
            .filter { it.packageName in socialMediaApps && it.totalTimeInForeground > 60000 }
            .map { stat ->
                AppUsageInfo(
                    packageName = stat.packageName,
                    appName = socialMediaApps[stat.packageName] ?: stat.packageName,
                    usageMinutes = stat.totalTimeInForeground / 60000
                )
            }
            .sortedByDescending { it.usageMinutes }
    }

    fun getOverdoseWarning(): String? {
        val usage = getSocialMediaUsageToday()
        val heavyUse = usage.filter { it.usageMinutes >= 30 }
        if (heavyUse.isEmpty()) return null

        val worst = heavyUse.first()
        val warning = overdoseWarnings.random()
        return String.format(warning, worst.usageMinutes, worst.appName)
            .replace(Regex("%[ds].*%[ds]")) {
                // Handle both orderings
                "${worst.usageMinutes} minute ${worst.appName}"
            }
    }

    fun getUsageSummary(): String {
        val usage = getSocialMediaUsageToday()
        if (usage.isEmpty()) return "Aaj social media use nahi kiya. Great job!"

        val totalMinutes = usage.sumOf { it.usageMinutes }
        val sb = StringBuilder("Aaj ka Social Media Report:\n")
        usage.forEach { app ->
            val hours = app.usageMinutes / 60
            val mins = app.usageMinutes % 60
            val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            sb.appendLine("${app.appName}: $timeStr")
        }
        val totalHours = totalMinutes / 60
        val totalMins = totalMinutes % 60
        sb.appendLine("\nTotal: ${if (totalHours > 0) "${totalHours}h " else ""}${totalMins}m")

        if (totalMinutes > 120) {
            sb.appendLine("\nBoss, 2 ghante se zyada social media? Thoda kam karo!")
        } else if (totalMinutes > 60) {
            sb.appendLine("\nEk ghante se zyada. Moderate hai, but dhyan rakho.")
        } else {
            sb.appendLine("\nAccha control hai! Keep it up!")
        }

        return sb.toString()
    }
}
