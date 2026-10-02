package com.ruhan.ai.assistant.premium

import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Free vs Premium features in RUHAN AI.
 *
 * FREE features (available to everyone):
 * - Voice commands (all 70+)
 * - 7 themes (Hacker, AMOLED, Dark, Pink, Blue, Gray, White)
 * - Dynamic greetings
 * - File scanner & cache cleaner
 * - Anti-tamper security
 * - Clipboard history
 * - Screen time tracking
 * - Emergency shake gesture
 * - Offline dictionary & quotes
 * - Sound effects
 * - Splash screen
 *
 * PREMIUM features (paid):
 * - Screen share / remote view
 * - Voice macro recorder
 * - Spam call therapy (AI auto-reply)
 * - Notification time travel
 * - One-shot behavior replication
 * - Ambient call context (OSINT)
 * - Emotion-adaptive display
 * - Priority support
 */
@Singleton
class PremiumManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    enum class Feature(val displayName: String, val isFree: Boolean) {
        VOICE_COMMANDS("Voice Commands (70+)", true),
        THEMES("7 Themes", true),
        GREETINGS("Dynamic Greetings", true),
        FILE_SCANNER("File Scanner & Cache Cleaner", true),
        ANTI_TAMPER("Anti-Tamper Security", true),
        CLIPBOARD("Smart Clipboard", true),
        SCREEN_TIME("Screen Time Tracking", true),
        EMERGENCY_SHAKE("Emergency Shake Gesture", true),
        SCREENSHOT_NARRATOR("Screenshot Narrator", true),
        OFFLINE_DATA("Offline Dictionary & Knowledge", true),
        SOUND_EFFECTS("Sound Effects", true),
        SPLASH_SCREEN("3D Splash Screen", true),
        DEEP_RESEARCH("Deep Web Research", true),
        AI_CHAT("AI Chat (Groq/Gemini)", true),

        // Premium
        SCREEN_SHARE("Screen Share / Remote View", false),
        MACRO_RECORDER("Voice Macro Recorder", false),
        SPAM_THERAPY("Spam Call Therapy", false),
        NOTIFICATION_TIME_TRAVEL("Notification Time Travel", false),
        BEHAVIOR_REPLICATION("One-Shot Behavior Replication", false),
        CALL_CONTEXT("Ambient Call Context", false),
        EMOTION_DISPLAY("Emotion-Adaptive Display", false),
        PRIORITY_SUPPORT("Priority Support", false),
    }

    fun isPremiumUser(): Boolean {
        return preferencesManager.isPremiumActivated()
    }

    fun isFeatureAvailable(feature: Feature): Boolean {
        return feature.isFree || isPremiumUser()
    }

    fun activatePremium(key: String): Boolean {
        // Simple activation - can be extended with server verification
        if (key.length >= 16) {
            preferencesManager.setPremiumActivated(true)
            preferencesManager.setPremiumKey(key)
            return true
        }
        return false
    }

    fun getFreeFeatures(): List<Feature> = Feature.entries.filter { it.isFree }
    fun getPremiumFeatures(): List<Feature> = Feature.entries.filter { !it.isFree }
}
