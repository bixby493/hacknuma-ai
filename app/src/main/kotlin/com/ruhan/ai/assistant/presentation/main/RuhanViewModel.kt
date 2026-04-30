package com.ruhan.ai.assistant.presentation.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruhan.ai.assistant.brain.BrainResponse
import com.ruhan.ai.assistant.brain.RuhanBrain
import com.ruhan.ai.assistant.data.local.ConversationEntity
import com.ruhan.ai.assistant.data.repository.ConversationRepository
import com.ruhan.ai.assistant.presentation.components.OrbState
import com.ruhan.ai.assistant.util.PreferencesManager
import com.ruhan.ai.assistant.voice.GeminiLiveVoice
import com.ruhan.ai.assistant.voice.LiveVoiceState
import com.ruhan.ai.assistant.voice.RuhanSpeechManager
import com.ruhan.ai.assistant.voice.RuhanVoiceEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RuhanUiState(
    val orbState: OrbState = OrbState.IDLE,
    val statusText: String = "Idle — Say Hello Ruhan",
    val conversations: List<ConversationEntity> = emptyList(),
    val currentTranscript: String = "",
    val isListening: Boolean = false,
    val showKeyboard: Boolean = false,
    val textInput: String = "",
    val isFirstBoot: Boolean = true,
    val pendingConfirmation: String? = null,
    val groqStatus: Boolean = false,
    val geminiStatus: Boolean = false,
    val hfStatus: Boolean = false,
    val tavilyStatus: Boolean = false,
    val isLiveVoiceActive: Boolean = false,
    val liveTranscript: String = ""
)

