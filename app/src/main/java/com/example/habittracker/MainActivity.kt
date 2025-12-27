package com.example.habittracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.HabitAdapter
import com.example.habittracker.viewmodel.HabitViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import java.time.DayOfWeek

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
            onEditClick = { habit -> showHabitDialog(habit) }
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
            showHabitDialog(null)
        }
    }

    private fun showHabitDialog(habit: HabitEntity?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_habit_form, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.habitNameInput)
        val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.habitDescriptionInput)
        val frequencyInput = dialogView.findViewById<TextInputEditText>(R.id.habitFrequencyInput)
        val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.habitCategoryInput)
        val reminderEnabledSwitch = dialogView.findViewById<MaterialSwitch>(R.id.reminderEnabledSwitch)
        val reminderTimeButton = dialogView.findViewById<MaterialButton>(R.id.reminderTimeButton)

        val dayCheckboxes = mapOf(
            DayOfWeek.MONDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.dayMonday),
            DayOfWeek.TUESDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.dayTuesday),
            DayOfWeek.WEDNESDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.dayWednesday),
            DayOfWeek.THURSDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.dayThursday),
            DayOfWeek.FRIDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.dayFriday),
            DayOfWeek.SATURDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.daySaturday),
            DayOfWeek.SUNDAY to dialogView.findViewById<android.widget.CheckBox>(R.id.daySunday)
        )

        var selectedTimeMinutes = habit?.reminderTimeMinutes
        if (habit != null) {
            nameInput.setText(habit.name)
            descriptionInput.setText(habit.description)
            frequencyInput.setText(habit.frequencyPerWeek.toString())
            categoryInput.setText(habit.category)
            reminderEnabledSwitch.isChecked = habit.reminderEnabled
            habit.reminderDaysOfWeek
                ?.split(",")
                ?.mapNotNull { day -> runCatching { DayOfWeek.valueOf(day) }.getOrNull() }
                ?.forEach { day -> dayCheckboxes[day]?.isChecked = true }
        }

        fun updateTimeButton() {
            val label = selectedTimeMinutes?.let { minutes ->
                String.format("%02d:%02d", minutes / 60, minutes % 60)
            } ?: getString(R.string.reminder_select_time)
            reminderTimeButton.text = label
        }

        fun updateReminderControls(enabled: Boolean) {
            reminderTimeButton.isEnabled = enabled
            dayCheckboxes.values.forEach { it.isEnabled = enabled }
        }

        updateTimeButton()
        updateReminderControls(reminderEnabledSwitch.isChecked)

        reminderTimeButton.setOnClickListener {
            val hour = selectedTimeMinutes?.div(60) ?: 9
            val minute = selectedTimeMinutes?.rem(60) ?: 0
            val timePicker = android.app.TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    selectedTimeMinutes = selectedHour * 60 + selectedMinute
                    updateTimeButton()
                },
                hour,
                minute,
                true
            )
            timePicker.show()
        }

        reminderEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateReminderControls(isChecked)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (habit == null) getString(R.string.add_habit) else getString(R.string.edit_habit))
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val name = nameInput.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    nameInput.error = getString(R.string.habit_name_required)
                    return@setOnClickListener
                }

                val frequency = frequencyInput.text?.toString()?.toIntOrNull() ?: 1
                val reminderEnabled = reminderEnabledSwitch.isChecked
                val selectedDays = dayCheckboxes.filterValues { it.isChecked }.keys
                val reminderDays = if (reminderEnabled && selectedDays.isNotEmpty()) {
                    selectedDays.joinToString(",") { it.name }
                } else {
                    null
                }

                if (reminderEnabled && (selectedTimeMinutes == null || reminderDays == null)) {
                    Toast.makeText(this, R.string.reminder_requirements, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val habitToSave = HabitEntity(
                    id = habit?.id ?: 0,
                    name = name,
                    description = descriptionInput.text?.toString()?.trim().orEmpty(),
                    frequencyPerWeek = frequency,
                    isActive = habit?.isActive ?: true,
                    reminderEnabled = reminderEnabled,
                    reminderTimeMinutes = if (reminderEnabled) selectedTimeMinutes else null,
                    reminderDaysOfWeek = reminderDays,
                    lastDone = habit?.lastDone,
                    createdAt = habit?.createdAt ?: System.currentTimeMillis(),
                    streak = habit?.streak ?: 0,
                    longestStreak = habit?.longestStreak ?: 0,
                    category = categoryInput.text?.toString()?.trim().orEmpty()
                )

                if (habit == null) {
                    viewModel.insert(habitToSave)
                } else {
                    viewModel.update(habitToSave)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun insertSampleData() {
        val sampleHabits = listOf(
            HabitEntity(
                name = "Drink Water",
                description = "2 liters a day",
                frequencyPerWeek = 7,
                isActive = true,
                reminderEnabled = false,
                reminderTimeMinutes = null,
                reminderDaysOfWeek = null,
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
                reminderEnabled = false,
                reminderTimeMinutes = null,
                reminderDaysOfWeek = null,
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
                reminderEnabled = false,
                reminderTimeMinutes = null,
                reminderDaysOfWeek = null,
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
