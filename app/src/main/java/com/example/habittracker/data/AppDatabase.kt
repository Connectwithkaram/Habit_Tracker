package com.example.habittracker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HabitEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var instance: AppDatabase? = null
                val roomCallback = object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val preferences = context.getSharedPreferences(
                            SAMPLE_PREFS,
                            Context.MODE_PRIVATE
                        )
                        if (!preferences.getBoolean(SAMPLE_DATA_INSERTED, false)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                instance?.let { database ->
                                    insertSampleData(database, preferences)
                                }
                            }
                        }
                    }
                }
                val built = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                ).addCallback(roomCallback).build()
                instance = built
                INSTANCE = built
                built
            }
        }

        private suspend fun insertSampleData(
            database: AppDatabase,
            preferences: SharedPreferences
        ) {
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
                    isActive = false,
                    lastDone = null,
                    createdAt = System.currentTimeMillis(),
                    streak = 0,
                    longestStreak = 0,
                    category = "Health"
                )
            )
            sampleHabits.forEach { habit ->
                database.habitDao().insertHabit(habit)
            }
            preferences.edit().putBoolean(SAMPLE_DATA_INSERTED, true).apply()
        }

        private const val SAMPLE_PREFS = "sample_habits"
        private const val SAMPLE_DATA_INSERTED = "sample_data_inserted"
    }
}
