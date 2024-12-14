// File: NotificationScheduler.kt
package com.example.todolistapp

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleTaskDeadlineNotification(
        context: Context,
        taskId: String,
        taskTitle: String,
        taskDescription: String,
        deadline: Long,
        triggerBeforeMillis: Long = TimeUnit.HOURS.toMillis(1) // Default 1 hour
    ) {
        // Calculate the time to trigger the notification
        val triggerTime = deadline - triggerBeforeMillis

        val currentTime = System.currentTimeMillis()
        val delay = triggerTime - currentTime

        if (delay <= 0) {
            // If the trigger time is in the past, do not schedule
            return
        }

        // Prepare input data for the Worker
        val inputData = workDataOf(
            "TASK_ID" to taskId,
            "TASK_TITLE" to taskTitle,
            "TASK_DESCRIPTION" to taskDescription
        )

        // Create a OneTimeWorkRequest with a unique tag
        val workRequest = OneTimeWorkRequestBuilder<TaskDeadlineWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(taskId) // Tagging with taskId for easy cancellation
            .build()

        // Enqueue the work
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelTaskDeadlineNotification(context: Context, taskId: String) {
        // Cancel all work with the given tag
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId)
    }
}
