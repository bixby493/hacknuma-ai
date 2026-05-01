package com.ruhan.ai.assistant.brain

import javax.inject.Inject
import javax.inject.Singleton

sealed class ParsedCommand {
    data class Call(val name: String) : ParsedCommand()
    data class Sms(val name: String, val message: String) : ParsedCommand()
    data class WhatsApp(val name: String, val message: String) : ParsedCommand()
    data class OpenApp(val appName: String) : ParsedCommand()
    data class CloseApp(val appName: String) : ParsedCommand()
    data class Reminder(val task: String, val delayMinutes: Int) : ParsedCommand()
    data class Setting(val setting: String, val action: String) : ParsedCommand()
    data class Brightness(val level: Int) : ParsedCommand()
    data class Volume(val level: Int) : ParsedCommand()
    data class WebSearch(val query: String) : ParsedCommand()
    data class Research(val topic: String) : ParsedCommand()
    data class Remember(val text: String) : ParsedCommand()
    data class ShareLocation(val name: String) : ParsedCommand()
    data class Navigate(val place: String) : ParsedCommand()
    data class Note(val content: String) : ParsedCommand()
    data class Email(val recipient: String, val subject: String, val body: String) : ParsedCommand()
    data class Weather(val location: String) : ParsedCommand()
    data class PlayMusic(val query: String) : ParsedCommand()
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : ParsedCommand()
    data class SetTimer(val seconds: Int) : ParsedCommand()
    data class Translate(val text: String, val targetLang: String) : ParsedCommand()
    data class Calculate(val expression: String) : ParsedCommand()
    data object ScreenAnalysis : ParsedCommand()
    data object BatteryInfo : ParsedCommand()
    data object TimeInfo : ParsedCommand()
    data object DateInfo : ParsedCommand()
    data object NetworkInfo : ParsedCommand()
    data object Emergency : ParsedCommand()
    data object ShowMemories : ParsedCommand()
    data object ShowNotes : ParsedCommand()
    data object CheckEmails : ParsedCommand()
    data object SystemDiagnostics : ParsedCommand()
    data object WifiScan : ParsedCommand()
    data object LiveVoice : ParsedCommand()
    data object AnswerCall : ParsedCommand()
    data object RejectCall : ParsedCommand()
    data object RecentCallLog : ParsedCommand()
    data object StorageInfo : ParsedCommand()
    data object SecurityCheck : ParsedCommand()
    data object ClearNotifications : ParsedCommand()
    data object TakeScreenshot : ParsedCommand()
    data class RecordAudio(val duration: Int = 30) : ParsedCommand()
    data class PhoneHealthReport(val detail: String = "") : ParsedCommand()
    data object ScanFiles : ParsedCommand()
    data object CleanJunk : ParsedCommand()
    data object ClearCache : ParsedCommand()
    data class FindFile(val query: String) : ParsedCommand()
    data object ScreenTimeReport : ParsedCommand()
    data object ClipboardHistory : ParsedCommand()
    data class ClipboardSearch(val query: String) : ParsedCommand()
    data object EmergencyStatus : ParsedCommand()
    data class AiChat(val message: String) : ParsedCommand()
}

@Singleton
class CommandParser @Inject constructor() {

    fun parse(input: String): ParsedCommand {
        val text = input.lowercase().trim()

        return parseCallControl(text)
            ?: parseCall(text)
            ?: parseSms(text)
            ?: parseWhatsApp(text)
            ?: parseCloseApp(text)
            ?: parseOpenApp(text)
            ?: parseReminder(text)
            ?: parseAlarmTimer(text)
            ?: parseSettingsToggle(text)
            ?: parseBrightness(text)
            ?: parseVolume(text)
            ?: parseWeather(text)
            ?: parseMusic(text)
            ?: parseScreenAnalysis(text)
            ?: parsePhoneInfo(text)
            ?: parseStorageInfo(text)
            ?: parseSecurityCheck(text)
            ?: parseEmergency(text)
            ?: parseMemory(text)
            ?: parseResearch(text)
            ?: parseSearch(text)
            ?: parseLocation(text)
            ?: parseNote(text)
            ?: parseEmail(text)
            ?: parseDiagnostics(text)
            ?: parseTranslate(text)
            ?: parseCalculate(text)
            ?: parseLiveVoice(text)
            ?: parseScreenshot(text)
            ?: parseClearNotifications(text)
            ?: parseFileCommands(text)
            ?: parseAdvancedCommands(text)
            ?: ParsedCommand.AiChat(input)
    }

