package com.ruhan.ai.assistant.domain.usecase

import com.ruhan.ai.assistant.data.repository.PhoneRepository
import com.ruhan.ai.assistant.util.PreferencesManager
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    private val phoneRepository: PhoneRepository,
    private val preferencesManager: PreferencesManager
) {
    fun execute(contactName: String): String {
        val contact = phoneRepository.findContact(contactName)
        val boss = preferencesManager.bossName
        return if (contact != null) {
            phoneRepository.makeCall(contact.phoneNumber)
            "$boss, ${contact.name} ko call laga raha hoon."
        } else {
            "$boss, '$contactName' naam ka contact nahi mila."
        }
    }
}
