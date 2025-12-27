package com.example.habittracker

import com.example.habittracker.util.StreakResult
import com.example.habittracker.util.StreakUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakUtilsTest {
    private val zoneId = ZoneId.of("UTC")

    private fun millisAt(date: LocalDate, hour: Int): Long {
        return date.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun startOfDayMillis(date: LocalDate): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    @Test
    fun alreadyCompletedTodayReturnsNoUpdate() {
        val today = LocalDate.of(2024, 5, 10)
        val nowMillis = millisAt(today, 14)
        val lastDoneMillis = millisAt(today, 8)

        val result = StreakUtils.calculateStreakUpdate(
            nowMillis = nowMillis,
            lastDoneMillis = lastDoneMillis,
            currentStreak = 3,
            longestStreak = 5,
            zoneId = zoneId
        )

        assertTrue(result is StreakResult.AlreadyCompletedToday)
    }

    @Test
    fun completedYesterdayIncrementsStreak() {
        val today = LocalDate.of(2024, 5, 10)
        val yesterday = today.minusDays(1)
        val nowMillis = millisAt(today, 15)
        val lastDoneMillis = millisAt(yesterday, 20)

        val result = StreakUtils.calculateStreakUpdate(
            nowMillis = nowMillis,
            lastDoneMillis = lastDoneMillis,
            currentStreak = 4,
            longestStreak = 6,
            zoneId = zoneId
        )

        require(result is StreakResult.Updated)
        assertEquals(5, result.streak)
        assertEquals(6, result.longestStreak)
        assertEquals(startOfDayMillis(today), result.lastDone)
    }

    @Test
    fun missedDayResetsStreakToOne() {
        val today = LocalDate.of(2024, 5, 10)
        val twoDaysAgo = today.minusDays(2)
        val nowMillis = millisAt(today, 9)
        val lastDoneMillis = millisAt(twoDaysAgo, 18)

        val result = StreakUtils.calculateStreakUpdate(
            nowMillis = nowMillis,
            lastDoneMillis = lastDoneMillis,
            currentStreak = 7,
            longestStreak = 7,
            zoneId = zoneId
        )

        require(result is StreakResult.Updated)
        assertEquals(1, result.streak)
        assertEquals(7, result.longestStreak)
        assertEquals(startOfDayMillis(today), result.lastDone)
    }

    @Test
    fun newLongestStreakUpdatesWhenExceeded() {
        val today = LocalDate.of(2024, 5, 10)
        val yesterday = today.minusDays(1)
        val nowMillis = millisAt(today, 11)
        val lastDoneMillis = millisAt(yesterday, 7)

        val result = StreakUtils.calculateStreakUpdate(
            nowMillis = nowMillis,
            lastDoneMillis = lastDoneMillis,
            currentStreak = 2,
            longestStreak = 2,
            zoneId = zoneId
        )

        require(result is StreakResult.Updated)
        assertEquals(3, result.streak)
        assertEquals(3, result.longestStreak)
        assertEquals(startOfDayMillis(today), result.lastDone)
    }
}
