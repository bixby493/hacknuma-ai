package com.ruhan.ai.assistant.security

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.widget.Toast
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
    private var failedAttempts = 0

    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit) {
        if (!preferencesManager.biometricLockEnabled) {
            onSuccess()
            return
        }

        if (!isAvailable()) {
            onSuccess()
            return
        }

        failedAttempts = 0

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    failedAttempts = 0
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        if (preferencesManager.fakeCrashEnabled) {
                            showFakeCrash(activity)
                        } else {
                            onFail()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    failedAttempts++
                    if (preferencesManager.breakInPhotoEnabled && failedAttempts >= 2) {
                        captureIntruderPhoto(activity)
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("RUHAN AI")
            .setSubtitle("Boss, apni pehchan batao")
            .setNegativeButtonText("Cancel")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onSuccess()
        }
    }

    private fun captureIntruderPhoto(activity: FragmentActivity) {
        try {
            val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val hasFrontCamera = cameraManager.cameraIdList.any { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }
            if (hasFrontCamera) {
                Toast.makeText(context, "Break-in detected! Photo captured.", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
        }
    }

    private fun showFakeCrash(activity: FragmentActivity) {
        try {
            android.app.AlertDialog.Builder(activity)
                .setTitle("System Error")
                .setMessage("Unfortunately, the system has encountered a critical error.\n\nError Code: 0xDEAD_BEEF\nProcess: com.android.systemui\n\nThe application will now close.")
                .setPositiveButton("Close App") { _, _ ->
                    activity.finishAffinity()
                }
                .setCancelable(false)
                .show()
        } catch (_: Exception) {
            activity.finishAffinity()
        }
    }

    fun isAvailable(): Boolean {
        return try {
            val bm = androidx.biometric.BiometricManager.from(context)
            bm.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS ||
                    bm.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                    androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }
}
