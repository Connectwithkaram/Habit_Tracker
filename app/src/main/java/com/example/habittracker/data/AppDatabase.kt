package com.example.habittracker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HabitEntity::class, HabitCompletionEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habit_completions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        completedAt INTEGER NOT NULL,
                        completedDate INTEGER NOT NULL,
                        FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completions_habitId ON habit_completions(habitId)")
                
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        frequencyPerWeek INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        lastDone INTEGER,
                        createdAt INTEGER NOT NULL,
                        streak INTEGER NOT NULL,
                        longestStreak INTEGER NOT NULL,
                        category TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO habits_new (id, name, description, frequencyPerWeek, isActive, lastDone, createdAt, streak, longestStreak, category)
                    SELECT id, name, description, frequencyPerWeek, isActive, lastDone, createdAt, streak, longestStreak, category FROM habits
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE habits")
                database.execSQL("ALTER TABLE habits_new RENAME TO habits")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val roomCallback = object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val preferences = context.getSharedPreferences(SAMPLE_PREFS, Context.MODE_PRIVATE)
                        if (!preferences.getBoolean(SAMPLE_DATA_INSERTED, false)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    insertSampleData(database, preferences)
                                }
                            }
                        }
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(roomCallback)
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
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
