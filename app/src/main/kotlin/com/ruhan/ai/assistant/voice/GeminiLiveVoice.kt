package com.ruhan.ai.assistant.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

enum class LiveVoiceState { IDLE, CONNECTING, LISTENING, PROCESSING, SPEAKING, ERROR }

@Singleton
class GeminiLiveVoice @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(LiveVoiceState.IDLE)
    val state: StateFlow<LiveVoiceState> = _state

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript

    var onTranscript: ((String) -> Unit)? = null

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

    fun startLiveSession() {
        val apiKey = preferencesManager.geminiApiKey
        if (apiKey.isBlank()) {
            _state.value = LiveVoiceState.ERROR
            return
        }

        _state.value = LiveVoiceState.CONNECTING

        val url = "wss://generativelanguage.googleapis.com/ws/" +
                "google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent" +
                "?key=$apiKey"

        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val setup = JSONObject().apply {
                    put("setup", JSONObject().apply {
                        put("model", "models/gemini-2.0-flash-live")
                        put("generationConfig", JSONObject().apply {
                            put("responseModalities", JSONArray().put("AUDIO").put("TEXT"))
                            put("speechConfig", JSONObject().apply {
                                put("voiceConfig", JSONObject().apply {
                                    put("prebuiltVoiceConfig", JSONObject().apply {
                                        put("voiceName", "Aoede")
                                    })
                                })
                            })
                        })
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().put(JSONObject().apply {
                                put("text", "You are RUHAN, a Jarvis-like AI assistant. " +
                                        "Speak in Hinglish. Be calm, confident. " +
                                        "Call user '${preferencesManager.bossName}'. " +
                                        "Keep replies SHORT. You ARE Ruhan.")
                            }))
                        })
                    })
                }
                webSocket.send(setup.toString())
                _state.value = LiveVoiceState.LISTENING
                startAudioCapture(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleTextResponse(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                playAudio(bytes.toByteArray())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _state.value = LiveVoiceState.ERROR
                stopLiveSession()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _state.value = LiveVoiceState.IDLE
            }
        })
    }

    private fun handleTextResponse(text: String) {
        try {
            val json = JSONObject(text)
            if (json.has("serverContent")) {
                val content = json.getJSONObject("serverContent")
                if (content.has("modelTurn")) {
                    val parts = content.getJSONObject("modelTurn").optJSONArray("parts")
                    if (parts != null) {
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("text")) {
                                val t = part.getString("text")
                                _transcript.value = _transcript.value + t
                                onTranscript?.invoke(t)
                            }
                            if (part.has("inlineData")) {
                                val data = part.getJSONObject("inlineData")
                                val audioBytes = android.util.Base64.decode(
                                    data.getString("data"),
                                    android.util.Base64.DEFAULT
                                )
                                _state.value = LiveVoiceState.SPEAKING
                                playAudio(audioBytes)
                            }
                        }
                    }
                }
                if (content.optBoolean("turnComplete", false)) {
                    _state.value = LiveVoiceState.LISTENING
                    _transcript.value = ""
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun startAudioCapture(ws: WebSocket) {
        recordingJob = scope.launch {
            try {
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioEncoding,
                    bufferSize * 2
                )
                audioRecord?.startRecording()

                val buffer = ByteArray(bufferSize)
                while (isActive && _state.value != LiveVoiceState.IDLE) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        val b64 = android.util.Base64.encodeToString(
                            buffer.copyOf(read),
                            android.util.Base64.NO_WRAP
                        )
                        val msg = JSONObject().apply {
                            put("realtimeInput", JSONObject().apply {
                                put("mediaChunks", JSONArray().put(JSONObject().apply {
                                    put("mimeType", "audio/pcm;rate=$sampleRate")
                                    put("data", b64)
                                }))
                            })
                        }
                        ws.send(msg.toString())
                    }
                }
            } catch (_: SecurityException) {
                _state.value = LiveVoiceState.ERROR
            } finally {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            }
        }
    }

    private fun playAudio(audioData: ByteArray) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val outSampleRate = 24000
                    val minBuf = AudioTrack.getMinBufferSize(
                        outSampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(outSampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(maxOf(minBuf, audioData.size))
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    track.write(audioData, 0, audioData.size)
                    track.play()

                    while (track.playbackHeadPosition < audioData.size / 2) {
                        kotlinx.coroutines.delay(50)
                    }
                    track.stop()
                    track.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun interrupt() {
        audioTrack?.stop()
        _state.value = LiveVoiceState.LISTENING
    }

    fun stopLiveSession() {
        scope.launch {
            recordingJob?.cancelAndJoin()
            recordingJob = null
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            webSocket?.close(1000, "Session ended")
            webSocket = null
            _state.value = LiveVoiceState.IDLE
        }
    }
}
