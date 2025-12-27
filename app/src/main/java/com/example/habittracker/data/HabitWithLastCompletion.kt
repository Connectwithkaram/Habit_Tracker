package com.example.habittracker.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class HabitWithLastCompletion(
    @Embedded val habit: HabitEntity,
    @ColumnInfo(name = "lastCompletedAt") val lastCompletedAt: Long?
)
