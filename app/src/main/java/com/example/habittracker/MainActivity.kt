package com.example.habittracker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.HabitAdapter
import com.example.habittracker.ui.HabitFormBottomSheet
import com.example.habittracker.viewmodel.HabitViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = HabitAdapter(
            onDoneClick = { habit -> viewModel.markAsDone(habit) },
            onToggleStatus = { habit -> viewModel.toggleStatus(habit) },
            onEditClick = { habit -> showHabitForm(habit) }
        )

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.allHabits.observe(this) { habits ->
            if (habits.isEmpty()) {
                insertSampleData()
            }
            adapter.submitList(habits)
        }

        binding.fab.setOnClickListener {
            showHabitForm(null)
        }
    }

    private fun showHabitForm(habit: HabitEntity?) {
        val sheet = HabitFormBottomSheet.newInstance(habit)
        sheet.onSave = { savedHabit ->
            if (habit == null) {
                viewModel.insert(savedHabit)
            } else {
                viewModel.update(savedHabit)
            }
        }
        sheet.show(supportFragmentManager, "HabitForm")
    }

    private fun insertSampleData() {
        val sampleHabits = listOf(
            HabitEntity(
                name = "Drink Water",
                description = "2 liters a day",
                frequencyPerWeek = 7,
                isActive = true,
                lastDone = null,
                createdAt = System.currentTimeMillis(),
                streak = 0,
                longestStreak = 0,
                category = "Health"
            ),
            HabitEntity(
                name = "Read a Book",
                description = "At least 10 pages",
                frequencyPerWeek = 5,
                isActive = true,
                lastDone = null,
                createdAt = System.currentTimeMillis(),
                streak = 0,
                longestStreak = 0,
                category = "Education"
            ),
            HabitEntity(
                name = "Gym",
                description = "Weight training",
                frequencyPerWeek = 3,
                isActive = false, // Paused example
                lastDone = null,
                createdAt = System.currentTimeMillis(),
                streak = 0,
                longestStreak = 0,
                category = "Health"
            )
        )
        sampleHabits.forEach { viewModel.insert(it) }
    }
}
