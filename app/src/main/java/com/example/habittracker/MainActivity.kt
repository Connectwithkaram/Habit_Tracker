package com.example.habittracker

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habittracker.databinding.ActivityMainBinding
import com.example.habittracker.ui.HabitAdapter
import com.example.habittracker.ui.HabitFormBottomSheet
import com.example.habittracker.viewmodel.HabitStatusFilter
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

        viewModel.filteredHabits.observe(this) { habits ->
            adapter.submitList(habits)
            binding.emptyState.visibility = if (habits.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.searchInput.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text?.toString().orEmpty())
        }

        binding.filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chipActive -> HabitStatusFilter.ACTIVE
                R.id.chipPaused -> HabitStatusFilter.PAUSED
                else -> HabitStatusFilter.ALL
            }
            viewModel.updateStatusFilter(filter)
        }

        binding.fab.setOnClickListener {
            val sheet = HabitFormBottomSheet.newInstance(null)
            sheet.onSave = { habit -> viewModel.insert(habit) }
            sheet.show(supportFragmentManager, HabitFormBottomSheet::class.java.simpleName)
        }
    }
}
