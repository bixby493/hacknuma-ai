package com.ruhan.ai.assistant.domain.usecase

import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject

class ControlSettingsUseCase @Inject constructor(
    private val phoneRepository: PhoneRepository,
    private val preferencesManager: PreferencesManager
) {
    private val boss: String get() = preferencesManager.bossName

    fun toggleWifi(enable: Boolean): String {
        phoneRepository.toggleWifi(enable)
        return "$boss, WiFi settings khol raha hoon."
    }

    fun toggleBluetooth(enable: Boolean): String {
        phoneRepository.toggleBluetooth(enable)
        return "$boss, Bluetooth ${if (enable) "on" else "off"} kar raha hoon."
    }

    fun setBrightness(increase: Boolean): String {
        phoneRepository.setBrightness(if (increase) 220 else 80)
        return "$boss, brightness ${if (increase) "badha" else "kam"} di."
    }

    fun adjustVolume(increase: Boolean): String {
        phoneRepository.adjustVolume(increase)
        return "$boss, volume ${if (increase) "badha" else "kam"} diya."
    }

    fun toggleDnd(enable: Boolean): String {
        phoneRepository.toggleDnd(enable)
        return "$boss, Do Not Disturb ${if (enable) "on" else "off"} kar diya."
    }

    fun toggleFlashlight(enable: Boolean): String {
        phoneRepository.toggleFlashlight(enable)
        return "$boss, flashlight ${if (enable) "on" else "off"} kar di."
    }

    fun toggleAirplaneMode(): String {
        phoneRepository.toggleAirplaneMode()
        return "$boss, airplane mode settings khol raha hoon."
    }
}
