package com.ruhan.ai.assistant.brain

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val commands: String,
    val triggerType: String,
    val triggerValue: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows WHERE enabled = 1 ORDER BY createdAt DESC")
    suspend fun getActiveWorkflows(): List<WorkflowEntity>

    @Query("SELECT * FROM workflows ORDER BY createdAt DESC")
    suspend fun getAllWorkflows(): List<WorkflowEntity>

    @Insert
    suspend fun insert(workflow: WorkflowEntity): Long

    @Query("UPDATE workflows SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM workflows WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM workflows WHERE id = :id")
    suspend fun getById(id: Long): WorkflowEntity?
}

@Singleton
class WorkflowEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workflowDao: WorkflowDao
) {
    suspend fun createWorkflow(
        name: String,
        commands: List<String>,
        triggerType: String = "daily",
        triggerValue: String = "08:00"
    ): Long {
        val id = workflowDao.insert(
            WorkflowEntity(
                name = name,
                commands = commands.joinToString("|"),
                triggerType = triggerType,
                triggerValue = triggerValue
            )
        )
        scheduleWorkflow(id, triggerType, triggerValue)
        return id
    }

    private fun scheduleWorkflow(id: Long, triggerType: String, triggerValue: String) {
        when (triggerType) {
            "daily" -> {
                val request = PeriodicWorkRequestBuilder<WorkflowWorker>(
                    24, TimeUnit.HOURS
                ).setInputData(
                    workDataOf("workflow_id" to id)
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "workflow_$id",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            }

            "hourly" -> {
                val request = PeriodicWorkRequestBuilder<WorkflowWorker>(
                    1, TimeUnit.HOURS
                ).setInputData(
                    workDataOf("workflow_id" to id)
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "workflow_$id",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            }
        }
    }

    suspend fun getAll(): List<WorkflowEntity> = workflowDao.getAllWorkflows()

    suspend fun toggleWorkflow(id: Long, enabled: Boolean) {
        workflowDao.setEnabled(id, enabled)
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork("workflow_$id")
        }
    }

    suspend fun deleteWorkflow(id: Long) {
        workflowDao.delete(id)
        WorkManager.getInstance(context).cancelUniqueWork("workflow_$id")
    }

    suspend fun getCommandsForWorkflow(id: Long): List<String> {
        return workflowDao.getById(id)?.commands?.split("|") ?: emptyList()
    }
}

class WorkflowWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return Result.success()
    }
}
