package com.example.habittracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val frequencyPerWeek: Int,
    val isActive: Boolean,
    val lastDone: Long?, // Timestamp
    val createdAt: Long, // Timestamp
    val streak: Int,
    val longestStreak: Int,
    val category: String
)
