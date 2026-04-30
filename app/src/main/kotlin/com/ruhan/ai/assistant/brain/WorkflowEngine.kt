package com.ruhan.ai.assistant.brain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ruhan.ai.assistant.util.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val commands: String,
    val trigger: String,
    val isActive: Boolean = true,
    val lastRun: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY createdAt DESC")
    suspend fun getAllWorkflows(): List<WorkflowEntity>

    @Query("SELECT * FROM workflows WHERE isActive = 1")
    suspend fun getActiveWorkflows(): List<WorkflowEntity>

    @Insert
    suspend fun insert(workflow: WorkflowEntity)

    @Update
    suspend fun update(workflow: WorkflowEntity)

    @Query("DELETE FROM workflows WHERE id = :id")
    suspend fun delete(id: Long)
}

@Singleton
class WorkflowEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workflowDao: WorkflowDao,
    private val preferencesManager: PreferencesManager
) {
    companion object {
        const val DAILY_BRIEFING_WORK = "ruhan_daily_briefing"
        const val WORKFLOW_WORK = "ruhan_workflow_"
    }

    fun scheduleDailyBriefing() {
        if (!preferencesManager.dailyBriefingEnabled) return

        val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_BRIEFING_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelDailyBriefing() {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_BRIEFING_WORK)
    }

    suspend fun createWorkflow(name: String, commands: List<String>, trigger: String) {
        val cmdStr = commands.joinToString("|")
        workflowDao.insert(
            WorkflowEntity(name = name, commands = cmdStr, trigger = trigger)
        )

        val request = PeriodicWorkRequestBuilder<WorkflowWorker>(
            if (trigger.contains("hourly")) 1L else 24L,
            if (trigger.contains("hourly")) TimeUnit.HOURS else TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "$WORKFLOW_WORK$name",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun getWorkflows(): List<WorkflowEntity> = workflowDao.getAllWorkflows()

    suspend fun toggleWorkflow(workflow: WorkflowEntity) {
        workflowDao.update(workflow.copy(isActive = !workflow.isActive))
    }

    suspend fun deleteWorkflow(workflow: WorkflowEntity) {
        workflowDao.delete(workflow.id)
        WorkManager.getInstance(context).cancelUniqueWork("$WORKFLOW_WORK${workflow.name}")
    }
}

class DailyBriefingWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val channelId = "ruhan_briefing"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Daily Briefing",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Ruhan daily briefing notifications" }
                notificationManager.createNotificationChannel(channel)
            }

            val dateFormat = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("hi", "IN"))
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val todayDate = dateFormat.format(Date())
            val currentTime = timeFormat.format(Date())

            val batteryLevel = getBatteryLevel()

            val briefingText = buildString {
                appendLine("Good morning Boss!")
                appendLine("Aaj $todayDate hai.")
                appendLine("Time: $currentTime")
                appendLine("Battery: $batteryLevel% hai.")
                appendLine("Koi kaam ho toh bataiye!")
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("RUHAN AI — Daily Briefing")
                .setContentText("Good morning Boss! Battery: $batteryLevel%")
                .setStyle(NotificationCompat.BigTextStyle().bigText(briefingText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1001, notification)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun getBatteryLevel(): Int {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
    }
}

class WorkflowWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val channelId = "ruhan_workflow"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Workflow",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Ruhan workflow notifications" }
                notificationManager.createNotificationChannel(channel)
            }

            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("RUHAN AI — Workflow")
                .setContentText("Workflow executed at $currentTime")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
