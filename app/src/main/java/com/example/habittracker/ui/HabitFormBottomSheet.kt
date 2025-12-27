package com.example.habittracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.databinding.HabitFormBottomSheetBinding
import com.example.habittracker.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HabitFormBottomSheet : BottomSheetDialogFragment() {

    private var _binding: HabitFormBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var existingHabit: HabitEntity? = null
    var onSave: ((HabitEntity) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        existingHabit = arguments?.let { bundle ->
            if (!bundle.containsKey(ARG_ID)) {
                return@let null
            }
            HabitEntity(
                id = bundle.getLong(ARG_ID),
                name = bundle.getString(ARG_NAME).orEmpty(),
                description = bundle.getString(ARG_DESCRIPTION).orEmpty(),
                frequencyPerWeek = bundle.getInt(ARG_FREQUENCY),
                isActive = bundle.getBoolean(ARG_ACTIVE),
                lastDone = bundle.getLong(ARG_LAST_DONE).let { if (it == ARG_NO_LONG) null else it },
                createdAt = bundle.getLong(ARG_CREATED_AT),
                streak = bundle.getInt(ARG_STREAK),
                longestStreak = bundle.getInt(ARG_LONGEST_STREAK),
                category = bundle.getString(ARG_CATEGORY).orEmpty()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HabitFormBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val habit = existingHabit
        if (habit != null) {
            binding.nameInput.setText(habit.name)
            binding.descriptionInput.setText(habit.description)
            binding.frequencyInput.setText(habit.frequencyPerWeek.toString())
            binding.categoryInput.setText(habit.category)
            binding.activeSwitch.isChecked = habit.isActive
        } else {
            binding.activeSwitch.isChecked = true
            binding.frequencyInput.setText(DEFAULT_FREQUENCY.toString())
        }

        binding.cancelButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener { submitForm() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submitForm() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val description = binding.descriptionInput.text?.toString()?.trim().orEmpty()
        val frequency = binding.frequencyInput.text?.toString()?.trim()?.toIntOrNull()
        val category = binding.categoryInput.text?.toString()?.trim().orEmpty()
        val isActive = binding.activeSwitch.isChecked

        var hasError = false
        if (name.isBlank()) {
            binding.nameLayout.error = getString(R.string.error_name_required)
            hasError = true
        } else {
            binding.nameLayout.error = null
        }

        if (frequency == null || frequency !in 1..7) {
            binding.frequencyLayout.error = getString(R.string.error_frequency_range)
            hasError = true
        } else {
            binding.frequencyLayout.error = null
        }

        if (hasError) {
            return
        }

        val baseHabit = existingHabit
        val habitToSave = if (baseHabit != null) {
            baseHabit.copy(
                name = name,
                description = description,
                frequencyPerWeek = frequency!!,
                category = category,
                isActive = isActive
            )
        } else {
            HabitEntity(
                name = name,
                description = description,
                frequencyPerWeek = frequency!!,
                isActive = isActive,
                lastDone = null,
                createdAt = System.currentTimeMillis(),
                streak = 0,
                longestStreak = 0,
                category = category
            )
        }

        onSave?.invoke(habitToSave)
        dismiss()
    }

    companion object {
        private const val ARG_ID = "habit_id"
        private const val ARG_NAME = "habit_name"
        private const val ARG_DESCRIPTION = "habit_description"
        private const val ARG_FREQUENCY = "habit_frequency"
        private const val ARG_CATEGORY = "habit_category"
        private const val ARG_ACTIVE = "habit_active"
        private const val ARG_LAST_DONE = "habit_last_done"
        private const val ARG_CREATED_AT = "habit_created_at"
        private const val ARG_STREAK = "habit_streak"
        private const val ARG_LONGEST_STREAK = "habit_longest_streak"
        private const val ARG_NO_LONG = -1L
        private const val DEFAULT_FREQUENCY = 1

        fun newInstance(habit: HabitEntity?): HabitFormBottomSheet {
            val sheet = HabitFormBottomSheet()
            if (habit != null) {
                sheet.arguments = Bundle().apply {
                    putLong(ARG_ID, habit.id)
                    putString(ARG_NAME, habit.name)
                    putString(ARG_DESCRIPTION, habit.description)
                    putInt(ARG_FREQUENCY, habit.frequencyPerWeek)
                    putString(ARG_CATEGORY, habit.category)
                    putBoolean(ARG_ACTIVE, habit.isActive)
                    putLong(ARG_LAST_DONE, habit.lastDone ?: ARG_NO_LONG)
                    putLong(ARG_CREATED_AT, habit.createdAt)
                    putInt(ARG_STREAK, habit.streak)
                    putInt(ARG_LONGEST_STREAK, habit.longestStreak)
                }
            }
            return sheet
        }
    }
}
