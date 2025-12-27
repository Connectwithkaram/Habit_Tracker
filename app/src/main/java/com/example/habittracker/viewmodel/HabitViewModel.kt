package com.example.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.AppDatabase
import com.example.habittracker.data.HabitCompletionEntity
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.data.HabitWithLastCompletion
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.util.StreakResult
import com.example.habittracker.util.StreakUtils
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    val allHabits: LiveData<List<HabitEntity>>
    val filteredHabits: LiveData<List<HabitEntity>>
    private val searchQuery = MutableLiveData("")
    private val statusFilter = MutableLiveData(HabitStatusFilter.ALL)
    private val filterState = MediatorLiveData<FilterState>()
    val allHabits: LiveData<List<HabitWithLastCompletion>>

    init {
        val habitDao = AppDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(habitDao)
        allHabits = repository.allHabits
        filterState.value = FilterState("", HabitStatusFilter.ALL)
        filterState.addSource(searchQuery) { query ->
            val current = filterState.value ?: FilterState("", HabitStatusFilter.ALL)
            filterState.value = current.copy(query = query)
        }
        filterState.addSource(statusFilter) { status ->
            val current = filterState.value ?: FilterState("", HabitStatusFilter.ALL)
            filterState.value = current.copy(status = status)
        }
        filteredHabits = Transformations.switchMap(filterState) { state ->
            val normalizedQuery = "%${state.query.trim()}%"
            val isActive = when (state.status) {
                HabitStatusFilter.ALL -> null
                HabitStatusFilter.ACTIVE -> true
                HabitStatusFilter.PAUSED -> false
            }
            repository.getHabitsFiltered(normalizedQuery, isActive)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateStatusFilter(filter: HabitStatusFilter) {
        statusFilter.value = filter
    }

    fun getHabitById(habitId: Long): LiveData<HabitEntity> = repository.getHabitById(habitId)

    fun getCompletionsForHabit(habitId: Long): LiveData<List<HabitCompletionEntity>> =
        repository.getCompletionsForHabit(habitId)

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
        repository.addCompletion(
            HabitCompletionEntity(
                habitId = habit.id,
                completedAt = Instant.now()
            )
        val today = System.currentTimeMillis()
        val completion = HabitCompletionEntity(
            habitId = habit.id,
            completedDate = LocalDate.now().toEpochDay()
        )
        val updatedHabit = habit.copy(
            streak = habit.streak + 1,
            longestStreak = maxOf(habit.streak + 1, habit.longestStreak)
        )
        repository.insertCompletion(completion)
        repository.update(updatedHabit)
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

enum class HabitStatusFilter {
    ALL,
    ACTIVE,
    PAUSED
}

data class FilterState(
    val query: String,
    val status: HabitStatusFilter
)
