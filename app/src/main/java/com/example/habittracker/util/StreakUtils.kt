package com.example.habittracker.util

import java.time.ZoneId

sealed class StreakResult {
    data object AlreadyCompletedToday : StreakResult()
    data class Updated(
        val streak: Int,
        val longestStreak: Int,
        val lastDone: Long
    ) : StreakResult()
}

object StreakUtils {
    fun calculateStreakUpdate(
        nowMillis: Long,
        lastDoneMillis: Long?,
        currentStreak: Int,
        longestStreak: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): StreakResult {
        val todayStart = DateUtils.startOfDayMillis(nowMillis, zoneId)
        val lastDoneDate = lastDoneMillis?.let { DateUtils.localDateFromMillis(it, zoneId) }
        val todayDate = DateUtils.localDateFromMillis(todayStart, zoneId)

        if (lastDoneDate == todayDate) {
            return StreakResult.AlreadyCompletedToday
        }

        val yesterdayDate = todayDate.minusDays(1)
        val nextStreak = if (lastDoneDate == yesterdayDate) {
            currentStreak + 1
        } else {
            1
        }

        val nextLongest = maxOf(nextStreak, longestStreak)

        return StreakResult.Updated(
            streak = nextStreak,
            longestStreak = nextLongest,
            lastDone = todayStart
        )
    }
}
