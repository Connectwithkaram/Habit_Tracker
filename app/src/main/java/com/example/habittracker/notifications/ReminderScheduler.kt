package com.example.habittracker.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.habittracker.data.HabitEntity
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME_PREFIX = "habit_reminder_"

    fun scheduleOrCancel(context: Context, habit: HabitEntity) {
        val workManager = WorkManager.getInstance(context)
        val workName = WORK_NAME_PREFIX + habit.id

        if (!habit.reminderEnabled || habit.reminderTimeMinutes == null || habit.reminderDaysOfWeek.isNullOrBlank()) {
            workManager.cancelUniqueWork(workName)
            return
        }

        val inputData = workDataOf(
            ReminderWorker.KEY_HABIT_NAME to habit.name,
            ReminderWorker.KEY_REMINDER_DAYS to habit.reminderDaysOfWeek,
            ReminderWorker.KEY_REMINDER_TIME_MINUTES to habit.reminderTimeMinutes
        )

        val initialDelayMillis = computeInitialDelayMillis(habit.reminderTimeMinutes)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInputData(inputData)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun computeInitialDelayMillis(reminderTimeMinutes: Int): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(reminderTimeMinutes / 60, reminderTimeMinutes % 60)
        var nextRun = now.with(targetTime)
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis().coerceAtLeast(0)
    }
}
