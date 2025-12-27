package com.example.habittracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): LiveData<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1")
    fun getActiveHabits(): LiveData<List<HabitEntity>>

    @Query(
        """
        SELECT habits.*, MAX(habit_completions.completedAt) AS lastCompletedAt
        FROM habits
        LEFT JOIN habit_completions ON habits.id = habit_completions.habitId
        GROUP BY habits.id
        ORDER BY habits.createdAt DESC
        """
    )
    fun getHabitsWithLastCompletion(): LiveData<List<HabitWithLastCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
}
