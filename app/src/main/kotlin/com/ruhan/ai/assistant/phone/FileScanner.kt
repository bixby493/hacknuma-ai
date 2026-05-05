package com.ruhan.ai.assistant.phone

import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

data class ScanResult(
    val totalFiles: Int,
    val totalSize: Long,
    val cacheSize: Long,
    val duplicateGroups: Int,
    val duplicateSize: Long,
    val largeFiles: List<FileInfo>,
    val emptyFiles: Int,
    val tempFiles: Int,
    val summary: String
)

data class FileInfo(
    val path: String,
    val size: Long,
    val name: String
)

@Singleton
class FileScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getStorageOverview(): String {
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = stat.totalBytes
        val availBytes = stat.availableBytes
        val usedBytes = totalBytes - availBytes

        val totalGB = String.format("%.1f", totalBytes / 1_073_741_824.0)
        val usedGB = String.format("%.1f", usedBytes / 1_073_741_824.0)
        val freeGB = String.format("%.1f", availBytes / 1_073_741_824.0)
        val usedPercent = ((usedBytes.toDouble() / totalBytes) * 100).toInt()

        return "Storage: ${usedGB}GB / ${totalGB}GB used ($usedPercent%), ${freeGB}GB free"
    }

    fun getCacheSize(): Long {
        var total = 0L
        try {
            total += getDirSize(context.cacheDir)
            context.externalCacheDir?.let { total += getDirSize(it) }
        } catch (_: Exception) {}
        return total
    }

    fun clearCache(): String {
        var cleared = 0L
        try {
            cleared += clearDir(context.cacheDir)
            context.externalCacheDir?.let { cleared += clearDir(it) }
        } catch (_: Exception) {}
        val mb = String.format("%.1f", cleared / 1_048_576.0)
        return "${mb}MB cache clear kar diya"
    }

    fun scanFiles(): ScanResult {
        val root = Environment.getExternalStorageDirectory()
        var totalFiles = 0
        var totalSize = 0L
        var emptyFiles = 0
        var tempFiles = 0
        val hashMap = mutableMapOf<String, MutableList<FileInfo>>()
        val largeFiles = mutableListOf<FileInfo>()
        val tempExtensions = setOf(".tmp", ".temp", ".log", ".bak", ".old", ".cache")

        fun scanDir(dir: File, depth: Int = 0) {
            if (depth > 8) return
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    scanDir(file, depth + 1)
                } else {
                    totalFiles++
                    totalSize += file.length()

                    if (file.length() == 0L) {
                        emptyFiles++
                    }

                    if (tempExtensions.any { file.name.endsWith(it, ignoreCase = true) }) {
                        tempFiles++
                    }

                    if (file.length() > 50_000_000) { // 50MB+
                        largeFiles.add(FileInfo(file.absolutePath, file.length(), file.name))
                    }

                    // Hash small-medium files for duplicate detection
                    if (file.length() in 1024..20_000_000) {
                        try {
                            val sizeKey = "${file.length()}_${file.extension}"
                            val info = FileInfo(file.absolutePath, file.length(), file.name)
                            hashMap.getOrPut(sizeKey) { mutableListOf() }.add(info)
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        try { scanDir(root) } catch (_: Exception) {}

        val duplicateGroups = hashMap.values.filter { it.size > 1 }
        val duplicateCount = duplicateGroups.size
        val duplicateSize = duplicateGroups.sumOf { group ->
            group.drop(1).sumOf { it.size }
        }

        val cacheSize = getCacheSize()
        val cacheMB = String.format("%.1f", cacheSize / 1_048_576.0)
        val totalMB = String.format("%.1f", totalSize / 1_048_576.0)
        val dupMB = String.format("%.1f", duplicateSize / 1_048_576.0)

        val summary = buildString {
            appendLine("Scan Complete!")
            appendLine("Total: $totalFiles files (${totalMB}MB)")
            appendLine("Cache: ${cacheMB}MB")
            if (duplicateCount > 0) appendLine("Duplicates: $duplicateCount groups (${dupMB}MB)")
            if (emptyFiles > 0) appendLine("Empty files: $emptyFiles")
            if (tempFiles > 0) appendLine("Temp files: $tempFiles")
            if (largeFiles.isNotEmpty()) {
                appendLine("Large files (50MB+):")
                largeFiles.sortedByDescending { it.size }.take(5).forEach {
                    val mb = String.format("%.1f", it.size / 1_048_576.0)
                    appendLine("  ${it.name} (${mb}MB)")
                }
            }
        }

        return ScanResult(
            totalFiles = totalFiles,
            totalSize = totalSize,
            cacheSize = cacheSize,
            duplicateGroups = duplicateCount,
            duplicateSize = duplicateSize,
            largeFiles = largeFiles.sortedByDescending { it.size }.take(10),
            emptyFiles = emptyFiles,
            tempFiles = tempFiles,
            summary = summary
        )
    }

    fun cleanJunk(): String {
        var cleaned = 0L
        val root = Environment.getExternalStorageDirectory()
        val junkExtensions = setOf(".tmp", ".temp", ".log", ".bak", ".old")

        fun cleanDir(dir: File, depth: Int = 0) {
            if (depth > 6) return
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    if (file.name.equals(".thumbnails", ignoreCase = true)) {
                        cleaned += clearDir(file)
                    } else {
                        cleanDir(file, depth + 1)
                    }
                } else {
                    if (file.length() == 0L || junkExtensions.any { file.name.endsWith(it, ignoreCase = true) }) {
                        try {
                            val size = file.length()
                            if (file.delete()) cleaned += size
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        try {
            cleaned += clearDir(context.cacheDir)
            context.externalCacheDir?.let { cleaned += clearDir(it) }
            cleanDir(root)
        } catch (_: Exception) {}

        val mb = String.format("%.1f", cleaned / 1_048_576.0)
        return "${mb}MB kachra saaf kar diya! Cache, temp files, empty files sab clean."
    }

    fun findFile(query: String): String {
        val root = Environment.getExternalStorageDirectory()
        val results = mutableListOf<FileInfo>()
        val q = query.lowercase()

        fun searchDir(dir: File, depth: Int = 0) {
            if (depth > 8 || results.size >= 20) return
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (results.size >= 20) return
                if (file.isDirectory) {
                    searchDir(file, depth + 1)
                } else if (file.name.lowercase().contains(q)) {
                    results.add(FileInfo(file.absolutePath, file.length(), file.name))
                }
            }
        }

        try { searchDir(root) } catch (_: Exception) {}

        if (results.isEmpty()) return "Koi file nahi mili '$query' naam se."

        val sb = StringBuilder("${results.size} files mili:\n")
        results.sortedByDescending { it.size }.forEach {
            val mb = String.format("%.1f", it.size / 1_048_576.0)
            sb.appendLine("${it.name} (${mb}MB) — ${it.path}")
        }
        return sb.toString()
    }

    private fun getDirSize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles() ?: return 0
        for (file in files) {
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
        return size
    }

    private fun clearDir(dir: File): Long {
        var cleared = 0L
        val files = dir.listFiles() ?: return 0
        for (file in files) {
            if (file.isDirectory) {
                cleared += clearDir(file)
                file.delete()
            } else {
                val size = file.length()
                if (file.delete()) cleared += size
            }
        }
        return cleared
    }
}
