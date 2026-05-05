package com.ruhan.ai.assistant.phone

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyShakeDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var shakeCount = 0
    private var lastShakeTime = 0L
    private var isActive = false
    private val handler = Handler(Looper.getMainLooper())
    private val shakeThreshold = 15f // m/s^2
    private val requiredShakes = 5
    private val shakeWindowMs = 3000L // 5 shakes within 3 seconds

    var onEmergencyTriggered: (() -> Unit)? = null

    fun startDetecting() {
        if (isActive) return
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            isActive = true
        }
    }

    fun stopDetecting() {
        sensorManager?.unregisterListener(this)
        isActive = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat() -
                SensorManager.GRAVITY_EARTH

        if (acceleration > shakeThreshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 200) { // debounce
                shakeCount++
                lastShakeTime = now

                // Reset count if too much time has passed
                handler.removeCallbacksAndMessages("shake_reset")
                handler.postDelayed({
                    shakeCount = 0
                }, shakeWindowMs)

                if (shakeCount >= requiredShakes) {
                    shakeCount = 0
                    triggerEmergency()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerEmergency() {
        val emergencyContact = preferencesManager.emergencyContact
        if (emergencyContact.isBlank()) {
            onEmergencyTriggered?.invoke()
            return
        }

        // Send SMS with emergency message
        try {
            val smsManager = SmsManager.getDefault()
            val message = "EMERGENCY! ${preferencesManager.bossName} ne emergency signal bheja hai. " +
                    "Please call immediately. Sent from Ruhan AI Emergency System."
            smsManager.sendTextMessage(emergencyContact, null, message, null, null)
        } catch (_: Exception) {}

        // Try to call
        try {
            val callIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$emergencyContact")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(callIntent)
        } catch (_: Exception) {}

        onEmergencyTriggered?.invoke()
    }

    fun getStatus(): String {
        val contact = preferencesManager.emergencyContact
        return if (contact.isNotBlank()) {
            "Emergency Shake: ON. Contact: $contact. 5 baar tez shake karo emergency trigger ke liye."
        } else {
            "Emergency Shake: ON, but emergency contact set nahi hai. Settings mein jaake set karo."
        }
    }
}
