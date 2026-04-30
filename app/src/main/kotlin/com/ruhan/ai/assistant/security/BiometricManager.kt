package com.ruhan.ai.assistant.security

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit) {
        if (!preferencesManager.biometricLockEnabled) {
            onSuccess()
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        onFail()
                    }
                }

                override fun onAuthenticationFailed() {
                    if (preferencesManager.breakInPhotoEnabled) {
                        captureIntruderPhoto()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Ruhan AI")
            .setSubtitle("Authenticate to access")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun captureIntruderPhoto() {
        // In a full implementation, this would use CameraX to capture a silent photo
        // For now, we log the attempt
    }

    fun isAvailable(): Boolean {
        return try {
            val bm = androidx.biometric.BiometricManager.from(context)
            bm.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }
}
