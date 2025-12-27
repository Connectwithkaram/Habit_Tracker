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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
}
