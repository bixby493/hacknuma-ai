package com.ruhan.ai.assistant.phone

import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import com.ruhan.ai.assistant.data.repository.AIRepository
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotNarrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiRepository: AIRepository,
    private val preferencesManager: PreferencesManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observer: ContentObserver? = null
    private var lastProcessedUri: String? = null
    var onNarration: ((String) -> Unit)? = null

    fun startWatching() {
        val handler = Handler(Looper.getMainLooper())
        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let { handleNewMedia(it) }
            }
        }

        val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        try {
            context.contentResolver.registerContentObserver(
                contentUri,
                true,
                observer!!
            )
        } catch (_: Exception) {}
    }

    fun stopWatching() {
        observer?.let {
            try { context.contentResolver.unregisterContentObserver(it) } catch (_: Exception) {}
        }
        observer = null
    }

    private fun handleNewMedia(uri: Uri) {
        val uriString = uri.toString()
        if (uriString == lastProcessedUri) return
        lastProcessedUri = uriString

        // Check if it's a screenshot
        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.RELATIVE_PATH
            )
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "" else ""
                    val pathIdx = it.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                    val path = if (pathIdx >= 0) (it.getString(pathIdx) ?: "") else ""

                    val isScreenshot = name.contains("screenshot", ignoreCase = true) ||
                            path.contains("screenshot", ignoreCase = true) ||
                            name.contains("Screen", ignoreCase = true)

                    if (isScreenshot) {
                        analyzeScreenshot(uri)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun analyzeScreenshot(uri: Uri) {
        if (!preferencesManager.hasGeminiKey()) return

        scope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) return@launch

                // Resize for API
                val maxDim = 1024
                val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height, 1f)
                val resized = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt(),
                        (bitmap.height * scale).toInt(),
                        true
                    )
                } else bitmap

                val baos = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                if (resized != bitmap) resized.recycle()
                bitmap.recycle()

                val prompt = """Analyze this screenshot in Hinglish. Be concise (2-3 sentences max).
Identify: app name, key content (text/numbers/dates), and any actionable items.
If it's a ticket/booking/receipt, extract key details (date, time, amount, booking ID).
If it's a chat, summarize the last message.
End with a helpful suggestion like "Calendar mein daalun?" or "Save karun?"."""

                val analysis = aiRepository.analyzeImage(base64, prompt)
                onNarration?.invoke(analysis)
            } catch (_: Exception) {}
        }
    }
}