    private fun parseCallControl(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(uthao|answer|pick\\s*up|receive).*"))) return ParsedCommand.AnswerCall
        if (text.matches(Regex(".*(kaat\\s*do|reject|decline|hang\\s*up|rakh\\s*do).*"))) return ParsedCommand.RejectCall
        if (text.matches(Regex(".*(recent\\s*call|call\\s*log|call\\s*history|last\\s*call|kaun.*call).*"))) return ParsedCommand.RecentCallLog
        return null
    }

    private fun parseCall(text: String): ParsedCommand? {
        val patterns = listOf(
            Regex("(.+?)\\s*ko\\s+(call|phone)\\s*(kar|laga|karo)"),
            Regex("(call|phone)\\s*(kar|laga|karo)\\s+(.+)"),
            Regex("(.+?)\\s*ko\\s+call"),
            Regex("call\\s+(.+)")
        )
        for (p in patterns) {
            val m = p.find(text.removePrefix("ruhan").trim())
            if (m != null) {
                val name = m.groupValues.last { it.isNotBlank() && it !in listOf("call", "phone", "kar", "laga", "karo") }
                    .replace(Regex("^(ruhan\\s+)"), "").trim()
                if (name.isNotBlank()) return ParsedCommand.Call(name)
            }
        }
        return null
    }

    private fun parseSms(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(message|sms|text)\\s*bhej.*"))) return null
        val parts = text.split(Regex("(bhej|—|-|:)"))
        val nameRaw = text.replace(Regex("(ruhan\\s+)"), "")
            .replace(Regex("\\s*ko\\s+(message|sms|text)\\s*bhej.*"), "").trim()
        val msg = if (parts.size > 1) parts.last().trim() else ""
        return ParsedCommand.Sms(nameRaw, msg)
    }

    private fun parseWhatsApp(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*whatsapp\\s*pe.*bhej.*"))) return null
        val parts = text.split(Regex("(bhej|—|-|:)"))
        val nameRaw = text.replace(Regex(".*whatsapp\\s*pe\\s*"), "")
            .replace(Regex("\\s*ko\\s*bhej.*"), "").trim()
        val msg = if (parts.size > 1) parts.last().trim() else ""
        return ParsedCommand.WhatsApp(nameRaw, msg)
    }

    private fun parseCloseApp(text: String): ParsedCommand? {
        val patterns = listOf(
            Regex("(.+?)\\s*(band|close|kill|stop)\\s*kar"),
            Regex("(band|close|kill|stop)\\s*kar\\s+(.+)")
        )
        for (p in patterns) {
            val m = p.find(text.removePrefix("ruhan").trim())
            if (m != null) {
                val app = m.groupValues.last { it.isNotBlank() && it !in listOf("band", "close", "kill", "stop", "kar") }
                    .replace("ruhan", "").trim()
                if (app.isNotBlank()) return ParsedCommand.CloseApp(app)
            }
        }
        return null
    }

    private fun parseOpenApp(text: String): ParsedCommand? {
        val cleaned = text.replace(Regex("^ruhan\\s+"), "").trim()
        val patterns = listOf(
            Regex("(.+?)\\s*(khol|kholo|open|launch|start|chalu)\\s*(kar|karo|kardo)?\\s*$"),
            Regex("(khol|kholo|open|launch|start|chalu)\\s*(kar|karo|kardo)?\\s+(.+)"),
            Regex("(.+?)\\s*ko\\s*(khol|open|launch|start|chalu)\\s*(kar|karo|kardo)?"),
        )
        for (p in patterns) {
            val m = p.find(cleaned)
            if (m != null) {
                val keywords = setOf("khol", "kholo", "open", "launch", "start", "chalu", "kar", "karo", "kardo", "ko")
                val app = m.groupValues.drop(1)
                    .firstOrNull { it.isNotBlank() && it !in keywords }
                    ?.trim()
                if (!app.isNullOrBlank()) return ParsedCommand.OpenApp(app)
            }
        }
        return null
    }

