package com.ruhan.ai.assistant.util

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.ruhan.ai.assistant.data.remote.HuggingFaceApiService
import com.ruhan.ai.assistant.data.remote.HuggingFaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class VoiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val huggingFaceApiService: HuggingFaceApiService,
    private val preferencesManager: PreferencesManager
) {
    private var textToSpeech: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isTtsReady = false

    fun initialize() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                textToSpeech?.language = Locale("hi", "IN")
                textToSpeech?.setSpeechRate(preferencesManager.voiceSpeed)
                textToSpeech?.setPitch(0.85f)
            }
        }
    }

    suspend fun speak(text: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        onStart()
        val spoken = if (preferencesManager.hasHuggingFaceKey()) {
            tryHuggingFaceTts(text)
        } else {
            false
        }
        if (!spoken) {
            speakWithAndroidTts(text)
        }
        onDone()
    }

    private suspend fun tryHuggingFaceTts(text: String): Boolean {
        return try {
            val response = withContext(Dispatchers.IO) {
                huggingFaceApiService.textToSpeech(
                    authHeader = "Bearer ${preferencesManager.huggingFaceApiKey}",
                    request = HuggingFaceRequest(inputs = text)
                )
            }
            val audioFile = File(context.cacheDir, "ruhan_speech.wav")
            withContext(Dispatchers.IO) {
                FileOutputStream(audioFile).use { fos ->
                    response.byteStream().use { input ->
                        input.copyTo(fos)
                    }
                }
            }
            playAudioFile(audioFile)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun playAudioFile(file: File) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    setOnCompletionListener {
                        it.release()
                        if (cont.isActive) cont.resume(Unit)
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        if (cont.isActive) cont.resume(Unit)
                        true
                    }
                    prepare()
                    start()
                }
                cont.invokeOnCancellation {
                    mediaPlayer?.release()
                }
            }
        }
    }

    private suspend fun speakWithAndroidTts(text: String) {
        if (!isTtsReady) return
        textToSpeech?.setSpeechRate(preferencesManager.voiceSpeed)
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (cont.isActive) cont.resume(Unit)
                    }
                    @Deprecated("Deprecated in API")
                    override fun onError(utteranceId: String?) {
                        if (cont.isActive) cont.resume(Unit)
                    }
                })
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ruhan_utterance")
                cont.invokeOnCancellation {
                    textToSpeech?.stop()
                }
            }
        }
    }

    fun stop() {
        textToSpeech?.stop()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
