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

        val hindiLocale = Locale("hi", "IN")
        val result = engine.setLanguage(hindiLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale("en", "IN"))
        }

        applyVoiceSettings(engine)
    }

    private fun applyVoiceSettings(engine: TextToSpeech) {
        val isMale = preferencesManager.voiceGender == "male"
        val basePitch = preferencesManager.voicePitch
        val baseSpeed = preferencesManager.voiceSpeed

        engine.setPitch(if (isMale) basePitch else basePitch + 0.35f)
        engine.setSpeechRate(baseSpeed)

        val voices = engine.voices ?: return

        val hindiVoices = voices.filter { v ->
            v.locale.language == "hi" ||
                    v.name.contains("hi-in", ignoreCase = true) ||
                    v.name.contains("hi_in", ignoreCase = true) ||
                    v.name.contains("hindi", ignoreCase = true)
        }.sortedByDescending { it.quality }

        val indianEnglishVoices = voices.filter { v ->
            (v.locale.language == "en" && v.locale.country == "IN") ||
                    v.name.contains("en-in", ignoreCase = true) ||
                    v.name.contains("en_in", ignoreCase = true)
        }.sortedByDescending { it.quality }

        val allCandidates = hindiVoices + indianEnglishVoices

        val selectedVoice: Voice? = if (isMale) {
            allCandidates.firstOrNull { it.name.contains("male", ignoreCase = true) && !it.name.contains("female", ignoreCase = true) }
                ?: allCandidates.firstOrNull { !it.name.contains("female", ignoreCase = true) }
                ?: allCandidates.firstOrNull()
        } else {
            allCandidates.firstOrNull { it.name.contains("female", ignoreCase = true) }
                ?: allCandidates.lastOrNull()
                ?: allCandidates.firstOrNull()
        }

        selectedVoice?.let { engine.voice = it }
    }

    fun updateSettings() {
        if (!isReady) return
        val engine = tts ?: return
        applyVoiceSettings(engine)
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
