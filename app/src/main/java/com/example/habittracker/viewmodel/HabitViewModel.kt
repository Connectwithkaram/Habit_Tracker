package com.example.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.AppDatabase
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.util.StreakResult
import com.example.habittracker.util.StreakUtils
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    val allHabits: LiveData<List<HabitEntity>>

    init {
        val habitDao = AppDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(habitDao)
        allHabits = repository.allHabits
    }

    fun insert(habit: HabitEntity) = viewModelScope.launch {
        repository.insert(habit)
    }

    fun update(habit: HabitEntity) = viewModelScope.launch {
        repository.update(habit)
    }
    
    fun toggleStatus(habit: HabitEntity) = viewModelScope.launch {
        val updatedHabit = habit.copy(isActive = !habit.isActive)
        repository.update(updatedHabit)
    }

    fun markAsDone(habit: HabitEntity) = viewModelScope.launch {
        when (
            val result = StreakUtils.calculateStreakUpdate(
                nowMillis = System.currentTimeMillis(),
                lastDoneMillis = habit.lastDone,
                currentStreak = habit.streak,
                longestStreak = habit.longestStreak
            )
        ) {
            StreakResult.AlreadyCompletedToday -> Unit
            is StreakResult.Updated -> {
                val updatedHabit = habit.copy(
                    lastDone = result.lastDone,
                    streak = result.streak,
                    longestStreak = result.longestStreak
                )
                repository.update(updatedHabit)
            }
        }
    }
}
