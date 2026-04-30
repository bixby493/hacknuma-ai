package com.ruhan.ai.assistant.premium

import android.content.Context
import android.content.Intent
import android.location.LocationManager as AndroidLocationManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun shareLocation(recipientName: String) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
            @Suppress("DEPRECATION")
            val location = try {
                lm.getLastKnownLocation(AndroidLocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(AndroidLocationManager.NETWORK_PROVIDER)
            } catch (_: SecurityException) {
                null
            }

            if (location != null) {
                val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Meri location: $mapsUrl - Shared by Ruhan AI")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, "Share location via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (_: Exception) {
        }
    }

    fun navigate(place: String) {
        try {
            val uri = Uri.parse("google.navigation:q=${Uri.encode(place)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val uri = Uri.parse("https://maps.google.com/maps?daddr=${Uri.encode(place)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
            @Suppress("DEPRECATION")
            val location = lm.getLastKnownLocation(AndroidLocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(AndroidLocationManager.NETWORK_PROVIDER)
            location?.let { Pair(it.latitude, it.longitude) }
        } catch (_: SecurityException) {
            null
        }
    }
}
