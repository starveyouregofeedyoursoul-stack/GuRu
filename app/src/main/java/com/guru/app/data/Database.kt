package com.guru.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuruDao {
    // Templates
    @Query("SELECT * FROM WorkoutTemplate ORDER BY isPreset ASC, category, name")
    fun templates(): Flow<List<WorkoutTemplate>>
    @Upsert suspend fun upsertTemplate(t: WorkoutTemplate): Long
    @Delete suspend fun deleteTemplate(t: WorkoutTemplate)
    @Query("SELECT COUNT(*) FROM WorkoutTemplate") suspend fun templateCount(): Int

    // Scheduled workouts
    @Query("SELECT * FROM ScheduledWorkout WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay, startMinuteOfDay")
    fun workoutsBetween(from: Long, to: Long): Flow<List<ScheduledWorkout>>
    @Upsert suspend fun upsertWorkout(w: ScheduledWorkout): Long
    @Insert suspend fun insertWorkouts(w: List<ScheduledWorkout>)
    @Delete suspend fun deleteWorkout(w: ScheduledWorkout)

    // Meals
    @Query("SELECT * FROM Meal ORDER BY kind, name") fun meals(): Flow<List<Meal>>
    @Query("SELECT * FROM Meal") suspend fun allMeals(): List<Meal>
    @Upsert suspend fun upsertMeal(m: Meal): Long
    @Delete suspend fun deleteMeal(m: Meal)
    @Query("SELECT COUNT(*) FROM Meal") suspend fun mealCount(): Int

    @Query("SELECT * FROM PlannedMeal WHERE epochDay BETWEEN :from AND :to")
    fun plannedBetween(from: Long, to: Long): Flow<List<PlannedMeal>>
    @Insert suspend fun insertPlanned(p: PlannedMeal)
    @Delete suspend fun deletePlanned(p: PlannedMeal)
    @Query("DELETE FROM PlannedMeal WHERE epochDay = :day AND slot = :slot")
    suspend fun clearSlot(day: Long, slot: String)

    // Reminders
    @Query("SELECT * FROM Reminder ORDER BY hour, minute") fun reminders(): Flow<List<Reminder>>
    @Query("SELECT * FROM Reminder") suspend fun allReminders(): List<Reminder>
    @Query("SELECT * FROM Reminder WHERE id = :id") suspend fun reminder(id: Long): Reminder?
    @Upsert suspend fun upsertReminder(r: Reminder): Long
    @Delete suspend fun deleteReminder(r: Reminder)

    // Sleep
    @Query("SELECT * FROM SleepEntry WHERE epochDay >= :from ORDER BY epochDay") fun sleepSince(from: Long): Flow<List<SleepEntry>>
    @Upsert suspend fun upsertSleep(e: SleepEntry)
    @Delete suspend fun deleteSleep(e: SleepEntry)
}

@Database(
    entities = [WorkoutTemplate::class, ScheduledWorkout::class, Meal::class, PlannedMeal::class, Reminder::class, SleepEntry::class],
    version = 3,
)
abstract class GuruDatabase : RoomDatabase() {
    abstract fun dao(): GuruDao

    companion object {
        @Volatile private var instance: GuruDatabase? = null
        fun get(context: Context): GuruDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, GuruDatabase::class.java, "guru.db")
                .fallbackToDestructiveMigration() // pre-release: schema changes re-seed presets
                .build().also { instance = it }
        }
    }
}
