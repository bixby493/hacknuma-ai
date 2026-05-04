package com.ruhan.ai.assistant.phone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClapDetectionService : Service() {

    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var wakeLock: PowerManager.WakeLock? = null
    private val clapTimestamps = mutableListOf<Long>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "ClapDetection"
        private const val NOTIFICATION_ID = 3001
        private const val CHANNEL_ID = "ruhan_clap_detection"
        private const val CLAP_TIMEOUT_MS = 800L
        private const val CLAP_ENERGY_THRESHOLD = 5000
        private const val CLAP_COOLDOWN_MS = 2000L
        const val ACTION_CLAP_DETECTED = "com.ruhan.ai.assistant.CLAP_WAKE_UP"

        var isActive = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "RUHAN:ClapWakeLock"
            )
        } catch (e: Throwable) {
            Log.w(TAG, "WakeLock init failed", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification("Listening for claps..."))
            startClapDetection()
            isActive = true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to start clap detection", e)
            stopSelf()
        }
        return START_STICKY
    }

    private fun startClapDetection() {
        if (isListening) return
        isListening = true

        try {
            wakeLock?.acquire(30 * 60 * 1000L)
        } catch (_: Throwable) {}

        val sampleRate = 8000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid buffer size")
            stopSelf()
            return
        }
        val bufferSize = minBufferSize * 2

        audioRecord = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Microphone permission denied", e)
            stopSelf()
            return
        } catch (e: Throwable) {
            Log.e(TAG, "AudioRecord creation failed", e)
            stopSelf()
            return
        }

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord not initialized")
            stopSelf()
            return
        }

        try {
            audioRecord?.startRecording()
        } catch (e: Throwable) {
            Log.e(TAG, "startRecording failed", e)
            stopSelf()
            return
        }

        val buffer = ShortArray(bufferSize)
        var lastClapBroadcast = 0L

        scope.launch {
            while (isListening) {
                try {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        var maxAmplitude = 0
                        for (i in 0 until read) {
                            val abs = if (buffer[i] < 0) -buffer[i].toInt() else buffer[i].toInt()
                            if (abs > maxAmplitude) maxAmplitude = abs
                        }

                        if (maxAmplitude > CLAP_ENERGY_THRESHOLD) {
                            val now = System.currentTimeMillis()
                            synchronized(clapTimestamps) {
                                clapTimestamps.add(now)
                                clapTimestamps.removeAll { now - it > CLAP_TIMEOUT_MS }

                                if (clapTimestamps.size >= 2 && now - lastClapBroadcast > CLAP_COOLDOWN_MS) {
                                    lastClapBroadcast = now
                                    clapTimestamps.clear()
                                    onDoubleClapDetected()
                                }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Audio read error", e)
                }
                delay(50)
            }
        }
    }

    private fun onDoubleClapDetected() {
        Log.d(TAG, "Double clap detected! Activating RUHAN...")
        try {
            val intent = Intent(ACTION_CLAP_DETECTED)
            intent.setPackage(packageName)
            sendBroadcast(intent)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to send clap broadcast", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RUHAN Clap Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Listens for double clap to activate voice commands"
            }
            try {
                getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
            } catch (_: Throwable) {}
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RUHAN AI")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        isListening = false
        isActive = false
        try { scope.cancel() } catch (_: Throwable) {}
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Throwable) {}
        audioRecord = null
        try { wakeLock?.release() } catch (_: Throwable) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
