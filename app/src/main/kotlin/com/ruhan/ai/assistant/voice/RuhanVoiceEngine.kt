package com.ruhan.ai.assistant.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
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
    private var currentAudioTrack: AudioTrack? = null

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

        if (isMale) {
            engine.setPitch((basePitch * 0.85f).coerceIn(0.5f, 2.0f))
        } else {
            engine.setPitch((basePitch * 1.2f).coerceIn(0.5f, 2.0f))
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

        val networkHindiVoices = voices.filter { isHindiLocale(it) && it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val localHindiVoices = voices.filter { isHindiLocale(it) && !it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val networkIndianEnglish = voices.filter { isIndianEnglish(it) && it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val localIndianEnglish = voices.filter { isIndianEnglish(it) && !it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val networkEnglish = voices.filter { it.locale.language == "en" && it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }
        val localEnglish = voices.filter { it.locale.language == "en" && !it.isNetworkConnectionRequired }
            .sortedByDescending { it.quality }

        val allCandidates = networkHindiVoices + localHindiVoices +
                networkIndianEnglish + localIndianEnglish +
                networkEnglish + localEnglish

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

                val response = huggingFaceApiService.textToSpeech(
                    authHeader = "Bearer $apiKey",
                    request = HuggingFaceRequest(inputs = text)
                )

                val audioBytes = response.bytes()
                if (audioBytes.isEmpty()) {
                    isSpeaking = false
                    onDone()
                    return@withContext false
                }

                playWavAudio(audioBytes)

                isSpeaking = false
                onDone()
                true
            } catch (_: Exception) {
                isSpeaking = false
                onDone()
                false
            }
        }
    }

    private fun playWavAudio(audioBytes: ByteArray) {
        try {
            val inputStream = BufferedInputStream(ByteArrayInputStream(audioBytes))

            // Parse WAV header
            val header = ByteArray(44)
            val headerRead = inputStream.read(header)
            if (headerRead < 44) {
                inputStream.close()
                return
            }

            val sampleRate = (header[24].toInt() and 0xFF) or
                    ((header[25].toInt() and 0xFF) shl 8) or
                    ((header[26].toInt() and 0xFF) shl 16) or
                    ((header[27].toInt() and 0xFF) shl 24)
            val bitsPerSample = (header[34].toInt() and 0xFF) or
                    ((header[35].toInt() and 0xFF) shl 8)
            val numChannels = (header[22].toInt() and 0xFF) or
                    ((header[23].toInt() and 0xFF) shl 8)

            val channelMask = if (numChannels == 2) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO
            val encoding = if (bitsPerSample == 16) AudioFormat.ENCODING_PCM_16BIT else AudioFormat.ENCODING_PCM_FLOAT

            val actualRate = if (sampleRate in 8000..48000) sampleRate else 16000

            val pcmData = inputStream.readBytes()
            inputStream.close()

            if (pcmData.isEmpty()) return

            val minBuf = AudioTrack.getMinBufferSize(actualRate, channelMask, encoding)
            if (minBuf == AudioTrack.ERROR || minBuf == AudioTrack.ERROR_BAD_VALUE) return

            stopCurrentAudio()

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(actualRate)
                        .setChannelMask(channelMask)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, pcmData.size))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            currentAudioTrack = track
            track.write(pcmData, 0, pcmData.size)
            track.play()

            val bytesPerFrame = (bitsPerSample / 8) * numChannels
            val totalFrames = if (bytesPerFrame > 0) pcmData.size / bytesPerFrame else 0
            while (track.playbackHeadPosition < totalFrames && isSpeaking) {
                Thread.sleep(50)
            }

            track.stop()
            track.release()
            if (currentAudioTrack == track) currentAudioTrack = null
        } catch (_: Exception) {
        }
    }

    private fun stopCurrentAudio() {
        try {
            currentAudioTrack?.stop()
            currentAudioTrack?.release()
        } catch (_: Exception) {}
        currentAudioTrack = null
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

        // Try HuggingFace TTS first (better quality Hindi voice)
        if (hasHuggingFaceKey()) {
            val success = speakWithHuggingFace(text, onStart, onDone)
            if (success) return
        }

        // Fallback to device TTS
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
        stopCurrentAudio()
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
