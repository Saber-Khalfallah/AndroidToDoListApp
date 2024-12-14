// File: TaskDeadlineWorker.kt
package com.example.todolistapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class TaskDeadlineWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Retrieve task details from input data
        val taskId = inputData.getString("TASK_ID") ?: return Result.failure()
        val taskTitle = inputData.getString("TASK_TITLE") ?: "Task"
        val taskDescription = inputData.getString("TASK_DESCRIPTION") ?: "You have a task pending."

        // Create an intent to open MainActivity when the notification is tapped
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode(), // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ensure you have this icon in drawable
            .setContentTitle("Upcoming Deadline: $taskTitle")
            .setContentText(taskDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.hashCode(), notification) // Use unique ID for each notification

        return Result.success()
    }
}
