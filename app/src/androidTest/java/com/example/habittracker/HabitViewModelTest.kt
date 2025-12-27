package com.example.habittracker

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habittracker.data.HabitDao
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.repository.HabitRepository
import com.example.habittracker.viewmodel.HabitViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun markAsDone_updatesStreakAndLastDoneWithFixedTime() = runTest {
        val dao = FakeHabitDao()
        val repository = HabitRepository(dao)
        val application = ApplicationProvider.getApplicationContext<Application>()
        val fixedTime = 1_700_000_000_000L
        val viewModel = HabitViewModel(
            application = application,
            repository = repository,
            timeProvider = { fixedTime }
        )

        val habit = HabitEntity(
            id = 1L,
            name = "Hydrate",
            description = "Drink water",
            frequencyPerWeek = 7,
            isActive = true,
            lastDone = null,
            createdAt = 1_699_000_000_000L,
            streak = 2,
            longestStreak = 5,
            category = "Health"
        )

        viewModel.markAsDone(habit)
        advanceUntilIdle()

        val updatedHabit = dao.updatedHabit
        assertNotNull(updatedHabit)
        assertEquals(fixedTime, updatedHabit?.lastDone)
        assertEquals(3, updatedHabit?.streak)
        assertEquals(5, updatedHabit?.longestStreak)
    }

    private class FakeHabitDao : HabitDao {
        val updatedHabitLiveData = MutableLiveData<List<HabitEntity>>(emptyList())
        var updatedHabit: HabitEntity? = null

        override fun getAllHabits() = updatedHabitLiveData

        override fun getActiveHabits() = updatedHabitLiveData

        override suspend fun insertHabit(habit: HabitEntity) = Unit

        override suspend fun updateHabit(habit: HabitEntity) {
            updatedHabit = habit
        }

        override suspend fun deleteHabit(habit: HabitEntity) = Unit
    }
}
