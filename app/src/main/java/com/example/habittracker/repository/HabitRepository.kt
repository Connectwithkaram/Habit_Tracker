package com.example.habittracker.repository

import androidx.lifecycle.LiveData
import com.example.habittracker.data.HabitCompletionEntity
import com.example.habittracker.data.HabitDao
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.data.HabitWithLastCompletion

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: LiveData<List<HabitWithLastCompletion>> = habitDao.getHabitsWithLastCompletion()
    val activeHabits: LiveData<List<HabitEntity>> = habitDao.getActiveHabits()

    fun getHabitsByName(query: String): LiveData<List<HabitEntity>> {
        return habitDao.getHabitsByName(query)
    }

    fun getHabitsFiltered(query: String, isActive: Boolean?): LiveData<List<HabitEntity>> {
        return habitDao.getHabitsFiltered(query, isActive)
    }
    fun getHabitById(habitId: Long): LiveData<HabitEntity> = habitDao.getHabitById(habitId)

    fun getCompletionsForHabit(habitId: Long): LiveData<List<HabitCompletionEntity>> =
        habitDao.getCompletionsForHabit(habitId)

    suspend fun insert(habit: HabitEntity) {
        habitDao.insertHabit(habit)
    }

    suspend fun addCompletion(completion: HabitCompletionEntity) {
        habitDao.insertCompletion(completion)
    }

    suspend fun update(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun delete(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }

    suspend fun insertCompletion(completion: HabitCompletionEntity) {
        habitDao.insertCompletion(completion)
    }
}
