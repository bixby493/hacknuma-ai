package com.ruhan.ai.assistant.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeWordDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isActive = false
    private var onWakeWordDetected: ((String) -> Unit)? = null

    fun start(callback: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        onWakeWordDetected = callback
        isActive = true
        startListening()
    }

    fun stop() {
        isActive = false
        onWakeWordDetected = null
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun startListening() {
        if (!isActive) return

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                if (isActive) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startListening()
                    }, 1000)
                }
            }

            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                val wakeWord = preferencesManager.wakeWord.lowercase()

                if (text.contains(wakeWord) || text.contains("ruhan sun") || text.contains("hello ruhan")) {
                    val command = text
                        .replace(wakeWord, "")
                        .replace("ruhan sun", "")
                        .replace("hello ruhan", "")
                        .trim()
                    onWakeWordDetected?.invoke(command)
                }

                if (isActive) {
                    startListening()
                }
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: SecurityException) {
            isActive = false
        }
    }
}
