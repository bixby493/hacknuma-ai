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
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(LiveVoiceState.IDLE)
    val state: StateFlow<LiveVoiceState> = _state

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    var onTranscript: ((String) -> Unit)? = null

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

    fun startLiveSession() {
        try {
            val apiKey = preferencesManager.geminiApiKey
            if (apiKey.isBlank()) {
                _errorMessage.value = "Gemini API key set karo Settings mein"
                _state.value = LiveVoiceState.ERROR
                return
            }

            stopLiveSessionSync()
            _state.value = LiveVoiceState.CONNECTING
            _errorMessage.value = ""

            val url = "wss://generativelanguage.googleapis.com/ws/" +
                    "google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent" +
                    "?key=$apiKey"

            val request = Request.Builder().url(url).build()

            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    try {
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
                    } catch (e: Exception) {
                        _errorMessage.value = "Setup failed: ${e.message}"
                        _state.value = LiveVoiceState.ERROR
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleTextResponse(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    playAudio(bytes.toByteArray())
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _errorMessage.value = t.message ?: "Connection failed"
                    _state.value = LiveVoiceState.ERROR
                    cleanupResources()
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _state.value = LiveVoiceState.IDLE
                    cleanupResources()
                }
            })
        } catch (e: Exception) {
            _errorMessage.value = "Live voice start failed: ${e.message}"
            _state.value = LiveVoiceState.ERROR
        }
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
                val permissionCheck = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                if (permissionCheck != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    _errorMessage.value = "Mic permission de do Settings mein, Boss"
                    _state.value = LiveVoiceState.ERROR
                    return@launch
                }

                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    _errorMessage.value = "Audio recording not supported on this device"
                    _state.value = LiveVoiceState.ERROR
                    return@launch
                }
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioEncoding,
                    bufferSize * 2
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    _errorMessage.value = "Mic busy hai Boss, doosri app band karo"
                    _state.value = LiveVoiceState.ERROR
                    return@launch
                }

                audioRecord?.startRecording()

                val buffer = ByteArray(bufferSize)
                while (isActive && _state.value != LiveVoiceState.IDLE && _state.value != LiveVoiceState.ERROR) {
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
                        try {
                            ws.send(msg.toString())
                        } catch (_: Exception) {
                            break
                        }
                    }
                }
            } catch (_: SecurityException) {
                _errorMessage.value = "Microphone permission denied"
                _state.value = LiveVoiceState.ERROR
            } catch (e: Exception) {
                _errorMessage.value = "Audio capture error: ${e.message}"
                _state.value = LiveVoiceState.ERROR
            } finally {
                try {
                    audioRecord?.stop()
                } catch (_: Exception) { }
                try {
                    audioRecord?.release()
                } catch (_: Exception) { }
                audioRecord = null
            }
        }
    }

    private fun playAudio(audioData: ByteArray) {
        if (audioData.isEmpty()) return
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val outSampleRate = 24000
                    val minBuf = AudioTrack.getMinBufferSize(
                        outSampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    if (minBuf == AudioTrack.ERROR || minBuf == AudioTrack.ERROR_BAD_VALUE) return@withContext

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

                    val totalFrames = audioData.size / 2
                    while (track.playbackHeadPosition < totalFrames) {
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
        _state.value = LiveVoiceState.LISTENING
    }

    private fun cleanupResources() {
        try {
            recordingJob?.cancel()
            recordingJob = null
        } catch (_: Exception) { }
        try {
            audioRecord?.stop()
        } catch (_: Exception) { }
        try {
            audioRecord?.release()
        } catch (_: Exception) { }
        audioRecord = null
    }

    private fun stopLiveSessionSync() {
        cleanupResources()
        try {
            webSocket?.close(1000, "Session ended")
        } catch (_: Exception) { }
        webSocket = null
    }

    fun stopLiveSession() {
        stopLiveSessionSync()
        _state.value = LiveVoiceState.IDLE
        _errorMessage.value = ""
    }
}
