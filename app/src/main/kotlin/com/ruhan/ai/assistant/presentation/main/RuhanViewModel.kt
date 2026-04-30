package com.ruhan.ai.assistant.presentation.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruhan.ai.assistant.data.local.ConversationEntity
import com.ruhan.ai.assistant.data.repository.ConversationRepository
import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.domain.usecase.AnalyzeScreenUseCase
import com.ruhan.ai.assistant.domain.usecase.CommandResult
import com.ruhan.ai.assistant.domain.usecase.PendingAction
import com.ruhan.ai.assistant.domain.usecase.ProcessCommandUseCase
import com.ruhan.ai.assistant.presentation.components.OrbState
import com.ruhan.ai.assistant.util.PreferencesManager
import com.ruhan.ai.assistant.util.ScreenshotHelper
import com.ruhan.ai.assistant.util.VoiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class RuhanUiState(
    val orbState: OrbState = OrbState.IDLE,
    val statusText: String = "Idle — Say Hello Ruhan",
    val conversations: List<ConversationEntity> = emptyList(),
    val currentTranscript: String = "",
    val isListening: Boolean = false,
    val showKeyboard: Boolean = false,
    val textInput: String = "",
    val pendingAction: PendingAction? = null,
    val isFirstBoot: Boolean = true
)

@HiltViewModel
class RuhanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val processCommandUseCase: ProcessCommandUseCase,
    private val analyzeScreenUseCase: AnalyzeScreenUseCase,
    private val conversationRepository: ConversationRepository,
    private val phoneRepository: PhoneRepository,
    private val voiceManager: VoiceManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RuhanUiState())
    val uiState: StateFlow<RuhanUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        voiceManager.initialize()
        loadConversations()
        checkFirstBoot()
    }

    private fun checkFirstBoot() {
        if (preferencesManager.isFirstBoot) {
            _uiState.value = _uiState.value.copy(isFirstBoot = true)
            val greeting = "Namaste ${preferencesManager.bossName}. Main Ruhan hoon — aapka personal AI assistant. Aaj main aapki kya madad kar sakta hoon?"
            viewModelScope.launch {
                conversationRepository.saveMessage(greeting, isUser = false)
                voiceManager.speak(
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

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.LISTENING,
                    statusText = "Listening...",
                    isListening = true
                )
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.THINKING,
                    statusText = "Thinking...",
                    isListening = false
                )
            }

            override fun onError(error: Int) {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.IDLE,
                    statusText = "Idle — Say Hello Ruhan",
                    isListening = false,
                    currentTranscript = ""
                )
            }

            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: return
                _uiState.value = _uiState.value.copy(currentTranscript = "")
                processUserInput(text)
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: return
                _uiState.value = _uiState.value.copy(currentTranscript = text)
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: SecurityException) {
            _uiState.value = _uiState.value.copy(
                statusText = "Microphone permission needed"
            )
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _uiState.value = _uiState.value.copy(
            isListening = false,
            orbState = OrbState.IDLE,
            statusText = "Idle — Say Hello Ruhan"
        )
    }

    fun processUserInput(input: String) {
        viewModelScope.launch {
            conversationRepository.saveMessage(input, isUser = true)

            _uiState.value = _uiState.value.copy(
                orbState = OrbState.THINKING,
                statusText = "Thinking...",
                textInput = ""
            )

            // Check for confirmation responses
            val pendingAction = _uiState.value.pendingAction
            if (pendingAction != null) {
                val lower = input.lowercase()
                if (lower.contains("han") || lower.contains("yes") || lower.contains("haan") || lower.contains("kar")) {
                    val result = processCommandUseCase.executePendingAction(pendingAction)
                    _uiState.value = _uiState.value.copy(pendingAction = null)
                    respondWithVoice(result)
                    return@launch
                } else if (lower.contains("nahi") || lower.contains("no") || lower.contains("mat") || lower.contains("cancel")) {
                    _uiState.value = _uiState.value.copy(pendingAction = null)
                    respondWithVoice("Theek hai ${preferencesManager.bossName}, cancel kar diya.")
                    return@launch
                }
                _uiState.value = _uiState.value.copy(pendingAction = null)
            }

            val result = processCommandUseCase.execute(input)
            when (result) {
                is CommandResult.Speak -> {
                    when (result.text) {
                        "SCREEN_ANALYSIS_REQUESTED" -> {
                            respondWithVoice("${preferencesManager.bossName}, screen dekh raha hoon...")
                        }
                        "EMERGENCY_MODE" -> handleEmergency()
                        else -> respondWithVoice(result.text)
                    }
                }
                is CommandResult.AskConfirmation -> {
                    _uiState.value = _uiState.value.copy(pendingAction = result.action)
                    respondWithVoice(result.text)
                }
            }
        }
    }

    fun analyzeScreen(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                orbState = OrbState.THINKING,
                statusText = "Screen analyze kar raha hoon..."
            )
            val screenshot = ScreenshotHelper.captureScreen(activity)
            if (screenshot != null) {
                val analysis = analyzeScreenUseCase.execute(screenshot)
                respondWithVoice(analysis)
            } else {
                respondWithVoice("${preferencesManager.bossName}, screen capture nahi ho paya.")
            }
        }
    }

    private fun handleEmergency() {
        viewModelScope.launch {
            val emergencyContact = preferencesManager.emergencyContact
            if (emergencyContact.isNotBlank()) {
                try {
                    @Suppress("DEPRECATION")
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(
                        emergencyContact,
                        null,
                        "EMERGENCY! Mujhe madad chahiye. Yeh message mere AI assistant Ruhan ne bheja hai.",
                        null,
                        null
                    )
                } catch (e: Exception) {
                    // Ignore SMS failures in emergency
                }

                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$emergencyContact")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(callIntent)
                } catch (e: SecurityException) {
                    // Fall through
                }
                respondWithVoice("${preferencesManager.bossName}, emergency contact ko SMS aur call bhej raha hoon!")
            } else {
                respondWithVoice("${preferencesManager.bossName}, pehle Settings mein emergency contact set karo.")
            }
        }
    }

    private suspend fun respondWithVoice(text: String) {
        conversationRepository.saveMessage(text, isUser = false)
        voiceManager.speak(
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

    fun toggleKeyboard() {
        _uiState.value = _uiState.value.copy(
            showKeyboard = !_uiState.value.showKeyboard
        )
    }

    fun updateTextInput(text: String) {
        _uiState.value = _uiState.value.copy(textInput = text)
    }

    fun sendTextMessage() {
        val text = _uiState.value.textInput.trim()
        if (text.isNotBlank()) {
            _uiState.value = _uiState.value.copy(showKeyboard = false)
            processUserInput(text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        voiceManager.shutdown()
    }
}
