package com.example.habittracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habittracker.R
import java.time.DayOfWeek
import java.time.LocalDate

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: return Result.success()
        val reminderDays = inputData.getString(KEY_REMINDER_DAYS)
        val days = reminderDays
            ?.split(",")
            ?.mapNotNull { name -> runCatching { DayOfWeek.valueOf(name) }.getOrNull() }
            ?.toSet()
            ?: emptySet()

        if (days.isNotEmpty() && DayOfWeek.from(LocalDate.now()) !in days) {
            return Result.success()
        }

        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Habit reminder")
            .setContentText("Time for \"$habitName\"")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(habitName.hashCode(), notification)
        return Result.success()
    }

    private fun createNotificationChannel() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Habit Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val KEY_HABIT_NAME = "habit_name"
        const val KEY_REMINDER_DAYS = "reminder_days"
        const val KEY_REMINDER_TIME_MINUTES = "reminder_time_minutes"
        private const val CHANNEL_ID = "habit_reminders"
    }
}
