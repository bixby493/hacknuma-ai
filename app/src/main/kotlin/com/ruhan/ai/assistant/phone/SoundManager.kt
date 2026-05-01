package com.ruhan.ai.assistant.phone

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ruhan.ai.assistant.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<String, Int>()
    private var isLoaded = false

    fun initialize() {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, _ ->
            isLoaded = true
        }

        soundIds["startup"] = soundPool!!.load(context, R.raw.sound_startup, 1)
        soundIds["listening"] = soundPool!!.load(context, R.raw.sound_listening, 1)
        soundIds["success"] = soundPool!!.load(context, R.raw.sound_success, 1)
        soundIds["error"] = soundPool!!.load(context, R.raw.sound_error, 1)
        soundIds["notification"] = soundPool!!.load(context, R.raw.sound_notification, 1)
        soundIds["thinking"] = soundPool!!.load(context, R.raw.sound_thinking, 1)
        soundIds["command"] = soundPool!!.load(context, R.raw.sound_command_accepted, 1)
        soundIds["wakeup"] = soundPool!!.load(context, R.raw.sound_wakeup, 1)
    }

    fun play(sound: String, volume: Float = 0.7f) {
        if (!isLoaded) return
        soundIds[sound]?.let { id ->
            soundPool?.play(id, volume, volume, 1, 0, 1f)
        }
    }

    fun playStartup() = play("startup", 0.5f)
    fun playListening() = play("listening", 0.6f)
    fun playSuccess() = play("success", 0.7f)
    fun playError() = play("error", 0.5f)
    fun playNotification() = play("notification", 0.6f)
    fun playThinking() = play("thinking", 0.3f)
    fun playCommand() = play("command", 0.6f)
    fun playWakeup() = play("wakeup", 0.7f)

    fun release() {
        soundPool?.release()
        soundPool = null
        isLoaded = false
        soundIds.clear()
    }
}
