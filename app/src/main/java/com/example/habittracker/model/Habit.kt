package com.example.habittracker.model

import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequencyPerWeek: Int, // 1 to 7
    val isActive: Boolean = true,
    val lastDone: LocalDate? = null,
    val createdAt: LocalDate = LocalDate.now(),
    val streak: Int = 0,
    val longestStreak: Int = 0,
    val category: String = "General"
)
