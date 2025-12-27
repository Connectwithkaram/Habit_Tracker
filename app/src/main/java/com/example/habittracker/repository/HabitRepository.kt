package com.example.habittracker.repository

import androidx.lifecycle.LiveData
import com.example.habittracker.data.HabitDao
import com.example.habittracker.data.HabitEntity

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: LiveData<List<HabitEntity>> = habitDao.getAllHabits()
    val activeHabits: LiveData<List<HabitEntity>> = habitDao.getActiveHabits()

    suspend fun insert(habit: HabitEntity) {
        habitDao.insertHabit(habit)
    }

    suspend fun update(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun delete(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }
}