@HiltViewModel
class RuhanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val brain: RuhanBrain,
    private val conversationRepository: ConversationRepository,
    private val speechManager: RuhanSpeechManager,
    private val voiceEngine: RuhanVoiceEngine,
    private val geminiLiveVoice: GeminiLiveVoice,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RuhanUiState())
    val uiState: StateFlow<RuhanUiState> = _uiState.asStateFlow()

    private var pendingOnConfirm: (suspend () -> String)? = null

    init {
        initializeVoice()
        loadConversations()
        checkFirstBoot()
        updateApiStatus()
        observeLiveVoice()
    }

    private fun initializeVoice() {
        voiceEngine.initialize()

        speechManager.onPartialResult = { text ->
            _uiState.value = _uiState.value.copy(currentTranscript = text)
        }

        speechManager.onFinalResult = { text ->
            _uiState.value = _uiState.value.copy(
                currentTranscript = "",
                isListening = false,
                orbState = OrbState.THINKING,
                statusText = "Thinking..."
            )
            processInput(text)
        }

        speechManager.onListeningStarted = {
            _uiState.value = _uiState.value.copy(
                orbState = OrbState.LISTENING,
                statusText = "Listening...",
                isListening = true
            )
        }

        speechManager.onListeningStopped = {
            if (_uiState.value.orbState == OrbState.LISTENING) {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.IDLE,
                    statusText = "Idle — Say Hello Ruhan",
                    isListening = false
                )
            }
        }

        speechManager.onError = { _ ->
            _uiState.value = _uiState.value.copy(
                isListening = false
            )
        }
    }

    private fun observeLiveVoice() {
        viewModelScope.launch {
            geminiLiveVoice.state.collect { state ->
                val orbState = when (state) {
                    LiveVoiceState.IDLE -> OrbState.IDLE
                    LiveVoiceState.CONNECTING -> OrbState.THINKING
                    LiveVoiceState.LISTENING -> OrbState.LISTENING
                    LiveVoiceState.PROCESSING -> OrbState.THINKING
                    LiveVoiceState.SPEAKING -> OrbState.SPEAKING
                    LiveVoiceState.ERROR -> OrbState.IDLE
                }
                _uiState.value = _uiState.value.copy(
                    orbState = orbState,
                    isLiveVoiceActive = state != LiveVoiceState.IDLE && state != LiveVoiceState.ERROR,
                    statusText = when (state) {
                        LiveVoiceState.IDLE -> "Idle — Say Hello Ruhan"
                        LiveVoiceState.CONNECTING -> "Connecting to Gemini Live..."
                        LiveVoiceState.LISTENING -> "Live Voice — Listening..."
                        LiveVoiceState.PROCESSING -> "Gemini processing..."
                        LiveVoiceState.SPEAKING -> "Ruhan speaking..."
                        LiveVoiceState.ERROR -> "Live voice error"
                    }
                )
                if (state == LiveVoiceState.ERROR) {
                    val errorMsg = geminiLiveVoice.errorMessage.value
                    if (errorMsg.isNotBlank()) {
                        val msg = "${preferencesManager.bossName}, $errorMsg"
                        viewModelScope.launch {
                            try { conversationRepository.saveMessage(msg, isUser = false) } catch (_: Exception) {}
                            speak(msg)
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            geminiLiveVoice.transcript.collect { text ->
                _uiState.value = _uiState.value.copy(liveTranscript = text)
            }
        }
    }

    private fun checkFirstBoot() {
        if (preferencesManager.isFirstBoot) {
            _uiState.value = _uiState.value.copy(isFirstBoot = true)
            val greeting = "Namaste ${preferencesManager.bossName}. Main Ruhan hoon — aapka personal AI assistant. Aaj main aapki kya madad kar sakta hoon?"
            viewModelScope.launch {
                conversationRepository.saveMessage(greeting, isUser = false)
                loadConversations()
                voiceEngine.speak(
                    greeting,
                    onStart = {
                        _uiState.value = _uiState.value.copy(
                            orbState = OrbState.SPEAKING,
                            statusText = "Speaking..."
                        )
                    },
                    onDone = {
                        _uiState.value = _uiState.value.copy(
                            orbState = OrbState.IDLE,
                            statusText = "Idle — Say Hello Ruhan"
                        )
                    }
                )
            }
            preferencesManager.isFirstBoot = false
        }
    }

    private fun loadConversations() {
        viewModelScope.launch {
            conversationRepository.getRecentConversations().collect { conversations ->
                _uiState.value = _uiState.value.copy(conversations = conversations)
            }
        }
    }

    fun updateApiStatus() {
        _uiState.value = _uiState.value.copy(
            groqStatus = preferencesManager.hasGroqKey(),
            geminiStatus = preferencesManager.hasGeminiKey(),
            hfStatus = preferencesManager.hasHuggingFaceKey(),
            tavilyStatus = preferencesManager.hasTavilyKey()
        )
    }

    fun startListening() {
        voiceEngine.stop()
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
        _uiState.value = _uiState.value.copy(
            orbState = OrbState.IDLE,
            statusText = "Idle — Say Hello Ruhan",
            isListening = false
        )
    }

    fun startLiveVoice() {
        speechManager.stopListening()
        voiceEngine.stop()
        geminiLiveVoice.startLiveSession()
    }

    fun stopLiveVoice() {
        geminiLiveVoice.stopLiveSession()
    }

    fun onTextInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(textInput = text)
    }

    fun toggleKeyboard() {
        _uiState.value = _uiState.value.copy(
            showKeyboard = !_uiState.value.showKeyboard
        )
    }

    fun submitTextInput() {
        val text = _uiState.value.textInput.trim()
        if (text.isBlank()) return
        _uiState.value = _uiState.value.copy(
            textInput = "",
            showKeyboard = false,
            orbState = OrbState.THINKING,
            statusText = "Thinking..."
        )
        processInput(text)
    }

    fun confirmAction() {
        val action = pendingOnConfirm ?: return
        pendingOnConfirm = null
        _uiState.value = _uiState.value.copy(
            pendingConfirmation = null,
            orbState = OrbState.THINKING,
            statusText = "Executing..."
        )
        viewModelScope.launch {
            val result = action()
            conversationRepository.saveMessage(result, isUser = false)
            speak(result)
        }
    }

    fun cancelAction() {
        pendingOnConfirm = null
        _uiState.value = _uiState.value.copy(
            pendingConfirmation = null,
            orbState = OrbState.IDLE,
            statusText = "Idle — Say Hello Ruhan"
        )
        viewModelScope.launch {
            val msg = "Cancel kar diya, ${preferencesManager.bossName}."
            conversationRepository.saveMessage(msg, isUser = false)
            speak(msg)
        }
    }

    private fun processInput(text: String) {
        viewModelScope.launch {
            try {
                conversationRepository.saveMessage(text, isUser = true)
            } catch (_: Exception) { }

            val response = try {
                brain.process(text)
            } catch (e: Exception) {
                BrainResponse.Speak("Boss, kuch gadbad ho gayi. Dobara try karo.")
            }
            when (response) {
                is BrainResponse.Speak -> {
                    conversationRepository.saveMessage(response.text, isUser = false)
                    speak(response.text)
                }

                is BrainResponse.Confirm -> {
                    pendingOnConfirm = response.onConfirm
                    _uiState.value = _uiState.value.copy(
                        pendingConfirmation = response.question,
                        orbState = OrbState.IDLE,
                        statusText = "Confirm karo..."
                    )
                    speak(response.question)
                }

                is BrainResponse.Navigate -> {
                    // Handle navigation to notes, research, etc.
                    _uiState.value = _uiState.value.copy(
                        orbState = OrbState.IDLE,
                        statusText = "Idle — Say Hello Ruhan"
                    )
                }

                is BrainResponse.StartLiveVoice -> {
                    speak(response.message)
                    startLiveVoice()
                }
            }
        }
    }

    private suspend fun speak(text: String) {
        voiceEngine.speak(
            text,
            onStart = {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.SPEAKING,
                    statusText = "Speaking..."
                )
            },
            onDone = {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.IDLE,
                    statusText = "Idle — Say Hello Ruhan"
                )
            }
        )
    }

    fun clearConversations() {
        viewModelScope.launch {
            conversationRepository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        voiceEngine.shutdown()
        geminiLiveVoice.stopLiveSession()
    }
}
