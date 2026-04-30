package com.ruhan.ai.assistant.domain.usecase

import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject

class SendSmsUseCase @Inject constructor(
    private val phoneRepository: PhoneRepository,
    private val preferencesManager: PreferencesManager
) {
    fun execute(contactName: String, message: String): String {
        val contact = phoneRepository.findContact(contactName)
        val boss = preferencesManager.bossName
        return if (contact != null) {
            phoneRepository.sendSms(contact.phoneNumber, message)
            "Done $boss, ${contact.name} ko message chala gaya."
        } else {
            "$boss, '$contactName' naam ka contact nahi mila."
        }
    }
}
