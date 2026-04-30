package com.ruhan.ai.assistant.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.premium.NotionManager
import com.ruhan.ai.assistant.util.PreferencesManager
import com.ruhan.ai.assistant.voice.RuhanVoiceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val bossName: String = "Boss",
    val wakeWord: String = "hello ruhan",
    val alwaysListening: Boolean = false,
    val floatingButton: Boolean = false,
    val voiceSpeed: Float = 0.9f,
    val voicePitch: Float = 0.85f,
    val voiceGender: String = "male",
    val dailyBriefingEnabled: Boolean = false,
    val dailyBriefingHour: Int = 8,
    val dailyBriefingMinute: Int = 0,
    val emergencyContact: String = "",
    val groqApiKey: String = "",
    val geminiApiKey: String = "",
    val huggingFaceApiKey: String = "",
    val tavilyApiKey: String = "",
    val language: String = "hinglish",
    val theme: String = "hacker",
    val groqKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val geminiKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val hfKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val tavilyKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val notionApiKey: String = "",
    val notionDatabaseId: String = "",
    val notionKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val biometricLockEnabled: Boolean = false,
    val fakeCrashEnabled: Boolean = false,
    val breakInPhotoEnabled: Boolean = false,
    val memoryEncryption: Boolean = true,
    val wakeSensitivity: Float = 0.5f
)

enum class KeyTestStatus {
    IDLE, TESTING, SUCCESS, FAILED
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val aiRepository: AIRepository,
    private val voiceEngine: RuhanVoiceEngine,
    private val notionManager: NotionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(loadSettings())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun loadSettings(): SettingsUiState {
        return SettingsUiState(
            bossName = preferencesManager.bossName,
            wakeWord = preferencesManager.wakeWord,
            alwaysListening = preferencesManager.alwaysListening,
            floatingButton = preferencesManager.floatingButtonEnabled,
            voiceSpeed = preferencesManager.voiceSpeed,
            voicePitch = preferencesManager.voicePitch,
            voiceGender = preferencesManager.voiceGender,
            dailyBriefingEnabled = preferencesManager.dailyBriefingEnabled,
            dailyBriefingHour = preferencesManager.dailyBriefingHour,
            dailyBriefingMinute = preferencesManager.dailyBriefingMinute,
            emergencyContact = preferencesManager.emergencyContact,
            groqApiKey = preferencesManager.groqApiKey,
            geminiApiKey = preferencesManager.geminiApiKey,
            huggingFaceApiKey = preferencesManager.huggingFaceApiKey,
            tavilyApiKey = preferencesManager.tavilyApiKey,
            notionApiKey = preferencesManager.notionApiKey,
            notionDatabaseId = preferencesManager.notionDatabaseId,
            language = preferencesManager.language,
            theme = preferencesManager.theme,
            biometricLockEnabled = preferencesManager.biometricLockEnabled,
            fakeCrashEnabled = preferencesManager.fakeCrashEnabled,
            breakInPhotoEnabled = preferencesManager.breakInPhotoEnabled,
            memoryEncryption = preferencesManager.memoryEncryption,
            wakeSensitivity = preferencesManager.wakeSensitivity
        )
    }

    fun updateBossName(name: String) {
        preferencesManager.bossName = name
        _uiState.value = _uiState.value.copy(bossName = name)
    }

    fun updateWakeWord(word: String) {
        preferencesManager.wakeWord = word
        _uiState.value = _uiState.value.copy(wakeWord = word)
    }

    fun toggleAlwaysListening(enabled: Boolean) {
        preferencesManager.alwaysListening = enabled
        _uiState.value = _uiState.value.copy(alwaysListening = enabled)
    }

    fun toggleFloatingButton(enabled: Boolean) {
        preferencesManager.floatingButtonEnabled = enabled
        _uiState.value = _uiState.value.copy(floatingButton = enabled)
    }

    fun updateVoiceSpeed(speed: Float) {
        preferencesManager.voiceSpeed = speed
        _uiState.value = _uiState.value.copy(voiceSpeed = speed)
        voiceEngine.updateSettings()
    }

    fun updateVoicePitch(pitch: Float) {
        preferencesManager.voicePitch = pitch
        _uiState.value = _uiState.value.copy(voicePitch = pitch)
        voiceEngine.updateSettings()
    }

    fun updateVoiceGender(gender: String) {
        preferencesManager.voiceGender = gender
        _uiState.value = _uiState.value.copy(voiceGender = gender)
        voiceEngine.updateSettings()
    }

    fun toggleDailyBriefing(enabled: Boolean) {
        preferencesManager.dailyBriefingEnabled = enabled
        _uiState.value = _uiState.value.copy(dailyBriefingEnabled = enabled)
    }

