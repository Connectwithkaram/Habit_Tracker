package com.example.habittracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habittracker.data.AppDatabase
import com.example.habittracker.data.HabitDao
import com.example.habittracker.data.HabitEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var habitDao: HabitDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = database.habitDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getActiveHabits_filtersInactiveEntries() = runBlocking {
        val activeHabit = baseHabit(id = 1L, name = "Run", isActive = true)
        val inactiveHabit = baseHabit(id = 2L, name = "Read", isActive = false)

        habitDao.insertHabit(activeHabit)
        habitDao.insertHabit(inactiveHabit)

        val activeHabits = habitDao.getActiveHabits().getOrAwaitValue()

        assertEquals(1, activeHabits.size)
        assertEquals(activeHabit.name, activeHabits.first().name)
        assertTrue(activeHabits.first().isActive)
    }

    @Test
    fun getAllHabits_returnsNewestFirst() = runBlocking {
        val olderHabit = baseHabit(id = 1L, name = "Stretch", createdAt = 1_600L)
        val newerHabit = baseHabit(id = 2L, name = "Meditate", createdAt = 2_600L)

        habitDao.insertHabit(olderHabit)
        habitDao.insertHabit(newerHabit)

        val allHabits = habitDao.getAllHabits().getOrAwaitValue()

        assertEquals(2, allHabits.size)
        assertEquals(newerHabit.name, allHabits.first().name)
        assertEquals(olderHabit.name, allHabits.last().name)
    }

    @Test
    fun updateHabit_persistsChanges() = runBlocking {
        val habit = baseHabit(id = 3L, name = "Journal", isActive = true)
        habitDao.insertHabit(habit)

        val updated = habit.copy(name = "Evening Journal", isActive = false)
        habitDao.updateHabit(updated)

        val allHabits = habitDao.getAllHabits().getOrAwaitValue()

        assertEquals(1, allHabits.size)
        assertEquals("Evening Journal", allHabits.first().name)
        assertFalse(allHabits.first().isActive)
    }

    private fun baseHabit(
        id: Long,
        name: String,
        createdAt: Long = 1_700_000_000_000L,
        isActive: Boolean = true
    ) = HabitEntity(
        id = id,
        name = name,
        description = "Description for $name",
        frequencyPerWeek = 3,
        isActive = isActive,
        lastDone = null,
        createdAt = createdAt,
        streak = 0,
        longestStreak = 0,
        category = "General"
    )
}
