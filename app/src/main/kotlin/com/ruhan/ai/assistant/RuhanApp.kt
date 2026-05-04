package com.ruhan.ai.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RuhanApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("RuhanAI", "CRASH: ${throwable.message}", throwable)
            try {
                val crashFile = java.io.File(getExternalFilesDir(null), "ruhan_crash.txt")
                crashFile.writeText(
                    "Time: ${java.util.Date()}\n" +
                    "Error: ${throwable.message}\n" +
                    "Stack: ${throwable.stackTraceToString()}\n"
                )
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
        try {
            createNotificationChannels()
        } catch (_: Throwable) {}
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
        const val CHANNEL_SERVICE = "ruhan_service_channel"
        const val CHANNEL_REMINDER = "ruhan_reminder_channel"
    }
}