    fun updateBriefingTime(hour: Int, minute: Int) {
        preferencesManager.dailyBriefingHour = hour
        preferencesManager.dailyBriefingMinute = minute
        _uiState.value = _uiState.value.copy(
            dailyBriefingHour = hour,
            dailyBriefingMinute = minute
        )
    }

    fun updateEmergencyContact(contact: String) {
        preferencesManager.emergencyContact = contact
        _uiState.value = _uiState.value.copy(emergencyContact = contact)
    }

    fun updateGroqKey(key: String) {
        preferencesManager.groqApiKey = key
        _uiState.value = _uiState.value.copy(groqApiKey = key, groqKeyStatus = KeyTestStatus.IDLE)
    }

    fun updateGeminiKey(key: String) {
        preferencesManager.geminiApiKey = key
        _uiState.value = _uiState.value.copy(geminiApiKey = key, geminiKeyStatus = KeyTestStatus.IDLE)
    }

    fun updateHuggingFaceKey(key: String) {
        preferencesManager.huggingFaceApiKey = key
        _uiState.value = _uiState.value.copy(huggingFaceApiKey = key, hfKeyStatus = KeyTestStatus.IDLE)
    }

    fun updateTavilyKey(key: String) {
        preferencesManager.tavilyApiKey = key
        _uiState.value = _uiState.value.copy(tavilyApiKey = key, tavilyKeyStatus = KeyTestStatus.IDLE)
    }

    fun updateLanguage(language: String) {
        preferencesManager.language = language
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun updateTheme(theme: String) {
        preferencesManager.theme = theme
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun toggleBiometricLock(enabled: Boolean) {
        preferencesManager.biometricLockEnabled = enabled
        _uiState.value = _uiState.value.copy(biometricLockEnabled = enabled)
    }

    fun toggleFakeCrash(enabled: Boolean) {
        preferencesManager.fakeCrashEnabled = enabled
        _uiState.value = _uiState.value.copy(fakeCrashEnabled = enabled)
    }

    fun toggleBreakInPhoto(enabled: Boolean) {
        preferencesManager.breakInPhotoEnabled = enabled
        _uiState.value = _uiState.value.copy(breakInPhotoEnabled = enabled)
    }

    fun toggleMemoryEncryption(enabled: Boolean) {
        preferencesManager.memoryEncryption = enabled
        _uiState.value = _uiState.value.copy(memoryEncryption = enabled)
    }

    fun updateWakeSensitivity(sensitivity: Float) {
        preferencesManager.wakeSensitivity = sensitivity
        _uiState.value = _uiState.value.copy(wakeSensitivity = sensitivity)
    }

    fun testVoice() {
        voiceEngine.testVoice()
    }

    fun testGroqKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(groqKeyStatus = KeyTestStatus.TESTING)
            val success = aiRepository.testGroqKey(_uiState.value.groqApiKey)
            _uiState.value = _uiState.value.copy(
                groqKeyStatus = if (success) KeyTestStatus.SUCCESS else KeyTestStatus.FAILED
            )
        }
    }

    fun testGeminiKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(geminiKeyStatus = KeyTestStatus.TESTING)
            val success = aiRepository.testGeminiKey(_uiState.value.geminiApiKey)
            _uiState.value = _uiState.value.copy(
                geminiKeyStatus = if (success) KeyTestStatus.SUCCESS else KeyTestStatus.FAILED
            )
        }
    }

    fun testHuggingFaceKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(hfKeyStatus = KeyTestStatus.TESTING)
            val success = aiRepository.testHuggingFaceKey(_uiState.value.huggingFaceApiKey)
            _uiState.value = _uiState.value.copy(
                hfKeyStatus = if (success) KeyTestStatus.SUCCESS else KeyTestStatus.FAILED
            )
        }
    }

    fun testTavilyKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(tavilyKeyStatus = KeyTestStatus.TESTING)
            val success = aiRepository.testTavilyKey(_uiState.value.tavilyApiKey)
            _uiState.value = _uiState.value.copy(
                tavilyKeyStatus = if (success) KeyTestStatus.SUCCESS else KeyTestStatus.FAILED
            )
        }
    }

    fun updateNotionApiKey(key: String) {
        preferencesManager.notionApiKey = key
        _uiState.value = _uiState.value.copy(notionApiKey = key, notionKeyStatus = KeyTestStatus.IDLE)
    }

    fun updateNotionDatabaseId(id: String) {
        preferencesManager.notionDatabaseId = id
        _uiState.value = _uiState.value.copy(notionDatabaseId = id, notionKeyStatus = KeyTestStatus.IDLE)
    }

    fun testNotionKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(notionKeyStatus = KeyTestStatus.TESTING)
            val success = notionManager.testConnection()
            _uiState.value = _uiState.value.copy(
                notionKeyStatus = if (success) KeyTestStatus.SUCCESS else KeyTestStatus.FAILED
            )
        }
    }
}
