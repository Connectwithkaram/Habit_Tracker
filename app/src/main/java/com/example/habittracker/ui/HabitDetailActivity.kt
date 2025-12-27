package com.example.habittracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habittracker.R
import com.example.habittracker.data.HabitCompletionEntity
import com.example.habittracker.databinding.ActivityHabitDetailBinding
import com.example.habittracker.viewmodel.HabitViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HabitDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHabitDetailBinding
    private val viewModel: HabitViewModel by viewModels()
    private val calendarAdapter = HabitHistoryAdapter()
    private var currentHabitId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentHabitId = intent.getLongExtra(EXTRA_HABIT_ID, 0)

        binding.completionHistory.apply {
            adapter = calendarAdapter
            layoutManager = GridLayoutManager(this@HabitDetailActivity, 7)
        }

        viewModel.getHabitById(currentHabitId).observe(this) { habit ->
            binding.habitTitle.text = habit.name
            binding.habitDescription.text = habit.description
            binding.streakValue.text = habit.streak.toString()
            binding.longestStreakValue.text = habit.longestStreak.toString()
            binding.frequencyValue.text = getString(
                R.string.frequency_target_value,
                habit.frequencyPerWeek
            )
            binding.lastDoneValue.text = formatLastDone(habit.lastDone)

            binding.markDoneButton.setOnClickListener {
                viewModel.markAsDone(habit)
            }
        }

        viewModel.getCompletionsForHabit(currentHabitId).observe(this) { completions ->
            calendarAdapter.submitList(buildCalendarDays(completions))
            binding.completionCount.text = getString(
                R.string.completion_count_value,
                completions.size
            )
        }
    }

    private fun buildCalendarDays(completions: List<HabitCompletionEntity>): List<CalendarDay> {
        val completedDates = completions.map { it.completedDate }.toSet()
        val today = LocalDate.now()
        val startDate = today.minusDays(CALENDAR_DAYS.toLong() - 1)
        return (0 until CALENDAR_DAYS).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            CalendarDay(date, completedDates.contains(date.toEpochDay()))
        }
    }

    private fun formatLastDone(lastDone: Long?): String {
        return if (lastDone == null) {
            getString(R.string.never_done)
        } else {
            val date = Instant.ofEpochMilli(lastDone)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    }

    companion object {
        private const val EXTRA_HABIT_ID = "extra_habit_id"
        private const val CALENDAR_DAYS = 28

        fun newIntent(context: Context, habitId: Long): Intent {
            return Intent(context, HabitDetailActivity::class.java).apply {
                putExtra(EXTRA_HABIT_ID, habitId)
            }
        }
    }
}
