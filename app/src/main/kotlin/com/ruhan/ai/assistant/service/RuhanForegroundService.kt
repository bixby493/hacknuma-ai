package com.ruhan.ai.assistant.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.ruhan.ai.assistant.MainActivity
import com.ruhan.ai.assistant.R
import com.ruhan.ai.assistant.RuhanApp
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RuhanForegroundService : Service() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        if (preferencesManager.alwaysListening) {
            startWakeWordListening()
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, RuhanApp.CHANNEL_SERVICE)
            .setContentTitle("Ruhan AI")
            .setContentText("Main yahin hoon, ${preferencesManager.bossName}.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun startWakeWordListening() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        } catch (_: Exception) { return }
        if (isListening) return

        try { speechRecognizer?.destroy() } catch (_: Exception) {}
        speechRecognizer = try {
            SpeechRecognizer.createSpeechRecognizer(this)
        } catch (_: Exception) { return }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                if (preferencesManager.alwaysListening) {
                    android.os.Handler(mainLooper).postDelayed({
                        startWakeWordListening()
                    }, 1000)
                }
            }

            override fun onResults(results: android.os.Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                val wakeWord = preferencesManager.wakeWord.lowercase()

                if (text.contains(wakeWord) || text.contains("ruhan sun") || text.contains("hello ruhan")) {
                    val mainIntent = Intent(this@RuhanForegroundService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("wake_word_triggered", true)
                        putExtra("voice_command", text.substringAfter(wakeWord).trim())
                    }
                    startActivity(mainIntent)
                }

                if (preferencesManager.alwaysListening) {
                    startWakeWordListening()
                }
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: SecurityException) {
            isListening = false
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        isListening = false
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
