package com.ruhan.ai.assistant.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ruhan_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var groqApiKey: String
        get() = prefs.getString(KEY_GROQ_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GROQ_API, value).apply()

    var geminiApiKey: String
        get() = prefs.getString(KEY_GEMINI_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GEMINI_API, value).apply()

    var huggingFaceApiKey: String
        get() = prefs.getString(KEY_HF_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_HF_API, value).apply()

    var tavilyApiKey: String
        get() = prefs.getString(KEY_TAVILY_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TAVILY_API, value).apply()

    var bossName: String
        get() = prefs.getString(KEY_BOSS_NAME, "Boss") ?: "Boss"
        set(value) = prefs.edit().putString(KEY_BOSS_NAME, value).apply()

    var wakeWord: String
        get() = prefs.getString(KEY_WAKE_WORD, "hello ruhan") ?: "hello ruhan"
        set(value) = prefs.edit().putString(KEY_WAKE_WORD, value).apply()

    var alwaysListening: Boolean
        get() = prefs.getBoolean(KEY_ALWAYS_LISTENING, false)
        set(value) = prefs.edit().putBoolean(KEY_ALWAYS_LISTENING, value).apply()

    var floatingButtonEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLOATING_BUTTON, false)
        set(value) = prefs.edit().putBoolean(KEY_FLOATING_BUTTON, value).apply()

    var voiceSpeed: Float
        get() = prefs.getFloat(KEY_VOICE_SPEED, 0.95f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_SPEED, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var biometricLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, value).apply()

    var fakeCrashEnabled: Boolean
        get() = prefs.getBoolean(KEY_FAKE_CRASH, true)
        set(value) = prefs.edit().putBoolean(KEY_FAKE_CRASH, value).apply()

    var breakInPhotoEnabled: Boolean
        get() = prefs.getBoolean(KEY_BREAK_IN_PHOTO, true)
        set(value) = prefs.edit().putBoolean(KEY_BREAK_IN_PHOTO, value).apply()

    var dailyBriefingEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_BRIEFING, false)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_BRIEFING, value).apply()

    var memoryEncryption: Boolean
        get() = prefs.getBoolean(KEY_MEMORY_ENCRYPTION, true)
        set(value) = prefs.edit().putBoolean(KEY_MEMORY_ENCRYPTION, value).apply()

    var wakeSensitivity: Float
        get() = prefs.getFloat(KEY_WAKE_SENSITIVITY, 0.5f)
        set(value) = prefs.edit().putFloat(KEY_WAKE_SENSITIVITY, value).apply()

    var dailyBriefingHour: Int
        get() = prefs.getInt(KEY_BRIEFING_HOUR, 8)
        set(value) = prefs.edit().putInt(KEY_BRIEFING_HOUR, value).apply()

    var dailyBriefingMinute: Int
        get() = prefs.getInt(KEY_BRIEFING_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_BRIEFING_MINUTE, value).apply()

    var emergencyContact: String
        get() = prefs.getString(KEY_EMERGENCY_CONTACT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMERGENCY_CONTACT, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "hinglish") ?: "hinglish"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var theme: String
        get() = prefs.getString(KEY_THEME, "amoled") ?: "amoled"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var isFirstBoot: Boolean
        get() = prefs.getBoolean(KEY_FIRST_BOOT, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_BOOT, value).apply()

    var voiceGender: String
        get() = prefs.getString(KEY_VOICE_GENDER, "male") ?: "male"
        set(value) = prefs.edit().putString(KEY_VOICE_GENDER, value).apply()

    fun hasGroqKey(): Boolean = groqApiKey.isNotBlank()
    fun hasGeminiKey(): Boolean = geminiApiKey.isNotBlank()
    fun hasHuggingFaceKey(): Boolean = huggingFaceApiKey.isNotBlank()
    fun hasTavilyKey(): Boolean = tavilyApiKey.isNotBlank()

    companion object {
        private const val KEY_GROQ_API = "groq_api_key"
        private const val KEY_GEMINI_API = "gemini_api_key"
        private const val KEY_HF_API = "huggingface_api_key"
        private const val KEY_TAVILY_API = "tavily_api_key"
        private const val KEY_BOSS_NAME = "boss_name"
        private const val KEY_WAKE_WORD = "wake_word"
        private const val KEY_ALWAYS_LISTENING = "always_listening"
        private const val KEY_FLOATING_BUTTON = "floating_button"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_BIOMETRIC_LOCK = "biometric_lock"
        private const val KEY_FAKE_CRASH = "fake_crash"
        private const val KEY_BREAK_IN_PHOTO = "break_in_photo"
        private const val KEY_DAILY_BRIEFING = "daily_briefing"
        private const val KEY_MEMORY_ENCRYPTION = "memory_encryption"
        private const val KEY_WAKE_SENSITIVITY = "wake_sensitivity"
        private const val KEY_BRIEFING_HOUR = "briefing_hour"
        private const val KEY_BRIEFING_MINUTE = "briefing_minute"
        private const val KEY_EMERGENCY_CONTACT = "emergency_contact"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_THEME = "theme"
        private const val KEY_FIRST_BOOT = "first_boot"
        private const val KEY_VOICE_GENDER = "voice_gender"
    }
}
