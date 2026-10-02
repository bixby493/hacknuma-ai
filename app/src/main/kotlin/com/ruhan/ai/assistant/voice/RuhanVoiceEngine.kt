package com.ruhan.ai.assistant.voice

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.ruhan.ai.assistant.data.remote.HuggingFaceApiService
import com.ruhan.ai.assistant.data.remote.HuggingFaceRequest
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class RuhanVoiceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val huggingFaceApiService: HuggingFaceApiService
) {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var isSpeaking = false
    private var mediaPlayer: MediaPlayer? = null

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
        val englishInLocale = Locale("en", "IN")
        val englishLocale = Locale.ENGLISH

        val result = engine.setLanguage(hindiLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            val result2 = engine.setLanguage(englishInLocale)
            if (result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(englishLocale)
            }
        }

        applyVoiceSettings(engine)
    }

    private fun applyVoiceSettings(engine: TextToSpeech) {
        val isMale = preferencesManager.voiceGender == "male"
        val basePitch = preferencesManager.voicePitch
        val baseSpeed = preferencesManager.voiceSpeed

        // Natural pitch — less extreme modifications for better quality
        if (isMale) {
            engine.setPitch((basePitch * 0.9f).coerceIn(0.5f, 2.0f))
        } else {
            engine.setPitch((basePitch * 1.1f).coerceIn(0.5f, 2.0f))
        }
        engine.setSpeechRate(baseSpeed)

        val voices = engine.voices ?: return

        val isHindiLocale = { v: Voice ->
            v.locale.language == "hi" ||
                    v.name.contains("hi-in", ignoreCase = true) ||
                    v.name.contains("hi_in", ignoreCase = true) ||
                    v.name.contains("hindi", ignoreCase = true)
        }

        val isIndianEnglish = { v: Voice ->
            (v.locale.language == "en" && v.locale.country == "IN") ||
                    v.name.contains("en-in", ignoreCase = true) ||
                    v.name.contains("en_in", ignoreCase = true)
        }

        // Prioritize high quality voices — quality 400+ is "very high"
        val isHighQuality = { v: Voice -> v.quality >= 300 }

        // Build priority list: HQ network > HQ local > any network > any local
        val networkHindiHQ = voices.filter { isHindiLocale(it) && it.isNetworkConnectionRequired && isHighQuality(it) }
            .sortedByDescending { it.quality }
        val localHindiHQ = voices.filter { isHindiLocale(it) && !it.isNetworkConnectionRequired && isHighQuality(it) }
            .sortedByDescending { it.quality }
        val networkHindi = voices.filter { isHindiLocale(it) && it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val localHindi = voices.filter { isHindiLocale(it) && !it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val networkIndianEnglishHQ = voices.filter { isIndianEnglish(it) && it.isNetworkConnectionRequired && isHighQuality(it) }
            .sortedByDescending { it.quality }
        val localIndianEnglishHQ = voices.filter { isIndianEnglish(it) && !it.isNetworkConnectionRequired && isHighQuality(it) }
            .sortedByDescending { it.quality }
        val networkIndianEnglish = voices.filter { isIndianEnglish(it) && it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val localIndianEnglish = voices.filter { isIndianEnglish(it) && !it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val networkEnglish = voices.filter { it.locale.language == "en" && it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val localEnglish = voices.filter { it.locale.language == "en" && !it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }

        val allCandidates = networkHindiHQ + localHindiHQ + networkHindi + localHindi +
                networkIndianEnglishHQ + localIndianEnglishHQ +
                networkIndianEnglish + localIndianEnglish +
                networkEnglish + localEnglish

        val selectedVoice: Voice? = if (isMale) {
            // For male: prefer voices without "female" in name
            allCandidates.firstOrNull { it.name.contains("male", ignoreCase = true) && !it.name.contains("female", ignoreCase = true) }
                ?: allCandidates.firstOrNull { !it.name.contains("female", ignoreCase = true) }
                ?: allCandidates.firstOrNull()
        } else {
            // For female: prefer voices with "female" in name
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

    private fun hasHuggingFaceKey(): Boolean {
        return preferencesManager.huggingFaceApiKey.isNotBlank()
    }

    private suspend fun speakWithHuggingFace(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit
    ): Boolean {
        val apiKey = preferencesManager.huggingFaceApiKey
        if (apiKey.isBlank()) return false

        return withContext(Dispatchers.IO) {
            try {
                onStart()
                isSpeaking = true

                // Try XTTS v2 first (natural voice), then MMS Hindi
                val models = listOf(
                    "https://api-inference.huggingface.co/models/coqui/XTTS-v2",
                    "https://api-inference.huggingface.co/models/facebook/mms-tts-hin"
                )

                var audioBytes: ByteArray? = null
                for (modelUrl in models) {
                    try {
                        val response = huggingFaceApiService.textToSpeechCustomModel(
                            url = modelUrl,
                            authHeader = "Bearer $apiKey",
                            request = HuggingFaceRequest(inputs = text)
                        )
                        val bytes = response.bytes()
                        if (bytes.size > 100) {
                            audioBytes = bytes
                            break
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }

                if (audioBytes == null || audioBytes.size < 100) {
                    isSpeaking = false
                    return@withContext false
                }

                val tempFile = File(context.cacheDir, "hf_tts_${System.currentTimeMillis()}.wav")
                tempFile.writeBytes(audioBytes)

                withContext(Dispatchers.Main) {
                    try {
                        stopMediaPlayer()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(tempFile.absolutePath)
                            setOnCompletionListener {
                                isSpeaking = false
                                onDone()
                                tempFile.delete()
                                it.release()
                                if (mediaPlayer == it) mediaPlayer = null
                            }
                            setOnErrorListener { mp, _, _ ->
                                isSpeaking = false
                                onDone()
                                tempFile.delete()
                                mp.release()
                                if (mediaPlayer == mp) mediaPlayer = null
                                true
                            }
                            prepare()
                            start()
                        }
                    } catch (_: Exception) {
                        isSpeaking = false
                        onDone()
                        tempFile.delete()
                    }
                }

                true
            } catch (_: Exception) {
                isSpeaking = false
                false
            }
        }
    }

    private fun stopMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    suspend fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {}
    ) {
        if (text.isBlank()) {
            onDone()
            return
        }

        if (hasHuggingFaceKey()) {
            try {
                val success = speakWithHuggingFace(text, onStart, onDone)
                if (success) return
            } catch (_: Exception) {}
        }

        if (!isReady) {
            repeat(50) {
                if (isReady) return@repeat
                delay(100)
            }
        }

        if (!isReady) {
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
        stopMediaPlayer()
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
