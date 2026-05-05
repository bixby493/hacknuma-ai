package com.ruhan.ai.assistant.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class SecurityReport(
    val isRooted: Boolean,
    val isDebuggable: Boolean,
    val isEmulator: Boolean,
    val hasXposed: Boolean,
    val hasFrida: Boolean,
    val hasDebuggerAttached: Boolean,
    val isInstalledFromPlayStore: Boolean,
    val isTampered: Boolean
) {
    val isSecure: Boolean
        get() = !isRooted && !isDebuggable && !hasXposed && !hasFrida &&
                !hasDebuggerAttached && !isTampered
}

@Singleton
class AntiTamperGuard @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun performSecurityCheck(): SecurityReport {
        return SecurityReport(
            isRooted = checkRoot(),
            isDebuggable = checkDebuggable(),
            isEmulator = checkEmulator(),
            hasXposed = checkXposed(),
            hasFrida = checkFrida(),
            hasDebuggerAttached = checkDebugger(),
            isInstalledFromPlayStore = checkPlayStoreInstall(),
            isTampered = checkSignatureTamper()
        )
    }

    fun getSecuritySummary(): String {
        val report = performSecurityCheck()
        val sb = StringBuilder()
        sb.appendLine("Security Status:")

        if (report.isSecure) {
            sb.appendLine("Phone SECURE hai! Koi threat nahi.")
        } else {
            if (report.isRooted) sb.appendLine("WARNING: Phone ROOTED hai!")
            if (report.isDebuggable) sb.appendLine("WARNING: Debug mode ON hai!")
            if (report.isEmulator) sb.appendLine("INFO: Emulator pe chal raha hai.")
            if (report.hasXposed) sb.appendLine("DANGER: Xposed Framework detected!")
            if (report.hasFrida) sb.appendLine("DANGER: Frida hooking tool detected!")
            if (report.hasDebuggerAttached) sb.appendLine("WARNING: Debugger attached hai!")
            if (report.isTampered) sb.appendLine("DANGER: App signature TAMPERED hai!")
        }

        sb.appendLine("Encryption: AES-256-GCM")
        sb.appendLine("Key Storage: Android Keystore")
        return sb.toString()
    }

    private fun checkRoot(): Boolean {
        // Check for common root indicators
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/data/adb/magisk"
        )
        for (path in rootPaths) {
            if (File(path).exists()) return true
        }

        // Check build tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        // Try executing su
        return try {
            Runtime.getRuntime().exec("su")
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun checkDebuggable(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun checkEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.PRODUCT.contains("sdk") ||
                Build.PRODUCT.contains("vbox86p") ||
                Build.BOARD.lowercase().contains("nox") ||
                Build.BOOTLOADER.lowercase().contains("nox") ||
                Build.SERIAL.contains("unknown"))
    }

    private fun checkXposed(): Boolean {
        // Check for Xposed framework
        val xposedPaths = arrayOf(
            "/system/framework/XposedBridge.jar",
            "/system/lib/libxposed_art.so",
            "/data/data/de.robv.android.xposed.installer",
            "/data/data/org.lsposed.manager"
        )
        for (path in xposedPaths) {
            if (File(path).exists()) return true
        }

        // Check loaded classes
        return try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    private fun checkFrida(): Boolean {
        // Check for Frida server running
        val fridaPaths = arrayOf(
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
            "/sdcard/frida-server"
        )
        for (path in fridaPaths) {
            if (File(path).exists()) return true
        }

        // Check running processes for frida
        return try {
            val process = Runtime.getRuntime().exec("ps")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            output.contains("frida") || output.contains("fridaserver")
        } catch (_: Exception) {
            false
        }
    }

    private fun checkDebugger(): Boolean {
        return android.os.Debug.isDebuggerConnected() || android.os.Debug.waitingForDebugger()
    }

    private fun checkPlayStoreInstall(): Boolean {
        return try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
            installer == "com.android.vending" || installer == "com.google.android.feedback"
        } catch (_: Exception) {
            false
        }
    }

    private fun checkSignatureTamper(): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            // In a real release build, you'd compare against your known certificate hash
            // For now, just verify signatures exist
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo == null
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures.isNullOrEmpty()
            }
        } catch (_: Exception) {
            true
        }
    }
}
