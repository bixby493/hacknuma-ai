package com.ruhan.ai.assistant.util

import android.app.Activity
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.PixelCopy
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ScreenshotHelper {

    suspend fun captureScreen(activity: Activity): String? {
        return suspendCoroutine { cont ->
            try {
                val window = activity.window
                val view = window.decorView
                val bitmap = Bitmap.createBitmap(
                    view.width,
                    view.height,
                    Bitmap.Config.ARGB_8888
                )
                PixelCopy.request(
                    window,
                    bitmap,
                    { result ->
                        if (result == PixelCopy.SUCCESS) {
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                            cont.resume(base64)
                        } else {
                            cont.resume(null)
                        }
                        bitmap.recycle()
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (e: Exception) {
                cont.resume(null)
            }
        }
    }
}
