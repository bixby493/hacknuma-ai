package com.ruhan.ai.assistant.phone

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Screen Share Manager for RUHAN AI.
 *
 * Voice command: "Ruhan, screen share karo"
 * This requests MediaProjection permission from user,
 * then starts capturing screen via VirtualDisplay.
 *
 * PREMIUM feature — requires premium activation.
 */
@Singleton
class ScreenShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isSharing = false

    companion object {
        const val TAG = "RuhanScreenShare"
        const val NOTIFICATION_CHANNEL = "ruhan_screen_share"
        const val NOTIFICATION_ID = 2001
        const val VIRTUAL_DISPLAY_NAME = "RuhanDisplay"
        const val VIDEO_WIDTH = 720
        const val VIDEO_HEIGHT = 1280
        const val VIDEO_DPI = 320
    }

    fun isScreenSharing(): Boolean = isSharing

    fun getStatus(): String {
        return if (isSharing) {
            "Screen sharing active hai. Stop karne ke liye bolo 'Ruhan, screen share band karo'."
        } else {
            "Screen sharing off hai. Start karne ke liye bolo 'Ruhan, screen share karo'. " +
            "Ye PREMIUM feature hai — MediaProjection API use karta hai."
        }
    }

    fun requestScreenShare(activity: Activity): Intent {
        val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return projectionManager.createScreenCaptureIntent()
    }

    fun startProjection(resultCode: Int, data: Intent) {
        try {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped")
                    isSharing = false
                    virtualDisplay?.release()
                    virtualDisplay = null
                }
            }, null)

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                VIRTUAL_DISPLAY_NAME,
                VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_DPI,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                null, null, null
            )

            isSharing = true
            Log.d(TAG, "Screen sharing started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start projection", e)
            isSharing = false
        }
    }

    fun stopProjection() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            mediaProjection?.stop()
            mediaProjection = null
            isSharing = false
            Log.d(TAG, "Screen sharing stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping projection", e)
        }
    }
}

/**
 * Foreground service for screen sharing.
 * Required by Android 10+ for MediaProjection.
 */
class ScreenShareService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(ScreenShareManager.NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ScreenShareManager.NOTIFICATION_CHANNEL,
                "RUHAN Screen Share",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Screen sharing notification"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, ScreenShareManager.NOTIFICATION_CHANNEL)
            .setContentTitle("RUHAN AI")
            .setContentText("Screen sharing active...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }
}
