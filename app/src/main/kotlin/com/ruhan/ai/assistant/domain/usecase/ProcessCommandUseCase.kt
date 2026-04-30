package com.ruhan.ai.assistant.domain.usecase

import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.data.repository.ConversationRepository
import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.util.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class CommandResult {
    data class Speak(val text: String) : CommandResult()
    data class AskConfirmation(val text: String, val action: PendingAction) : CommandResult()
}

sealed class PendingAction {
    data class Call(val name: String, val number: String) : PendingAction()
    data class Sms(val name: String, val number: String, val message: String) : PendingAction()
    data class WhatsApp(val name: String, val number: String, val message: String) : PendingAction()
}

class ProcessCommandUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val phoneRepository: PhoneRepository,
    private val conversationRepository: ConversationRepository,
    private val preferencesManager: PreferencesManager
) {
    private val bossName: String get() = preferencesManager.bossName

    suspend fun execute(command: String): CommandResult {
        val lower = command.lowercase().trim()

        return when {
            // Phone calls
            lower.matches(Regex(".*(call kar|ko call|phone kar|dial kar).*")) -> handleCallCommand(lower)

            // SMS
            lower.matches(Regex(".*(message bhej|sms bhej|msg bhej|text bhej).*")) -> handleSmsCommand(lower)

            // WhatsApp
            lower.matches(Regex(".*(whatsapp.*bhej|whatsapp pe).*")) -> handleWhatsAppCommand(lower)

            // Open apps
            lower.matches(Regex(".*(khol|open|launch|start).*")) -> handleOpenAppCommand(lower)

            // Reminders
            lower.matches(Regex(".*(yaad dila|remind|reminder).*")) -> handleReminderCommand(lower, command)

            // WiFi
            lower.matches(Regex(".*(wifi|wi-fi).*")) -> {
                val enable = !lower.contains("off") && !lower.contains("band")
                phoneRepository.toggleWifi(enable)
                CommandResult.Speak("$bossName, WiFi ${if (enable) "on" else "off"} kar raha hoon.")
            }

            // Bluetooth
            lower.contains("bluetooth") -> {
                val enable = !lower.contains("off") && !lower.contains("band")
                phoneRepository.toggleBluetooth(enable)
                CommandResult.Speak("$bossName, Bluetooth settings khol raha hoon.")
            }

            // Brightness
            lower.matches(Regex(".*(brightness|chamak).*")) -> {
                val increase = lower.contains("badha") || lower.contains("increase") || lower.contains("zyada")
                phoneRepository.setBrightness(if (increase) 220 else 80)
                CommandResult.Speak("$bossName, brightness ${if (increase) "badha" else "kam"} di.")
            }

            // Volume
            lower.matches(Regex(".*(volume|awaaz|awaz).*")) -> {
                val increase = lower.contains("badha") || lower.contains("increase") || lower.contains("zyada")
                phoneRepository.adjustVolume(increase)
                CommandResult.Speak("$bossName, volume ${if (increase) "badha" else "kam"} di.")
            }

            // DND
            lower.matches(Regex(".*(do not disturb|dnd|disturb).*")) -> {
                val enable = !lower.contains("off") && !lower.contains("band") && !lower.contains("hata")
                phoneRepository.toggleDnd(enable)
                CommandResult.Speak("$bossName, Do Not Disturb ${if (enable) "on" else "off"} kar diya.")
            }

            // Flashlight
            lower.matches(Regex(".*(flashlight|torch|flash).*")) -> {
                val enable = !lower.contains("off") && !lower.contains("band")
                phoneRepository.toggleFlashlight(enable)
                CommandResult.Speak("$bossName, flashlight ${if (enable) "on" else "off"} kar di.")
            }

            // Airplane mode
            lower.matches(Regex(".*(airplane|flight mode|havaai).*")) -> {
                phoneRepository.toggleAirplaneMode()
                CommandResult.Speak("$bossName, airplane mode settings khol raha hoon.")
            }

            // Battery
            lower.matches(Regex(".*(battery|charge).*")) -> {
                val level = phoneRepository.getBatteryLevel()
                CommandResult.Speak("$bossName, $level% battery bachi hai.")
            }

            // Time
            lower.matches(Regex(".*(time|samay|waqt|baj).*")) -> {
                val sdf = SimpleDateFormat("h 'baj ke' mm 'minute'", Locale("hi", "IN"))
                val time = sdf.format(Date())
                CommandResult.Speak("$bossName, abhi $time hue hain.")
            }

            // Date
            lower.matches(Regex(".*(date|tarikh|din|aaj).*")) && !lower.contains("weather") -> {
                val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("hi", "IN"))
                val date = sdf.format(Date())
                CommandResult.Speak("$bossName, aaj $date hai.")
            }

            // Network
            lower.matches(Regex(".*(network|signal|internet).*")) -> {
                val info = phoneRepository.getNetworkInfo()
                CommandResult.Speak("$bossName, $info")
            }

            // Screen reading
            lower.matches(Regex(".*(yeh kya hai|screen|dekh|kya dikh).*")) -> {
                CommandResult.Speak("SCREEN_ANALYSIS_REQUESTED")
            }

            // Emergency
            lower.matches(Regex(".*(emergency|help|madad|bachao).*")) -> {
                CommandResult.Speak("EMERGENCY_MODE")
            }

            // Previous conversations
            lower.matches(Regex(".*(pehle|pichle|history|yaad).*kaha.*")) -> {
                val history = conversationRepository.getContextMessages(10)
                val summary = history.reversed().joinToString("\n") { msg ->
                    "${if (msg.isUser) "Aap" else "Main"}: ${msg.message}"
                }
                if (summary.isBlank()) {
                    CommandResult.Speak("$bossName, abhi tak koi conversation nahi hui.")
                } else {
                    val response = aiRepository.chat("User apni pichli conversations dekhna chahta hai. Yeh rahi history:\n$summary\nInka short summary do.", history)
                    CommandResult.Speak(response)
                }
            }

            // Web search
            lower.matches(Regex(".*(search|google|latest|news|khabar).*")) -> {
                val query = lower.replace(Regex(".*(search|google|latest|news|khabar)\\s*"), "").trim()
                val response = aiRepository.webSearch(query.ifBlank { command })
                CommandResult.Speak(response)
            }

            // Weather
            lower.contains("weather") || lower.contains("mausam") -> {
                val response = aiRepository.webSearch("current weather today")
                CommandResult.Speak(response)
            }

            // Default: ask AI
            else -> {
                val history = conversationRepository.getContextMessages()
                val response = aiRepository.chat(command, history)
                CommandResult.Speak(response)
            }
        }
    }

    private fun handleCallCommand(command: String): CommandResult {
        val namePattern = Regex("(.*?)\\s*(ko\\s+call|call\\s+kar|phone\\s+kar|dial\\s+kar)")
        val match = namePattern.find(command)
        val name = match?.groupValues?.get(1)?.trim()
            ?.replace(Regex("^(ruhan\\s+)"), "")
            ?.trim()
            ?: return CommandResult.Speak("$bossName, kisko call karna hai?")

        val contact = phoneRepository.findContact(name)
        return if (contact != null) {
            CommandResult.AskConfirmation(
                "$bossName, ${contact.name} ka number mila — ${contact.phoneNumber}. Call karun?",
                PendingAction.Call(contact.name, contact.phoneNumber)
            )
        } else {
            CommandResult.Speak("$bossName, '$name' naam ka koi contact nahi mila.")
        }
    }

    private fun handleSmsCommand(command: String): CommandResult {
        val pattern = Regex("(.*?)\\s*ko\\s*(message|sms|msg|text)\\s*bhej\\s*[-—]?\\s*(.*)", RegexOption.DOT_MATCHES_ALL)
        val match = pattern.find(command)
        if (match != null) {
            val name = match.groupValues[1].trim().replace(Regex("^(ruhan\\s+)"), "").trim()
            val message = match.groupValues[3].trim()
            val contact = phoneRepository.findContact(name)
            return if (contact != null) {
                CommandResult.AskConfirmation(
                    "$bossName, ${contact.name} ko '$message' bhejun?",
                    PendingAction.Sms(contact.name, contact.phoneNumber, message)
                )
            } else {
                CommandResult.Speak("$bossName, '$name' naam ka koi contact nahi mila.")
            }
        }
        return CommandResult.Speak("$bossName, kisko message bhejna hai aur kya likhna hai?")
    }

    private fun handleWhatsAppCommand(command: String): CommandResult {
        val pattern = Regex("whatsapp\\s*pe\\s*(.*?)\\s*ko\\s*bhej\\s*[-—]?\\s*(.*)", RegexOption.DOT_MATCHES_ALL)
        val match = pattern.find(command)
        if (match != null) {
            val name = match.groupValues[1].trim()
            val message = match.groupValues[2].trim()
            val contact = phoneRepository.findContact(name)
            return if (contact != null) {
                CommandResult.AskConfirmation(
                    "$bossName, WhatsApp pe ${contact.name} ko '$message' bhejun?",
                    PendingAction.WhatsApp(contact.name, contact.phoneNumber, message)
                )
            } else {
                CommandResult.Speak("$bossName, '$name' naam ka koi contact nahi mila.")
            }
        }
        return CommandResult.Speak("$bossName, WhatsApp pe kisko message bhejna hai?")
    }

    private fun handleOpenAppCommand(command: String): CommandResult {
        val appName = command
            .replace(Regex("(ruhan\\s+)"), "")
            .replace(Regex("(khol|open|launch|start)"), "")
            .trim()

        if (appName.isBlank()) {
            return CommandResult.Speak("$bossName, kaunsa app kholna hai?")
        }

        val opened = phoneRepository.openApp(appName)
        return if (opened) {
            CommandResult.Speak("$bossName, $appName khol raha hoon.")
        } else {
            CommandResult.Speak("$bossName, $appName nahi mila phone mein.")
        }
    }

    private fun handleReminderCommand(command: String, original: String): CommandResult {
        val timePattern = Regex("(\\d{1,2})\\s*baje")
        val timeMatch = timePattern.find(command)
        val hour = timeMatch?.groupValues?.get(1)?.toIntOrNull() ?: -1

        val isMorning = command.contains("subah") || command.contains("morning")
        val adjustedHour = when {
            hour < 0 -> -1
            isMorning && hour < 12 -> hour
            !isMorning && hour < 12 -> hour + 12
            else -> hour
        }

        val taskPattern = Regex("(yaad dila|remind|reminder)\\s*(.*)", RegexOption.DOT_MATCHES_ALL)
        val taskMatch = taskPattern.find(command)
        val task = taskMatch?.groupValues?.get(2)?.trim()
            ?.replace(Regex("(kal|aaj|subah|shaam|\\d+\\s*baje)"), "")
            ?.trim()
            ?.ifBlank { original }
            ?: original

        if (adjustedHour >= 0) {
            phoneRepository.setReminder(task, adjustedHour, 0)
            CommandResult.Speak("Ho gaya $bossName, $adjustedHour baje yaad dila dunga.")
        } else {
            phoneRepository.setReminder(task, 0, 0, delayMinutes = 60)
            CommandResult.Speak("$bossName, 1 ghante mein yaad dila dunga.")
        }
        return CommandResult.Speak("Ho gaya $bossName, reminder set kar diya.")
    }

    fun executePendingAction(action: PendingAction): String {
        return when (action) {
            is PendingAction.Call -> {
                phoneRepository.makeCall(action.number)
                "$bossName, ${action.name} ko call laga raha hoon."
            }
            is PendingAction.Sms -> {
                phoneRepository.sendSms(action.number, action.message)
                "Done $bossName, ${action.name} ko message chala gaya."
            }
            is PendingAction.WhatsApp -> {
                phoneRepository.openWhatsApp(action.number, action.message)
                "$bossName, WhatsApp khol raha hoon ${action.name} ke liye."
            }
        }
    }
}
