package com.example.habittracker.repository

import androidx.lifecycle.LiveData
import com.example.habittracker.data.HabitDao
import com.example.habittracker.data.HabitEntity

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: LiveData<List<HabitEntity>> = habitDao.getAllHabits()
    val activeHabits: LiveData<List<HabitEntity>> = habitDao.getActiveHabits()

    fun getHabitsByName(query: String): LiveData<List<HabitEntity>> {
        return habitDao.getHabitsByName(query)
    }

    fun getHabitsFiltered(query: String, isActive: Boolean?): LiveData<List<HabitEntity>> {
        return habitDao.getHabitsFiltered(query, isActive)
    }

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
