package com.example.habittracker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.HabitAdapter
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
            onToggleStatus = { habit -> viewModel.toggleStatus(habit) }
        )

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.allHabits.observe(this) { habits ->
            adapter.submitList(habits)
        }

        binding.fab.setOnClickListener {
            // Future: Show Add Habit Dialog
        }
    }

}
