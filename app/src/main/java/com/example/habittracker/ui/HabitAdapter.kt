package com.example.habittracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittracker.data.HabitEntity
import com.example.habittracker.databinding.ItemHabitBinding
import java.text.SimpleDateFormat
import java.util.*

class HabitAdapter(
    private val onDoneClick: (HabitEntity) -> Unit,
    private val onToggleStatus: (HabitEntity) -> Unit
) : ListAdapter<HabitEntity, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(habit: HabitEntity) {
            binding.habitName.text = habit.name
            val lastDoneStr = habit.lastDone?.let {
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                "Last done: ${sdf.format(Date(it))}"
            } ?: "Never done"
            
            binding.habitDetails.text = "${habit.frequencyPerWeek} times a week • $lastDoneStr"
            binding.habitStatus.text = if (habit.isActive) "Status: Active" else "Status: Paused"
            
            binding.btnDone.setOnClickListener { onDoneClick(habit) }
            binding.root.setOnLongClickListener {
                onToggleStatus(habit)
                true
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<HabitEntity>() {
        override fun areItemsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HabitEntity, newItem: HabitEntity): Boolean = oldItem == newItem
    }
}
