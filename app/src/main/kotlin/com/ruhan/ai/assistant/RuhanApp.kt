package com.ruhan.ai.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RuhanApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.reminder_channel_description)
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