    private fun parseReminder(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(yaad\\s*dila|remind|reminder).*"))) return null
        val task = text.replace(Regex(".*(yaad\\s*dila|remind|reminder)\\s*"), "").trim()
        val minutes = extractTimeMinutes(text)
        return ParsedCommand.Reminder(task.ifBlank { "Reminder" }, minutes)
    }

    private fun parseAlarmTimer(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(alarm|alaram)\\s*(laga|set|rakh).*"))) {
            val hourMatch = Regex("(\\d{1,2})\\s*(baj|:)\\s*(\\d{1,2})?").find(text)
            val hour = hourMatch?.groupValues?.get(1)?.toIntOrNull() ?: 8
            val minute = hourMatch?.groupValues?.get(3)?.toIntOrNull() ?: 0
            val label = text.replace(Regex(".*(alarm|alaram)\\s*(laga|set|rakh)\\s*"), "")
                .replace(Regex("\\d+\\s*(baj|:)\\s*\\d*"), "").trim()
            return ParsedCommand.SetAlarm(hour, minute, label.ifBlank { "Alarm" })
        }
        if (text.matches(Regex(".*(timer|countdown)\\s*(laga|set|start).*"))) {
            val minMatch = Regex("(\\d+)\\s*(minute|min|mint)").find(text)
            val secMatch = Regex("(\\d+)\\s*(second|sec)").find(text)
            val minutes = minMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val seconds = secMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val total = minutes * 60 + seconds
            return ParsedCommand.SetTimer(if (total > 0) total else 60)
        }
        return null
    }

    private fun parseSettingsToggle(text: String): ParsedCommand? {
        val settingsMap = mapOf(
            Regex(".*(wifi|wi-fi).*") to "wifi",
            Regex(".*bluetooth.*") to "bluetooth",
            Regex(".*(flashlight|torch|flash).*") to "flashlight",
            Regex(".*(dnd|do not disturb|disturb).*") to "dnd",
            Regex(".*(airplane|flight|havaai).*") to "airplane",
            Regex(".*(hotspot|tethering).*") to "hotspot",
            Regex(".*(location|gps)\\s*(on|off|band|chalu).*") to "location",
            Regex(".*(battery\\s*saver|power\\s*saving).*") to "battery_saver",
            Regex(".*(dark\\s*mode|night\\s*mode).*") to "dark_mode",
            Regex(".*(silent|vibrate)\\s*(mode)?.*") to "silent",
            Regex(".*(mobile\\s*data|data)\\s*(on|off|chalu|band).*") to "mobile_data",
            Regex(".*(nfc)\\s*(on|off|chalu|band).*") to "nfc",
            Regex(".*(screen\\s*rotation|rotation|auto\\s*rotate).*") to "rotation",
            Regex(".*(focus\\s*mode).*") to "focus_mode"
        )

        for ((pattern, setting) in settingsMap) {
            if (text.matches(pattern)) {
                val action = if (text.contains("on") || text.contains("chalu") ||
                    text.contains("enable") || text.contains("start")
                ) "on" else "off"
                return ParsedCommand.Setting(setting, action)
            }
        }
        return null
    }

    private fun parseBrightness(text: String): ParsedCommand? {
        if (!text.contains("brightness")) return null
        val numMatch = Regex("(\\d+)").find(text)
        if (numMatch != null) return ParsedCommand.Brightness(numMatch.value.toInt().coerceIn(0, 100))
        if (text.matches(Regex(".*brightness.*(badha|increase|zyada|max|full).*"))) return ParsedCommand.Brightness(100)
        if (text.matches(Regex(".*brightness.*(kam|decrease|low|min|dim).*"))) return ParsedCommand.Brightness(20)
        return null
    }

    private fun parseVolume(text: String): ParsedCommand? {
        if (!text.contains("volume")) return null
        val numMatch = Regex("(\\d+)").find(text)
        if (numMatch != null) return ParsedCommand.Volume(numMatch.value.toInt().coerceIn(0, 100))
        if (text.matches(Regex(".*volume.*(badha|increase|zyada|max|full).*"))) return ParsedCommand.Volume(100)
        if (text.matches(Regex(".*volume.*(kam|decrease|low|min|mute).*"))) return ParsedCommand.Volume(0)
        return null
    }

    private fun parseWeather(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(weather|mausam|mausm|tapman|temperature).*"))) return null
        val location = text.replace(Regex("(ruhan\\s+|weather|mausam|mausm|tapman|temperature|kya hai|batao|ka|ki|ke|aaj|kal)"), "").trim()
        return ParsedCommand.Weather(location.ifBlank { "current" })
    }

    private fun parseMusic(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(music|gana|song|gaana|bajao|play)\\s*(bajao|play|laga|chala)?.*"))) return null
        if (text.matches(Regex(".*(khol|open|launch).*"))) return null
        val query = text.replace(Regex("(ruhan\\s+|music|gana|song|gaana|bajao|play|laga|chala|karo)"), "").trim()
        return if (query.isNotBlank()) ParsedCommand.PlayMusic(query) else null
    }

    private fun parseScreenAnalysis(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(screen|yeh\\s*kya|dekh|analyze|kya\\s*hai|kya\\s*likha|kya\\s*dikh).*")) &&
            (text.contains("screen") || text.contains("yeh") || text.contains("dekh"))
        ) return ParsedCommand.ScreenAnalysis
        return null
    }

    private fun parsePhoneInfo(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(battery|charge)\\s*(kitni|level|status|kya|hai).*"))) return ParsedCommand.BatteryInfo
        if (text.matches(Regex(".*(time|samay|baj|waqt|kitne\\s*baj).*"))) return ParsedCommand.TimeInfo
        if (text.matches(Regex(".*(date|tarikh|din|aaj\\s*kya).*"))) return ParsedCommand.DateInfo
        if (text.matches(Regex(".*(network|signal|internet|wifi.*status|connection).*kaisa.*"))) return ParsedCommand.NetworkInfo
        if (text.matches(Regex(".*(network|signal|internet|connection)\\s*(kya|kaisa|status).*"))) return ParsedCommand.NetworkInfo
        return null
    }

    private fun parseEmergency(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(emergency|help|bachao|madad|sos).*"))) return ParsedCommand.Emergency
        return null
    }

    private fun parseMemory(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(yaad\\s*rakho|remember|mera.*hai|yaad.*rakh).*"))) return ParsedCommand.Remember(text)
        if (text.matches(Regex(".*(kya\\s*pata|kya\\s*yaad|memory|memories|yaad|mere\\s*baare).*"))) return ParsedCommand.ShowMemories
        return null
    }

    private fun parseResearch(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(research|investigate|detailed.*report|deep\\s*dive).*"))) return null
        val topic = text.replace(Regex("(ruhan\\s+|research\\s*karo\\s*|par\\s*|pe\\s*|investigate\\s*)"), "").trim()
        return ParsedCommand.Research(topic)
    }

    private fun parseSearch(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(search|dhundho|google|latest|kya\\s*hai).*"))) return null
        if (text.contains("screen") || text.contains("yeh")) return null
        val query = text.replace(Regex("(ruhan\\s+|search\\s*karo?\\s*|dhundho\\s*|google\\s*karo?\\s*)"), "").trim()
        return if (query.isNotBlank()) ParsedCommand.WebSearch(query) else null
    }

    private fun parseLocation(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(location|jagah).*(bhej|share|send).*"))) {
            val name = text.replace(Regex(".*(location|jagah)\\s*"), "")
                .replace(Regex("\\s*(ko\\s*)?(bhej|share|send).*"), "").trim()
            return ParsedCommand.ShareLocation(name)
        }
        if (text.matches(Regex(".*(kaise\\s*jaun|navigate|direction|rasta|route).*"))) {
            val place = text.replace(Regex("(ruhan\\s+|kaise\\s*jaun\\s*|navigate\\s*to\\s*|rasta\\s*dikha\\s*)"), "").trim()
            return ParsedCommand.Navigate(place)
        }
        return null
    }

    private fun parseNote(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(note\\s*karo|note\\s*bana|add\\s*note|note\\s*le|likh).*"))) {
            val content = text.replace(Regex("(ruhan\\s+|note\\s*(karo|bana|le)\\s*|add\\s*note\\s*|likh\\s*)"), "")
                .replace("—", "").replace("-", "").trim()
            return if (content.isNotBlank()) ParsedCommand.Note(content) else null
        }
        if (text.matches(Regex(".*(notes?\\s*dikha|show\\s*notes?|mere\\s*notes?).*"))) return ParsedCommand.ShowNotes
        return null
    }

    private fun parseEmail(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(email|gmail)\\s*(check|dekh|padh).*"))) return ParsedCommand.CheckEmails
        if (text.matches(Regex(".*(email|gmail).*bhej.*"))) {
            val parts = text.split(Regex("(—|-|:)"))
            val name = text.replace(Regex("(ruhan\\s+|.*ko\\s+email\\s*bhej)"), "").trim()
            return ParsedCommand.Email(
                recipient = name,
                subject = if (parts.size > 1) parts[1].trim() else "",
                body = if (parts.size > 2) parts[2].trim() else ""
            )
        }
        return null
    }

    private fun parseDiagnostics(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(system|health|diagnostic|phone.*report|phone.*status).*"))) return ParsedCommand.SystemDiagnostics
        if (text.matches(Regex(".*(wifi\\s*scan|network\\s*scan).*"))) return ParsedCommand.WifiScan
        return null
    }

    private fun parseTranslate(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(translate|anuvad|hindi\\s*me|english\\s*me).*"))) return null
        val targetLang = if (text.contains("hindi") || text.contains("हिंदी")) "hi" else "en"
        val content = text.replace(Regex("(ruhan\\s+|translate\\s*karo?\\s*|anuvad\\s*karo?\\s*|hindi\\s*me\\s*|english\\s*me\\s*)"), "").trim()
        return ParsedCommand.Translate(content, targetLang)
    }

    private fun parseCalculate(text: String): ParsedCommand? {
        if (!text.matches(Regex(".*(calculate|hisab|kitna|jod|guna|bhaag|minus|plus).*"))) return null
        val expr = text.replace(Regex("(ruhan\\s+|calculate\\s*karo?\\s*|hisab\\s*karo?\\s*|kitna\\s*hai\\s*)"), "").trim()
        return if (expr.isNotBlank()) ParsedCommand.Calculate(expr) else null
    }

    private fun parseLiveVoice(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(live\\s*voice|live\\s*baat|real.*time.*baat|live\\s*mode|live\\s*conversation).*")))
            return ParsedCommand.LiveVoice
        return null
    }

    private fun parseStorageInfo(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(storage|memory|space|jagah|kitni jagah|ram)\\s*(check|karo|dikha|batao|kitni|hai|status).*")))
            return ParsedCommand.StorageInfo
        if (text.matches(Regex(".*(storage|space|jagah).*")))
            return ParsedCommand.StorageInfo
        return null
    }

    private fun parseSecurityCheck(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(phone\\s*secure|security\\s*check|safe|surakshit|secure).*")))
            return ParsedCommand.SecurityCheck
        return null
    }

    private fun parseScreenshot(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(screenshot|screen\\s*capture|ss\\s*le).*")))
            return ParsedCommand.TakeScreenshot
        return null
    }

    private fun parseClearNotifications(text: String): ParsedCommand? {
        if (text.matches(Regex(".*(notification|notif)\\s*(clear|saaf|hata|band|delete).*")))
            return ParsedCommand.ClearNotifications
        if (text.matches(Regex(".*(clear|saaf|hata)\\s*(notification|notif).*")))
            return ParsedCommand.ClearNotifications
        return null
    }

    private fun extractTimeMinutes(text: String): Int {
        val hourMatch = Regex("(\\d+)\\s*(ghante|hour|hrs?)").find(text)
        val minMatch = Regex("(\\d+)\\s*(minute|min|mint)").find(text)

        val hours = hourMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val mins = minMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

        if (hours == 0 && mins == 0) {
            if (text.contains("kal") || text.contains("tomorrow")) return 24 * 60
            if (text.contains("subah")) return calculateMinutesToMorning()
            return 60
        }
        return hours * 60 + mins
    }

    private fun parseAdvancedCommands(text: String): ParsedCommand? {
        // Screen time / social media usage
        if (text.matches(Regex(".*(screen\\s*time|social\\s*media|instagram|youtube|tiktok).*(report|kitna|usage|check|time|use).*")) ||
            text.matches(Regex(".*(kitna|kitni|kab\\s*se).*(phone|screen|instagram|youtube|social).*")) ||
            text.matches(Regex(".*(phone|screen).*(kitna|kitni|time|usage|report).*")) ||
            text.matches(Regex(".*(overdose|addiction|zyada).*"))) {
            return ParsedCommand.ScreenTimeReport
        }

        // Clipboard
        if (text.matches(Regex(".*(clipboard|copy|paste).*(history|dikhao|show|list|kya).*")) ||
            text.matches(Regex(".*(kya|last).*(copy|clipboard|paste).*"))) {
            return ParsedCommand.ClipboardHistory
        }
        val clipSearch = Regex("(?:clipboard|copy).*(?:mein|me|search|dhundh)\\s+(.+)").find(text)
        if (clipSearch != null) {
            val q = clipSearch.groupValues[1].trim()
            if (q.isNotBlank()) return ParsedCommand.ClipboardSearch(q)
        }

        // Emergency
        if (text.matches(Regex(".*(emergency|sos|shake).*(status|kya|check|on|off|kaise).*"))) {
            return ParsedCommand.EmergencyStatus
        }

        return null
    }

    private fun parseFileCommands(text: String): ParsedCommand? {
        // Scan
        if (text.matches(Regex(".*(scan|analyse|analyze|check).*(file|storage|phone|memory|disk).*")) ||
            text.matches(Regex(".*(file|storage|phone).*(scan|analyse|analyze|check).*")) ||
            text.matches(Regex(".*(hard\\s*scan|deep\\s*scan|full\\s*scan).*"))) {
            return ParsedCommand.ScanFiles
        }
        // Clean junk
        if (text.matches(Regex(".*(kachra|junk|garbage|saaf|clean).*(kar|karo|karde|do|hatao|delete).*")) ||
            text.matches(Regex(".*(clean|clear).*(junk|temp|garbage|kachra).*")) ||
            text.matches(Regex(".*(saaf|clean)\\s*(kar|karo|karde).*"))) {
            return ParsedCommand.CleanJunk
        }
        // Clear cache
        if (text.matches(Regex(".*(cache|cach).*(clear|saaf|delete|hatao|remove|kar).*")) ||
            text.matches(Regex(".*(clear|saaf|delete).*(cache|cach).*"))) {
            return ParsedCommand.ClearCache
        }
        // Duplicate photo
        if (text.matches(Regex(".*(duplicate|copy).*(photo|image|pic|file).*(delete|hatao|remove|dhundh|find).*")) ||
            text.matches(Regex(".*(delete|hatao|remove|dhundh|find).*(duplicate|copy).*(photo|image|pic|file).*"))) {
            return ParsedCommand.ScanFiles
        }
        // Find file
        val findPatterns = listOf(
            Regex("(?:dhundh|find|search|locate|khoj).*?(?:file|naam|name)?\\s+(.+)"),
            Regex("(.+?)\\s+(?:dhundh|find|search|khoj)")
        )
        for (p in findPatterns) {
            val m = p.find(text)
            if (m != null) {
                val query = (m.groupValues.getOrNull(1) ?: "").trim()
                if (query.isNotBlank() && query.length > 1) {
                    return ParsedCommand.FindFile(query)
                }
            }
        }
        return null
    }

    private fun calculateMinutesToMorning(): Int {
        val now = java.util.Calendar.getInstance()
        val morning = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 8)
            set(java.util.Calendar.MINUTE, 0)
        }
        return ((morning.timeInMillis - now.timeInMillis) / 60000).toInt()
    }
}
