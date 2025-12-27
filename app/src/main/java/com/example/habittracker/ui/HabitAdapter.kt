package com.example.habittracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.data.HabitWithLastCompletion
import com.example.habittracker.databinding.ItemHabitBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HabitAdapter(
    private val onDoneClick: (HabitWithLastCompletion) -> Unit,
    private val onToggleStatus: (HabitWithLastCompletion) -> Unit
) : ListAdapter<HabitWithLastCompletion, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {
    private val onDoneClick: (HabitEntity) -> Unit,
    private val onToggleStatus: (HabitEntity) -> Unit,
    private val onHabitClick: (HabitEntity) -> Unit
    private val onEditClick: (HabitEntity) -> Unit
) : ListAdapter<HabitEntity, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HabitWithLastCompletion) {
            val habit = item.habit
            binding.habitName.text = habit.name
            val lastDoneStr = item.lastCompletedAt?.let {
                val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                val localDateTime = LocalDateTime.ofInstant(it, ZoneId.systemDefault())
                "Last done: ${formatter.format(localDateTime)}"
            } ?: "Never done"

            binding.habitDetails.text = "${habit.frequencyPerWeek} times a week • $lastDoneStr"
            binding.habitStatus.text = if (habit.isActive) "Status: Active" else "Status: Paused"

            binding.btnDone.setOnClickListener { onDoneClick(item) }
            binding.root.setOnLongClickListener {
                onToggleStatus(item)
            
            binding.btnDone.setOnClickListener { onDoneClick(habit) }
            binding.root.setOnClickListener { onHabitClick(habit) }
            binding.habitStatus.setOnClickListener { onToggleStatus(habit) }
            binding.root.setOnLongClickListener {
                onEditClick(habit)
                true
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<HabitWithLastCompletion>() {
        override fun areItemsTheSame(oldItem: HabitWithLastCompletion, newItem: HabitWithLastCompletion): Boolean {
            return oldItem.habit.id == newItem.habit.id
        }

        override fun areContentsTheSame(oldItem: HabitWithLastCompletion, newItem: HabitWithLastCompletion): Boolean {
            return oldItem == newItem
        }
    }
}
