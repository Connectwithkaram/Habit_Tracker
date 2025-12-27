package com.example.habittracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.R
import com.example.habittracker.databinding.ItemCompletionDayBinding
import java.time.format.DateTimeFormatter

class HabitHistoryAdapter : ListAdapter<CalendarDay, HabitHistoryAdapter.DayViewHolder>(DayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCompletionDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(private val binding: ItemCompletionDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: CalendarDay) {
            binding.dayNumber.text = day.date.format(DateTimeFormatter.ofPattern("d"))
            binding.dayLabel.text = day.date.format(DateTimeFormatter.ofPattern("EEE"))

            val colorRes = if (day.isCompleted) {
                R.color.completion_done
            } else {
                R.color.completion_missed
            }
            val color = ContextCompat.getColor(binding.root.context, colorRes)
            binding.dayCard.setCardBackgroundColor(color)
            binding.root.contentDescription = binding.root.context.getString(
                if (day.isCompleted) {
                    R.string.completion_day_done
                } else {
                    R.string.completion_day_missed
                },
                day.date.format(DateTimeFormatter.ofPattern("MMM dd"))
            )
        }
    }

    class DayDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}

data class CalendarDay(
    val date: java.time.LocalDate,
    val isCompleted: Boolean
)
