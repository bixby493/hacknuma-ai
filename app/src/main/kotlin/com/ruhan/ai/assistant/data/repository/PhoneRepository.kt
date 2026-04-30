package com.ruhan.ai.assistant.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import com.ruhan.ai.assistant.service.ReminderReceiver
import com.ruhan.ai.assistant.util.ContactsHelper
import com.ruhan.ai.assistant.util.ContactInfo
import com.ruhan.ai.assistant.util.PhoneController
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneRepository @Inject constructor(
    private val phoneController: PhoneController,
    private val contactsHelper: ContactsHelper,
    @ApplicationContext private val context: Context
) {

    fun findContact(name: String): ContactInfo? {
        return contactsHelper.findContact(name)
    }

    fun makeCall(phoneNumber: String) {
        phoneController.makeCall(phoneNumber)
    }

    fun sendSms(phoneNumber: String, message: String) {
        phoneController.sendSms(phoneNumber, message)
    }

    fun openWhatsApp(phoneNumber: String, message: String) {
        phoneController.openWhatsApp(phoneNumber, message)
    }

    fun setReminder(message: String, hour: Int, minute: Int, delayMinutes: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_message", message)
        }
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = if (delayMinutes > 0) {
            Calendar.getInstance().apply {
                add(Calendar.MINUTE, delayMinutes)
            }
        } else {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun toggleWifi(enable: Boolean) = phoneController.toggleWifi(enable)
    fun toggleBluetooth(enable: Boolean) = phoneController.toggleBluetooth(enable)
    fun setBrightness(level: Int) = phoneController.setBrightness(level)
    fun adjustVolume(increase: Boolean) = phoneController.adjustVolume(increase)
    fun toggleDnd(enable: Boolean) = phoneController.toggleDnd(enable)
    fun toggleFlashlight(enable: Boolean) = phoneController.toggleFlashlight(enable)
    fun toggleAirplaneMode() = phoneController.toggleAirplaneMode()
    fun getBatteryLevel(): Int = phoneController.getBatteryLevel()
    fun openApp(appName: String): Boolean = phoneController.openApp(appName)
    fun getNetworkInfo(): String = phoneController.getNetworkInfo()
}
