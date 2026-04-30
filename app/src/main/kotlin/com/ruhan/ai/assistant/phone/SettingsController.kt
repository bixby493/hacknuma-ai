package com.ruhan.ai.assistant.phone

import android.app.NotificationManager
import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val boss get() = preferencesManager.bossName
    private var isFlashlightOn = false

    fun toggleSetting(setting: String, enable: Boolean): String {
        return when (setting) {
            "wifi" -> toggleWifi(enable)
            "bluetooth" -> toggleBluetooth(enable)
            "flashlight" -> toggleFlashlight(enable)
            "dnd" -> toggleDnd(enable)
            "airplane" -> {
                openSettings(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                "$boss, airplane mode settings khol raha hoon."
            }
            "hotspot" -> {
                openSettings("android.settings.TETHERING_SETTINGS")
                "$boss, hotspot settings khol raha hoon."
            }
            "location" -> {
                openSettings(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                "$boss, location settings khol raha hoon."
            }
            "battery_saver" -> {
                openSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                "$boss, battery saver settings khol raha hoon."
            }
            "dark_mode" -> toggleDarkMode(enable)
            "silent" -> setSilentMode()
            "mobile_data" -> {
                openSettings(Settings.ACTION_DATA_USAGE_SETTINGS)
                "$boss, data settings khol raha hoon."
            }
            "nfc" -> {
                openSettings(Settings.ACTION_NFC_SETTINGS)
                "$boss, NFC settings khol raha hoon."
            }
            else -> "$boss, yeh setting abhi control nahi kar sakta."
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleWifi(enable: Boolean): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            wifiManager.isWifiEnabled = enable
            "$boss, WiFi ${if (enable) "on" else "off"} kar diya."
        } catch (_: Exception) {
            openSettings(Settings.ACTION_WIFI_SETTINGS)
            "$boss, WiFi settings khol raha hoon."
        }
    }

    private fun toggleBluetooth(enable: Boolean): String {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = btManager?.adapter
        return try {
            if (enable) {
                @Suppress("DEPRECATION")
                adapter?.enable()
            } else {
                @Suppress("DEPRECATION")
                adapter?.disable()
            }
            "$boss, Bluetooth ${if (enable) "on" else "off"} kar diya."
        } catch (_: SecurityException) {
            openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
            "$boss, Bluetooth settings khol raha hoon."
        }
    }

    private fun toggleFlashlight(enable: Boolean): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isFlashlightOn = enable
                "$boss, flashlight ${if (enable) "on" else "off"} kar di."
            } else {
                "$boss, flashlight nahi mili phone mein."
            }
        } catch (_: Exception) {
            "$boss, flashlight control nahi ho pa rahi."
        }
    }

    private fun toggleDnd(enable: Boolean): String {
        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (notifManager.isNotificationPolicyAccessGranted) {
            notifManager.setInterruptionFilter(
                if (enable) NotificationManager.INTERRUPTION_FILTER_NONE
                else NotificationManager.INTERRUPTION_FILTER_ALL
            )
            "$boss, Do Not Disturb ${if (enable) "on" else "off"} kar diya."
        } else {
            openSettings(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            "$boss, DND permission chahiye. Settings khol raha hoon."
        }
    }

    private fun toggleDarkMode(enable: Boolean): String {
        return try {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            uiModeManager.nightMode = if (enable) {
                UiModeManager.MODE_NIGHT_YES
            } else {
                UiModeManager.MODE_NIGHT_NO
            }
            "$boss, dark mode ${if (enable) "on" else "off"} kar diya."
        } catch (_: Exception) {
            openSettings(Settings.ACTION_DISPLAY_SETTINGS)
            "$boss, display settings khol raha hoon."
        }
    }

    private fun setSilentMode(): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        return "$boss, phone vibrate mode pe set kar diya."
    }

    fun setBrightnessLevel(percent: Int) {
        try {
            val value = (percent * 255) / 100
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value.coerceIn(0, 255)
            )
        } catch (_: Exception) {
            openSettings(Settings.ACTION_DISPLAY_SETTINGS)
        }
    }

    fun setVolumeLevel(percent: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = (percent * maxVolume) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getNetworkInfo(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)

        return if (capabilities != null) {
            val type = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Unknown"
            }
            val speed = capabilities.linkDownstreamBandwidthKbps
            "$boss, $type se connected ho. Speed: ${speed / 1000} Mbps."
        } else {
            "$boss, koi network connection nahi hai abhi."
        }
    }

    fun getSystemDiagnostics(): String {
        val battery = getBatteryLevel()
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val totalMem = runtime.totalMemory() / (1024 * 1024)

        val sb = StringBuilder()
        sb.appendLine("$boss, phone ka health report:")
        sb.appendLine("• Battery: $battery%")
        sb.appendLine("• RAM: ${usedMem}MB / ${totalMem}MB used")
        sb.appendLine("• Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("• Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        sb.appendLine("• Processors: ${runtime.availableProcessors()}")

        return sb.toString()
    }

    fun scanWifi(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return try {
            @Suppress("DEPRECATION")
            val results = wifiManager.scanResults
            if (results.isEmpty()) {
                "$boss, koi WiFi network nahi mila nearby."
            } else {
                val sb = StringBuilder("$boss, nearby WiFi networks:\n")
                results.take(10).forEachIndexed { i, result ->
                    @Suppress("DEPRECATION")
                    val name = result.SSID.ifBlank { "(Hidden)" }
                    sb.appendLine("${i + 1}. $name (Signal: ${result.level} dBm)")
                }
                sb.toString()
            }
        } catch (_: SecurityException) {
            "$boss, WiFi scan ke liye location permission chahiye."
        }
    }

    private fun openSettings(action: String) {
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
