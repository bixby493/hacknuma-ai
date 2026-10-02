package com.ruhan.ai.assistant.brain

import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.data.repository.ConversationRepository
import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.phone.AppUsageTracker
import com.ruhan.ai.assistant.phone.EmergencyShakeDetector
import com.ruhan.ai.assistant.phone.FileScanner
import com.ruhan.ai.assistant.phone.SettingsController
import com.ruhan.ai.assistant.phone.SmartClipboard
import com.ruhan.ai.assistant.security.AntiTamperGuard
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
    private val conversationRepository: ConversationRepository,
    private val phoneRepository: PhoneRepository,
    private val memoryManager: MemoryManager,
    private val settingsController: SettingsController,
    private val fileScanner: FileScanner,
    private val antiTamperGuard: AntiTamperGuard,
    private val appUsageTracker: AppUsageTracker,
    private val smartClipboard: SmartClipboard,
    private val emergencyShakeDetector: EmergencyShakeDetector,
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

    private fun randomGreeting(): String {
        val b = boss
        return listOf(
            "Haan $b, bolo! Kya karna hai aaj?",
            "Ji $b, main hoon. Kaise madad karun?",
            "Bolo $b, sab tayaar hai. Koi kaam ho toh batao.",
            "Haan ji $b! Aaj kya plan hai?",
            "Main yahaan hoon $b. Bas ek lafz kahiye.",
            "$b, aapka Ruhan hazir hai. Farmaaiye!",
            "Ji janab! Bataiye kya kar sakta hoon aapke liye?",
            "Suno $b, aaj ka vichaar — Mehnat ka koi shortcut nahi hota. Ab bolo, kya karna hai?",
            "$b, aapki ek awaaz pe hazir! Kya hukum hai?",
            "Haan $b! Naya din, naye iraade. Shuru karein?",
            "Ji $b, RUHAN ready hai. Koi bhi kaam bolo!",
            "$b, aap bole aur main karu. Batao!",
            "Assalamu Alaikum $b! Aaj kya karna hai humein?",
            "Bolo $b, duniya hila dete hain aaj!",
            "$b, main hamesha yahan hoon. Bolo kya chahiye?",
            "Ji $b! Ek shayri sunein — Himmat waale ko haar nahi milti, dhoondhne waale ko darr nahi milti. Ab kaam batao!",
            "$b, good vibes only! Aaj kya explore karna hai?",
            "Haan $b, taiyaar hoon. Bas ishara karo!",
            "Hello $b! Aaj ka mood kaisa hai? Koi kaam batao!",
            "Ji $b! Ruhan hamesha aapke saath hai. Kya madad chahiye?"
        ).random()
    }

    private fun isGreeting(text: String): Boolean {
        val t = text.lowercase().trim()
        return t.isEmpty() ||
            t == "ruhan" || t == "ruha" || t == "rohan" ||
            t == "hello" || t == "hi" || t == "hey" ||
            t.matches(Regex("^(hello|hi|hey|namaste|salam|assalamu|haan|bolo)\\s*(ruhan|ruha|rohan)?\\s*$"))
    }

    private suspend fun processInternal(input: String): BrainResponse {
        if (isGreeting(input)) {
            return BrainResponse.Speak(randomGreeting())
        }

        if (memoryManager.parseAndStore(input)) {
            return BrainResponse.Speak("Yaad rakh liya, $boss!")
        }

        return when (val cmd = commandParser.parse(input)) {
            is ParsedCommand.Call -> handleCall(cmd.name)
            is ParsedCommand.Sms -> handleSms(cmd.name, cmd.message)
            is ParsedCommand.WhatsApp -> handleWhatsApp(cmd.name, cmd.message)
            is ParsedCommand.OpenApp -> handleOpenApp(cmd.appName)
            is ParsedCommand.CloseApp -> handleCloseApp(cmd.appName)
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
            is ParsedCommand.AnswerCall -> BrainResponse.Speak("$boss, call answer karne ke liye phone ka button use karo.")
            is ParsedCommand.RejectCall -> BrainResponse.Speak("$boss, call reject kar raha hoon.")
            is ParsedCommand.RecentCallLog -> handleRecentCallLog()
            is ParsedCommand.StorageInfo -> handleStorageInfo()
            is ParsedCommand.SecurityCheck -> handleSecurityCheck()
            is ParsedCommand.ClearNotifications -> BrainResponse.Speak("$boss, notifications clear karne ke liye notification panel se swipe karo.")
            is ParsedCommand.TakeScreenshot -> BrainResponse.Speak("$boss, screenshot lene ke liye Power + Volume Down dabao.")
            is ParsedCommand.RecordAudio -> BrainResponse.Speak("$boss, audio recording ${cmd.duration} seconds ke liye shuru.")
            is ParsedCommand.PhoneHealthReport -> handleDiagnostics()
            is ParsedCommand.Weather -> handleWeather(cmd.location)
            is ParsedCommand.PlayMusic -> handlePlayMusic(cmd.query)
            is ParsedCommand.SetAlarm -> handleSetAlarm(cmd.hour, cmd.minute, cmd.label)
            is ParsedCommand.SetTimer -> handleSetTimer(cmd.seconds)
            is ParsedCommand.Translate -> handleTranslate(cmd.text, cmd.targetLang)
            is ParsedCommand.Calculate -> handleCalculate(cmd.expression)
            is ParsedCommand.ScanFiles -> handleScanFiles()
            is ParsedCommand.CleanJunk -> handleCleanJunk()
            is ParsedCommand.ClearCache -> handleClearCache()
            is ParsedCommand.FindFile -> handleFindFile(cmd.query)
            is ParsedCommand.ScreenTimeReport -> handleScreenTime()
            is ParsedCommand.ClipboardHistory -> handleClipboardHistory()
            is ParsedCommand.ClipboardSearch -> handleClipboardSearch(cmd.query)
            is ParsedCommand.EmergencyStatus -> handleEmergencyStatus()
            is ParsedCommand.ClapDetectionOn -> handleClapDetection("on")
            is ParsedCommand.ClapDetectionOff -> handleClapDetection("off")
            is ParsedCommand.ClapDetectionStatus -> handleClapDetection("status")
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

    private fun handleCloseApp(appName: String): BrainResponse {
        return BrainResponse.Speak("$boss, $appName band karne ki koshish kar raha hoon. Recent apps se hatao.")
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

    private fun handleRecentCallLog(): BrainResponse {
        return BrainResponse.Speak("$boss, recent call log check karne ke liye Phone app khol raha hoon.")
    }

    private fun handleStorageInfo(): BrainResponse {
        val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        val totalBytes = stat.blockSizeLong * stat.blockCountLong
        val freeBytes = stat.blockSizeLong * stat.availableBlocksLong
        val usedBytes = totalBytes - freeBytes
        val totalGB = String.format("%.1f", totalBytes / (1024.0 * 1024 * 1024))
        val usedGB = String.format("%.1f", usedBytes / (1024.0 * 1024 * 1024))
        val freeGB = String.format("%.1f", freeBytes / (1024.0 * 1024 * 1024))
        return BrainResponse.Speak("$boss, phone ki storage: Total $totalGB GB, Used $usedGB GB, Free $freeGB GB.")
    }

    private fun handleSecurityCheck(): BrainResponse {
        val lockEnabled = preferencesManager.isLockSetup
        val lockType = preferencesManager.lockType
        val lockStatus = if (lockEnabled) "$lockType lock ON" else "Koi lock nahi"
        val securityReport = antiTamperGuard.getSecuritySummary()
        return BrainResponse.Speak("$boss, Lock: $lockStatus. $securityReport")
    }

    private suspend fun handleWeather(location: String): BrainResponse {
        val query = if (location == "current") "current weather" else "weather in $location"
        val result = aiRepository.searchWeb(query)
        return BrainResponse.Speak(result ?: "$boss, weather info nahi mil rahi. Internet check karo.")
    }

    private fun handlePlayMusic(query: String): BrainResponse {
        val searchUrl = "https://www.youtube.com/results?search_query=${java.net.URLEncoder.encode(query, "UTF-8")}"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(searchUrl)).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            return BrainResponse.Speak("$boss, YouTube pe '$query' search kar raha hoon.")
        } catch (_: Exception) {
            return BrainResponse.Speak("$boss, music app nahi khul raha.")
        }
    }

    private fun handleSetAlarm(hour: Int, minute: Int, label: String): BrainResponse {
        val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
            putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
            putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, label)
            putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            return BrainResponse.Speak("$boss, alarm $hour:${String.format("%02d", minute)} pe set kar diya.")
        } catch (_: Exception) {
            return BrainResponse.Speak("$boss, alarm set nahi ho raha. Clock app check karo.")
        }
    }

    private fun handleSetTimer(seconds: Int): BrainResponse {
        val intent = android.content.Intent(android.provider.AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(android.provider.AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            val display = if (seconds >= 60) "${seconds / 60} minute" else "$seconds second"
            return BrainResponse.Speak("$boss, $display ka timer set kar diya.")
        } catch (_: Exception) {
            return BrainResponse.Speak("$boss, timer set nahi ho raha.")
        }
    }

    private suspend fun handleTranslate(text: String, targetLang: String): BrainResponse {
        val langName = if (targetLang == "hi") "Hindi" else "English"
        val prompt = "Translate this to $langName: \"$text\". Only give the translation, nothing else."
        val result = aiRepository.chat(prompt)
        return BrainResponse.Speak(result)
    }

    private suspend fun handleCalculate(expression: String): BrainResponse {
        val prompt = "Calculate: $expression. Give only the answer."
        val result = aiRepository.chat(prompt)
        return BrainResponse.Speak("$boss, answer hai: $result")
    }

    private suspend fun handleAiChat(message: String): BrainResponse {
        val memContext = memoryManager.getAllMemories()
            .take(5)
            .joinToString("\n") { "${it.key}: ${it.value}" }

        val history = try {
            conversationRepository.getContextMessages(10)
        } catch (_: Exception) {
            emptyList()
        }

        val extraContext = if (memContext.isNotBlank()) "\n\nMemory context:\n$memContext" else ""
        val response = aiRepository.chat(message + extraContext, history)
        return BrainResponse.Speak(response)
    }

    private fun handleScanFiles(): BrainResponse {
        return try {
            val result = fileScanner.scanFiles()
            BrainResponse.Speak("$boss, ${result.summary}")
        } catch (_: Exception) {
            BrainResponse.Speak("$boss, file scan mein storage permission chahiye. Settings se allow karo.")
        }
    }

    private fun handleCleanJunk(): BrainResponse {
        return BrainResponse.Confirm("$boss, kachra saaf karun? Cache, temp files, empty files sab delete honge.") {
            fileScanner.cleanJunk()
        }
    }

    private fun handleClearCache(): BrainResponse {
        return BrainResponse.Confirm("$boss, cache clear karun?") {
            fileScanner.clearCache()
        }
    }

    private fun handleFindFile(query: String): BrainResponse {
        return try {
            val result = fileScanner.findFile(query)
            BrainResponse.Speak(result)
        } catch (_: Exception) {
            BrainResponse.Speak("$boss, file dhundhne mein storage permission chahiye.")
        }
    }

    private fun handleScreenTime(): BrainResponse {
        return try {
            val summary = appUsageTracker.getUsageSummary()
            val warning = appUsageTracker.getOverdoseWarning()
            val response = if (warning != null) "$summary\n\n$warning" else summary
            BrainResponse.Speak("$boss, $response")
        } catch (_: Exception) {
            BrainResponse.Speak("$boss, screen time check karne ke liye Usage Access permission chahiye. Settings > Apps > Special Access > Usage Access.")
        }
    }

    private fun handleClipboardHistory(): BrainResponse {
        val summary = smartClipboard.getHistorySummary()
        return BrainResponse.Speak("$boss, $summary")
    }

    private fun handleClipboardSearch(query: String): BrainResponse {
        val result = smartClipboard.searchClipboard(query)
        return BrainResponse.Speak("$boss, $result")
    }

    private fun handleEmergencyStatus(): BrainResponse {
        val status = emergencyShakeDetector.getStatus()
        return BrainResponse.Speak("$boss, $status")
    }

    private fun handleClapDetection(action: String): BrainResponse {
        val ctx = context
        return when (action) {
            "on" -> {
                try {
                    val intent = android.content.Intent(ctx, com.ruhan.ai.assistant.phone.ClapDetectionService::class.java)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        ctx.startForegroundService(intent)
                    } else {
                        ctx.startService(intent)
                    }
                    BrainResponse.Speak("$boss, clap detection ON hai ab! Do baar tali bajao aur main active ho jaunga.")
                } catch (e: Throwable) {
                    BrainResponse.Speak("$boss, clap detection start nahi ho paya: ${e.message}")
                }
            }
            "off" -> {
                try {
                    ctx.stopService(android.content.Intent(ctx, com.ruhan.ai.assistant.phone.ClapDetectionService::class.java))
                    BrainResponse.Speak("$boss, clap detection band kar diya.")
                } catch (_: Throwable) {
                    BrainResponse.Speak("$boss, clap detection pehle se band hai.")
                }
            }
            else -> {
                val isActive = com.ruhan.ai.assistant.phone.ClapDetectionService.isActive
                if (isActive) {
                    BrainResponse.Speak("$boss, clap detection active hai. Do baar tali bajao toh main sun lunga!")
                } else {
                    BrainResponse.Speak("$boss, clap detection band hai. Bolo 'clap detection on karo' toh chalu kar dunga.")
                }
            }
        }
    }

    private val context get() = phoneRepository.getContext()
}
