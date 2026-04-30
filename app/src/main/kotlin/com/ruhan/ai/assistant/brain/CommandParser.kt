package com.ruhan.ai.assistant.brain

import javax.inject.Inject
import javax.inject.Singleton

sealed class ParsedCommand {
    data class Call(val name: String) : ParsedCommand()
    data class Sms(val name: String, val message: String) : ParsedCommand()
    data class WhatsApp(val name: String, val message: String) : ParsedCommand()
    data class OpenApp(val appName: String) : ParsedCommand()
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
    data class PhoneHealthReport(val detail: String = "") : ParsedCommand()
    data class AiChat(val message: String) : ParsedCommand()
}

@Singleton
class CommandParser @Inject constructor() {

    fun parse(input: String): ParsedCommand {
        val text = input.lowercase().trim()

        return when {
            text.matches(Regex(".*(call|phone)\\s*kar.*")) -> {
                val name = text.replace(Regex(".*(call|phone)\\s*kar\\s*"), "")
                    .replace(Regex("(ko|ruhan|\\s)+$"), "").trim()
                val n = text.replace(Regex("^.*(ruhan\\s+)"), "")
                    .replace(Regex("\\s*(ko\\s+)?(call|phone)\\s*kar.*"), "").trim()
                ParsedCommand.Call(n.ifBlank { name })
            }

            text.matches(Regex(".*ko\\s+(call|phone)\\s*kar.*")) -> {
                val name = text.replace(Regex("(ruhan\\s+)"), "")
                    .replace(Regex("\\s*ko\\s+(call|phone)\\s*kar.*"), "").trim()
                ParsedCommand.Call(name)
            }

            text.matches(Regex(".*whatsapp\\s*pe.*bhej.*")) -> {
                val parts = text.split(Regex("(bhej|—|-)"))
                val nameRaw = text.replace(Regex(".*whatsapp\\s*pe\\s*"), "")
                    .replace(Regex("\\s*ko\\s*bhej.*"), "").trim()
                val msg = if (parts.size > 1) parts.last().trim() else ""
                ParsedCommand.WhatsApp(nameRaw, msg)
            }

            text.matches(Regex(".*(message|sms|text)\\s*bhej.*")) -> {
                val parts = text.split(Regex("(bhej|—|-)"))
                val nameRaw = text.replace(Regex("(ruhan\\s+)"), "")
                    .replace(Regex("\\s*ko\\s+(message|sms|text)\\s*bhej.*"), "").trim()
                val msg = if (parts.size > 1) parts.last().trim() else ""
                ParsedCommand.Sms(nameRaw, msg)
            }

            text.matches(Regex(".*(khol|open|launch|start).*")) -> {
                val app = text.replace(Regex("(ruhan\\s+)"), "")
                    .replace(Regex("\\s*(khol|open|launch|start).*"), "").trim()
                ParsedCommand.OpenApp(app)
            }

            text.matches(Regex(".*(yaad\\s*dila|remind|reminder).*")) -> {
                val task = text.replace(Regex(".*(yaad\\s*dila|remind|reminder)\\s*"), "").trim()
                val minutes = extractTimeMinutes(text)
                ParsedCommand.Reminder(task.ifBlank { "Reminder" }, minutes)
            }

            text.matches(Regex(".*(wifi|wi-fi).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("wifi", action)
            }

            text.matches(Regex(".*bluetooth.*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("bluetooth", action)
            }

            text.matches(Regex(".*(flashlight|torch|flash).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("flashlight", action)
            }

            text.matches(Regex(".*(dnd|do not disturb|disturb).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("dnd", action)
            }

            text.matches(Regex(".*(airplane|flight|havaai).*")) -> {
                ParsedCommand.Setting("airplane", "on")
            }

            text.matches(Regex(".*(hotspot|tethering).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("hotspot", action)
            }

            text.matches(Regex(".*(location|gps)\\s*(on|off|band|chalu).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("location", action)
            }

            text.matches(Regex(".*(battery\\s*saver|power\\s*saving).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("battery_saver", action)
            }

            text.matches(Regex(".*(dark\\s*mode|night\\s*mode).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("dark_mode", action)
            }

            text.matches(Regex(".*(silent|vibrate)\\s*(mode)?.*")) -> {
                ParsedCommand.Setting("silent", "on")
            }

            text.matches(Regex(".*(mobile\\s*data|data)\\s*(on|off|chalu|band).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("mobile_data", action)
            }

            text.matches(Regex(".*(nfc)\\s*(on|off|chalu|band).*")) -> {
                val action = if (text.contains("on") || text.contains("chalu")) "on" else "off"
                ParsedCommand.Setting("nfc", action)
            }

            text.matches(Regex(".*brightness.*(\\d+).*%.*")) -> {
                val level = Regex("(\\d+)").find(text)?.value?.toIntOrNull() ?: 50
                ParsedCommand.Brightness(level)
            }

            text.matches(Regex(".*brightness.*(badha|increase|zyada|max).*")) -> ParsedCommand.Brightness(100)
            text.matches(Regex(".*brightness.*(kam|decrease|low|min).*")) -> ParsedCommand.Brightness(20)

            text.matches(Regex(".*volume.*(\\d+).*%.*")) -> {
                val level = Regex("(\\d+)").find(text)?.value?.toIntOrNull() ?: 50
                ParsedCommand.Volume(level)
            }

            text.matches(Regex(".*volume.*(badha|increase|zyada|max).*")) -> ParsedCommand.Volume(100)
            text.matches(Regex(".*volume.*(kam|decrease|low|min|mute).*")) -> ParsedCommand.Volume(0)

            text.matches(Regex(".*(screen|yeh\\s*kya|dekh|analyze|kya\\s*hai|kya\\s*likha).*")) &&
                    (text.contains("screen") || text.contains("yeh") || text.contains("dekh")) ->
                ParsedCommand.ScreenAnalysis

            text.matches(Regex(".*(battery|charge)\\s*(kitni|level|status|kya).*")) ->
                ParsedCommand.BatteryInfo

            text.matches(Regex(".*(time|samay|baj|waqt).*")) -> ParsedCommand.TimeInfo
            text.matches(Regex(".*(date|tarikh|din)\\s*(kya|batao).*")) -> ParsedCommand.DateInfo

            text.matches(Regex(".*(network|signal|internet|wifi.*status|connection).*")) ->
                ParsedCommand.NetworkInfo

            text.matches(Regex(".*(emergency|help|bachao|madad).*")) -> ParsedCommand.Emergency

            text.matches(Regex(".*(yaad\\s*rakho|remember|mera.*hai).*")) ->
                ParsedCommand.Remember(text)

            text.matches(Regex(".*(kya\\s*pata|kya\\s*yaad|memory|memories|yaad).*")) ->
                ParsedCommand.ShowMemories

            text.matches(Regex(".*(research|investigate|detailed.*report).*")) -> {
                val topic = text.replace(Regex("(ruhan\\s+|research\\s*karo\\s*|par\\s*)"), "").trim()
                ParsedCommand.Research(topic)
            }

            text.matches(Regex(".*(search|dhundho|google|latest).*")) -> {
                val query = text.replace(Regex("(ruhan\\s+|search\\s*karo?\\s*|dhundho\\s*)"), "").trim()
                ParsedCommand.WebSearch(query)
            }

            text.matches(Regex(".*(location|jagah).*(bhej|share|send).*")) -> {
                val name = text.replace(Regex(".*(location|jagah)\\s*"), "")
                    .replace(Regex("\\s*(ko\\s*)?(bhej|share|send).*"), "").trim()
                ParsedCommand.ShareLocation(name)
            }

            text.matches(Regex(".*(kaise\\s*jaun|navigate|direction|rasta).*")) -> {
                val place = text.replace(Regex("(ruhan\\s+|kaise\\s*jaun\\s*|navigate\\s*to\\s*)"), "").trim()
                ParsedCommand.Navigate(place)
            }

            text.matches(Regex(".*(note\\s*karo|note\\s*bana|add\\s*note).*")) -> {
                val content = text.replace(Regex("(ruhan\\s+|note\\s*karo\\s*|note\\s*bana\\s*)"), "")
                    .replace("—", "").replace("-", "").trim()
                ParsedCommand.Note(content)
            }

            text.matches(Regex(".*(notes?\\s*dikha|show\\s*notes?|mere\\s*notes?).*")) ->
                ParsedCommand.ShowNotes

            text.matches(Regex(".*(email|gmail)\\s*(check|dekh|padh).*")) -> ParsedCommand.CheckEmails

            text.matches(Regex(".*(email|gmail).*bhej.*")) -> {
                val parts = text.split(Regex("(—|-)"))
                val name = text.replace(Regex("(ruhan\\s+|.*ko\\s+email\\s*bhej)"), "").trim()
                ParsedCommand.Email(
                    recipient = name,
                    subject = if (parts.size > 1) parts[1].trim() else "",
                    body = if (parts.size > 2) parts[2].trim() else ""
                )
            }

            text.matches(Regex(".*(system|health|diagnostic|phone.*report).*")) ->
                ParsedCommand.SystemDiagnostics

            text.matches(Regex(".*(wifi\\s*scan|network\\s*scan).*")) -> ParsedCommand.WifiScan

            text.matches(Regex(".*(live\\s*voice|live\\s*baat|real.*time.*baat).*")) ->
                ParsedCommand.LiveVoice

            else -> ParsedCommand.AiChat(input)
        }
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
