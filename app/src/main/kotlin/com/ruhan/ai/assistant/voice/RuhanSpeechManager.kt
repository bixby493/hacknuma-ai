package com.ruhan.ai.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuhanSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isContinuousMode = false
    private val handler = Handler(Looper.getMainLooper())

    var onPartialResult: ((String) -> Unit)? = null
    var onFinalResult: ((String) -> Unit)? = null
    var onListeningStarted: (() -> Unit)? = null
    var onListeningStopped: (() -> Unit)? = null
    var onError: ((Int) -> Unit)? = null

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(
                RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,
                arrayListOf("hi-IN", "en-IN")
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                3000L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                2000L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                1500L
            )
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            onListeningStarted?.invoke()
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            isListening = false
        }

        override fun onError(error: Int) {
            isListening = false
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    if (isContinuousMode) {
                        handler.postDelayed({ restartListening() }, 500)
                    } else {
                        onListeningStopped?.invoke()
                    }
                }
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    onError?.invoke(error)
                    onListeningStopped?.invoke()
                }
                SpeechRecognizer.ERROR_CLIENT -> {
                    onListeningStopped?.invoke()
                }
                else -> {
                    if (isContinuousMode) {
                        handler.postDelayed({ restartListening() }, 1000)
                    } else {
                        onListeningStopped?.invoke()
                    }
                }
            }
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()?.trim() ?: ""
            if (text.isNotBlank()) {
                // Strip wake word if present, but process ALL speech
                val wakeWord = preferencesManager.wakeWord.lowercase()
                var command = text.lowercase()
                    .replace("hello ruhan", "")
                    .replace("ruhan sun", "")
                    .replace(wakeWord, "")
                    .replace("ruhan", "")
                    .replace("ruha", "")
                    .replace("rohan", "")
                    .trim()
                if (command.isBlank()) command = text

                // Always process the command
                onFinalResult?.invoke(command)
            }
            if (isContinuousMode) {
                handler.postDelayed({ restartListening() }, 500)
            } else {
                onListeningStopped?.invoke()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()?.trim() ?: ""
            if (text.isNotBlank()) {
                onPartialResult?.invoke(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        isContinuousMode = false
        initAndStart()
    }

    fun startContinuousListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        isContinuousMode = true
        initAndStart()
    }

    private fun initAndStart() {
        handler.post {
            destroyRecognizer()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
            try {
                speechRecognizer?.startListening(createRecognizerIntent())
            } catch (e: SecurityException) {
                onError?.invoke(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)
            }
        }
    }

    private fun restartListening() {
        if (!isContinuousMode) return
        initAndStart()
    }

    fun stopListening() {
        isContinuousMode = false
        isListening = false
        handler.post {
            speechRecognizer?.stopListening()
            onListeningStopped?.invoke()
        }
    }

    fun destroy() {
        isContinuousMode = false
        isListening = false
        handler.removeCallbacksAndMessages(null)
        handler.post { destroyRecognizer() }
    }

    private fun destroyRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun isCurrentlyListening(): Boolean = isListening
}
