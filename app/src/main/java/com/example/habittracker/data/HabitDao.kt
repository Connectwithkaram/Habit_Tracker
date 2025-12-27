package com.example.habittracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): LiveData<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1")
    fun getActiveHabits(): LiveData<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE name LIKE :query ORDER BY createdAt DESC")
    fun getHabitsByName(query: String): LiveData<List<HabitEntity>>

    @Query(
        "SELECT * FROM habits " +
            "WHERE name LIKE :query " +
            "AND (:isActive IS NULL OR isActive = :isActive) " +
            "ORDER BY createdAt DESC"
    )
    fun getHabitsFiltered(query: String, isActive: Boolean?): LiveData<List<HabitEntity>>
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
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: Long): LiveData<HabitEntity>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedDate DESC")
    fun getCompletionsForHabit(habitId: Long): LiveData<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
}
