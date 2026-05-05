package com.ruhan.ai.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RuhanApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RuhanApp.onCreate START")
        installCrashHandler()
        try { createNotificationChannels() } catch (t: Throwable) {
            Log.e(TAG, "Notification channels failed", t)
        }
        Log.d(TAG, "RuhanApp.onCreate DONE")
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "UNCAUGHT CRASH on ${thread.name}: ${throwable.message}", throwable)
            try {
                val crashDir = getExternalFilesDir(null) ?: filesDir
                val crashFile = java.io.File(crashDir, "ruhan_crash.txt")
                val ts = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    .format(java.util.Date())
                crashFile.writeText(
                    "RUHAN AI Crash Report\n" +
                    "Time: $ts\n" +
                    "Thread: ${thread.name}\n" +
                    "Device: ${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})\n" +
                    "Error: ${throwable.javaClass.name}: ${throwable.message}\n\n" +
                    "Stack Trace:\n${throwable.stackTraceToString()}\n\n" +
                    "Caused by:\n${throwable.cause?.stackTraceToString() ?: "none"}\n"
                )
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "Ruhan AI Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps Ruhan AI running in the background"
            setShowBadge(false)
        }

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            "Ruhan Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders set by Ruhan AI"
            enableVibration(true)
        }

        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(reminderChannel)
    }

    companion object {
        private const val TAG = "RuhanApp"
        const val CHANNEL_SERVICE = "ruhan_service_channel"
        const val CHANNEL_REMINDER = "ruhan_reminder_channel"
    }
}
