package com.ruhan.ai.assistant.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private var failedAttempts = 0
    private val handler = Handler(Looper.getMainLooper())

    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onFail: () -> Unit) {
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
                    } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                        errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                    ) {
                        if (preferencesManager.breakInPhotoEnabled) {
                            captureIntruderPhoto(activity)
                        }
                        onFail()
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
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Break-in attempt detected!", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            val frontCameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }

            if (frontCameraId == null) {
                Toast.makeText(context, "Break-in detected! No front camera.", Toast.LENGTH_SHORT).show()
                return
            }

            val characteristics = cameraManager.getCameraCharacteristics(frontCameraId)
            val streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return
            val jpegSizes = streamMap.getOutputSizes(ImageFormat.JPEG)
            val captureSize = jpegSizes.minByOrNull { it.width * it.height } ?: return

            val imageReader = ImageReader.newInstance(captureSize.width, captureSize.height, ImageFormat.JPEG, 2)
            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                try {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    val breakinDir = File(context.filesDir, "breakin_photos")
                    breakinDir.mkdirs()
                    val file = File(breakinDir, "intruder_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { it.write(bytes) }

                    handler.post {
                        Toast.makeText(context, "Break-in detected! Photo saved.", Toast.LENGTH_LONG).show()
                    }
                } catch (_: Exception) {
                } finally {
                    image.close()
                }
            }, handler)

            cameraManager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    try {
                        val captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                        captureBuilder.addTarget(imageReader.surface)
                        captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270)

                        camera.createCaptureSession(
                            listOf(imageReader.surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    try {
                                        session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {}, handler)
                                        handler.postDelayed({
                                            try {
                                                session.close()
                                                camera.close()
                                                imageReader.close()
                                            } catch (_: Exception) {}
                                        }, 3000)
                                    } catch (_: Exception) {
                                        camera.close()
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    camera.close()
                                }
                            },
                            handler
                        )
                    } catch (_: Exception) {
                        camera.close()
                    }
                }

                override fun onDisconnected(camera: CameraDevice) { camera.close() }
                override fun onError(camera: CameraDevice, error: Int) { camera.close() }
            }, handler)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Break-in detected! Camera permission needed.", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Break-in attempt detected!", Toast.LENGTH_LONG).show()
        }
    }

    private fun showFakeCrash(activity: FragmentActivity) {
        try {
            handler.post {
                try {
                    android.app.AlertDialog.Builder(activity)
                        .setTitle("System UI has stopped")
                        .setMessage(
                            "Unfortunately, System UI has stopped working.\n\n" +
                                    "Error: 0xDEAD_BEEF\n" +
                                    "Process: com.android.systemui\n" +
                                    "Signal: 11 (SIGSEGV)\n\n" +
                                    "This application will now close."
                        )
                        .setPositiveButton("OK") { _, _ ->
                            activity.finishAffinity()
                        }
                        .setCancelable(false)
                        .show()
                } catch (_: Exception) {
                    activity.finishAffinity()
                }
            }
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
