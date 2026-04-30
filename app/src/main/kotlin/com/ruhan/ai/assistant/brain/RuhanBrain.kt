package com.ruhan.ai.assistant.brain

import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.phone.SettingsController
import com.ruhan.ai.assistant.premium.LocationManager
import com.ruhan.ai.assistant.premium.NotesManager
import com.ruhan.ai.assistant.research.DeepResearch
import com.ruhan.ai.assistant.screen.ScreenAnalyzer
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

sealed class BrainResponse {
    data class Speak(val text: String) : BrainResponse()
    data class Confirm(val question: String, val onConfirm: suspend () -> String) : BrainResponse()
    data class Navigate(val route: String) : BrainResponse()
    data class StartLiveVoice(val message: String = "Live voice session shuru ho rahi hai, Boss.") : BrainResponse()
}

@Singleton
class RuhanBrain @Inject constructor(
    private val commandParser: CommandParser,
    private val aiRepository: AIRepository,
    private val phoneRepository: PhoneRepository,
    private val memoryManager: MemoryManager,
    private val settingsController: SettingsController,
    private val notesManager: NotesManager,
    private val locationManager: LocationManager,
    private val deepResearch: DeepResearch,
    private val screenAnalyzer: ScreenAnalyzer,
    private val preferencesManager: PreferencesManager
) {
    private val boss get() = preferencesManager.bossName

    suspend fun process(input: String): BrainResponse {
        return try {
            processInternal(input)
        } catch (e: Exception) {
            BrainResponse.Speak("$boss, ek error aa gaya: ${e.message ?: "unknown error"}. Dobara try karo.")
        }
    }

    private suspend fun processInternal(input: String): BrainResponse {
        if (memoryManager.parseAndStore(input)) {
            return BrainResponse.Speak("Yaad rakh liya, $boss!")
        }

        return when (val cmd = commandParser.parse(input)) {
            is ParsedCommand.Call -> handleCall(cmd.name)
            is ParsedCommand.Sms -> handleSms(cmd.name, cmd.message)
            is ParsedCommand.WhatsApp -> handleWhatsApp(cmd.name, cmd.message)
            is ParsedCommand.OpenApp -> handleOpenApp(cmd.appName)
            is ParsedCommand.Reminder -> handleReminder(cmd.task, cmd.delayMinutes)
            is ParsedCommand.Setting -> handleSetting(cmd.setting, cmd.action)
            is ParsedCommand.Brightness -> handleBrightness(cmd.level)
            is ParsedCommand.Volume -> handleVolume(cmd.level)
            is ParsedCommand.WebSearch -> handleSearch(cmd.query)
            is ParsedCommand.Research -> handleResearch(cmd.topic)
            is ParsedCommand.Remember -> BrainResponse.Speak("Yaad rakh liya, $boss!")
            is ParsedCommand.ShowMemories -> {
                val summary = memoryManager.getMemorySummary()
                BrainResponse.Speak(summary)
            }
            is ParsedCommand.ScreenAnalysis -> handleScreenAnalysis()
            is ParsedCommand.BatteryInfo -> {
                val level = settingsController.getBatteryLevel()
                BrainResponse.Speak("$boss, $level% battery bachi hai.")
            }
            is ParsedCommand.TimeInfo -> {
                val time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date())
                BrainResponse.Speak("$boss, abhi $time baj rahe hain.")
            }
            is ParsedCommand.DateInfo -> {
                val date = java.text.SimpleDateFormat("dd MMMM yyyy, EEEE", java.util.Locale("hi", "IN"))
                    .format(java.util.Date())
                BrainResponse.Speak("$boss, aaj $date hai.")
            }
            is ParsedCommand.NetworkInfo -> {
                val info = settingsController.getNetworkInfo()
                BrainResponse.Speak(info)
            }
            is ParsedCommand.Emergency -> handleEmergency()
            is ParsedCommand.Note -> handleNote(cmd.content)
            is ParsedCommand.ShowNotes -> BrainResponse.Navigate("notes")
            is ParsedCommand.ShareLocation -> handleShareLocation(cmd.name)
            is ParsedCommand.Navigate -> handleNavigate(cmd.place)
            is ParsedCommand.Email -> handleEmail(cmd.recipient, cmd.subject, cmd.body)
            is ParsedCommand.CheckEmails -> BrainResponse.Speak("$boss, Gmail integration ke liye Settings mein Gmail connect karo.")
            is ParsedCommand.SystemDiagnostics -> handleDiagnostics()
            is ParsedCommand.WifiScan -> handleWifiScan()
            is ParsedCommand.LiveVoice -> BrainResponse.StartLiveVoice()
            is ParsedCommand.PhoneHealthReport -> handleDiagnostics()
            is ParsedCommand.AiChat -> handleAiChat(cmd.message)
        }
    }

    private suspend fun handleCall(name: String): BrainResponse {
        val resolved = memoryManager.resolveNickname(name) ?: name
        val contact = phoneRepository.findContact(resolved)
            ?: return BrainResponse.Speak("$boss, $resolved ka number nahi mila.")

        return BrainResponse.Confirm(
            "$boss, ${contact.name} ko call karun? Number: ${contact.phoneNumber}"
        ) {
            phoneRepository.makeCall(contact.phoneNumber)
            "Call laga raha hoon ${contact.name} ko, $boss."
        }
    }

    private suspend fun handleSms(name: String, message: String): BrainResponse {
        val resolved = memoryManager.resolveNickname(name) ?: name
        val contact = phoneRepository.findContact(resolved)
            ?: return BrainResponse.Speak("$boss, $resolved ka number nahi mila.")

        return BrainResponse.Confirm(
            "$boss, ${contact.name} ko message bhejun: \"$message\"?"
        ) {
            phoneRepository.sendSms(contact.phoneNumber, message)
            "Message bhej diya, $boss."
        }
    }

    private fun handleWhatsApp(name: String, message: String): BrainResponse {
        return BrainResponse.Confirm(
            "$boss, WhatsApp pe $name ko bhejun: \"$message\"?"
        ) {
            phoneRepository.openWhatsApp(name, message)
            "WhatsApp khol raha hoon, $boss."
        }
    }

    private fun handleOpenApp(appName: String): BrainResponse {
        val opened = phoneRepository.openApp(appName)
        return if (opened) {
            BrainResponse.Speak("$appName khol raha hoon, $boss.")
        } else {
            BrainResponse.Speak("$boss, $appName nahi mila phone mein.")
        }
    }

    private fun handleReminder(task: String, delayMinutes: Int): BrainResponse {
        phoneRepository.setReminder(task, 0, 0, delayMinutes)
        val timeStr = if (delayMinutes >= 60) {
            "${delayMinutes / 60} ghante baad"
        } else {
            "$delayMinutes minute baad"
        }
        return BrainResponse.Speak("Ho gaya $boss, $timeStr yaad dila dunga: $task")
    }

    private fun handleSetting(setting: String, action: String): BrainResponse {
        val result = settingsController.toggleSetting(setting, action == "on")
        return BrainResponse.Speak(result)
    }

    private fun handleBrightness(level: Int): BrainResponse {
        settingsController.setBrightnessLevel(level)
        return BrainResponse.Speak("$boss, brightness $level% set kar di.")
    }

    private fun handleVolume(level: Int): BrainResponse {
        settingsController.setVolumeLevel(level)
        return BrainResponse.Speak("$boss, volume $level% set kar di.")
    }

    private suspend fun handleSearch(query: String): BrainResponse {
        val result = aiRepository.searchWeb(query)
        return BrainResponse.Speak(result ?: "$boss, search mein kuch nahi mila.")
    }

    private suspend fun handleResearch(topic: String): BrainResponse {
        val report = deepResearch.research(topic)
        return BrainResponse.Speak(report)
    }

    private suspend fun handleScreenAnalysis(): BrainResponse {
        return BrainResponse.Speak("$boss, screen analysis ke liye screen capture permission chahiye. Settings mein enable karo.")
    }

    private fun handleEmergency(): BrainResponse {
        val contact = preferencesManager.emergencyContact
        if (contact.isBlank()) {
            return BrainResponse.Speak("$boss, emergency contact set nahi hai! Settings mein set karo.")
        }
        phoneRepository.sendSms(contact, "EMERGENCY! Help needed. - Sent by Ruhan AI")
        phoneRepository.makeCall(contact)
        return BrainResponse.Speak("$boss, emergency contact ko SMS aur call bhej raha hoon!")
    }

    private suspend fun handleNote(content: String): BrainResponse {
        notesManager.addNote(content)
        return BrainResponse.Speak("Note save ho gaya, $boss.")
    }

    private fun handleShareLocation(name: String): BrainResponse {
        locationManager.shareLocation(name)
        return BrainResponse.Speak("$boss, location share kar raha hoon $name ko.")
    }

    private fun handleNavigate(place: String): BrainResponse {
        locationManager.navigate(place)
        return BrainResponse.Speak("$boss, $place ka rasta dikha raha hoon.")
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleEmail(recipient: String, subject: String, body: String): BrainResponse {
        return BrainResponse.Speak("$boss, Gmail integration ke liye Settings mein Gmail connect karo.")
    }

    private fun handleDiagnostics(): BrainResponse {
        val report = settingsController.getSystemDiagnostics()
        return BrainResponse.Speak(report)
    }

    private fun handleWifiScan(): BrainResponse {
        val result = settingsController.scanWifi()
        return BrainResponse.Speak(result)
    }

    private suspend fun handleAiChat(message: String): BrainResponse {
        val context = memoryManager.getAllMemories()
            .take(5)
            .joinToString("\n") { "${it.key}: ${it.value}" }

        val extraContext = if (context.isNotBlank()) "\n\nMemory context:\n$context" else ""
        val response = aiRepository.chat(message + extraContext)
        return BrainResponse.Speak(response)
    }
}
