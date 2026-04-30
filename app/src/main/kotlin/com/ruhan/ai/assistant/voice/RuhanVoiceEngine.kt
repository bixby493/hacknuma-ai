package com.ruhan.ai.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RuhanVoiceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var isSpeaking = false

    fun initialize(onReady: () -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                configureTts()
                onReady()
            }
        }
    }

    private fun configureTts() {
        val engine = tts ?: return
        engine.language = Locale("hi", "IN")
        engine.setPitch(preferencesManager.voicePitch)
        engine.setSpeechRate(preferencesManager.voiceSpeed)

        val hindiVoice: Voice? = engine.voices?.filter { voice ->
            voice.locale.language == "hi" && !voice.isNetworkConnectionRequired
        }?.minByOrNull { it.quality }

        val fallbackVoice: Voice? = engine.voices?.filter { voice ->
            voice.name.contains("hi-in", ignoreCase = true) ||
                    voice.name.contains("hindi", ignoreCase = true)
        }?.firstOrNull()

        (hindiVoice ?: fallbackVoice)?.let { engine.voice = it }
    }

    fun updateSettings() {
        if (!isReady) return
        tts?.setPitch(preferencesManager.voicePitch)
        tts?.setSpeechRate(preferencesManager.voiceSpeed)
    }

    suspend fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        if (!isReady || text.isBlank()) {
            onDone()
            return
        }
        onStart()
        isSpeaking = true
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val utteranceId = "RUHAN_${System.currentTimeMillis()}"
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}

                    override fun onDone(id: String?) {
                        isSpeaking = false
                        onDone()
                        if (cont.isActive) cont.resume(Unit)
                    }

                    @Deprecated("Deprecated")
                    override fun onError(id: String?) {
                        isSpeaking = false
                        onDone()
                        if (cont.isActive) cont.resume(Unit)
                    }

                    override fun onError(id: String?, errorCode: Int) {
                        isSpeaking = false
                        onDone()
                        if (cont.isActive) cont.resume(Unit)
                    }
                })

                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

                cont.invokeOnCancellation {
                    tts?.stop()
                    isSpeaking = false
                    onDone()
                }
            }
        }
    }

    fun speakImmediate(text: String) {
        if (!isReady) return
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RUHAN_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    fun isSpeakingNow(): Boolean = isSpeaking

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    fun testVoice() {
        speakImmediate("Namaste ${preferencesManager.bossName}, main Ruhan hoon. Meri awaaz kaisi lag rahi hai?")
    }
}
