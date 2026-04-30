package com.ruhan.ai.assistant.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val voiceSpeed: Float = 1.0f,
    val dailyBriefingHour: Int = 8,
    val dailyBriefingMinute: Int = 0,
    val emergencyContact: String = "",
    val groqApiKey: String = "",
    val geminiApiKey: String = "",
    val huggingFaceApiKey: String = "",
    val tavilyApiKey: String = "",
    val language: String = "hinglish",
    val theme: String = "amoled",
    val groqKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val geminiKeyStatus: KeyTestStatus = KeyTestStatus.IDLE,
    val hfKeyStatus: KeyTestStatus = KeyTestStatus.IDLE
)

enum class KeyTestStatus {
    IDLE, TESTING, SUCCESS, FAILED
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val aiRepository: AIRepository
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
            dailyBriefingHour = preferencesManager.dailyBriefingHour,
            dailyBriefingMinute = preferencesManager.dailyBriefingMinute,
            emergencyContact = preferencesManager.emergencyContact,
            groqApiKey = preferencesManager.groqApiKey,
            geminiApiKey = preferencesManager.geminiApiKey,
            huggingFaceApiKey = preferencesManager.huggingFaceApiKey,
            tavilyApiKey = preferencesManager.tavilyApiKey,
            language = preferencesManager.language,
            theme = preferencesManager.theme
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
        _uiState.value = _uiState.value.copy(tavilyApiKey = key)
    }

    fun updateLanguage(language: String) {
        preferencesManager.language = language
        _uiState.value = _uiState.value.copy(language = language)
    }

    fun updateTheme(theme: String) {
        preferencesManager.theme = theme
        _uiState.value = _uiState.value.copy(theme = theme)
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
}
