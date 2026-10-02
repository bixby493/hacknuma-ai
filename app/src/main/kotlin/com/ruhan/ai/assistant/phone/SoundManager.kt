package com.ruhan.ai.assistant.phone

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
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
        try {
            if (soundPool != null) return
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attrs)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) isLoaded = true
            }

            loadSound("startup", R.raw.sound_startup)
            loadSound("listening", R.raw.sound_listening)
            loadSound("success", R.raw.sound_success)
            loadSound("error", R.raw.sound_error)
            loadSound("notification", R.raw.sound_notification)
            loadSound("thinking", R.raw.sound_thinking)
            loadSound("command", R.raw.sound_command_accepted)
            loadSound("wakeup", R.raw.sound_wakeup)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize sounds", e)
            isLoaded = false
        }
    }

    private fun loadSound(name: String, resId: Int) {
        try {
            val id = soundPool?.load(context, resId, 1) ?: return
            soundIds[name] = id
        } catch (e: Exception) {
            Log.w("SoundManager", "Failed to load sound: $name", e)
        }
    }

    fun play(sound: String, volume: Float = 0.7f) {
        if (!isLoaded) return
        try {
            soundIds[sound]?.let { id ->
                soundPool?.play(id, volume, volume, 1, 0, 1f)
            }
        } catch (e: Exception) {
            Log.w("SoundManager", "Failed to play sound: $sound", e)
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
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
        isLoaded = false
        soundIds.clear()
    }
}
