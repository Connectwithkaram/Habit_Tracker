package com.example.habittracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object DateUtils {
    fun localDateFromMillis(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochMilli(timestampMillis).atZone(zoneId).toLocalDate()
    }

    fun startOfDayMillis(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val date = localDateFromMillis(timestampMillis, zoneId)
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
}
